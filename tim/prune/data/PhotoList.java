package tim.prune.data;

import java.util.ArrayList;

/**
 * Class to hold a list of Photos
 */
public class PhotoList
{
	private ArrayList _photos = null;

	/**
	 * @return the number of photos in the list
	 */
	public int getNumPhotos()
	{
		if (_photos == null) return 0;
		return _photos.size();
	}


	/**
	 * Add a List of Photos
	 * @param inList List containing Photo objects
	 */
	public void addPhoto(Photo inPhoto)
	{
		// Make sure array is initialised
		if (_photos == null)
		{
			_photos = new ArrayList();
		}
		// Add the photo
		if (inPhoto != null)
		{
			_photos.add(inPhoto);
		}
	}


	/**
	 * Checks if the specified Photo is already in the list
	 * @param inPhoto Photo object to check
	 * @return true if it's already in the list
	 */
	public boolean contains(Photo inPhoto)
	{
		// Check if we need to check
		if (getNumPhotos() <= 0 || inPhoto == null || inPhoto.getFile() == null)
			return false;
		// Loop around photos in list
		for (int i=0; i<getNumPhotos(); i++)
		{
			if (getPhoto(i) != null && getPhoto(i).equals(inPhoto))
			{
				return true;
			}
		}
		// not found
		return false;
	}


	/**
	 * Get the Photo at the given index
	 * @param inIndex index number, starting at 0
	 * @return specified Photo object
	 */
	public Photo getPhoto(int inIndex)
	{
		if (inIndex < 0 || inIndex >= getNumPhotos()) return null;
		return (Photo) _photos.get(inIndex);
	}


	/**
	 * Crop the photo list to the specified size
	 * @param inIndex previous size
	 */
	public void cropTo(int inIndex)
	{
		if (inIndex <= 0)
		{
			// delete whole list
			_photos.clear();
		}
		else
		{
			// delete photos to previous size
			while (_photos.size() > inIndex)
			{
				_photos.remove(_photos.size()-1);
			}
		}
	}

	/**
	 * @return array of file names
	 */
	public String[] getNameList()
	{
		String[] names = new String[getNumPhotos()];
		for (int i=0; i<getNumPhotos(); i++)
		{
			names[i] = getPhoto(i).getFile().getName();
		}
		return names;
	}
}
