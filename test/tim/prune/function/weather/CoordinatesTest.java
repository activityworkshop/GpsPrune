package tim.prune.function.weather;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import tim.prune.App;
import tim.prune.data.DataPoint;
import tim.prune.data.Latitude;
import tim.prune.data.Longitude;

/**
 * Test the determination of which coordinates to use
 */
public class CoordinatesTest
{
	@Test
	public void testCoordsWhenTrackEmpty()
	{
		LatLongPair latLong = GetWeatherForecastFunction.getCoordinatesFromTrack(null);
		Assertions.assertNull(latLong);

		App app = new App(null, null);
		latLong = GetWeatherForecastFunction.getCoordinatesFromTrack(app.getTrackInfo());
		Assertions.assertNull(latLong);
	}

	@Test
	public void testCoordsWhenNoPointSelected()
	{
		App app = new App(null, null);
		DataPoint point1 = new DataPoint(Latitude.make("1.0"), Longitude.make("-5.0"));
		app.getTrackInfo().getTrack().appendPoint(point1);
		DataPoint point2 = new DataPoint(Latitude.make("3.0"), Longitude.make("55.0"));
		app.getTrackInfo().getTrack().appendPoint(point2);
		LatLongPair latLong = GetWeatherForecastFunction.getCoordinatesFromTrack(app.getTrackInfo());
		Assertions.assertNotNull(latLong);
		Assertions.assertEquals(2.0, latLong.getLatitude());
		Assertions.assertEquals(25.0, latLong.getLongitude());
	}

	@Test
	public void testCoordsWhenPointSelected()
	{
		App app = new App(null, null);
		DataPoint point1 = new DataPoint(Latitude.make("1.0"), Longitude.make("-5.0"));
		app.getTrackInfo().getTrack().appendPoint(point1);
		DataPoint point2 = new DataPoint(Latitude.make("3.0"), Longitude.make("55.0"));
		app.getTrackInfo().getTrack().appendPoint(point2);
		app.getTrackInfo().selectPoint(0);
		LatLongPair latLong = GetWeatherForecastFunction.getCoordinatesFromTrack(app.getTrackInfo());
		Assertions.assertNotNull(latLong);
		Assertions.assertEquals(1.0, latLong.getLatitude());
		Assertions.assertEquals(-5.0, latLong.getLongitude());

		app.getTrackInfo().selectPoint(1);
		latLong = GetWeatherForecastFunction.getCoordinatesFromTrack(app.getTrackInfo());
		Assertions.assertNotNull(latLong);
		Assertions.assertEquals(3.0, latLong.getLatitude());
		Assertions.assertEquals(55.0, latLong.getLongitude());
	}
}
