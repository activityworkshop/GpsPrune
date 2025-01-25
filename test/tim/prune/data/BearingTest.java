package tim.prune.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BearingTest
{
	@Test
	public void testSamePoint()
	{
		DataPoint point = new DataPoint(Latitude.make("1.81"), Longitude.make("5.44"));
		double bearing = Bearing.calculateDegrees(point,  point);
		Assertions.assertEquals(0.0, bearing);
	}

	@Test
	public void testNorthSouth()
	{
		DataPoint point1 = new DataPoint(Latitude.make("1.81"), Longitude.make("5.44"));
		DataPoint point2 = new DataPoint(Latitude.make("2.81"), Longitude.make("5.44"));
		double bearing = Bearing.calculateDegrees(point1,  point2);
		Assertions.assertEquals(0.0, bearing);
		bearing = Bearing.calculateDegrees(point2,  point1);
		Assertions.assertEquals(180.0, bearing);
	}

	@Test
	public void testEastWest()
	{
		DataPoint point1 = new DataPoint(Latitude.make("1.81"), Longitude.make("5.44"));
		DataPoint point2 = new DataPoint(Latitude.make("1.81"), Longitude.make("5.48"));
		double bearing = Bearing.calculateDegrees(point1,  point2);
		Assertions.assertEquals(90.0, bearing, 0.001);
		bearing = Bearing.calculateDegrees(point2,  point1);
		Assertions.assertEquals(-90.0, bearing, 0.001);
	}

	@Test
	public void testNorthEast_SouthWest()
	{
		DataPoint point1 = new DataPoint(Latitude.make("0.0"), Longitude.make("0.0"));
		DataPoint point2 = new DataPoint(Latitude.make("0.25"), Longitude.make("0.25"));
		double bearing = Bearing.calculateDegrees(point1,  point2);
		Assertions.assertEquals(45.0, bearing, 0.001);
		bearing = Bearing.calculateDegrees(point2,  point1);
		Assertions.assertEquals(-135.0, bearing, 0.001);
	}

	@Test
	public void testAngleDifferences()
	{
		Assertions.assertEquals(1.0, Bearing.angleDifferenceDegrees(1.0, 0.0));
		Assertions.assertEquals(1.0, Bearing.angleDifferenceDegrees(0.0, 1.0));
		Assertions.assertEquals(90.0, Bearing.angleDifferenceDegrees(0.0, 90.0));
		Assertions.assertEquals(90.0, Bearing.angleDifferenceDegrees(0.0, -90.0));
		Assertions.assertEquals(180.0, Bearing.angleDifferenceDegrees(-90.0, 90.0));
		Assertions.assertEquals(0.0, Bearing.angleDifferenceDegrees(-90.0, -90.0));
		Assertions.assertEquals(0.0, Bearing.angleDifferenceDegrees(90.0, -270.0));
		Assertions.assertEquals(0.0, Bearing.angleDifferenceDegrees(405.0, -315.0));
		Assertions.assertEquals(0.0, Bearing.angleDifferenceDegrees(-315.0, 405.0));
		Assertions.assertEquals(20.0, Bearing.angleDifferenceDegrees(10.0, 350.0));
		Assertions.assertEquals(20.0, Bearing.angleDifferenceDegrees(350.0, 10.0));
		Assertions.assertEquals(180.0, Bearing.angleDifferenceDegrees(0.0, 180.0));
		Assertions.assertEquals(180.0, Bearing.angleDifferenceDegrees(90.0, -90.0));
	}
}
