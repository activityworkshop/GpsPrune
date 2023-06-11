package tim.prune.function.media;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.cmd.ConnectMediaCmd;
import tim.prune.data.AudioClip;
import tim.prune.data.DataPoint;
import tim.prune.data.Photo;


/**
 * Function to connect either a photo or an audio clip (or both) to the current point
 */
public class ConnectToPointFunction extends GenericFunction
{
	/**
	 * Constructor
	 * @param inApp app object
	 */
	public ConnectToPointFunction(App inApp) {
		super(inApp);
	}

	/**
	 * @return name key
	 */
	public String getNameKey() {
		return "function.connecttopoint";
	}

	/**
	 * Perform function
	 */
	public void begin()
	{
		DataPoint point = _app.getTrackInfo().getCurrentPoint();
		Photo photo = _app.getTrackInfo().getCurrentPhoto();
		AudioClip audio = _app.getTrackInfo().getCurrentAudio();
		ConnectMediaCmd command = new ConnectMediaCmd(point, photo, audio);
		command.setDescription(getDescription(photo, audio));
		command.setConfirmText(I18nManager.getText("confirm.media.connect"));
		_app.execute(command);
	}

	/**
	 * @return description of operation including photo and/or audio filename(s)
	 */
	public String getDescription(Photo inPhoto, AudioClip inAudio)
	{
		final String photoName = (inPhoto == null ? null : inPhoto.getName());
		final String audioName = (inAudio == null ? null : inAudio.getName());
		return I18nManager.getText("undo.connect", denull(photoName)
			+ ((photoName != null && audioName != null) ? ", " : "")
			+ denull(audioName));
	}

	/**
	 * Convert null strings to empty strings
	 */
	private String denull(String inName) {
		return inName == null ? "" : inName;
	}
}
