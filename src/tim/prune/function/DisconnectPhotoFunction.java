package tim.prune.function;

import tim.prune.App;
import tim.prune.DataSubscriber;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.DataPoint;
import tim.prune.data.Photo;
import tim.prune.undo.UndoDisconnectMedia;

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
			UndoDisconnectMedia undo = new UndoDisconnectMedia(point, true, false, photo.getName());
			// disconnect
			photo.setDataPoint(null);
			point.setPhoto(null);
			UpdateMessageBroker.informSubscribers(DataSubscriber.SELECTION_CHANGED);
			_app.completeFunction(undo, I18nManager.getText("confirm.photo.disconnect"));
		}
	}
}
