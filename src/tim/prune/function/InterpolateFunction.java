package tim.prune.function;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.cmd.InsertVariousPointsCmd;
import tim.prune.data.DataPoint;
import tim.prune.data.Track;

/**
 * Function to interpolate between the points in a range
 */
public class InterpolateFunction extends SingleNumericParameterFunction
{
	/**
	 * Constructor
	 * @param inApp app object
	 */
	public InterpolateFunction(App inApp) {
		super(inApp, 1, 1000);
	}

	/** @return name key */
	public String getNameKey() {
		return "function.interpolate";
	}

	/** @return description key for input parameter */
	public String getDescriptionKey() {
		return "dialog.interpolate.parameter.text";
	}

	/** @return current (or default) parameter value */
	public int getCurrentParamValue() {
		return 0;
	}

	/**
	 * Complete the function after the input parameter has been chosen
	 */
	public void completeFunction(int inParam)
	{
		// Firstly, work out whether the selected range only contains waypoints or not
		final int startIndex = _app.getTrackInfo().getSelection().getStart();
		final int endIndex   = _app.getTrackInfo().getSelection().getEnd();
		if (startIndex < 0 || endIndex < 0 || endIndex <= startIndex || inParam <= 0) {
			return;
		}

		boolean betweenWaypoints = false;
		// if there are only waypoints, then ask whether to interpolate them
		if (!_app.getTrackInfo().getTrack().isTrackPointWithin(startIndex, endIndex))
		{
			int answer = JOptionPane.showConfirmDialog(_parentFrame,
				I18nManager.getText("dialog.interpolate.betweenwaypoints"),
				getName(), JOptionPane.YES_NO_OPTION);
			if (answer != JOptionPane.YES_OPTION) {
				// user said no (or cancel), so nothing to do
				return;
			}
			betweenWaypoints = true;
		}

		final int numToAdd = inParam;
		final Track track = _app.getTrackInfo().getTrack();
		InsertVariousPointsCmd command = makeCommand(track, startIndex, endIndex, numToAdd, betweenWaypoints);
		if (_app.execute(command))
		{
			final int lastIndex = endIndex + command.getNumInserted();
			_app.getTrackInfo().getSelection().selectRange(startIndex, lastIndex);
			_app.getTrackInfo().selectPoint(lastIndex);
		}
	}

	/**
	 * Create the command to do the interpolation
	 * @param inTrack track object
	 * @param inStartIndex start of selection
	 * @param inEndIndex end of selection, inclusive
	 * @param inNumToAdd number of points to interpolate between each pair
	 * @param inBetweenWaypoints true to also interpolate between waypoints
	 * @return command object
	 */
	private InsertVariousPointsCmd makeCommand(Track inTrack, int inStartIndex, int inEndIndex, int inNumToAdd,
		boolean inBetweenWaypoints)
	{
		ArrayList<Integer> indexes = new ArrayList<>();
		ArrayList<DataPoint> insertedPoints = new ArrayList<>();
		// Points before selected range
		for (int i=0; i<=inStartIndex; i++) {
			indexes.add(i);
		}
		// Interpolate the selection
		DataPoint prevPoint = inTrack.getPoint(inStartIndex);
		for (int i=inStartIndex+1; i<=inEndIndex; i++)
		{
			DataPoint currPoint = inTrack.getPoint(i);
			boolean interpolate = (currPoint.isWaypoint() && prevPoint.isWaypoint() && inBetweenWaypoints)
				|| (!currPoint.isWaypoint() && !prevPoint.isWaypoint() && !currPoint.getSegmentStart());
			if (interpolate)
			{
				for (int j=0; j<inNumToAdd; j++)
				{
					DataPoint newPoint = PointUtils.interpolate(prevPoint, currPoint, j, inNumToAdd);
					if (j == 0 && inBetweenWaypoints) {
						newPoint.setSegmentStart(true);
					}
					insertedPoints.add(newPoint);
					indexes.add(inTrack.getNumPoints() + insertedPoints.size() - 1);
				}
			}
			indexes.add(i);
			if (!currPoint.isWaypoint() || inBetweenWaypoints) {
				prevPoint = currPoint;
			}
		}
		// Points after selected range
		for (int i=inEndIndex+1; i<inTrack.getNumPoints(); i++) {
			indexes.add(i);
		}
		InsertVariousPointsCmd command = new InsertVariousPointsCmd(indexes, insertedPoints);
		command.setDescription(getName());
		command.setConfirmText(I18nManager.getTextWithNumber("confirm.pointsadded", insertedPoints.size()));
		return command;
	}
}
