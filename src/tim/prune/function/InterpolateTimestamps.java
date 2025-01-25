package tim.prune.function;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.cmd.EditSingleFieldCmd;
import tim.prune.data.*;
import tim.prune.function.edit.PointEdit;

import java.util.ArrayList;

public class InterpolateTimestamps extends GenericFunction
{
	private final ArrayList<PointEdit> _edits = new ArrayList<>();


	public InterpolateTimestamps(App inApp) {
		super(inApp);
	}

	/** Execute the function on the selected range */
	public void begin()
	{
		_edits.clear();
		double distanceSoFar = 0.0;
		ArrayList<IndexAndDistance> pointList = new ArrayList<>();
		final int selStart = _app.getTrackInfo().getSelection().getStart();
		final int selEnd = _app.getTrackInfo().getSelection().getEnd();
		if (selStart < 0 || selEnd <= selStart) {
			return;
		}

		DataPoint fromPoint = null;
		DataPoint latestPoint = null;
		for (int i=selStart; i<=selEnd; i++)
		{
			DataPoint point = _app.getTrackInfo().getTrack().getPoint(i);
			if (point.isWaypoint()) {
				continue;
			}
			if (point.getSegmentStart())
			{
				pointList.clear();
				fromPoint = null;
			}
			if (point.hasTimestamp())
			{
				if (fromPoint != null && !pointList.isEmpty())
				{
				   distanceSoFar += distanceBetween(latestPoint, point);
				   processPointList(pointList, distanceSoFar,
						fromPoint.getTimestamp(), point.getTimestamp());
				}
				fromPoint = point;
				pointList.clear();
				distanceSoFar = 0.0;
			}
			else if (fromPoint != null)
			{
				distanceSoFar += distanceBetween(latestPoint, point);
				pointList.add(new IndexAndDistance(i, distanceSoFar));
			}
			latestPoint = point;
		}

		if (_edits.isEmpty()) {
			_app.showErrorMessage(getNameKey(), "error.interpolatetimestamps.notpossible");
		}
		else
		{
			EditSingleFieldCmd command = new EditSingleFieldCmd(Field.TIMESTAMP, _edits, null);
			command.setDescription(I18nManager.getText("undo.applytimestamps"));
			command.setConfirmText(I18nManager.getText("confirm.applytimestamps"));
			_app.execute(command);
		}
	}

	/**
	 * Handle a range of points without timestamps
	 * @param inPointList list of point indexes and their distances
	 * @param inTotalDistance total distance between the two end points
	 * @param inStartTimestamp timestamp of point before the range
	 * @param inEndTimestamp timestamp of point after the range
	 */
	private void processPointList(ArrayList<IndexAndDistance> inPointList, double inTotalDistance,
								  Timestamp inStartTimestamp, Timestamp inEndTimestamp)
	{
		for (IndexAndDistance idxDist : inPointList)
		{
			final double fraction = idxDist._distance / inTotalDistance;
			final long millisAfter = (long) (inEndTimestamp.getMillisecondsSince(inStartTimestamp) * fraction);
			Timestamp stamp = inStartTimestamp.addOffsetMilliseconds(millisAfter);
			String stampText = stamp.getText(Timestamp.Format.ISO8601, null);
			_edits.add(new PointEdit(idxDist._pointIndex, stampText));
		}
	}

	/** @return the distance between two points */
	private static double distanceBetween(DataPoint inPoint1, DataPoint inPoint2)
	{
		double radians = DataPoint.calculateRadiansBetween(inPoint1, inPoint2);
		return Distance.convertRadiansToDistance(radians, UnitSetLibrary.UNITS_METRES);
	}

	public String getNameKey() {
		return "function.interpolatetimestamps";
	}


	/** Inner class to hold points and distances in a list */
	private static class IndexAndDistance
	{
		private final int _pointIndex;
		private final double _distance;

		private IndexAndDistance(int inIndex, double inDistance)
		{
			_pointIndex = inIndex;
			_distance = inDistance;
		}
	}
}
