package tim.prune.function.compress;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.cmd.CompoundCommand;
import tim.prune.cmd.PointFlag;
import tim.prune.cmd.ShuffleAndCropCmd;
import tim.prune.data.DataPoint;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;
import tim.prune.function.media.LinkedMediaAction;
import tim.prune.function.media.PopupResponse;


/**
 * Function to delete the marked points in the track
 */
public class DeleteMarkedPointsFunction extends GenericFunction
{
	private boolean _splitSegments = false;
	private String  _parentFunctionKey = null;

	/** Constructor */
	public DeleteMarkedPointsFunction(App inApp) {
		super(inApp);
	}

	public String getNameKey() {
		return "function.deletemarked";
	}

	/**
	 * Get notification about parent function
	 * @param inKey parent function name key
	 * @param inSplitSegments true to split segment, false to not
	 */
	public void setParentFunction(String inKey, boolean inSplitSegments)
	{
		_parentFunctionKey = inKey;
		_splitSegments = inSplitSegments;
	}

	public void begin()
	{
		ArrayList<Integer> indexesToKeep = new ArrayList<>();
		ArrayList<Integer> indexesToDelete = new ArrayList<>();
		ArrayList<Integer> indexesOfSegments = new ArrayList<>();
		TrackInfo trackInfo = _app.getTrackInfo();
		int numSelectedMedia = 0;

		for (int i=0; i<trackInfo.getTrack().getNumPoints(); i++)
		{
			if (trackInfo.isPointMarkedForDeletion(i))
			{
				indexesToDelete.add(i);
				if (trackInfo.isPointMarkedForSegmentBreak(i) || _splitSegments) {
					indexesOfSegments.add(i);
				}
				if (trackInfo.getTrack().getPoint(i).hasMedia()) {
					numSelectedMedia++;
				}
			}
			else {
				indexesToKeep.add(i);
			}
		}

		if (indexesToDelete.isEmpty())
		{
			String titleKey = (_parentFunctionKey == null ? getNameKey() : _parentFunctionKey);
			_app.showErrorMessage(titleKey, "dialog.deletemarked.nonefound");
		}
		else
		{
			final PopupResponse response = numSelectedMedia > 0 ?
				LinkedMediaAction.chooseMediaAction(_app.getFrame(), numSelectedMedia)
				: PopupResponse.MEDIA_NOT_CONNECTED;
			if (response == PopupResponse.CANCEL) {
				return;
			}
			CompoundCommand command = new ShuffleAndCropCmd(indexesToKeep, indexesToDelete,
				makeSegmentFlags(trackInfo.getTrack(), indexesToDelete, indexesOfSegments));
			switch (response)
			{
				case DELETE:
					command.addCommand(LinkedMediaAction.makeDeleteMediaCommand(indexesToDelete, trackInfo.getTrack()));
					break;
				case UNLINK:
					command.addCommand(LinkedMediaAction.makeUnlinkMediaCommand(indexesToDelete, trackInfo.getTrack()));
					break;
				case CANCEL:
					return;
				case MEDIA_NOT_CONNECTED:
				default:
					break;
			}
			_app.execute(command);
		}
	}

	private List<PointFlag> makeSegmentFlags(Track inTrack, List<Integer> inIndexesToDelete,
		List<Integer> inIndexesOfSegments)
	{
		ArrayList<PointFlag> flags = new ArrayList<>();
		HashSet<Integer> deleteSet = new HashSet<>(inIndexesToDelete);
		HashSet<Integer> segmentSet = new HashSet<>(inIndexesOfSegments);
		final int numPoints = inTrack.getNumPoints();
		boolean setSegmentBreak = false;
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = inTrack.getPoint(i);
			if (point.isWaypoint()) {
				continue;
			}
			setSegmentBreak = setSegmentBreak || segmentSet.contains(i);
			if (!deleteSet.contains(i) && setSegmentBreak)
			{
				flags.add(new PointFlag(point, true));
				setSegmentBreak = false;
			}
		}
		return flags;
	}
}
