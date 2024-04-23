package tim.prune.function.weather;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import tim.prune.App;
import tim.prune.data.DataPoint;
import tim.prune.data.Latitude;
import tim.prune.data.Longitude;

/**
 * Test the parsing of the weather xml
 */
public class ParsingTest
{
	@Test
	public void testEmptyTrack()
	{
		App app = new App(null, null);
		GetWeatherForecastFunction function = new GetWeatherForecastFunction(app);
		function.setStreamProvider(new FileStreamProvider());
		WeatherResults weather = function.getCurrentWeather(true);
		assertNull(weather);
		WeatherResults forecast = function.getWeatherForecast(true, true);
		assertNull(forecast);
	}

	@Test
	public void testCurrentWeather()
	{
		App app = new App(null, null);
		DataPoint point = new DataPoint(Latitude.make("1.234"), Longitude.make("-5.0"));
		app.getTrackInfo().getTrack().appendPoint(point);
		GetWeatherForecastFunction function = new GetWeatherForecastFunction(app);
		function.setStreamProvider(new FileStreamProvider());
		WeatherResults weather = function.getCurrentWeather(true);
		assertNotNull(weather);
		assertEquals("Katolis", weather.getLocationName());
		// Cannot test weather.getSunriseTime() because it depends on the local timezone where the test is run
		assertEquals("2013-07-25T05:22:46", weather.getUpdateTime());
		assertEquals("Nebel", weather.getForecast(0).getDescription());
		assertEquals("fog", weather.getForecast(0).getImageName());
		assertEquals("17", weather.getForecast(0).getTemps());
	}

	@Test
	public void testForecast()
	{
		App app = new App(null, null);
		DataPoint point = new DataPoint(Latitude.make("1.234"), Longitude.make("-5.0"));
		app.getTrackInfo().getTrack().appendPoint(point);
		GetWeatherForecastFunction function = new GetWeatherForecastFunction(app);
		function.setStreamProvider(new FileStreamProvider());
		WeatherResults weather = function.getCurrentWeather(true);
		assertNotNull(weather);
		WeatherResults forecast = function.getWeatherForecast(true, true);
		assertEquals("Elarion", forecast.getLocationName());
		assertEquals("klarer Himmel", forecast.getForecast(0).getDescription());
		assertEquals("sehr starker Regen", forecast.getForecast(1).getDescription());
		assertEquals("clear-day", forecast.getForecast(0).getImageName());
		assertEquals("rain", forecast.getForecast(1).getImageName());
		assertEquals("15 &mdash; 24", forecast.getForecast(0).getTemps());
	}
}
