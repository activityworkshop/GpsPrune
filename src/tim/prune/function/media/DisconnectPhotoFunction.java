package tim.prune.function.media;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.cmd.ConnectMediaCmd;
import tim.prune.data.DataPoint;
import tim.prune.data.Photo;


/**
 * Function to disconnect the current photo from the current point
 */
public class DisconnectPhotoFunction extends GenericFunction
{
	/**
	 * Constructor
	 * @param inApp app object
	 */
	public DisconnectPhotoFunction(App inApp) {
		super(inApp);
	}

	/** @return name key */
	public String getNameKey() {
		return "function.disconnectfrompoint";
	}

	/**
	 * Perform the operation
	 */
	public void begin()
	{
		Photo photo = _app.getTrackInfo().getCurrentPhoto();
		if (photo != null && photo.getDataPoint() != null)
		{
			DataPoint point = photo.getDataPoint();
			ConnectMediaCmd command = new ConnectMediaCmd(point, null, point.getAudio());
			command.setDescription(I18nManager.getText("undo.disconnect", photo.getName()));
			command.setConfirmText(I18nManager.getText("confirm.photo.disconnect"));
			_app.execute(command);
		}
	}
}
