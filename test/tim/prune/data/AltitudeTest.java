package tim.prune.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AltitudeTest
{
	@Test
	public void testConstructorWithString()
	{
		Altitude altWithCm = new Altitude("123.45", UnitSetLibrary.UNITS_METRES);
		String valueInMetres = altWithCm.getStringValue(UnitSetLibrary.UNITS_METRES);
		Assertions.assertEquals("123.45", valueInMetres);
		String valueInFeet = altWithCm.getStringValue(UnitSetLibrary.UNITS_FEET);
		Assertions.assertEquals("405.01476", valueInFeet);
		int metres = (int) altWithCm.getValue(UnitSetLibrary.UNITS_METRES);
		Assertions.assertEquals(123, metres);

		Altitude altInFeet = new Altitude("1234.5", UnitSetLibrary.UNITS_FEET);
		valueInMetres = altInFeet.getStringValue(UnitSetLibrary.UNITS_METRES);
		Assertions.assertEquals("376.2801755669349", valueInMetres);
		valueInFeet = altInFeet.getStringValue(UnitSetLibrary.UNITS_FEET);
		Assertions.assertEquals("1234.5", valueInFeet);
		metres = (int) altInFeet.getValue(UnitSetLibrary.UNITS_METRES);
		Assertions.assertEquals(376, metres);

		altInFeet = new Altitude("1236.601", UnitSetLibrary.UNITS_FEET);
		metres = (int) altInFeet.getValue(UnitSetLibrary.UNITS_METRES);
		Assertions.assertEquals(376, metres); // rounded down
		metres = altInFeet.getIntValue(UnitSetLibrary.UNITS_METRES);
		Assertions.assertEquals(377, metres); // rounded to nearest
	}

	@Test
	public void testValidStrings()
	{
		Altitude valid = new Altitude("1e4", null);
		Assertions.assertTrue(valid.isValid());
		Assertions.assertEquals(10000.0, valid.getMetricValue());
		valid = new Altitude("-11.81", null);
		Assertions.assertTrue(valid.isValid());
		Assertions.assertEquals(-12, valid.getIntValue(null));
	}

	@Test
	public void testInvalidStrings()
	{
		Altitude notValid = new Altitude(null, null);
		Assertions.assertFalse(notValid.isValid());
		notValid = new Altitude("", null);
		Assertions.assertFalse(notValid.isValid());
		notValid = new Altitude("--", UnitSetLibrary.UNITS_METRES);
		Assertions.assertFalse(notValid.isValid());
		notValid = new Altitude("abc", UnitSetLibrary.UNITS_METRES);
		Assertions.assertFalse(notValid.isValid());
	}

	@Test
	public void testInterpolate()
	{
		Altitude altitude1 = new Altitude(7000.0, UnitSetLibrary.UNITS_METRES);
		Altitude altitude2 = new Altitude(8000.0, UnitSetLibrary.UNITS_METRES);
		Altitude middle = Altitude.interpolate(altitude1, altitude2, 0.5);
		Assertions.assertEquals(7500.0, middle.getMetricValue());

		middle = Altitude.interpolate(altitude2, altitude1, 0.5);
		Assertions.assertEquals(7500.0, middle.getMetricValue());
	}
}
