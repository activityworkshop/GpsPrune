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
	/** Web page for more details */
	private String _webUrl = null;
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
	 * @param inUrl web page url
	 */
	public void setWebUrl(String inUrl)
	{
		_webUrl = inUrl;
	}

	/**
	 * @return web url
	 */
	public String getWebUrl()
	{
		return _webUrl;
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
