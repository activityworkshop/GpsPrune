package tim.prune.function;

import java.util.ArrayList;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.cmd.AppendRangeCmd;
import tim.prune.config.Config;
import tim.prune.data.DataPoint;
import tim.prune.data.RangeStats;
import tim.prune.data.UnitSetLibrary;

/**
 * Function to create waypoints marking regular distance intervals,
 * regular time intervals, or halfway points
 */
public class CreateMarkerWaypointsFunction extends DistanceTimeLimitFunction
{
	/** ArrayList of points to append to the track */
	private final ArrayList<DataPoint> _pointsToAdd = new ArrayList<>();
	/** Counter of previously used multiple */
	private int _previousMultiple = 0;

	/*
	 * Type of halfway point
	 */
	private enum HalfwayType
	{
		HALF_DISTANCE,
		HALF_CLIMB,
		HALF_DESCENT
	}

	/**
	 * Constructor
	 */
	public CreateMarkerWaypointsFunction(App inApp) {
		super(inApp, true);
	}

	/**
	 * @return name key
	 */
	public String getNameKey() {
		return "function.createmarkerwaypoints";
	}

	/**
	 * Init the state to start collecting a new set of points
	 */
	private void initMemory()
	{
		_pointsToAdd.clear();
		_previousMultiple = 0;
	}

	/**
	 * The dialog has been completed and OK pressed, so do the point creation
	 */
	protected void performFunction()
	{
		// Determine which kind of markers to create
		final int timeLimitSeconds = getTimeLimitInSeconds();
		final boolean createByTime = (timeLimitSeconds > 0);
		final double distLimitKm = getDistanceLimitKilometres();
		final boolean createByDistance = (distLimitKm > 0.0);
		final boolean createHalves = isHalvesSelected();

		// set up the memory from scratch to collect the created points
		initMemory();

		if (createByTime || createByDistance) {
			createWaypointsAtIntervals(timeLimitSeconds, distLimitKm);
		}
		else if (createHalves) {
			createHalfwayWaypoints();
		}
		else {
			return;
		}

		if (!_pointsToAdd.isEmpty())
		{
			AppendRangeCmd command = new AppendRangeCmd(_pointsToAdd);
			command.setDescription(getName());
			final String confirmMessage = I18nManager.getTextWithNumber("confirm.pointsadded", _pointsToAdd.size());
			command.setConfirmText(confirmMessage);
			_app.execute(command);
		}
		_dialog.dispose();
	}

	/**
	 * Create waypoints according to the given intervals
	 * @param inTimeLimitSeconds time limit in seconds
	 * @param inDistLimitKm distance limit in kilometres
	 */
	private void createWaypointsAtIntervals(int inTimeLimitSeconds, double inDistLimitKm)
	{
		final boolean createByTime = (inTimeLimitSeconds > 0);
		final boolean createByDistance = (inDistLimitKm > 0.0);

		// Make new waypoints, looping through the points in the track
		DataPoint prevPoint = null;
		double currValue = 0.0, prevValue = 0.0;
		final int altitudeTolerance = getConfig().getConfigInt(Config.KEY_ALTITUDE_TOLERANCE) / 100;
		RangeStats rangeStats = new RangeStats(altitudeTolerance);
		final int numPoints = _app.getTrackInfo().getTrack().getNumPoints();
		for (int i=0; i<numPoints; i++)
		{
			DataPoint currPoint = _app.getTrackInfo().getTrack().getPoint(i);
			rangeStats.addPoint(currPoint);

			if (!currPoint.isWaypoint())
			{
				// Calculate current value
				if (createByTime)
				{
					currValue = rangeStats.getMovingDurationInSeconds();
					processValue(prevPoint, prevValue, inTimeLimitSeconds, currPoint, currValue);
				}
				else if (createByDistance)
				{
					currValue = rangeStats.getMovingDistanceKilometres();
					processValue(prevPoint, prevValue, inDistLimitKm, currPoint, currValue);
				}

				prevPoint = currPoint;
				prevValue = currValue;
			}
		}
	}

	/**
	 * Consider a pair of points in the track to see if a new marker should be inserted between them
	 * @param inPrevPoint previous point
	 * @param inPrevValue value of function at this previous point
	 * @param inLimit user-specified limit for marker values
	 * @param inCurrPoint current point
	 * @param inCurrValue value of function at this current point
	 */
	private void processValue(DataPoint inPrevPoint, double inPrevValue, double inLimit,
		DataPoint inCurrPoint, double inCurrValue)
	{
		// Check the current multiple and compare with previously used one
		final int currMultiple = (int) Math.floor(inCurrValue / inLimit);
		for (int m=_previousMultiple+1; m<=currMultiple; m++)
		{
			// Calculate position of limit between the two points
			final double valueBeforeBreak = (m * inLimit) - inPrevValue;
			final double valueAfterBreak = inCurrValue - (m * inLimit);
			final double fractionFromPrev = valueBeforeBreak / (valueBeforeBreak + valueAfterBreak);
			DataPoint marker = PointUtils.interpolate(inPrevPoint, inCurrPoint, fractionFromPrev);
			marker.setWaypointName(createLimitDescription(m));
			_pointsToAdd.add(marker);
		}
		_previousMultiple = currMultiple;
	}

