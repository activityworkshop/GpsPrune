package tim.prune.function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import tim.prune.data.DataPoint;

public class PointUtilsTest
{
	@Test
	public void test_projectNorthSouth()
	{
		DataPoint point1 = new DataPoint(0.0, 2.0);
		// project north along meridian
		DataPoint pointNorth = PointUtils.projectPoint(point1, 0.0, 0.1);
		Assertions.assertEquals(2.0, pointNorth.getLongitude().getDouble(), 0.00001);
		Assertions.assertEquals(5.7296, pointNorth.getLatitude().getDouble(), 0.0001);
		// project south along meridian
		DataPoint pointSouth = PointUtils.projectPoint(point1, Math.PI, 0.1);
		Assertions.assertEquals(2.0, pointSouth.getLongitude().getDouble(), 0.00001);
		Assertions.assertEquals(-5.7296, pointSouth.getLatitude().getDouble(), 0.0001);
	}

	@Test
	public void test_projectEastWest()
	{
		DataPoint point1 = new DataPoint(0.0, 2.0);
		// project east along equator
		DataPoint pointEast = PointUtils.projectPoint(point1, Math.PI/2.0, 0.1);
		Assertions.assertEquals(7.7296, pointEast.getLongitude().getDouble(), 0.0001);
		Assertions.assertEquals(0.0, pointEast.getLatitude().getDouble(), 0.00001);
		// project west along equator
		DataPoint pointWest = PointUtils.projectPoint(point1, -Math.PI/2.0, 0.1);
		Assertions.assertEquals(-3.7296, pointWest.getLongitude().getDouble(), 0.0001);
		Assertions.assertEquals(0.0, pointWest.getLatitude().getDouble(), 0.00001);
	}

	@Test
	public void test_interpolatePoints()
	{
		DataPoint point1 = new DataPoint(0.0, 2.0);
		DataPoint point2 = new DataPoint(0.0, 3.0);
		// interpolate three points between these two
		for (int i=0; i<3; i++)
		{
			DataPoint mid = PointUtils.interpolate(point1, point2, i, 3);
			Assertions.assertEquals(2.0 + (i+1)/4.0, mid.getLongitude().getDouble(), 0.0001);
			Assertions.assertEquals(0.0, mid.getLatitude().getDouble(), 0.00001);
		}
	}
}
