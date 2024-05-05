package tim.prune.function.weather;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import tim.prune.GpsPrune;
import tim.prune.data.NumberUtils;

/**
 * Runtime stream provider using the online services of openweathermap.org
 */
public class OnlineStreamProvider
{
	/** Unique API key for GpsPrune */
	private static final String OPENWEATHERMAP_API_KEY = "d1c5d792362f5a5c2eacf70a3b72ecd6";

	public InputStream getLocationStream(double inLatitude, double inLongitude) throws IOException
	{
		// Build url with coordinates
		final String urlString = "http://api.openweathermap.org/geo/1.0/reverse?"
			+ "lat=" + NumberUtils.formatNumberUk(inLatitude, 5)
			+ "&lon=" + NumberUtils.formatNumberUk(inLongitude, 5)
			+ "&limit=1"
			+ "&appid=" + OPENWEATHERMAP_API_KEY;

		URLConnection conn = new URL(urlString).openConnection();
		conn.setRequestProperty("User-Agent", "GpsPrune v" + GpsPrune.VERSION_NUMBER);
		return conn.getInputStream();
	}
}
