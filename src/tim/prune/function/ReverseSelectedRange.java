package tim.prune.function;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.cmd.Command;
import tim.prune.cmd.PointFlag;
import tim.prune.cmd.ReverseRangeCmd;
import tim.prune.data.DataPoint;
import tim.prune.data.Track;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to provide the function to reverse the currently selected range
 */
public class ReverseSelectedRange extends TimeSensitiveFunction
{
	/**
	 * Constructor
	 * @param inApp application object for callback
	 */
	public ReverseSelectedRange(App inApp) {
		super(inApp);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.reverserange";
	}

	/**
	 * Begin the function
	 */
	public void begin()
	{
		if (!confirmTimestampMangling()) {
			return;
		}
		final int startIndex = _app.getTrackInfo().getSelection().getStart();
		final int endIndex = _app.getTrackInfo().getSelection().getEnd();
		if (startIndex < 0 || endIndex <= startIndex) {
			return;
		}
		Track track = _app.getTrackInfo().getTrack();
		Command command = makeReverseCommand(track, startIndex, endIndex);
		command.setDescription(getName());
		command.setConfirmText(I18nManager.getText("confirm.reverserange"));
		_app.execute(command);
	}

	/**
	 * Make a reverse command combining the required operations
	 * @param inTrack track object, used to interrogate segments
	 * @param inStartIndex start index of the range to be reversed
	 * @param inEndIndex end index, inclusive
	 * @return command to be executed
	 */
	public static Command makeReverseCommand(Track inTrack, int inStartIndex, int inEndIndex)
	{
		List<Integer> pointIndexes = makeNewOrdering(inTrack.getNumPoints(),
				inStartIndex, inEndIndex);
		List<PointFlag> pointFlags = null;
		if (inTrack.isTrackPointWithin(inStartIndex, inEndIndex))
		{
			pointFlags = new ArrayList<>();
			pointFlags.add(new PointFlag(inTrack.getNextTrackPoint(inEndIndex + 1), true));
			boolean newSeg = true;
			for (int i=inEndIndex; i>=inStartIndex; i--)
			{
				DataPoint point = inTrack.getPoint(i);
				if (!point.isWaypoint())
				{
					// Shift segment flag to next point
					pointFlags.add(new PointFlag(point, newSeg));
					newSeg = point.getSegmentStart();
				}
			}
		}
		return new ReverseRangeCmd(pointIndexes, inStartIndex, inEndIndex, pointFlags);
	}

	/**
	 * Make the new ordering of the points based on the reversal
	 * @param totalPoints total number of points
	 * @param startIndex start index of selection to reverse
	 * @param endIndex end index of selection to reverse
	 * @return new ordering
	 */
	private static List<Integer> makeNewOrdering(int totalPoints, int startIndex, int endIndex)
	{
		ArrayList<Integer> references = new ArrayList<>();
		for (int i=0; i<startIndex; i++) {
			references.add(i);
		}
		for (int i=endIndex; i>=startIndex; i--) {
			references.add(i);
		}
		for (int i=endIndex + 1; i<totalPoints; i++) {
			references.add(i);
		}
		return references;
	}
}
