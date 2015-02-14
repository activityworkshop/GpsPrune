package tim.prune.gui;

import javax.swing.AbstractListModel;

import tim.prune.data.Photo;
import tim.prune.data.PhotoList;

/**
 * Class to act as list model for the photo list
 */
public class PhotoListModel extends AbstractListModel
{
	PhotoList _photos = null;

	/**
	 * Constructor giving PhotoList object
	 * @param inList PhotoList
	 */
	public PhotoListModel(PhotoList inList)
	{
		_photos = inList;
	}

	/**
	 * @see javax.swing.ListModel#getSize()
	 */
	public int getSize()
	{
		return _photos.getNumPhotos();
	}

	/**
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	public Object getElementAt(int inIndex)
	{
		return _photos.getPhoto(inIndex).getFile().getName();
	}

	/**
	 * Get the Photo at the given index
	 * @param inIndex index number, starting at 0
	 * @return Photo object
	 */
	public Photo getPhoto(int inIndex)
	{
		return _photos.getPhoto(inIndex);
	}

	/**
	 * Fire event to notify that contents have changed
	 */
	public void fireChanged()
	{
		this.fireContentsChanged(this, 0, getSize()-1);
	}
}
