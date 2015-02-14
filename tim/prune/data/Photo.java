package tim.prune.data;

import java.awt.Dimension;
import java.io.File;

import javax.swing.ImageIcon;

/**
 * Class to represent a photo and link to DataPoint
 */
public class Photo
{
	/** File where photo is stored */
	private File _file = null;
	/** Timestamp, if any */
	private Timestamp _timestamp = null;
	/** Associated DataPoint if correlated */
	private DataPoint _dataPoint = null;
	/** Size of original image */
	private Dimension _size = null;
	/** Status of photo when loaded */
	private Status _originalStatus = Status.NOT_CONNECTED;
	/** Current photo status */
	private Status _currentStatus = Status.NOT_CONNECTED;
	/** rotation flag (clockwise from 0 to 3) */
	private int _rotation = 0;
	// TODO: Need to store caption for image?
	// thumbnail for image (from exif)
	private byte[] _exifThumbnail = null;

	/** Photo status */
	public enum Status {
		/** Photo is not connected to any point */
		NOT_CONNECTED,
		/** Photo has been connected to a point since it was loaded */
		TAGGED,
		/** Photo is connected to a point */
		CONNECTED
	};

	/**
	 * Constructor
	 * @param inFile File object for photo
	 */
	public Photo(File inFile)
	{
		_file = inFile;
	}


	/**
	 * @return File object where photo resides
	 */
	public File getFile()
	{
		return _file;
	}


	/**
	 * Set the data point associated with the photo
	 * @param inPoint DataPoint with coordinates etc
	 */
	public void setDataPoint(DataPoint inPoint)
	{
		_dataPoint = inPoint;
		// set status according to point
		if (inPoint == null) {
			setCurrentStatus(Status.NOT_CONNECTED);
		}
		else {
			setCurrentStatus(Status.CONNECTED);
		}
	}

	/**
	 * @return the DataPoint object
	 */
	public DataPoint getDataPoint()
	{
		return _dataPoint;
	}

	/**
	 * @param inTimestamp Timestamp of photo
	 */
	public void setTimestamp(Timestamp inTimestamp)
	{
		_timestamp = inTimestamp;
	}

	/**
	 * @return timestamp of photo
	 */
	public Timestamp getTimestamp()
	{
		return _timestamp;
	}

	/**
	 * Calculate the size of the image (slow)
	 */
	private void calculateSize()
	{
		ImageIcon icon = new ImageIcon(_file.getAbsolutePath());
		int width = icon.getIconWidth();
		int height = icon.getIconHeight();
		if (width > 0 && height > 0)
		{
			_size = new Dimension(width, height);
		}
	}

	/**
	 * @return size of image as Dimension object
	 */
	public Dimension getSize()
	{
		if (_size == null)
		{
			calculateSize();
		}
		return _size;
	}

	/**
	 * @return width of the image, if known
	 */
	public int getWidth()
	{
		if (_size == null)
		{
			calculateSize();
			if (_size == null) {return -1;}
		}
		return _size.width;
	}

	/**
	 * @return height of the image, if known
	 */
	public int getHeight()
	{
		if (_size == null)
		{
			calculateSize();
			if (_size == null) {return -1;}
		}
		return _size.height;
	}

	/**
	 * @param inStatus status of photo when loaded
	 */
	public void setOriginalStatus(Status inStatus)
	{
		_originalStatus = inStatus;
		_currentStatus = inStatus;
	}

	/**
	 * @return status of photo when it was loaded
	 */
	public Status getOriginalStatus()
	{
		return _originalStatus;
	}

	/**
	 * @return current status of photo
	 */
	public Status getCurrentStatus()
	{
		return _currentStatus;
	}
	/**
	 * @param inStatus current status of photo
	 */
	public void setCurrentStatus(Status inStatus)
	{
		_currentStatus = inStatus;
	}

	/**
	 * @return true if photo is connected to a point
	 */
	public boolean isConnected()
	{
		return _currentStatus != Status.NOT_CONNECTED;
	}

	/**
	 * @return byte array of thumbnail data
	 */
	public byte[] getExifThumbnail()
	{
		return _exifThumbnail;
	}

	/**
	 * @param inBytes byte array from exif
	 */
	public void setExifThumbnail(byte[] inBytes)
	{
		_exifThumbnail = inBytes;
	}

	/**
	 * Delete the cached data when the Photo is no longer needed
	 */
	public void resetCachedData()
	{
		_size = null;
		// remove thumbnail too
	}

	/**
	 * Check if a Photo object refers to the same File as another
	 * @param inOther other Photo object
	 * @return true if the Files are the same
	 */
	public boolean equals(Photo inOther)
	{
		return (inOther != null && inOther.getFile() != null && getFile() != null
			&& inOther.getFile().equals(getFile()));
	}

	/**
	 * @param inRotation initial rotation value (from exif)
	 */
	public void setRotation(int inRotation)
	{
		if (inRotation >= 0 && inRotation <= 3) {
			_rotation = inRotation;
		}
	}

	/**
	 * Rotate the image by 90 degrees
	 * @param inRight true to rotate right, false for left
	 */
	public void rotate(boolean inRight)
	{
		int dir = inRight?1:3;
		_rotation = (_rotation + dir) % 4;
	}

	/**
	 * @return rotation status
	 */
	public int getRotationDegrees()
	{
		return _rotation * 90;
	}
}
