package tim.prune.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LatitudeTest
{
	@Test
	public void testRejectOutOfRange()
	{
		Assertions.assertNull(Latitude.make(91.0));
		Assertions.assertNull(Latitude.make(-91.0));
	}

	@Test
	public void testInRange()
	{
		Coordinate eightyNine = Latitude.make(89.0);
		Assertions.assertNotNull(eightyNine);
		Assertions.assertEquals(89.0, eightyNine.getDouble());
	}

	@Test
	public void testRejectStringOutOfRange()
	{
		Assertions.assertNull(Latitude.make("91.0"));
		Assertions.assertNull(Latitude.make("-91.0"));
		Assertions.assertNull(Latitude.make("N 101d13.0"));
		Assertions.assertNull(Latitude.make("S 101d13 12.00"));
	}

	@Test
	public void testStringWithoutCardinal()
	{
		Coordinate coord = Latitude.make("65.0");
		Assertions.assertNotNull(coord);
		Assertions.assertEquals("N 65.000", coord.output(Coordinate.Format.DEG, 3));

		coord = Latitude.make("-34 55.0");
		Assertions.assertNotNull(coord);
		Assertions.assertEquals("S034°55.00'", coord.output(Coordinate.Format.DEG_MIN, 2));
	}

	@Test
	public void testStringWithCardinal()
	{
		Coordinate coord = Latitude.make("N 22.123");
		Assertions.assertNotNull(coord);
		Assertions.assertEquals("N 22.12", coord.output(Coordinate.Format.DEG, 2));
		Assertions.assertEquals(22.123, coord.getDouble());

		coord = Latitude.make("-3 59 30.0");
		Assertions.assertNotNull(coord);
		Assertions.assertEquals("S003°59.50'", coord.output(Coordinate.Format.DEG_MIN, 2));
		Assertions.assertTrue(coord.getDouble() < -3.0);
	}

	@Test
	public void testUseOriginalStringIfPossible()
	{
		Coordinate coord = Latitude.make("27.123401234012340");
		Assertions.assertNotNull(coord);
		Assertions.assertEquals("N 27.12", coord.output(Coordinate.Format.DEG, 2));
		Assertions.assertEquals("N 27.12340123", coord.output(Coordinate.Format.DEG));

		Assertions.assertEquals("27.12", coord.output(Coordinate.Format.DECIMAL_FORCE_POINT, 2));
		Assertions.assertEquals("27.123401234012340", coord.output(Coordinate.Format.DECIMAL_FORCE_POINT));
	}

	@Test
	public void testUseOriginalStringNotPossible()
	{
		Coordinate coord = Latitude.make("N 27.123401234012340");
		Assertions.assertNotNull(coord);
		Assertions.assertEquals("N 27.12", coord.output(Coordinate.Format.DEG, 2));
		Assertions.assertEquals("N 27.123401234012340", coord.output(Coordinate.Format.DEG));

		// Don't use original string with all the decimal places, re-format it to 8
		Assertions.assertEquals("27.12", coord.output(Coordinate.Format.DECIMAL_FORCE_POINT, 2));
		Assertions.assertEquals("27.12340123", coord.output(Coordinate.Format.DECIMAL_FORCE_POINT));

		coord = Latitude.make("27,123401234012340");
		Assertions.assertNotNull(coord);
		Assertions.assertEquals("N 27.12", coord.output(Coordinate.Format.DEG, 2));
		Assertions.assertEquals("N 27.12340123", coord.output(Coordinate.Format.DEG));

		// Can't use original string because it has a comma, re-format it to 8 decimal places
		Assertions.assertEquals("27.12", coord.output(Coordinate.Format.DECIMAL_FORCE_POINT, 2));
		Assertions.assertEquals("27.12340123", coord.output(Coordinate.Format.DECIMAL_FORCE_POINT));
	}
}
