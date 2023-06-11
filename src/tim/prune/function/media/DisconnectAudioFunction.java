package tim.prune.function.media;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.cmd.ConnectMediaCmd;
import tim.prune.data.AudioClip;
import tim.prune.data.DataPoint;


/**
 * Function to disconnect the current audio object from the current point (like DisconnectPhotoFunction)
 */
public class DisconnectAudioFunction extends GenericFunction
{
	/**
	 * Constructor
	 * @param inApp app object
	 */
	public DisconnectAudioFunction(App inApp) {
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
		AudioClip audio = _app.getTrackInfo().getCurrentAudio();
		if (audio != null && audio.getDataPoint() != null)
		{
			DataPoint point = audio.getDataPoint();
			ConnectMediaCmd command = new ConnectMediaCmd(point, point.getPhoto(), null);
			command.setDescription(I18nManager.getText("undo.disconnect", audio.getName()));
			command.setConfirmText(I18nManager.getText("confirm.audio.disconnect"));
			_app.execute(command);
		}
	}
}
