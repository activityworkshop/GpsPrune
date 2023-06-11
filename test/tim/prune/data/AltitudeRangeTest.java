package tim.prune.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for the AltitudeRange class
 */
class AltitudeRangeTest
{
	@Test
	void testEmptyRange()
	{
		AltitudeRange range = new AltitudeRange(0);
		assertFalse(range.hasRange());
		range.ignoreValue(null);
		range.addValue(null);
		assertFalse(range.hasRange());
	}

	@Test
	void testConstantRange()
	{
		AltitudeRange range = new AltitudeRange(0);
		Unit metres = UnitSetLibrary.UNITS_METRES;
		Altitude alt = new Altitude(100, metres);
		range.ignoreValue(alt);
		range.addValue(alt);
		assertTrue(range.hasRange());
		assertEquals(100, range.getMinimum(metres));
		assertEquals(100, range.getMaximum(metres));
		assertEquals(0, range.getMetricHeightDiff());
		assertEquals(0, range.getClimb(metres));
		assertEquals(0, range.getDescent(metres));
	}

	@Test
	void testConstantClimb()
	{
		AltitudeRange range = new AltitudeRange(0);
		Unit metres = UnitSetLibrary.UNITS_METRES;
		final int baseAlt = 1000;
		final int climb = 250;
		Altitude alt = new Altitude(baseAlt, metres);
		range.ignoreValue(alt);
		for (int i=0; i<=climb; i++) {
			range.addValue(new Altitude(baseAlt + i, metres));
		}
		assertTrue(range.hasRange());
		assertEquals(baseAlt, range.getMinimum(metres));
		assertEquals(baseAlt+climb, range.getMaximum(metres));
		assertEquals(climb, range.getMetricHeightDiff());
		assertEquals(climb, range.getClimb(metres));
		assertEquals(0, range.getDescent(metres));
	}

	@Test
	void testUndulationsWithTolerance()
	{
		AltitudeRange rangeZero = new AltitudeRange(0);
		AltitudeRange rangeTen = new AltitudeRange(10);
		Unit metres = UnitSetLibrary.UNITS_METRES;
		final int baseAlt = 200;
		Altitude alt = new Altitude(baseAlt, metres);
		rangeZero.ignoreValue(alt);
		rangeTen.ignoreValue(alt);
		for (int i=0; i<=100; i++)
		{
			// 5 metre undulations
			alt = new Altitude(baseAlt + i%5, metres);
			rangeZero.addValue(alt);
			rangeTen.addValue(alt);
		}
		assertTrue(rangeZero.hasRange());
		assertTrue(rangeTen.hasRange());
		assertEquals(baseAlt, rangeZero.getMinimum(metres));
		assertEquals(baseAlt, rangeTen.getMinimum(metres));
		assertEquals(baseAlt + 4, rangeZero.getMaximum(metres));
		assertEquals(baseAlt + 4, rangeTen.getMaximum(metres));
		// with zero tolerance, climb = descent = 80m
		assertEquals(80, rangeZero.getClimb(metres));
		assertEquals(80, rangeZero.getDescent(metres));
		// with 10m tolerance, climb = descent = 0
		assertEquals(0, rangeTen.getClimb(metres));
		assertEquals(0, rangeTen.getDescent(metres));
	}
}
