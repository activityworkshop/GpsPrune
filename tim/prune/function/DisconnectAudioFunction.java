package tim.prune.function;

import tim.prune.App;
import tim.prune.DataSubscriber;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.AudioClip;
import tim.prune.data.DataPoint;
import tim.prune.undo.UndoDisconnectMedia;
import tim.prune.undo.UndoOperation;

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
			UndoOperation undo = new UndoDisconnectMedia(point, false, true, audio.getName());
			// disconnect
			audio.setDataPoint(null);
			point.setAudio(null);
			UpdateMessageBroker.informSubscribers(DataSubscriber.SELECTION_CHANGED);
			_app.completeFunction(undo, I18nManager.getText("confirm.audio.disconnect"));
		}
	}
}
