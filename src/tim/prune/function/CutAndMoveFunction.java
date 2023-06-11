package tim.prune.function;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.cmd.Command;
import tim.prune.cmd.CutAndMoveCmd;
import tim.prune.cmd.PointFlag;
import tim.prune.data.DataPoint;
import tim.prune.data.Track;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to provide the function to cut and move the currently selected range
 */
public class CutAndMoveFunction extends TimeSensitiveFunction
{
	/**
	 * Constructor
	 * @param inApp application object for callback
	 */
	public CutAndMoveFunction(App inApp) {
		super(inApp);
	}

	/** Get the name key */
	public String getNameKey() {
		return "menu.range.cutandmove";
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
		final int pointIndex = _app.getTrackInfo().getSelection().getCurrentPointIndex();
		if (startIndex < 0 || endIndex <= startIndex || pointIndex < 0 ||
			(pointIndex >= startIndex && pointIndex <= endIndex))
		{
			return;
		}
		Track track = _app.getTrackInfo().getTrack();
		_app.execute(makeCommand(track, startIndex, endIndex, pointIndex));
		_app.getTrackInfo().getSelection().clearAll();
		_app.getTrackInfo().selectPoint(pointIndex);
	}

	/**
	 * Make a cut-and-move command combining the required operations
	 * @param inTrack track object, used to interrogate segments
	 * @param inStartIndex start index of the range to be reversed
	 * @param inEndIndex end index, inclusive
	 * @param inPointIndex point index to insert before
	 * @return command to be executed
	 */
	public Command makeCommand(Track inTrack, int inStartIndex, int inEndIndex, int inPointIndex)
	{
		List<Integer> pointIndexes = makeNewOrdering(inTrack.getNumPoints(),
				inStartIndex, inEndIndex, inPointIndex);
		DataPoint firstTrackPointInRange = inTrack.getNextTrackPoint(inStartIndex);
		DataPoint firstTrackPointAfterRange = inTrack.getNextTrackPoint(inEndIndex + 1);
		DataPoint insertPoint = inTrack.getPoint(inPointIndex);
		List<PointFlag> pointFlags = new ArrayList<>();
		pointFlags.add(new PointFlag(firstTrackPointInRange, true));
		pointFlags.add(new PointFlag(firstTrackPointAfterRange, true));
		pointFlags.add(new PointFlag(insertPoint, true));
		Command command = new CutAndMoveCmd(pointIndexes, pointFlags);
		command.setDescription(getName());
		command.setConfirmText(I18nManager.getText("confirm.cutandmove"));

		return command;
	}

	/**
	 * Make the new ordering of the points based on the reversal
	 * @param inTotalPoints total number of points
	 * @param inStartIndex start index of selection to move
	 * @param inEndIndex end index of selection to move
	 * @param inPointIndex point index for insertion
	 * @return new ordering
	 */
	private static List<Integer> makeNewOrdering(int inTotalPoints, int inStartIndex, int inEndIndex,
			int inPointIndex)
	{
		ArrayList<Integer> references = new ArrayList<>();
		// up to insertion point but not the section to be moved
		for (int i=0; i<inPointIndex; i++)
		{
			if (i < inStartIndex || i > inEndIndex) {
				references.add(i);
			}
		}
		// section to be moved
		for (int i=inStartIndex; i<=inEndIndex; i++) {
			references.add(i);
		}
		// after the insertion point but not the section to be moved
		for (int i=inPointIndex; i<inTotalPoints; i++)
		{
			if (i < inStartIndex || i > inEndIndex) {
				references.add(i);
			}
		}
		return references;
	}
}
