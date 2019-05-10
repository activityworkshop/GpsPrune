package tim.prune.function;

import tim.prune.App;
import tim.prune.DataSubscriber;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.Photo;
import tim.prune.undo.UndoRotatePhoto;

/**
 * Class to provide the function to rotate a photo
 * either clockwise or anticlockwise
 */
public class RotatePhoto extends GenericFunction
{
	/** Direction of rotation */
	private boolean _direction = true;

	/**
	 * Constructor
	 * @param inApp application object for callback
	 * @param inDir true for clockwise, false for anticlockwise
	 */
	public RotatePhoto(App inApp, boolean inDir)
	{
		super(inApp);
		_direction = inDir;
	}

	/** Get the name key */
	public String getNameKey() {
		return _direction?"function.rotatephotoright":"function.rotatephotoleft";
	}

	/**
	 * Begin the function
	 */
	public void begin()
	{
		Photo photo = _app.getTrackInfo().getCurrentPhoto();
		if (photo != null)
		{
			UndoRotatePhoto undo = new UndoRotatePhoto(photo, _direction);
			photo.rotate(_direction);
			UpdateMessageBroker.informSubscribers(DataSubscriber.PHOTOS_MODIFIED);
			_app.completeFunction(undo, I18nManager.getText("confirm.rotatephoto"));
		}
	}
}
