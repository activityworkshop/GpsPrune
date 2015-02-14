package tim.prune.gui.map;

import tim.prune.I18nManager;

/**
 * Class to provide a map source for all OSM-like sources
 * (eg mapnik, opencyclemap, openpistemap etc).
 * These can be single-layer or double-layer sources with png tiles
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

	/**
	 * Constructor giving single name and url
	 * @param inName source name
	 * @param inUrl base url
	 */
	public OsmMapSource(String inName, String inUrl)
	{
		this(inName, inUrl, null, 18);
	}

	/**
	 * Constructor giving name, urls and maximum zoom
	 * @param inName source name
	 * @param inUrl1 base layer url
	 * @param inUrl2 upper layer url
	 * @param inMaxZoom maximum zoom level
	 */
	public OsmMapSource(String inName, String inUrl1, String inUrl2, int inMaxZoom)
	{
		_name = inName;
		if (_name == null || _name.trim().equals("")) {_name = I18nManager.getText("mapsource.unknown");}
		_baseUrls = new String[2];
		_baseUrls[0] = fixBaseUrl(inUrl1);
		_baseUrls[1] = fixBaseUrl(inUrl2);
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
				if (items.length == 3) {
					source = new OsmMapSource(items[0], items[1], null, Integer.parseInt(items[2]));
				}
				else if (items.length == 4) {
					source = new OsmMapSource(items[0], items[1], items[2], Integer.parseInt(items[3]));
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
		return _baseUrls[inLayerNum] + inZoom + "/" + inX + "/" + inY + getFileExtension(inLayerNum);
	}

	/** file extension is always png */
	public final String getFileExtension(int inLayerNum) {
		return ".png";
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
		return "o:" +  getName() + ";" + getSiteStrings() + getMaxZoomLevel();
	}
}
