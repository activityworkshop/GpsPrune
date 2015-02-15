package tim.prune.data;

import java.io.File;

/**
 * Class to represent a general media file for correlation.
 * Subclasses are currently Photo and AudioFile
 */
public abstract class MediaFile
{
	/** File where media is stored */
	protected File _file = null;
	/** Timestamp, if any */
	protected Timestamp _timestamp = null;
	/** Associated DataPoint if correlated */
	protected DataPoint _dataPoint = null;
	/** Status when loaded */
	private Status _originalStatus = Status.NOT_CONNECTED;
	/** Current status */
	private Status _currentStatus = Status.NOT_CONNECTED;

	/** Connection status */
	public enum Status {
		/** Media is not connected to any point */
		NOT_CONNECTED,
		/** Media has been connected to a point since it was loaded */
		TAGGED,
		/** Media is connected to a point */
		CONNECTED
	};


	/**
	 * Constructor
	 * @param inFile file object
	 * @param inStamp timestamp object
	 */
	public MediaFile(File inFile, Timestamp inStamp)
	{
		_file = inFile;
		_timestamp = inStamp;
	}

	/**
	 * @return the file object
	 */
	public File getFile() {
		return _file;
	}

	/**
	 * @return the timestamp object
	 */
	public Timestamp getTimestamp() {
		return _timestamp;
	}

	/**
	 * @param inTimestamp Timestamp object
	 */
	public void setTimestamp(Timestamp inTimestamp) {
		_timestamp = inTimestamp;
	}

	/**
	 * @return true if details are valid (might not have timestamp)
	 */
	public boolean isValid() {
		return _file != null && _file.exists() && _file.canRead();
	}

	/**
	 * @return true if file has timestamp
	 */
	public boolean hasTimestamp() {
		 return _timestamp != null && _timestamp.isValid();
	}

	/**
	 * Check if this object refers to the same File as another
	 * @param inOther other MediaFile object
	 * @return true if the Files are the same
	 */
	public boolean equals(MediaFile inOther)
	{
		return (inOther != null && inOther.getFile() != null && getFile() != null
			&& inOther.getFile().equals(getFile()));
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
	public DataPoint getDataPoint() {
		return _dataPoint;
	}

	/**
	 * @param inStatus status of file when loaded
	 */
	public void setOriginalStatus(Status inStatus)
	{
		_originalStatus = inStatus;
		_currentStatus = inStatus;
	}

	/**
	 * @return status of file when it was loaded
	 */
	public Status getOriginalStatus()
	{
		return _originalStatus;
	}

	/**
	 * @return current status
	 */
	public Status getCurrentStatus()
	{
		return _currentStatus;
	}
	/**
	 * @param inStatus current status
	 */
	public void setCurrentStatus(Status inStatus)
	{
		_currentStatus = inStatus;
	}

	/**
	 * @return true if file is connected to a point
	 */
	public boolean isConnected()
	{
		return _currentStatus != Status.NOT_CONNECTED;
	}

	/**
	 * Reset any cached data held by the media file (eg thumbnail)
	 */
	public void resetCachedData() {}
}
