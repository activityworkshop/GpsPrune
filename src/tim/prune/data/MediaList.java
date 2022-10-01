package tim.prune.data;

import java.util.ArrayList;

/**
 * Class to hold a list of Media, either Photos or Audio files
 */
public abstract class MediaList
{
	/** list of media objects */
	protected ArrayList<MediaObject> _media = null;


	/**
	 * Constructor
	 * @param inList ArrayList containing media objects
	 */
	protected MediaList(ArrayList<MediaObject> inList)
	{
		_media = inList;
		if (_media == null) {
			_media = new ArrayList<MediaObject>();
		}
	}

	/**
	 * @return the number of media in the list
	 */
	public int getNumMedia() {
		return _media.size();
	}

	/**
	 * Add an object to the list
	 * @param inObject object to add
	 */
	public void addMedia(MediaObject inObject)
	{
		if (inObject != null) {
			_media.add(inObject);
		}
	}

	/**
	 * Add an object to the list at a specified index (used for undo)
	 * @param inObject object to add
	 * @param inIndex index at which to add
	 */
	public void addMedia(MediaObject inObject, int inIndex)
	{
		if (inObject != null) {
			_media.add(inIndex, inObject);
		}
	}


	/**
	 * Remove the selected media from the list
	 * @param inIndex index number to remove
	 */
	public void deleteMedia(int inIndex)
	{
		// Maybe throw exception if this fails?
		_media.remove(inIndex);
	}


	/**
	 * Checks if the specified object is already in the list
	 * @param inMedia media object to check
	 * @return true if it's already in the list
	 */
	public boolean contains(MediaObject inMedia) {
		return (getMediaIndex(inMedia) > -1);
	}


	/**
	 * Get the index of the given media
	 * @param inMedia object to check
	 * @return index of this object in the list, or -1 if not found
	 */
	public int getMediaIndex(MediaObject inMedia)
	{
		// Check if we need to check
		final int num = getNumMedia();
		if (num <= 0 || inMedia == null)
			return -1;
		// Loop over list
		for (int i=0; i<num; i++)
		{
			MediaObject m = _media.get(i);
			if (m != null && m.equals(inMedia)) {
				return i;
			}
		}
		// not found
		return -1;
	}


	/**
	 * Get the media at the given index
	 * @param inIndex index number, starting at 0
	 * @return specified object
	 */
	public MediaObject getMedia(int inIndex)
	{
		if (inIndex < 0 || inIndex >= getNumMedia()) return null;
		return _media.get(inIndex);
	}


	/**
	 * Crop the list to the specified size
	 * @param inIndex previous size
	 */
	public void cropTo(int inIndex)
	{
		if (inIndex <= 0)
		{
			// delete whole list
			_media.clear();
		}
		else
		{
			// delete to previous size
			while (_media.size() > inIndex) {
				_media.remove(_media.size()-1);
			}
		}
	}


	/**
	 * @return true if list contains correlated media
	 */
	public boolean hasCorrelatedMedia()
	{
		for (MediaObject m : _media) {
			if (m.getDataPoint() != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return true if list contains uncorrelated media
	 */
	public boolean hasUncorrelatedMedia()
	{
		for (MediaObject m : _media) {
			if (m.getDataPoint() == null && m.hasTimestamp()) {
				return true;
			}
		}
		return false;
	}


	/**
	 * Remove all correlated media from the list
	 */
	public void removeCorrelatedMedia()
	{
		if (getNumMedia() > 0)
		{
			// Construct new list to copy into
			ArrayList<MediaObject> listCopy = new ArrayList<MediaObject>();
			// Loop over list
			for (MediaObject m : _media)
			{
				// Copy media if it has no point
				if (m != null)
				{
					if (m.getDataPoint() == null)
						listCopy.add(m);
					else
						m.resetCachedData();
				}
			}
			// Switch reference to new list
			_media = listCopy;
		}
	}

	/**
	 * @return true if any of the media objects have Files
	 */
	public boolean hasMediaWithFile()
	{
		for (MediaObject m: _media) {
			if (m.getFile() != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return true if there are any modified media in the list
	 */
	public boolean hasModifiedMedia()
	{
		for (MediaObject m: _media) {
			if (m.isModified()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return clone of list contents
	 */
	public abstract MediaList cloneList();

	/**
	 * Restore contents from other MediaList
	 * @param inOther MediaList with cloned contents
	 */
	public void restore(MediaList inOther)
	{
		_media.clear();
		if (inOther != null && inOther.getNumMedia() > 0)
		{
			// Copy contents from other list
			_media.addAll(inOther._media);
		}
	}
}
