package tim.prune.function.weather;

import java.io.IOException;
import java.io.InputStream;


public class FileStreamProvider implements StreamProvider
{
	@Override
	public InputStream getCurrentWeatherStream(double inLatitude, double inLongitude, String inLocationId,
			boolean inUseCelsius) throws IOException
	{
		return getClass().getResourceAsStream("xml/examplecurrentweather.xml");
	}

	@Override
	public InputStream getForecastStream(String inLocationId, boolean inDaily, boolean inCelsius) throws IOException
	{
		return getClass().getResourceAsStream("xml/exampleweatherforecast.xml");
	}
}
