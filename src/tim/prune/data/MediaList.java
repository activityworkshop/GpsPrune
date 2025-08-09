package tim.prune.data;

import java.util.ArrayList;

/**
 * Class to hold a list of MediaObjects, either Photos or Audio files
 */
public class MediaList<T extends MediaObject>
{
	/** list of media objects */
	protected final ArrayList<T> _media = new ArrayList<>();

	/**
	 * @return the number of media in the list
	 */
	public int getCount() {
		return _media.size();
	}

	/**
	 * Add an object to the list
	 * @param inObject object to add
	 */
	public void add(T inObject)
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
	public void add(T inObject, int inIndex)
	{
		if (inObject != null) {
			_media.add(inIndex, inObject);
		}
	}


	/**
	 * Remove the selected media from the list
	 * @param inIndex index number to remove
	 */
	public void delete(int inIndex)
	{
		// Maybe throw exception if this fails?
		_media.remove(inIndex);
	}

	/**
	 * Remove the selected media from the list
	 * @param inMedia object to remove
	 */
	public void delete(T inMedia)
	{
		int index = getIndexOf(inMedia);
		if (index > -1) {
			_media.remove(index);
		}
	}

	/**
	 * Checks if the specified object is already in the list
	 * @param inMedia media object to check
	 * @return true if it's already in the list
	 */
	public boolean contains(T inMedia) {
		return getIndexOf(inMedia) > -1;
	}

	/**
	 * Get the index of the given media
	 * @param inMedia object to check
	 * @return index of this object in the list, or -1 if not found
	 */
	public int getIndexOf(T inMedia)
	{
		// Check if we need to check
		final int num = getCount();
		if (num <= 0 || inMedia == null) {
			return -1;
		}
		// Loop over list
		for (int i=0; i<num; i++)
		{
			T m = _media.get(i);
			if (m == inMedia) {
				return i;
			}
		}
		// not found
		return -1;
	}

	/**
	 * Checks if a media object _like_ the given one is already in the list
	 * @param inMedia media object to check
	 * @return true if there's a duplicate one already in the list
	 */
	public boolean hasDuplicate(T inMedia)
	{
		final int num = getCount();
		if (num <= 0 || inMedia == null) {
			return false;
		}
		for (int i=0; i<num; i++)
		{
			T m = _media.get(i);
			if (inMedia.equals(m)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get the media at the given index
	 * @param inIndex index number, starting at 0
	 * @return specified object
	 */
	public T get(int inIndex)
	{
		if (inIndex < 0 || inIndex >= getCount()) {
			return null;
		}
		return _media.get(inIndex);
	}

	/**
	 * @return true if this list has any media
	 */
	public boolean hasAny() {
		return !_media.isEmpty();
	}

	/**
	 * @return true if list contains correlated media
	 */
	public boolean hasCorrelatedMedia()
	{
		for (T m : _media)
		{
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
		for (T m : _media)
		{
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
		if (getCount() > 0)
		{
			// Construct new list to copy into
			ArrayList<T> listCopy = new ArrayList<>();
			// Loop over list
			for (T m : _media)
			{
				// Copy media if it has no point
				if (m != null)
				{
					if (m.getDataPoint() == null) {
						listCopy.add(m);
					}
				}
			}
			// Switch list contents
			_media.clear();
			_media.addAll(listCopy);
		}
	}

	/**
	 * @return true if any of the media objects have Files
	 */
	public boolean hasMediaWithFile()
	{
		for (T m: _media)
		{
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
		for (T m : _media)
		{
			if (m.isModified()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Restore contents from other MediaList
	 * @param inOther MediaList with cloned contents
	 */
	public void restore(MediaList<T> inOther)
	{
		_media.clear();
		if (inOther != null && inOther.getCount() > 0)
		{
			// Copy contents from other list
			_media.addAll(inOther._media);
		}
	}
}
