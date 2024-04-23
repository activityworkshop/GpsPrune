package tim.prune.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests for the static methods of the Distance class
 */
public class DistanceTest
{
	@Test
	public void testConvertUnits()
	{
		final Unit metres = UnitSetLibrary.UNITS_METRES;
		final Unit feet = UnitSetLibrary.UNITS_FEET;
		assertEquals(1.0, Distance.convertBetweenUnits(1.0, metres, metres));
		assertEquals(1.0, Distance.convertBetweenUnits(1.0, feet, feet));

		// metres to feet
		assertEquals(3.2808, Distance.convertBetweenUnits(1.0, metres, feet));
		// feet to metres
		assertEquals(0.30480370641307, Distance.convertBetweenUnits(1.0, feet, metres));
	}

	@Test
	public void testDistances()
	{
		// North/South from 0,0 to 1 degree north of there
		double radiansForOneDegree = Distance.calculateRadiansBetween(0.0, 0.0, 1.0, 0.0);
		assertEquals(0.0174533, radiansForOneDegree, 0.0000001);
		int metresForOneDegree = (int) Distance.convertRadiansToDistance(radiansForOneDegree, UnitSetLibrary.UNITS_METRES);
		assertEquals(111226, metresForOneDegree);

		// East/West from 0,0 to 1 degree east of there
		radiansForOneDegree = Distance.calculateRadiansBetween(0.0, 0.0, 0.0, 1.0);
		assertEquals(0.0174533, radiansForOneDegree, 0.0000001);
		metresForOneDegree = (int) Distance.convertRadiansToDistance(radiansForOneDegree, UnitSetLibrary.UNITS_METRES);
		assertEquals(111226, metresForOneDegree);

		// North/South from 60 to 61 degrees north
		radiansForOneDegree = Distance.calculateRadiansBetween(60.0, 0.0, 61.0, 0.0);
		assertEquals(0.0174533, radiansForOneDegree, 0.0000001);
		metresForOneDegree = (int) Distance.convertRadiansToDistance(radiansForOneDegree, UnitSetLibrary.UNITS_METRES);
		assertEquals(111226, metresForOneDegree);

		// East/West from 60 degrees north - now the angles and distances are much smaller because it's further from the equator
		radiansForOneDegree = Distance.calculateRadiansBetween(60.0, 0.0, 60.0, 1.0);
		assertEquals(0.0087266, radiansForOneDegree, 0.0000001);
		metresForOneDegree = (int) Distance.convertRadiansToDistance(radiansForOneDegree, UnitSetLibrary.UNITS_METRES);
		assertEquals(55612, metresForOneDegree);

		// same for south
		radiansForOneDegree = Distance.calculateRadiansBetween(-60.0, 0.0, -60.0, 1.0);
		assertEquals(0.0087266, radiansForOneDegree, 0.0000001);
		metresForOneDegree = (int) Distance.convertRadiansToDistance(radiansForOneDegree, UnitSetLibrary.UNITS_METRES);
		assertEquals(55612, metresForOneDegree);
	}

	@Test
	public void testDistancesSamePoint()
	{
		double latitude = 42.123, longitude = -81.101;
		double radians = Distance.calculateRadiansBetween(latitude, longitude, latitude, longitude);
		assertEquals(0.0, radians);
	}
}
