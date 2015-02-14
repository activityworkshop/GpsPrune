package tim.prune.data;

import java.util.ArrayList;

/**
 * Class to hold a list of Photos
 */
public class PhotoList
{
	private ArrayList _photos = null;

	/**
	 * Empty constructor
	 */
	public PhotoList()
	{
		this(null);
	}

	/**
	 * Constructor
	 * @param inList ArrayList containing Photo objects
	 */
	private PhotoList(ArrayList inList)
	{
		_photos = inList;
	}


	/**
	 * @return the number of photos in the list
	 */
	public int getNumPhotos()
	{
		if (_photos == null) return 0;
		return _photos.size();
	}


	/**
	 * Add a Photo to the list
	 * @param inPhoto Photo object to add
	 */
	public void addPhoto(Photo inPhoto)
	{
		if (inPhoto != null)
		{
			// Make sure array is initialised
			if (_photos == null)
			{
				_photos = new ArrayList();
			}
			// Add the photo
			_photos.add(inPhoto);
		}
	}


	/**
	 * Add a Photo to the list
	 * @param inPhoto Photo object to add
	 * @param inIndex index at which to add photo
	 */
	public void addPhoto(Photo inPhoto, int inIndex)
	{
		if (inPhoto != null)
		{
			// Make sure array is initialised
			if (_photos == null)
			{
				_photos = new ArrayList();
			}
			// Add the photo
			_photos.add(inIndex, inPhoto);
		}
	}


	/**
	 * Remove the selected photo from the list
	 * @param inIndex index number to remove
	 */
	public void deletePhoto(int inIndex)
	{
		// Maybe throw exception if this fails?
		if (_photos != null)
		{
			_photos.remove(inIndex);
		}
	}


	/**
	 * Checks if the specified Photo is already in the list
	 * @param inPhoto Photo object to check
	 * @return true if it's already in the list
	 */
	public boolean contains(Photo inPhoto)
	{
		return (getPhotoIndex(inPhoto) > -1);
	}


	/**
	 * Get the index of the given Photo
	 * @param inPhoto Photo object to check
	 * @return index of this Photo in the list, or -1 if not found
	 */
	public int getPhotoIndex(Photo inPhoto)
	{
		// Check if we need to check
		int numPhotos = getNumPhotos();
		if (numPhotos <= 0 || inPhoto == null || inPhoto.getFile() == null)
			return -1;
		// Loop around photos in list
		Photo foundPhoto = null;
		for (int i=0; i<numPhotos; i++)
		{
			foundPhoto = getPhoto(i);
			if (foundPhoto != null && foundPhoto.equals(inPhoto))
			{
				return i;
			}
		}
		// not found
		return -1;
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
			if (_photos != null) {_photos.clear();}
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


	/**
	 * @return true if photo list contains correlated photos
	 */
	public boolean hasCorrelatedPhotos()
	{
		int numPhotos = getNumPhotos();
		boolean hasCorrelated = false;
		// Loop over photos in list
		for (int i=0; i<numPhotos && !hasCorrelated; i++)
		{
			if (getPhoto(i).getDataPoint() != null)
				hasCorrelated = true;
		}
		return hasCorrelated;
	}


	/**
	 * Remove all correlated photos from the list
	 */
	public void removeCorrelatedPhotos()
	{
		int numPhotos = getNumPhotos();
		if (numPhotos > 0)
		{
			// Construct new list to copy into
			ArrayList listCopy = new ArrayList();
			// Loop over photos in list
			for (int i=0; i<numPhotos; i++)
			{
				// Copy photo if it has no point
				Photo photo = getPhoto(i);
				if (photo != null)
				{
					if (photo.getDataPoint() == null)
						listCopy.add(photo);
					else
						photo.resetCachedData();
				}
			}
			// Switch reference to new list
			_photos = listCopy;
		}
	}


	/**
	 * @return clone of photo list contents
	 */
	public PhotoList cloneList()
	{
		if (_photos == null) return this;
		return new PhotoList((ArrayList) _photos.clone());
	}


	/**
	 * Restore contents from other PhotoList
	 * @param inOther PhotoList with cloned contents
	 */
	public void restore(PhotoList inOther)
	{
		if (inOther.getNumPhotos() == 0)
		{
			// List is empty
			_photos = null;
		}
		else
		{
			// Clear array and copy over from other one
			_photos.clear();
			_photos.addAll(inOther._photos);
		}
	}
}
