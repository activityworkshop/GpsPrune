package tim.prune.load;

import tim.prune.data.Photo;
import tim.prune.data.PhotoList;

/**
 * This class starts a new thread to preload image sizes
 * TODO: # Cache small image thumbnails too?
 */
public class PhotoMeasurer implements Runnable
{
	/** PhotoList to loop through */
	private PhotoList _photoList = null;


	/**
	 * Constructor
	 * @param inPhotoList photo list to loop through
	 */
	public PhotoMeasurer(PhotoList inPhotoList)
	{
		_photoList = inPhotoList;
	}


	/**
	 * Start off the process to measure the photo sizes
	 */
	public void measurePhotos()
	{
		// check if any photos in list
		if (_photoList != null && _photoList.getNumPhotos() > 0)
		{
			// start new thread
			new Thread(this).start();
		}
	}


	/**
	 * Run method called in new thread
	 */
	public void run()
	{
		try
		{
			// loop over all photos in list
			for (int i=0; i<_photoList.getNumPhotos(); i++)
			{
				Photo photo = _photoList.getPhoto(i);
				if (photo != null)
				{
					// call get size method which will calculate it if necessary
					photo.getSize();
				}
			}
		}
		catch (ArrayIndexOutOfBoundsException obe) {} // ignore, must have been changed by other thread
	}
}