	/**
	 * Create waypoints for the halfway markers
	 */
	private void createHalfwayWaypoints()
	{
		// Calculate the details of the whole track so we can see what to halve
		final int numPoints = _app.getTrackInfo().getTrack().getNumPoints();
		final int altitudeTolerance = getConfig().getConfigInt(Config.KEY_ALTITUDE_TOLERANCE) / 100;
		RangeStats totalStats = new RangeStats(altitudeTolerance);
		for (int i=0; i<numPoints; i++)
		{
			DataPoint currPoint = _app.getTrackInfo().getTrack().getPoint(i);
			totalStats.addPoint(currPoint);
		}
		// Calculate total moving distance of track
		final double totalDist = totalStats.getMovingDistanceKilometres();
		// If the track has altitudes, also calculate total climb and total descent
		final double totalClimb = totalStats.getMovingAltitudeRange().getClimb(UnitSetLibrary.UNITS_METRES);
		final double totalDescent = totalStats.getMovingAltitudeRange().getDescent(UnitSetLibrary.UNITS_METRES);

		final double halfDistance = totalDist / 2.0;
		final double halfClimb = totalClimb / 2.0;
		final double halfDescent = totalDescent / 2.0;

		// Now loop through points again, looking for the halfway points
		RangeStats partialStats = new RangeStats(altitudeTolerance);
		DataPoint prevPoint = null;
		boolean createdDistance = false, createdClimb = false, createdDescent = false;
		double prevDistance = 0.0, prevClimb = 0.0, prevDescent = 0.0;
		for (int i=0; i<numPoints; i++)
		{
			DataPoint currPoint = _app.getTrackInfo().getTrack().getPoint(i);
			partialStats.addPoint(currPoint);
			if (!currPoint.isWaypoint())
			{
				// distance
				if (!createdDistance && totalDist > 0.0)
				{
					final double currDist = partialStats.getMovingDistanceKilometres();
					createdDistance = processHalfValue(prevPoint, prevDistance, halfDistance,
						currPoint, currDist, HalfwayType.HALF_DISTANCE);
					prevDistance = currDist;
				}
				// climb
				if (!createdClimb && totalClimb > 0.0)
				{
					final double currClimb = partialStats.getMovingAltitudeRange().getClimb(UnitSetLibrary.UNITS_METRES);
					createdClimb = processHalfValue(prevPoint, prevClimb, halfClimb,
						currPoint, currClimb, HalfwayType.HALF_CLIMB);
					prevClimb = currClimb;
				}
				// descent
				if (!createdDescent && totalDescent > 0.0)
				{
					final double currDescent = partialStats.getMovingAltitudeRange().getDescent(UnitSetLibrary.UNITS_METRES);
					createdDescent = processHalfValue(prevPoint, prevDescent, halfDescent,
						currPoint, currDescent, HalfwayType.HALF_DESCENT);
					prevDescent = currDescent;
				}

				prevPoint = currPoint;
			}
		}
	}

	/**
	 * Consider a pair of points in the track to see if a new halfway marker should be inserted between them
	 * @param inPrevPoint previous point
	 * @param inPrevValue value of function at this previous point
	 * @param inTargetValue target halfway value
	 * @param inCurrPoint current point
	 * @param inCurrValue value of function at this current point
	 * @param inType type of halfway point
	 */
	private boolean processHalfValue(DataPoint inPrevPoint, double inPrevValue, double inTargetValue,
		DataPoint inCurrPoint, double inCurrValue, HalfwayType inType)
	{
		if (inPrevValue <= inTargetValue && inCurrValue >= inTargetValue)
		{
			// Calculate position of limit between the two points
			final double valueBeforeBreak = inTargetValue - inPrevValue;
			final double valueAfterBreak = inCurrValue - inTargetValue;
			final double fractionFromPrev = valueBeforeBreak / (valueBeforeBreak + valueAfterBreak);
			DataPoint marker = PointUtils.interpolate(inPrevPoint, inCurrPoint, fractionFromPrev);
			marker.setWaypointName(createHalfwayName(inType));
			_pointsToAdd.add(marker);
			return true;
		}
		return false;
	}

	/**
	 * Create the name of the halfway point according to type
	 * @param inType type of point
	 */
	private String createHalfwayName(HalfwayType inType)
	{
		final String typeString;
		switch (inType)
		{
			case HALF_DISTANCE:
				typeString = "distance";
				break;
			case HALF_CLIMB:
				typeString = "climb";
				break;
			case HALF_DESCENT:
				typeString = "descent";
				break;
			default:
				return "half";
		}
		return I18nManager.getText("dialog.markers.half." + typeString);
	}
}
