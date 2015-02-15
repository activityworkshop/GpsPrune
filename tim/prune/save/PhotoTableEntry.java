package tim.prune.save;

import tim.prune.I18nManager;
import tim.prune.data.Photo;

/**
 * Class to represent a row of the photo table for saving exif
 */
public class PhotoTableEntry
{
	private Photo _photo = null;
	private String _photoName = null;
	private boolean _save = true;
	private String _status = null;

	/**
	 * Constructor
	 * @param inPhoto photo object
	 */
	public PhotoTableEntry(Photo inPhoto)
	{
		_photo = inPhoto;
		if (inPhoto != null)
		{
			_photoName = inPhoto.getName();
			_status = getStatusString(inPhoto.getOriginalStatus(), inPhoto.getCurrentStatus());
		}
	}


	/**
	 * Make a status string from the given status bytes
	 * @param inOriginalStatus original status of photo
	 * @param inCurrentStatus current status of photo
	 * @return status string for display
	 */
	private static String getStatusString (Photo.Status inOriginalStatus, Photo.Status inCurrentStatus)
	{
		if (inOriginalStatus != inCurrentStatus)
		{
			if (inOriginalStatus == Photo.Status.NOT_CONNECTED)
			{
				// originally didn't have a point, now it has
				return I18nManager.getText("dialog.saveexif.photostatus.connected");
			}
			if (inCurrentStatus == Photo.Status.NOT_CONNECTED)
			{
				// originally had a point, now it doesn't
				return I18nManager.getText("dialog.saveexif.photostatus.disconnected");
			}
			// originally had a point, now it has a different one
			return I18nManager.getText("dialog.saveexif.photostatus.modified");
		}
		// unrecognised status
		return null;
	}

	/**
	 * @return Photo object
	 */
	public Photo getPhoto()
	{
		return _photo;
	}

	/**
	 * @return photo filename
	 */
	public String getName()
	{
		return _photoName;
	}

	/**
	 * @return photo status as string
	 */
	public String getStatus()
	{
		return _status;
	}

	/**
	 * @param inFlag true to save exif, false otherwise
	 */
	public void setSaveFlag(boolean inFlag)
	{
		_save = inFlag;
	}

	/**
	 * @return true to save exif, false otherwise
	 */
	public boolean getSaveFlag()
	{
		return _save;
	}
}
