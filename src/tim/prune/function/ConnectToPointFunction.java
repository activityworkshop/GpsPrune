package tim.prune.function;

import tim.prune.App;
import tim.prune.DataSubscriber;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.AudioClip;
import tim.prune.data.DataPoint;
import tim.prune.data.Photo;
import tim.prune.undo.UndoConnectMedia;
import tim.prune.undo.UndoOperation;

/**
 * Function to connect either a photo or an audio clip to the current point
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
		Photo photo = _app.getTrackInfo().getCurrentPhoto();
		DataPoint point = _app.getTrackInfo().getCurrentPoint();
		AudioClip audio = _app.getTrackInfo().getCurrentAudio();
		boolean connectPhoto = (point != null && photo != null && point.getPhoto() == null);
		boolean connectAudio = (point != null && audio != null && point.getAudio() == null);

		// if (connectPhoto && connectAudio) {
			// TODO: Let user choose whether to connect photo/audio or both
		// }
		// Make undo object
		UndoOperation undo = new UndoConnectMedia(point, connectPhoto?photo.getName():null,
			connectAudio?audio.getName():null);
		// Connect the media
		if (connectPhoto) {
			photo.setDataPoint(point);
			point.setPhoto(photo);
		}
		if (connectAudio) {
			audio.setDataPoint(point);
			point.setAudio(audio);
		}
		UpdateMessageBroker.informSubscribers(DataSubscriber.SELECTION_CHANGED);
		_app.completeFunction(undo, I18nManager.getText("confirm.media.connect"));
	}
}
