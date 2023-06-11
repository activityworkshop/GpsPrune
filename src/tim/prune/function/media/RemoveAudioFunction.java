package tim.prune.function.media;

import javax.swing.JOptionPane;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.cmd.Command;
import tim.prune.cmd.CompoundCommand;
import tim.prune.cmd.ConnectMediaCmd;
import tim.prune.cmd.RemoveAudioCmd;
import tim.prune.cmd.RemovePhotoCmd;
import tim.prune.cmd.DeletePointCmd;
import tim.prune.data.AudioClip;
import tim.prune.data.DataPoint;


/**
 * Function to remove the currently selected audio clip
 */
public class RemoveAudioFunction extends GenericFunction
{
	/**
	 * Constructor
	 * @param inApp App object
	 */
	public RemoveAudioFunction(App inApp) {
		super(inApp);
	}

	/** @return name key */
	public String getNameKey() {
		return "function.removeaudio";
	}

	/**
	 * Delete the current audio, and optionally its point too
	 */
	public void begin()
	{
		AudioClip currentAudio = _app.getTrackInfo().getCurrentAudio();
		if (currentAudio == null) {
			return;
		}
		final RemoveAudioCmd deleteCommand = new RemoveAudioCmd(_app.getTrackInfo().getSelection().getCurrentAudioIndex());
		final Command command;
		// Audio is selected, see if it has a point or not
		DataPoint point = currentAudio.getDataPoint();
		switch (shouldDeletePoint(currentAudio))
		{
			default:
			case CANCEL:
				return;
			case MEDIA_NOT_CONNECTED:
				command = deleteCommand;
				break;
			case UNLINK:
				command = new CompoundCommand()
						.addCommand(deleteCommand)
						.addCommand(new ConnectMediaCmd(point, point.getPhoto(), null));
				break;
			case DELETE:
				String pointName = (point == null ? null : point.getWaypointName());
				command = new CompoundCommand()
						.addCommand(deleteCommand)
						.addCommand(new DeletePointCmd(_app.getTrackInfo().getSelection().getCurrentPointIndex(), pointName))
						.addCommand(point == null || point.getPhoto() == null ? null :
							new RemovePhotoCmd(_app.getTrackInfo().getSelection().getCurrentPhotoIndex()));
				break;
		}
		command.setDescription(I18nManager.getText("undo.removeaudio", currentAudio.getName()));
		command.setConfirmText(I18nManager.getText("confirm.media.removed", currentAudio.getName()));
		_app.execute(command);
	}

	/**
	 * Determine whether to delete the point, just unlink, or cancel
	 * @param inAudio audio to delete
	 * @return decision what do to with the connected point
	 */
	private PopupResponse shouldDeletePoint(AudioClip inAudio)
	{
		final DataPoint point = inAudio.getDataPoint();
		if (point == null) {
			return PopupResponse.MEDIA_NOT_CONNECTED;
		}
		final boolean hasPhoto = point.getPhoto() != null;
		// Need to ask whether to delete or just unlink
		final String message;
		if (hasPhoto) {
			message = I18nManager.getText("dialog.deleteaudio.deletepointandphoto", point.getPhoto().getName());
		} else {
			message = I18nManager.getText("dialog.deleteaudio.deletepoint");
		}
		int response = JOptionPane.showConfirmDialog(_app.getFrame(), message,
				I18nManager.getText("dialog.deleteaudio.title"),
				JOptionPane.YES_NO_CANCEL_OPTION);
		if (response == JOptionPane.CANCEL_OPTION || response == JOptionPane.CLOSED_OPTION) {
			// cancel pressed- abort delete
			return PopupResponse.CANCEL;
		}
		return (response == JOptionPane.YES_OPTION ? PopupResponse.DELETE : PopupResponse.UNLINK);
	}
}
