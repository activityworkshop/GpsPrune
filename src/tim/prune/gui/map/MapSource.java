package tim.prune.gui.map;

import java.net.MalformedURLException;
import java.net.URL;


/**
 * Class to represent any map source, whether an OsmMapSource
 * or one of the more complicated ones.
 * Map sources may contain just one or several layers, and may
 * build their URLs in different ways depending on the source
 */
public abstract class MapSource
{
	/** File extensions */
	protected String[] _extensions = null;


	/**
	 * @return the number of layers used in this source
	 */
	public abstract int getNumLayers();

	/**
	 * @return the name of the source
	 */
	public abstract String getName();

	/**
	 * @return the base url for the specified layer
	 */
	public abstract String getBaseUrl(int inLayerNum);

	/**
	 * @return the site name for the specified layer
	 */
	public abstract String getSiteName(int inLayerNum);

	/**
	 * @return the file extension for the specified layer
	 */
	public final String getFileExtension(int inLayerNum) {
		return _extensions[inLayerNum];
	}

	/**
	 * Make the URL to get the specified tile
	 * @param inLayerNum number of layer, from 0 (base) to getNumLayers-1 (top)
	 * @param inZoom zoom level
	 * @param inX x coordinate of tile
	 * @param inY y coordinate of tile
	 * @return URL as string
	 */
	public abstract String makeURL(int inLayerNum, int inZoom, int inX, int inY);

	/**
	 * @return the maximum zoom level for this source
	 */
	public abstract int getMaxZoomLevel();

	/**
	 * Make a relative file path from the base directory including site name
	 * @param inLayerNum layer number
	 * @param inZoom zoom level
	 * @param inX x coordinate
	 * @param inY y coordinate
	 * @return relative file path as String
	 */
	public String makeFilePath(int inLayerNum, int inZoom, int inX, int inY)
	{
		return getSiteName(inLayerNum) + inZoom + "/" + inX + "/" + inY + "." + getFileExtension(inLayerNum);
	}

	/**
	 * Checks the given url for having the right prefix and trailing slash
	 * @param inUrl url to check
	 * @return validated url with correct prefix and trailing slash, or null
	 */
	public static String fixBaseUrl(String inUrl)
	{
		if (inUrl == null || inUrl.equals("")) {return null;}
		String urlstr = inUrl;
		boolean urlOk = false;

		// check prefix
		try
		{
			urlOk = new URL(urlstr.replace('[', 'w').replace(']', 'w')).toString() != null;
		}
		catch (MalformedURLException e)
		{
			urlOk = false;
		}

		if (!urlOk)
		{
			// fail if protocol specified
			if (urlstr.contains("://")) {return null;}
			// add the http protocol
			urlstr = "http://" + urlstr;
		}
		// check trailing /
		if (!urlstr.endsWith("/")) {
			urlstr = urlstr + "/";
		}
		// Validate current url, return null if not ok
		try
		{
			URL url = new URL(urlstr.replace('[', 'w').replace(']', 'w'));
			// url host must contain a dot
			if (url.getHost().indexOf('.') < 0) {return null;}
		}
		catch (MalformedURLException e) {
			urlstr = null;
		}
		return urlstr;
	}


	/**
	 * @return string which can be written to the Config
	 */
	public abstract String getConfigString();

	/**
	 * @return semicolon-separated list of base urls and extensions in order
	 */
	public String getSiteStrings()
	{
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<getNumLayers(); i++)
		{
			String url = getBaseUrl(i);
			if (url != null)
			{
				sb.append(url);
				sb.append(';');
				sb.append(getFileExtension(i));
				sb.append(';');
			}
		}
		return sb.toString();
	}
}
