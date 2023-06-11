package tim.prune.function.media;

import javax.swing.JOptionPane;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.cmd.*;
import tim.prune.data.DataPoint;
import tim.prune.data.Photo;


/**
 * Function to remove the currently selected photo
 */
public class RemovePhotoFunction extends GenericFunction
{
	/**
	 * Constructor
	 * @param inApp App object
	 */
	public RemovePhotoFunction(App inApp) {
		super(inApp);
	}

	/** @return name key */
	public String getNameKey() {
		return "function.removephoto";
	}

	/**
	 * Delete the current photo, and optionally its point too
	 */
	public void begin()
	{
		Photo currentPhoto = _app.getTrackInfo().getCurrentPhoto();
		if (currentPhoto == null) {
			return;
		}
		final RemovePhotoCmd deleteCommand = new RemovePhotoCmd(_app.getTrackInfo().getSelection().getCurrentPhotoIndex());
		final Command command;
		// Photo is selected, see if it has a point or not
		DataPoint point = currentPhoto.getDataPoint();
		switch (shouldDeletePoint(currentPhoto))
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
						.addCommand(new ConnectMediaCmd(point, null, point.getAudio()));
				break;
			case DELETE:
				String pointName = (point == null ? null : point.getWaypointName());
				command = new CompoundCommand()
						.addCommand(deleteCommand)
						.addCommand(new DeletePointCmd(_app.getTrackInfo().getSelection().getCurrentPointIndex(), pointName))
						.addCommand(point == null || point.getPhoto() == null ? null :
								new RemoveAudioCmd(_app.getTrackInfo().getSelection().getCurrentAudioIndex()));
				break;
		}
		command.setDescription(I18nManager.getText("undo.removephoto", currentPhoto.getName()));
		command.setConfirmText(I18nManager.getText("confirm.media.removed", currentPhoto.getName()));
		_app.execute(command);
	}

	/**
	 * Determine whether to delete the point, just unlink, or cancel
	 * @param inPhoto photo to delete
	 * @return decision what do to with the connected point
	 */
	private PopupResponse shouldDeletePoint(Photo inPhoto)
	{
		final DataPoint point = inPhoto.getDataPoint();
		if (point == null) {
			return PopupResponse.MEDIA_NOT_CONNECTED;
		}
		final boolean hasAudio = point.getAudio() != null;
		// Need to ask whether to delete or just unlink
		final String message;
		if (hasAudio) {
			message = I18nManager.getText("dialog.deletephoto.deletepointandaudio", point.getAudio().getName());
		} else {
			message = I18nManager.getText("dialog.deletephoto.deletepoint");
		}
		int response = JOptionPane.showConfirmDialog(_app.getFrame(), message,
				I18nManager.getText("dialog.deletephoto.title"),
				JOptionPane.YES_NO_CANCEL_OPTION);
		if (response == JOptionPane.CANCEL_OPTION || response == JOptionPane.CLOSED_OPTION) {
			// cancel pressed- abort delete
			return PopupResponse.CANCEL;
		}
		return (response == JOptionPane.YES_OPTION ? PopupResponse.DELETE : PopupResponse.UNLINK);
	}
}
