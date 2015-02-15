package tim.prune.data;

import java.io.File;

/**
 * Class to represent a general media object for correlation.
 * Subclasses are currently Photo and AudioClip
 */
public abstract class MediaObject
{
	/** File where media is stored (if any) */
	protected File _file = null;
	/** Name of file */
	protected String _name = null;
	/** Cached data if downloaded */
	protected byte[] _data = null;
	/** URL if media came from net */
	protected String _url = null;
	/** Timestamp, if any */
	protected Timestamp _timestamp = null;
	/** Associated DataPoint if correlated */
	protected DataPoint _dataPoint = null;
	/** Status when loaded */
	private Status _originalStatus = Status.NOT_CONNECTED;
	/** Current status */
	private Status _currentStatus = Status.NOT_CONNECTED;

	/** Connection status */
	public enum Status
	{
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
	public MediaObject(File inFile, Timestamp inStamp)
	{
		_file = inFile;
		_name = inFile.getName();
		_data = null;
		_timestamp = inStamp;
	}

	/**
	 * Constructor for byte arrays
	 * @param inData byte array containing data
	 * @param inName name of object
	 * @param inUrl source url of object or null
	 */
	public MediaObject(byte[] inData, String inName, String inUrl)
	{
		_file = null;
		_data = inData;
		_name = inName;
		_url = inUrl;
		_timestamp = null;
	}

	/**
	 * @return the file object
	 */
	public File getFile() {
		return _file;
	}

	/** @return media name */
	public String getName() {
		return _name;
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
	 * @return byte data of media
	 */
	public byte[] getByteData() {
		return _data;
	}

	/**
	 * @return source Url (or null)
	 */
	public String getUrl() {
		return _url;
	}

	/**
	 * @return the full path to the media, either filename or url
	 */
	public String getFullPath()
	{
		if (_file != null) return _file.getAbsolutePath();
		return getUrl();
	}

	/**
	 * @return true if details are valid (might not have timestamp)
	 */
	public boolean isValid()
	{
		return ((_file != null && _file.exists() && _file.canRead())
			|| (_data != null && _data.length > 0));
	}

	/**
	 * @return true if file has timestamp
	 */
	public boolean hasTimestamp() {
		 return _timestamp != null && _timestamp.isValid();
	}

	/**
	 * Check if this object refers to the same object as another
	 * @param inOther other MediaObject
	 * @return true if the objects are the same
	 */
	public boolean equals(MediaObject inOther)
	{
		if (_file != null)
		{
			// compare file objects
			return (inOther != null && inOther.getFile() != null && getFile() != null
				&& inOther.getFile().equals(getFile()));
		}
		// compare data arrays
		return (inOther != null && _name != null && inOther._name != null && _name.equals(inOther._name)
			&& _data != null && inOther._data != null && _data.length == inOther._data.length);
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
	 * @return true if this object is connected to a point
	 */
	public boolean isConnected()
	{
		return _currentStatus != Status.NOT_CONNECTED;
	}

	/**
	 * @return true if status has changed since load
	 */
	public boolean isModified()
	{
		return _currentStatus != _originalStatus;
	}

	/**
	 * Reset any cached data (eg thumbnail)
	 */
	public void resetCachedData() {}
}
