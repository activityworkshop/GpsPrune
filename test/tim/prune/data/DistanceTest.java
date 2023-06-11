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

}
