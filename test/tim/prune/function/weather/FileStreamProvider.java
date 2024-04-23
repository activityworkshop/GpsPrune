package tim.prune.function.weather;

import java.io.InputStream;


public final class FileStreamProvider implements StreamProvider
{
	@Override
	public InputStream getCurrentWeatherStream(double inLatitude, double inLongitude, String inLocationId,
			boolean inUseCelsius)
	{
		return getClass().getResourceAsStream("xml/examplecurrentweather.xml");
	}

	@Override
	public InputStream getForecastStream(String inLocationId, boolean inDaily, boolean inCelsius)
	{
		return getClass().getResourceAsStream("xml/exampleweatherforecast.xml");
	}
}
