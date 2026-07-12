package tim.prune.function.media;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import tim.prune.I18nManager;
import tim.prune.cmd.Command;
import tim.prune.cmd.ConnectMultipleMediaCmd;
import tim.prune.cmd.MediaLinkType;
import tim.prune.cmd.PointAndMedia;
import tim.prune.cmd.RemoveMediaCmd;
import tim.prune.data.DataPoint;
import tim.prune.data.MediaObject;
import tim.prune.data.Track;

/**
 * Provides functions for operating with linked media
 */
public class LinkedMediaAction
{
	/**
	 * Decide whether to delete the media or unlink them
	 * @param inParentFrame parent frame for question dialog
	 * @param inNumMedia number of media found
	 * @return decision what to do with them
	 */
	public static PopupResponse chooseMediaAction(JFrame inParentFrame, int inNumMedia)
	{
		if (inNumMedia <= 0) {
			return PopupResponse.MEDIA_NOT_CONNECTED;
		}
		final String message = inNumMedia == 1 ?
			I18nManager.getText("dialog.deletepoints.deletemedia.single")
			: I18nManager.getTextWithNumber("dialog.deletepoints.deletemedia", inNumMedia);
		final int response = JOptionPane.showConfirmDialog(inParentFrame, message,
			I18nManager.getText("dialog.deletepoints.title"),
			JOptionPane.YES_NO_CANCEL_OPTION);
		if (response == JOptionPane.CANCEL_OPTION || response == JOptionPane.CLOSED_OPTION)
		{
			// cancel pressed- abort delete
			return PopupResponse.CANCEL;
		}
		return response == JOptionPane.YES_OPTION ? PopupResponse.DELETE : PopupResponse.UNLINK;
	}

	/**
	 * @param inIndexesToDelete indexes of points which will be deleted
	 * @param inTrack track containing the points
	 * @return command to remove the media belonging to these points
	 */
	public static Command makeDeleteMediaCommand(List<Integer> inIndexesToDelete, Track inTrack)
	{
		ArrayList<MediaObject> media = new ArrayList<>();
		for (int i : inIndexesToDelete)
		{
			DataPoint point = inTrack.getPoint(i);
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
	 * @param inIndexesToDelete indexes of points which will be deleted
	 * @param inTrack track containing the points
	 * @return command to unlink points from their media
	 */
	public static Command makeUnlinkMediaCommand(List<Integer> inIndexesToDelete, Track inTrack)
	{
		ArrayList<PointAndMedia> points = new ArrayList<>();
		for (int i : inIndexesToDelete)
		{
			DataPoint point = inTrack.getPoint(i);
			if (point.hasMedia()) {
				points.add(new PointAndMedia(point, null, null)); // disconnect
			}
		}
		return new ConnectMultipleMediaCmd(MediaLinkType.LINK_BOTH, points);
	}
}
