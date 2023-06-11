package tim.prune.function.media;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.cmd.RotatePhotoCmd;
import tim.prune.data.Photo;


/**
 * Class to provide the function to rotate a photo
 * either clockwise or anticlockwise
 */
public class RotatePhoto extends GenericFunction
{
	/** Direction of rotation (clockwise = true) */
	private final boolean _direction;

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
		return _direction ? "function.rotatephotoright" : "function.rotatephotoleft";
	}

	/**
	 * Begin the function
	 */
	public void begin()
	{
		Photo photo = _app.getTrackInfo().getCurrentPhoto();
		if (photo != null)
		{
			RotatePhotoCmd command = new RotatePhotoCmd(photo, _direction);
			command.setDescription(getName());
			command.setConfirmText(I18nManager.getText("confirm.rotatephoto"));
			_app.execute(command);
		}
	}
}
