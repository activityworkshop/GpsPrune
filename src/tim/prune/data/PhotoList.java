package tim.prune.data;

import java.util.ArrayList;

/**
 * Class to hold a list of Photos, using the MediaList superclass
 */
public class PhotoList extends MediaList
{
	/**
	 * Empty constructor
	 */
	public PhotoList() {
		this(null);
	}

	/**
	 * Constructor
	 * @param inList ArrayList containing Photo objects
	 */
	private PhotoList(ArrayList<MediaObject> inList) {
		super(inList);
	}

	/**
	 * @return clone of list contents
	 */
	public PhotoList cloneList()
	{
		if (getNumMedia() == 0) return this;
		ArrayList<MediaObject> listCopy = new ArrayList<MediaObject>();
		listCopy.addAll(_media);
		return new PhotoList(listCopy);
	}

	/**
	 * @return the number of photos in the list
	 */
	public int getNumPhotos() {
		return getNumMedia();
	}

	/**
	 * Add a Photo to the list
	 * @param inPhoto Photo object to add
	 */
	public void addPhoto(Photo inPhoto) {
		addMedia(inPhoto);
	}

	/**
	 * Add a Photo to the list
	 * @param inPhoto Photo object to add
	 * @param inIndex index at which to add photo
	 */
	public void addPhoto(Photo inPhoto, int inIndex) {
		addMedia(inPhoto, inIndex);
	}

	/**
	 * Remove the selected photo from the list
	 * @param inIndex index number to remove
	 */
	public void deletePhoto(int inIndex) {
		deleteMedia(inIndex);
	}

	/**
	 * Get the index of the given Photo
	 * @param inPhoto Photo object to check
	 * @return index of this Photo in the list, or -1 if not found
	 */
	public int getPhotoIndex(Photo inPhoto) {
		return getMediaIndex(inPhoto);
	}

	/**
	 * Get the Photo at the given index
	 * @param inIndex index number, starting at 0
	 * @return specified Photo object
	 */
	public Photo getPhoto(int inIndex) {
		return (Photo) getMedia(inIndex);
	}

	/**
	 * @return true if photo list contains correlated photos
	 */
	public boolean hasCorrelatedPhotos() {
		return hasCorrelatedMedia();
	}

	/**
	 * Remove all correlated photos from the list
	 */
	public void removeCorrelatedPhotos() {
		removeCorrelatedMedia();
	}
}
