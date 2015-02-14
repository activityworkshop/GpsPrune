package tim.prune.function;

import tim.prune.App;
import tim.prune.DataSubscriber;
import tim.prune.GenericFunction;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.Photo;

/**
 * Class to provide the function to disable the exif thumbnail
 * for the current photo so that the full image must be loaded
 */
public class IgnoreExifThumb extends GenericFunction
{
	/**
	 * Constructor
	 * @param inApp application object for callback
	 */
	public IgnoreExifThumb(App inApp)
	{
		super(inApp);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.ignoreexifthumb";
	}

	/**
	 * Begin the function
	 */
	public void begin()
	{
		Photo photo = _app.getTrackInfo().getCurrentPhoto();
		if (photo != null)
		{
			// no undo necessary, no data being edited
			photo.setExifThumbnail(null);
			UpdateMessageBroker.informSubscribers(DataSubscriber.PHOTOS_MODIFIED);
		}
	}
}
