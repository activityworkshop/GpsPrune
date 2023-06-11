package tim.prune.function.weather;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import tim.prune.GpsPrune;
import tim.prune.I18nManager;
import tim.prune.data.NumberUtils;

/**
 * Runtime stream provider using the online services of openweathermap.org
 */
public class OnlineStreamProvider implements StreamProvider
{
	/** Unique API key for GpsPrune */
	private static final String OPENWEATHERMAP_API_KEY = "d1c5d792362f5a5c2eacf70a3b72ecd6";

	@Override
	public InputStream getCurrentWeatherStream(double inLatitude, double inLongitude, String inLocationId,
			boolean inUseCelsius) throws IOException
	{
		// Build url either with coordinates or with location id if available
		final String urlString = "http://api.openweathermap.org/data/2.5/weather?"
			+ (inLocationId == null ? ("lat=" + NumberUtils.formatNumberUk(inLatitude, 5)
			+ "&lon=" + NumberUtils.formatNumberUk(inLongitude, 5))
				: ("id=" + inLocationId))
			+ "&lang=" + I18nManager.getText("openweathermap.lang")
			+ "&mode=xml&units=" + (inUseCelsius ? "metric" : "imperial")
			+ "&APPID=" + OPENWEATHERMAP_API_KEY;

		URLConnection conn = new URL(urlString).openConnection();
		conn.setRequestProperty("User-Agent", "GpsPrune v" + GpsPrune.VERSION_NUMBER);
		return conn.getInputStream();
	}

	@Override
	public InputStream getForecastStream(String inLocationId, boolean inDaily, boolean inCelsius) throws IOException
	{
		final String forecastCount = inDaily ? "8" : "3";
		final String urlString = "http://api.openweathermap.org/data/2.5/forecast"
			+ (inDaily ? "/daily" : "") + "?id=" + inLocationId + "&lang=" + I18nManager.getText("openweathermap.lang")
			+ "&mode=xml&units=" + (inCelsius ? "metric" : "imperial") + "&cnt=" + forecastCount
			+ "&APPID=" + OPENWEATHERMAP_API_KEY;
		URLConnection conn = new URL(urlString).openConnection();
		conn.setRequestProperty("User-Agent", "GpsPrune v" + GpsPrune.VERSION_NUMBER);
		return conn.getInputStream();
	}
}
