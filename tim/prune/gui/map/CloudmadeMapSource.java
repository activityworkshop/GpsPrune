package tim.prune.gui.map;

/**
 * Class to act as a source for Cloudmade maps with a given style
 */
public class CloudmadeMapSource extends OsmMapSource
{
	/** Selected style number */
	private String _style = null;
	/** Server prefix including API-key unique to Prune application */
	private static final String SERVER_PREFIX = "tile.cloudmade.com/03d86b66f51f4a3b8c236ac06f2a2e57/";

	/**
	 * Constructor
	 * @param inName name to use for map source
	 * @param inStyle style, given as integer
	 * @param inMaxZoom maximum zoom level, 18 by default
	 */
	public CloudmadeMapSource(String inName, String inStyle, int inMaxZoom)
	{
		// Note: Could check style for valid integer value here
		super(inName, SERVER_PREFIX + inStyle + "/256/", null, inMaxZoom);
		_style = inStyle;
	}

	/**
	 * @return semicolon-separated list of all fields
	 */
	public String getConfigString()
	{
		return "c:" +  getName() + ";" + _style + ";" + getMaxZoomLevel();
	}

	/**
	 * Construct a new map source from its config string
	 * @param inConfigString string from Config, separated by semicolons
	 * @return new map source, or null if not parseable
	 */
	public static CloudmadeMapSource fromConfig(String inConfigString)
	{
		CloudmadeMapSource source = null;
		if (inConfigString.startsWith("c:"))
		{
			String[] items = inConfigString.substring(2).split(";");
			try {
				if (items.length == 3) {
					source = new CloudmadeMapSource(items[0], items[1], Integer.parseInt(items[2]));
				}
			} catch (NumberFormatException nfe) {}
		}
		return source;
	}
}
