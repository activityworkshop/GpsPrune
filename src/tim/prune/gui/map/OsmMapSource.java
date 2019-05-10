package tim.prune.gui.map;

import java.util.regex.Matcher;
import tim.prune.I18nManager;

/**
 * Class to provide a map source for all OSM-like sources
 * (eg mapnik, opencyclemap, openpistemap etc).
 * These can be single-layer or double-layer sources with tiles
 * in various formats (default png)
 */
public class OsmMapSource extends MapSource
{
	/** Name for this source */
	private String _name = null;
	/** Base urls */
	private String[] _baseUrls = null;
	/** Site names */
	private String[] _siteNames = null;
	/** Maximum zoom level */
	private int _maxZoom = 0;
	/** API key, usually remains empty */
	private String _apiKey = null;


	/**
	 * Constructor giving single name and url
	 * @param inName source name
	 * @param inUrl base url
	 */
	public OsmMapSource(String inName, String inUrl)
	{
		this(inName, inUrl, "png", null, null, 18);
	}

	/**
	 * Constructor giving name, two strings and maximum zoom
	 * @param inName source name
	 * @param inStr1 base layer url
	 * @param inStr2 either base layer extension or upper layer url
	 * @param inMaxZoom maximum zoom level
	 */
	public OsmMapSource(String inName, String inStr1, String inStr2, int inMaxZoom)
	{
		if (inStr2 != null && inStr2.length() == 3)
			init(inName, inStr1, inStr2, null, null, inMaxZoom);
		else
			init(inName, inStr1, "png", inStr2, "png", inMaxZoom);
	}

	/**
	 * Constructor giving name, urls, extensions and maximum zoom
	 * @param inName source name
	 * @param inUrl1 base layer url
	 * @param inExt1 extension for base layer
	 * @param inUrl2 upper layer url
	 * @param inExt2 extension for top layer
	 * @param inMaxZoom maximum zoom level
	 */
	public OsmMapSource(String inName, String inUrl1, String inExt1,
		String inUrl2, String inExt2, int inMaxZoom)
	{
		init(inName, inUrl1, inExt1, inUrl2, inExt2, inMaxZoom);
	}

	/**
	 * Initialisation giving name, urls, extensions and maximum zoom
	 * @param inName source name
	 * @param inUrl1 base layer url
	 * @param inExt1 extension for base layer
	 * @param inUrl2 upper layer url
	 * @param inExt2 extension for top layer
	 * @param inMaxZoom maximum zoom level
	 */
	private void init(String inName, String inUrl1, String inExt1,
		String inUrl2, String inExt2, int inMaxZoom)
	{
		_name = inName;
		if (_name == null || _name.trim().equals("")) {_name = I18nManager.getText("mapsource.unknown");}
		_baseUrls = new String[2];
		_baseUrls[0] = fixBaseUrl(inUrl1);
		_baseUrls[1] = fixBaseUrl(inUrl2);
		_extensions = new String[2];
		_extensions[0] = inExt1;
		_extensions[1] = inExt2;
		_siteNames = new String[2];
		_siteNames[0] = fixSiteName(_baseUrls[0]);
		_siteNames[1] = fixSiteName(_baseUrls[1]);
		// Swap layers if second layer given without first
		if (_baseUrls[0] == null && _baseUrls[1] != null)
		{
			_baseUrls[0] = _baseUrls[1];
			_siteNames[0] = _siteNames[1];
			_baseUrls[1] = _siteNames[1] = null;
		}
		_maxZoom = inMaxZoom;
	}

	/** Set the API key (if required) */
	public void setApiKey(String inKey)
	{
		_apiKey = inKey;
	}

	/**
	 * Construct a new map source from its config string
	 * @param inConfigString string from Config, separated by semicolons
	 * @return new map source, or null if not parseable
	 */
	public static OsmMapSource fromConfig(String inConfigString)
	{
		OsmMapSource source = null;
		if (inConfigString.startsWith("o:"))
		{
			String[] items = inConfigString.substring(2).split(";");
			try {
				if (items.length == 3) { // single source url
					source = new OsmMapSource(items[0], items[1], null, Integer.parseInt(items[2]));
				}
				else if (items.length == 4) { // two urls or one url plus extension
					source = new OsmMapSource(items[0], items[1], items[2], Integer.parseInt(items[3]));
				}
				else if (items.length == 6) { // two urls and two extensions
					source = new OsmMapSource(items[0], items[1], items[2], items[3], items[4], Integer.parseInt(items[5]));
				}
			} catch (NumberFormatException nfe) {}
		}
		return source;
	}

	/**
	 * @return name
	 */
	public String getName() {
		return _name;
	}

	/** Number of layers */
	public int getNumLayers() {
		return _baseUrls[1] == null?1:2;
	}

	/** Base url for this source */
	public String getBaseUrl(int inLayerNum) {
		return _baseUrls[inLayerNum];
	}

	/** site name without protocol or www. */
	public String getSiteName(int inLayerNum) {
		return _siteNames[inLayerNum];
	}

	/**
	 * Make the URL to get the specified tile
	 */
	public String makeURL(int inLayerNum, int inZoom, int inX, int inY)
	{
		// Check if the base url has a [1234], if so replace at random
		StringBuffer url = new StringBuffer();
		url.append(pickServerUrl(_baseUrls[inLayerNum]));
		url.append(inZoom).append('/').append(inX).append('/').append(inY);
		url.append('.').append(getFileExtension(inLayerNum));
		if (_apiKey != null)
		{
			url.append("?apikey=").append(_apiKey);
		}
		return url.toString();
	}

	/**
	 * @return maximum zoom level
	 */
	public final int getMaxZoomLevel()
	{
		return _maxZoom;
	}

	/**
	 * If the base url contains something like [1234], then pick a server
	 * @param inBaseUrl base url
	 * @return modified base url
	 */
	protected static final String pickServerUrl(String inBaseUrl)
	{
		if (inBaseUrl == null || inBaseUrl.indexOf('[') < 0) {
			return inBaseUrl;
		}
		// Check for [.*] (once only)
		// Only need to support one, make things a bit easier
		final Matcher matcher = WILD_PATTERN.matcher(inBaseUrl);
		// if not, return base url unchanged
		if (!matcher.matches()) {
			return inBaseUrl;
		}
		// if so, pick one at random and replace in the String
		final String match = matcher.group(2);
		final int numMatches = match.length();
		String server = null;
		if (numMatches > 0)
		{
			int matchNum = (int) Math.floor(Math.random() * numMatches);
			server = "" + match.charAt(matchNum);
		}
		final String result = matcher.group(1) + (server==null?"":server) + matcher.group(3);
		return result;
	}

	/**
	 * @return semicolon-separated list of all fields
	 */
	public String getConfigString()
	{
		return "o:" +  getName() + ";" + getSiteStrings() + getMaxZoomLevel();
	}
}
