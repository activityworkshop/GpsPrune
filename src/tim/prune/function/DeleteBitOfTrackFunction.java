package tim.prune.function;

import java.util.List;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.cmd.CompoundCommand;
import tim.prune.cmd.PointFlag;
import tim.prune.cmd.ShuffleAndCropCmd;
import tim.prune.data.Track;
import tim.prune.function.media.LinkedMediaAction;
import tim.prune.function.media.PopupResponse;


/**
 * Abstract class to hold general deletion routines to delete
 * either one or two track sections.  Forms parent class to
 * the DeleteSelection and CropToSelection functions
 */
public abstract class DeleteBitOfTrackFunction extends GenericFunction
{
	/**
	 * Constructor
	 * @param inApp application object for callback
	 */
	protected DeleteBitOfTrackFunction(App inApp) {
		super(inApp);
	}

	/**
	 * Prepare the lists of point indexes, photos and audios
	 * @param inIndexesToKeep list of point indexes to keep
	 * @param inIndexesToDelete list of point indexes to delete
	 * @param inCriterion index criterion describing range to keep
	 * @return number of media found
	 */
	protected int fillLists(List<Integer> inIndexesToKeep, List<Integer> inIndexesToDelete,
		PointToKeepCriterion inCriterion)
	{
		Track track = _app.getTrackInfo().getTrack();
		int numMedia = 0;
		int numPoints = track.getNumPoints();
		for (int i=0; i<numPoints; i++)
		{
			if (inCriterion.keepIndex(i)) {
				inIndexesToKeep.add(i);
			}
			else
			{
				inIndexesToDelete.add(i);
				if (track.getPoint(i).hasMedia()) {
					numMedia++;
				}
			}
		}
		return numMedia;
	}

	/**
	 * Create the compound command to do the delete
	 * @param inIndexesToKeep list of indexes to keep, in order
	 * @param inIndexesToDelete list of indexes to delete
	 * @param inSegmentFlags segment flags to set, if any
	 * @param inNumMedia number of media found to be deleted / unlinked
	 * @return command to be executed
	 */
	protected CompoundCommand createCommand(List<Integer> inIndexesToKeep, List<Integer> inIndexesToDelete,
		List<PointFlag> inSegmentFlags, int inNumMedia)
	{
		PopupResponse mediaAction = LinkedMediaAction.chooseMediaAction(_app.getFrame(), inNumMedia);
		if (mediaAction == PopupResponse.CANCEL) {
			return null;
		}
		CompoundCommand command = new ShuffleAndCropCmd(inIndexesToKeep, inIndexesToDelete, inSegmentFlags);
		switch (mediaAction)
		{
			case DELETE:
				command.addCommand(LinkedMediaAction.makeDeleteMediaCommand(inIndexesToDelete, _app.getTrackInfo().getTrack()));
				break;
			case UNLINK:
				command.addCommand(LinkedMediaAction.makeUnlinkMediaCommand(inIndexesToDelete, _app.getTrackInfo().getTrack()));
				break;
			case MEDIA_NOT_CONNECTED:
			case CANCEL:
			default:
				break;
		}
		return command;
	}

	/**
	 * Criterion for keeping a point index
	 */
	interface PointToKeepCriterion {
		boolean keepIndex(int inIndex);
	}
}
