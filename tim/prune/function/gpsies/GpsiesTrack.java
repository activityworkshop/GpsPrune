package tim.prune.function.gpsies;

/**
 * Class to hold a single track from Gpsies.com
 */
public class GpsiesTrack
{
	/** Track name or title */
	private String _trackName = null;
	/** Description */
	private String _description = null;
	/** File id for more details */
	private String _fileId = null;
	/** Track length in metres */
	private double _trackLength = 0.0;
	/** Download link */
	private String _downloadLink = null;


	/**
	 * @param inName name of track
	 */
	public void setTrackName(String inName)
	{
		_trackName = inName;
	}

	/**
	 * @return track name
	 */
	public String getTrackName()
	{
		return _trackName;
	}

	/**
	 * @param inDesc description
	 */
	public void setDescription(String inDesc)
	{
		_description = inDesc;
	}

	/**
	 * @return track description
	 */
	public String getDescription()
	{
		return _description;
	}

	/**
	 * @param inId id of track
	 */
	public void setFileId(String inId)
	{
		_fileId = inId;
	}

	/**
	 * @return file id
	 */
	public String getFileId()
	{
		return _fileId;
	}

	/**
	 * @param inLength length of track
	 */
	public void setLength(double inLength)
	{
		_trackLength = inLength;
	}

	/**
	 * @return track length
	 */
	public double getLength()
	{
		return _trackLength;
	}

	/**
	 * @param inLink link to download track
	 */
	public void setDownloadLink(String inLink)
	{
		_downloadLink = inLink;
	}

	/**
	 * @return download link
	 */
	public String getDownloadLink()
	{
		return _downloadLink;
	}
}
