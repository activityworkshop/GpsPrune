package tim.prune.function.search;

/**
 * Class to hold a search result from wikipedia or other online service
 */
public class SearchResult implements Comparable<SearchResult>
{
	/** Track name or title */
	private String _trackName = null;
	/** Point type (for POIs) */
	private String _pointType = null;
	/** Description */
	private String _description = null;
	/** Web page for more details */
	private String _webUrl = null;
	/** Track length in metres */
	private double _trackLength = 0.0;
	/** Download link */
	private String _downloadLink = null;
	/** Coordinates of point */
	private String _latitude = null, _longitude = null;


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
	 * @param inType type of point (for POIs)
	 */
	public void setPointType(String inType)
	{
		_pointType = inType;
	}

	/**
	 * @return type of point (for POIs)
	 */
	public String getPointType()
	{
		return _pointType;
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

	/**
	 * @param inLatitude latitude
	 */
	public void setLatitude(String inLatitude) {
		_latitude = inLatitude;
	}

	/**
	 * @return latitude
	 */
	public String getLatitude() {
		return _latitude;
	}

	/**
	 * @param inLongitude longitude
	 */
	public void setLongitude(String inLongitude) {
		_longitude = inLongitude;
	}

	/**
	 * @return longitude
	 */
	public String getLongitude() {
		return _longitude;
	}

	/**
	 * Compare two search results for sorting (nearest first, then alphabetic)
	 */
	public int compareTo(SearchResult inOther)
	{
		double distDiff = getLength() - inOther.getLength();
		if (distDiff < 0.0)
		{
			return -1;
		}
		if (distDiff > 0.0)
		{
			return 1;
		}
		return getTrackName().compareTo(inOther.getTrackName());
	}
}
