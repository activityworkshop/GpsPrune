package tim.prune.gui.map;

import java.net.MalformedURLException;
import java.net.URL;

import tim.prune.config.Config;

/**
 * Class to hold the config for the map tiles
 * and retrieve the correct URL prefix
 */
public class MapTileConfig
{
	/** Index of map server */
	private int _index = 0;
	/** Url for other */
	private String _url = null;

	/** server urls for known maps */
	private static final String[] SERVER_URLS = {
		"http://tile.openstreetmap.org/", // mapnik
		"http://tah.openstreetmap.org/Tiles/tile/",      // osma
		"http://andy.sandbox.cloudmade.com/tiles/cycle/" // cyclemap
	};
	/** Index of 'other' server with freeform url */
	private static final int OTHER_SERVER_NUM = 3;


	/**
	 * Default constructor using Config
	 */
	public MapTileConfig()
	{
		_index = Config.getConfigInt(Config.KEY_MAPSERVERINDEX);
		_url = fixUrl(Config.getConfigString(Config.KEY_MAPSERVERURL));
		// reset index wrong or if other url too short
		if (_index < 0 || _index > OTHER_SERVER_NUM ||
			(_index == OTHER_SERVER_NUM && (_url == null || _url.length() < 5)))
		{
			_index = 0;
		}
	}

	/**
	 * @return url
	 */
	public String getUrl()
	{
		if (_index == OTHER_SERVER_NUM) {return _url;}
		return SERVER_URLS[_index];
	}

	/**
	 * Checks the given url for having the right prefix and trailing slash
	 * @param inUrl url to check
	 * @return validated url with correct prefix and trailing slash, or null
	 */
	private static String fixUrl(String inUrl)
	{
		if (inUrl == null || inUrl.equals("")) {return null;}
		String url = inUrl;
		// check prefix
		try {
			new URL(url);
		}
		catch (MalformedURLException e) {
			// add the http protocol
			url = "http://" + url;
		}
		// check trailing /
		if (!url.endsWith("/")) {
			url = url + "/";
		}
		return url;
	}

	/**
	 * @param inOther other config object
	 * @return true if the objects are exactly the same
	 */
	public boolean equals(MapTileConfig inOther)
	{
		// Other object must be non-null and must have same index
		if (inOther == null || inOther._index != _index) {return false;}
		// Check url if other selected
		if (_index == OTHER_SERVER_NUM) {
			return inOther._url.equals(_url);
		}
		// Not other so must match
		return true;
	}
}
