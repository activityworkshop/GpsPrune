package tim.prune.function;

import javax.swing.JOptionPane;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.Track;
import tim.prune.undo.UndoInterpolate;

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
	 * Perform the operation
	 */
	public void begin()
	{
		// not needed, we just use the completeFunction method instead
	}

	/**
	 * Complete the function after the input parameter has been chosen
	 */
	public void completeFunction(int inParam)
	{
		// Firstly, work out whether the selected range only contains waypoints or not
		final int startIndex = _app.getTrackInfo().getSelection().getStart();
		final int endIndex   = _app.getTrackInfo().getSelection().getEnd();
		boolean betweenWaypoints = false;
		// if there are only waypoints, then ask whether to interpolate them
		if (!selectedRangeHasTrackpoints(_app.getTrackInfo().getTrack(), startIndex, endIndex))
		{
			int answer = JOptionPane.showConfirmDialog(_parentFrame,
				I18nManager.getText("dialog.interpolate.betweenwaypoints"),
				I18nManager.getText(getNameKey()), JOptionPane.YES_NO_OPTION);
			if (answer == JOptionPane.NO_OPTION) {
				// user said no, so nothing to do
				return;
			}
			betweenWaypoints = true;
		}

		if (startIndex < 0 || endIndex < 0 || endIndex <= startIndex) {
			return;
		}

		// construct new point array with the interpolated points
		final int numToAdd = inParam;
		final Track track = _app.getTrackInfo().getTrack();
		final int maxToAdd = (endIndex-startIndex) * numToAdd;
		final int extendedSize = track.getNumPoints() + maxToAdd;
		DataPoint[] oldPoints = track.cloneContents();
		DataPoint[] newPoints = new DataPoint[extendedSize];
		// Copy points before
		System.arraycopy(oldPoints, 0, newPoints, 0, startIndex);
		// Loop, copying points and interpolating
		int destIndex = startIndex;
		DataPoint prevPoint = null;
		for (int i=startIndex; i<= endIndex; i++)
		{
			DataPoint p = _app.getTrackInfo().getTrack().getPoint(i);
			if (prevPoint != null && ((p.isWaypoint() && betweenWaypoints) || (!p.isWaypoint() && !p.getSegmentStart())))
			{
				// interpolate between the previous point and this one
				DataPoint[] addition = prevPoint.interpolate(p, numToAdd);
				System.arraycopy(addition, 0, newPoints, destIndex, numToAdd);
				destIndex += numToAdd;
			}
			// copy point
			newPoints[destIndex] = p;
			destIndex++;
			if (!p.isWaypoint() || betweenWaypoints)
			{
				prevPoint = p;
			}
			else if (!p.isWaypoint()) {
				prevPoint = null;
			}
			// If it's a waypoint, then keep the old prevPoint
		}
		final int totalInserted = destIndex - endIndex - 1;
		// Copy the points after the selected range
		System.arraycopy(oldPoints, endIndex, newPoints, destIndex-1, track.getNumPoints()-endIndex);

		// If necessary, make a new array of the correct size and do another arraycopy into it
		final int newTotalPoints = track.getNumPoints() + totalInserted;
		if (newTotalPoints != newPoints.length)
		{
			DataPoint[] croppedPoints = new DataPoint[newTotalPoints];
			System.arraycopy(newPoints, 0, croppedPoints, 0, newTotalPoints);
			newPoints = croppedPoints;
		}

		// Make undo object
		UndoInterpolate undo = new UndoInterpolate(_app.getTrackInfo(), totalInserted);
		// Replace track with new points array
		if (track.replaceContents(newPoints))
		{
			final String confirmMessage = I18nManager.getTextWithNumber("confirm.pointsadded", totalInserted);
			_app.completeFunction(undo, confirmMessage);
			// Alter selection
			_app.getTrackInfo().getSelection().selectRange(startIndex, endIndex + totalInserted);
		}
	}

	/**
	 * Check if the given Track has trackpoints in the specified range
	 * @param inTrack track object
	 * @param inStart start index
	 * @param inEnd end index
	 * @return true if there are any non-waypoints in the range
	 */
	private static boolean selectedRangeHasTrackpoints(Track inTrack, int inStart, int inEnd)
	{
		for (int i=inStart; i<= inEnd; i++)
		{
			DataPoint p = inTrack.getPoint(i);
			if (p != null && !p.isWaypoint()) {
				return true;
			}
		}
		return false;
	}
}
