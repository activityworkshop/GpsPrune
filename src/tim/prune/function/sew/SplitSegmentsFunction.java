package tim.prune.function.sew;

import javax.swing.JOptionPane;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.DataPoint;
import tim.prune.function.DistanceTimeLimitFunction;
import tim.prune.undo.UndoSplitSegments;

/**
 * Function to split a track into segments using
 * either a distance limit or a time limit
 */
public class SplitSegmentsFunction extends DistanceTimeLimitFunction
{
	/**
	 * Constructor
	 */
	public SplitSegmentsFunction(App inApp) {
		super(inApp, false);
	}

	/**
	 * @return name key
	 */
	public String getNameKey() {
		return "function.splitsegments";
	}


	/**
	 * The dialog has been completed and OK pressed, so do the split
	 */
	protected void performFunction()
	{
		// Split either by distance or time
		final int timeLimitSeconds = getTimeLimitInSeconds();
		final boolean splitByTime = (timeLimitSeconds > 0);
		final double distLimitRadians = getDistanceLimitRadians();
		final boolean splitByDistance = (distLimitRadians > 0.0);
		if (!splitByTime && !splitByDistance) {
			return; // neither option selected
		}

		// Make undo object
		UndoSplitSegments undo = new UndoSplitSegments(_app.getTrackInfo().getTrack());
		final int numPoints = _app.getTrackInfo().getTrack().getNumPoints();
		DataPoint currPoint = null, prevPoint = null;
		int numSplitsMade = 0;

		// Now actually do it, looping through the points in the track
		for (int i=0; i<numPoints; i++)
		{
			currPoint = _app.getTrackInfo().getTrack().getPoint(i);
			if (!currPoint.isWaypoint())
			{
				boolean splitHere = (prevPoint != null)
					&& ((splitByDistance && DataPoint.calculateRadiansBetween(prevPoint, currPoint) > distLimitRadians)
						|| (splitByTime && currPoint.hasTimestamp() && prevPoint.hasTimestamp()
							&& currPoint.getTimestamp().getSecondsSince(prevPoint.getTimestamp()) > timeLimitSeconds));
				if (splitHere && !currPoint.getSegmentStart())
				{
					currPoint.setSegmentStart(true);
					numSplitsMade++;
				}
				prevPoint = currPoint;
			}
		}

		if (numSplitsMade > 0)
		{
			_app.completeFunction(undo, I18nManager.getTextWithNumber("confirm.splitsegments", numSplitsMade));
			UpdateMessageBroker.informSubscribers();
			_dialog.dispose();
		}
		else
		{
			// Complain that no split was made
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("error.tracksplit.nosplit"),
				I18nManager.getText("error.function.noop.title"), JOptionPane.WARNING_MESSAGE);
		}
	}
}
