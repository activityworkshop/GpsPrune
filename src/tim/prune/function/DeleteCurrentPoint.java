package tim.prune.function;

import tim.prune.App;
import tim.prune.DataSubscriber;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.cmd.*;
import tim.prune.data.AudioClip;
import tim.prune.data.DataPoint;
import tim.prune.data.Photo;
import tim.prune.function.media.PopupResponse;

import javax.swing.*;

/**
 * Delete the currently selected point
 */
public class DeleteCurrentPoint extends GenericFunction
{
	/**
	 * Constructor
	 * @param inApp app object
	 */
	public DeleteCurrentPoint(App inApp) {
		super(inApp);
	}

	@Override
	public String getNameKey() {
		return "menu.point.deletepoint";
	}

	@Override
	public void begin()
	{
		DataPoint currentPoint = _app.getTrackInfo().getCurrentPoint();
		if (currentPoint == null) {
			return;
		}
		// Check for photo and/or audio
		PopupResponse mediaAction = shouldDeleteMedia(currentPoint);
		if (mediaAction == PopupResponse.CANCEL) {
			return;
		}
		// make command
		final Command command;
		if (mediaAction == PopupResponse.MEDIA_NOT_CONNECTED) {
			command = new DeletePointCmd(_app.getTrackInfo().getSelection().getCurrentPointIndex());
		}
		else
		{
			CompoundCommand cmd = new CompoundCommand(DataSubscriber.ALL_DATA);
			cmd.addCommand(new DeletePointCmd(_app.getTrackInfo().getSelection().getCurrentPointIndex()));
			Photo photo = currentPoint.getPhoto();
			AudioClip audio = currentPoint.getAudio();
			if (mediaAction == PopupResponse.DELETE)
			{
				if (photo != null) {
					cmd.addCommand(new RemovePhotoCmd(_app.getTrackInfo().getPhotoList().getIndexOf(photo)));
				}
				if (audio != null) {
					cmd.addCommand(new RemoveAudioCmd(_app.getTrackInfo().getAudioList().getIndexOf(audio)));
				}
			}
			else {
				cmd.addCommand(new ConnectMediaCmd(currentPoint, null, null));
			}
			command = cmd;
		}
		Describer undoDescriber = new Describer("undo.deletepoint", "undo.deletepoint.withname");
		command.setDescription(undoDescriber.getDescriptionWithNameOrNot(currentPoint.getWaypointName()));
		command.setConfirmText(I18nManager.getText("confirm.deletepoint.single"));
		_app.execute(command);
	}


	/**
	 * Determine whether to delete the media, just unlink, or cancel
	 * @param inPoint current point to delete
	 * @return decision what do to with the media
	 */
	private PopupResponse shouldDeleteMedia(DataPoint inPoint)
	{
		final boolean hasPhoto = inPoint.getPhoto() != null;
		final boolean hasAudio = inPoint.getAudio() != null;
		if (!hasPhoto && !hasAudio) {
			return PopupResponse.MEDIA_NOT_CONNECTED;
		}
		// Need to ask whether to delete or just unlink
		final String message;
		if (hasPhoto && hasAudio) {
			message = I18nManager.getText("dialog.deletepoint.deletephotoandaudio");
		} else if (hasPhoto) {
			message = I18nManager.getText("dialog.deletepoint.deletephoto", inPoint.getPhoto().getName());
		} else {
			message = I18nManager.getText("dialog.deletepoint.deleteaudio", inPoint.getAudio().getName());
		}
		int response = JOptionPane.showConfirmDialog(_app.getFrame(), message,
				I18nManager.getText("dialog.deletepoint.title"),
				JOptionPane.YES_NO_CANCEL_OPTION);
		if (response == JOptionPane.CANCEL_OPTION || response == JOptionPane.CLOSED_OPTION) {
			// cancel pressed- abort delete
			return PopupResponse.CANCEL;
		}
		return (response == JOptionPane.YES_OPTION ? PopupResponse.DELETE : PopupResponse.UNLINK);
	}
}
