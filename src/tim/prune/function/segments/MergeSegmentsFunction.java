package tim.prune.function.segments;

import java.util.ArrayList;
import java.util.List;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.cmd.PointFlag;
import tim.prune.cmd.SetSegmentsCmd;
import tim.prune.data.DataPoint;
import tim.prune.data.Track;

/**
 * Function to merge the track segments of the currently selected range
 */
public class MergeSegmentsFunction extends GenericFunction
{
	public MergeSegmentsFunction(App inApp) {
		super(inApp);
	}

	@Override
	public String getNameKey() {
		return "menu.range.mergetracksegments";
	}

	@Override
	public void begin()
	{
		final int selectionStart = _app.getTrackInfo().getSelection().getStart();
		final int selectionEnd = _app.getTrackInfo().getSelection().getEnd();
		if (selectionStart < 0 || selectionEnd <= selectionStart) {
			return;
		}
		List<PointFlag> flags = getFlags(_app.getTrackInfo().getTrack(), selectionStart, selectionEnd);
		if (!flags.isEmpty())
		{
			SetSegmentsCmd command = new SetSegmentsCmd(flags);
			command.setConfirmText(I18nManager.getText("confirm.mergetracksegments"));
			command.setDescription(getName());
			_app.execute(command);
		}
	}

	/**
	 * Compile the list of point flags which need to be applied
	 * @param inTrack track object
	 * @param inRangeStart index of range start
	 * @param inRangeEnd index of range end, inclusive
	 * @return list of points to modify (if any)
	 */
	private List<PointFlag> getFlags(Track inTrack, int inRangeStart, int inRangeEnd)
	{
		ArrayList<PointFlag> flags = new ArrayList<>();
		boolean firstTrackPoint = true;
		for (int i=inRangeStart; i<=inRangeEnd; i++)
		{
			DataPoint point = inTrack.getPoint(i);
			if (point.isWaypoint()) {
				continue;
			}
			boolean segment = point.getSegmentStart();
			if (firstTrackPoint)
			{
				if (!segment) {
					flags.add(new PointFlag(point, true));
				}
				firstTrackPoint = false;
			}
			else if (segment) {
				flags.add(new PointFlag(point, false));
			}
		}
		if (!flags.isEmpty()) {
			DataPoint nextPoint = inTrack.getNextTrackPoint(inRangeEnd + 1);
			if (nextPoint != null) {
				flags.add(new PointFlag(nextPoint, true));
			}
		}
		return flags;
	}
}
