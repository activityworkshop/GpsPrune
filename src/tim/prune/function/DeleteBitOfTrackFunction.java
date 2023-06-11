package tim.prune.function;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.cmd.Command;
import tim.prune.cmd.CompoundCommand;
import tim.prune.cmd.ConnectMultipleMediaCmd;
import tim.prune.cmd.MediaLinkType;
import tim.prune.cmd.PointAndMedia;
import tim.prune.cmd.PointFlag;
import tim.prune.cmd.RemoveMediaCmd;
import tim.prune.cmd.ShuffleAndCropCmd;
import tim.prune.data.DataPoint;
import tim.prune.data.MediaObject;
import tim.prune.data.Track;
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
	 * Decide whether to delete the media or unlink them
	 * @param inNumMedia number of media found
	 * @return decision what to do with them
	 */
	protected PopupResponse chooseMediaAction(int inNumMedia)
	{
		if (inNumMedia <= 0) {
			return PopupResponse.MEDIA_NOT_CONNECTED;
		}
		final String message = I18nManager.getTextWithNumber("dialog.deletepoints.deletemedia", inNumMedia);
		int response = JOptionPane.showConfirmDialog(_app.getFrame(), message,
			I18nManager.getText("dialog.deletepoints.title"),
			JOptionPane.YES_NO_CANCEL_OPTION);
		if (response == JOptionPane.CANCEL_OPTION || response == JOptionPane.CLOSED_OPTION)
		{
			// cancel pressed- abort delete
			return PopupResponse.CANCEL;
		}
		return (response == JOptionPane.YES_OPTION ? PopupResponse.DELETE : PopupResponse.UNLINK);
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
		PopupResponse mediaAction = chooseMediaAction(inNumMedia);
		if (mediaAction == PopupResponse.CANCEL) {
			return null;
		}
		CompoundCommand command = new ShuffleAndCropCmd(inIndexesToKeep, inIndexesToDelete, inSegmentFlags);
		switch (mediaAction)
		{
			case DELETE:
				command.addCommand(makeDeleteMediaCommand(inIndexesToDelete));
				break;
			case UNLINK:
				command.addCommand(makeUnlinkMediaCommand(inIndexesToDelete));
				break;
			case MEDIA_NOT_CONNECTED:
			case CANCEL:
			default:
				break;
		}
		return command;
	}

	/**
	 * @param inIndexesToDelete indexes of points which will be deleted
	 * @return command to unlink points from their media
	 */
	private Command makeUnlinkMediaCommand(List<Integer> inIndexesToDelete)
	{
		ArrayList<PointAndMedia> points = new ArrayList<>();
		for (int i : inIndexesToDelete)
		{
			DataPoint point = _app.getTrackInfo().getTrack().getPoint(i);
			if (point.hasMedia()) {
				points.add(new PointAndMedia(point, null, null)); // disconnect
			}
		}
		return new ConnectMultipleMediaCmd(MediaLinkType.LINK_BOTH, points);
	}

	/**
	 * @param inIndexesToDelete indexes of points which will be deleted
	 * @return command to remove the media belonging to these points
	 */
	private Command makeDeleteMediaCommand(List<Integer> inIndexesToDelete)
	{
		ArrayList<MediaObject> media = new ArrayList<>();
		for (int i : inIndexesToDelete)
		{
			DataPoint point = _app.getTrackInfo().getTrack().getPoint(i);
			if (point.getPhoto() != null) {
				media.add(point.getPhoto());
			}
			if (point.getAudio() != null) {
				media.add(point.getAudio());
			}
		}
		return new RemoveMediaCmd(media);
	}

	/**
	 * Criterion for keeping a point index
	 */
	interface PointToKeepCriterion {
		boolean keepIndex(int inIndex);
	}
}
