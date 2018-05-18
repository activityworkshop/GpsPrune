package tim.prune.function;

import java.util.ArrayList;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.FieldList;
import tim.prune.data.Track;
import tim.prune.undo.UndoAppendPoints;

/**
 * Function to create waypoints marking either
 * at regular distance intervals or time intervals
 */
public class CreateMarkerWaypointsFunction extends DistanceTimeLimitFunction
{
	/** ArrayList of points to append to the track */
	private ArrayList<DataPoint> _pointsToAdd = new ArrayList<DataPoint>();
	/** Counter of previously used multiple */
	private int _previousMultiple = 0;


	/**
	 * Constructor
	 */
	public CreateMarkerWaypointsFunction(App inApp) {
		super(inApp);
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
		// Distribute either by distance or time
		final int timeLimitSeconds = getTimeLimitInSeconds();
		final boolean createByTime = (timeLimitSeconds > 0);
		final double distLimitRadians = getDistanceLimitRadians();
		final boolean createByDistance = (distLimitRadians > 0.0);
		if (!createByTime && !createByDistance) {
			return; // neither option selected
		}

		// Make undo object
		final int numPoints = _app.getTrackInfo().getTrack().getNumPoints();
		UndoAppendPoints undo = new UndoAppendPoints(numPoints);

		// set up the memory from scratch to collect the created points
		initMemory();

		// Make new waypoints, looping through the points in the track
		DataPoint currPoint = null, prevPoint = null;
		double currValue = 0.0, prevValue = 0.0;
		for (int i=0; i<numPoints; i++)
		{
			currPoint = _app.getTrackInfo().getTrack().getPoint(i);
			if (!currPoint.isWaypoint())
			{
				if (!currPoint.getSegmentStart() && prevPoint != null)
				{
					// Calculate current value
					if (createByTime)
					{
						if (currPoint.hasTimestamp() && prevPoint.hasTimestamp())
						{
							currValue += (currPoint.getTimestamp().getMillisecondsSince(prevPoint.getTimestamp()) / 1000.0);
							processValue(prevPoint, prevValue, timeLimitSeconds, currPoint, currValue);
						}
					}
					else if (createByDistance)
					{
						currValue += DataPoint.calculateRadiansBetween(prevPoint, currPoint);
						processValue(prevPoint, prevValue, distLimitRadians, currPoint, currValue);
					}
				}
				prevPoint = currPoint;
				prevValue = currValue;
			}
		}

		// System.out.println(_pointsToAdd.size() + " markers to add...");
		if (!_pointsToAdd.isEmpty())
		{
			// Append created points to Track
			Field[] fields = {Field.LATITUDE, Field.LONGITUDE, Field.ALTITUDE, Field.WAYPT_NAME};
			final int numPointsToAdd = _pointsToAdd.size();
			DataPoint[] waypoints = new DataPoint[numPointsToAdd];
			_pointsToAdd.toArray(waypoints);
			Track wpTrack = new Track(new FieldList(fields), waypoints);
			_app.getTrackInfo().getTrack().combine(wpTrack);

			undo.setNumPointsAppended(numPointsToAdd);
			_app.completeFunction(undo, I18nManager.getText("confirm.interpolate"));
			// TODO: Maybe add new token including number of points added/created
			UpdateMessageBroker.informSubscribers();
		}
		_dialog.dispose();
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
			DataPoint marker = DataPoint.interpolate(inPrevPoint, inCurrPoint, fractionFromPrev);
			marker.setFieldValue(Field.WAYPT_NAME, createLimitDescription(m), false);
			_pointsToAdd.add(marker);
			// System.out.println("I would add a point here with values " + inPrevValue + " and " + inCurrValue);
		}
		_previousMultiple = currMultiple;
	}
}
