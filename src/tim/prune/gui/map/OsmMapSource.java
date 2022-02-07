package tim.prune.gui.map;

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
	private final int _maxZoom;


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
		if (inStr2 != null && inStr2.length() == 3) {
			init(inName, inStr1, inStr2, null, null);
		}
		else {
			init(inName, inStr1, "png", inStr2, "png");
		}
		_maxZoom = inMaxZoom;
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
		init(inName, inUrl1, inExt1, inUrl2, inExt2);
		_maxZoom = inMaxZoom;
	}

	/**
	 * Initialisation giving name, urls, extensions and maximum zoom
	 * @param inName source name
	 * @param inUrl1 base layer url
	 * @param inExt1 extension for base layer
	 * @param inUrl2 upper layer url
	 * @param inExt2 extension for top layer
	 */
	private void init(String inName, String inUrl1, String inExt1,
		String inUrl2, String inExt2)
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
		_siteNames[0] = SiteNameUtils.convertUrlToDirectory(_baseUrls[0]);
		_siteNames[1] = SiteNameUtils.convertUrlToDirectory(_baseUrls[1]);
		// Swap layers if second layer given without first
		if (_baseUrls[0] == null && _baseUrls[1] != null)
		{
			_baseUrls[0] = _baseUrls[1];
			_siteNames[0] = _siteNames[1];
			_baseUrls[1] = _siteNames[1] = null;
		}
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
			} catch (NumberFormatException ignored) {}
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
		String path = SiteNameUtils.pickServerUrl(_baseUrls[inLayerNum]);
		if (path.indexOf('{') < 0) {
			return path + inZoom + '/' + inX + '/' + inY +
					'.' + getFileExtension(inLayerNum);
		}
		return path.replace("{z}", Integer.toString(inZoom))
				.replace("{x}", Integer.toString(inX))
				.replace("{y}", Integer.toString(inY));
	}

	/**
	 * @return maximum zoom level
	 */
	public final int getMaxZoomLevel()
	{
		return _maxZoom;
	}

	/**
	 * @return semicolon-separated list of all fields
	 */
	public String getConfigString()
	{
		return "o:" + getName() + ";" + getSiteStrings() + getMaxZoomLevel();
	}
}
