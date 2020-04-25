package tim.prune.gui.map;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	/** Regular expression for catching server wildcards */
	protected static final Pattern WILD_PATTERN = Pattern.compile("^(.*)\\[(.*)\\](.*)$");


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
			if (urlstr.indexOf("://") >= 0) {return null;}
			// add the http protocol
			urlstr = "http://" + urlstr;
		}
		// check trailing / (unless it's a custom url with parameters)
		if (!urlstr.endsWith("/") && !urlstr.contains("?") && !urlstr.contains("{x}")) {
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
	 * Fix the site name by stripping off protocol and www.
	 * This is used to create the file path for disk caching
	 * @param inUrl url to strip
	 * @return stripped url
	 */
	protected static String fixSiteName(String inUrl)
	{
		if (inUrl == null || inUrl.equals("")) {return null;}
		String url = inUrl.toLowerCase();
		int idx = url.indexOf("://");
		if (idx >= 0) {url = url.substring(idx + 3);}
		if (url.startsWith("www.")) {url = url.substring(4);}
		// Strip out any "[.*]" as well
		if (url.indexOf('[') >= 0)
		{
			Matcher matcher = WILD_PATTERN.matcher(url);
			if (matcher.matches()) {
				url = matcher.group(1) + matcher.group(3);
				if (url.length() > 1 && url.charAt(0) == '.') {
					url = url.substring(1);
				}
			}
		}
		return url;
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
