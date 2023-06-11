package tim.prune.function.weather;

import java.io.IOException;
import java.io.InputStream;

/**
 * Provider of input streams to weather xml
 */
public interface StreamProvider
{
	/** @return a stream to the xml for the current weather */
	public InputStream getCurrentWeatherStream(double inLatitude, double inLongitude,
		String inLocationId, boolean inUseCelsius) throws IOException;

	/** @return a stream to the xml for the weather forecast */
	public InputStream getForecastStream(String inLocationId, boolean inDaily, boolean inCelsius)
		 throws IOException;
}
