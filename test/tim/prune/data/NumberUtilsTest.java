package tim.prune.data;

import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NumberUtilsTest
{
	@Test
	public void testCountDecimals_none()
	{
		Assertions.assertEquals(-1, NumberUtils.numberOfDecimalPlaces(null));
		Assertions.assertEquals(-1, NumberUtils.numberOfDecimalPlaces(""));
		Assertions.assertEquals(-1, NumberUtils.numberOfDecimalPlaces("abc"));
		Assertions.assertEquals(-1, NumberUtils.numberOfDecimalPlaces("4a"));
		Assertions.assertEquals(-1, NumberUtils.numberOfDecimalPlaces("4e"));
		Assertions.assertEquals(-1, NumberUtils.numberOfDecimalPlaces("-"));
		Assertions.assertEquals(-1, NumberUtils.numberOfDecimalPlaces(" "));
	}

	@Test
	public void testCountDecimals_zero()
	{
		Assertions.assertEquals(0, NumberUtils.numberOfDecimalPlaces("4"));
		Assertions.assertEquals(0, NumberUtils.numberOfDecimalPlaces("-44"));
		Assertions.assertEquals(0, NumberUtils.numberOfDecimalPlaces("  1234  "));
		Assertions.assertEquals(0, NumberUtils.numberOfDecimalPlaces("81. "));
	}

	@Test
	public void testFormatNumber()
	{
		Assertions.assertEquals("102", NumberUtils.formatNumberLocal(102.4, 0));
		Assertions.assertEquals("103", NumberUtils.formatNumberLocal(102.51, 0));
		// Depends on local formatting
		Assertions.assertEquals("123.4", NumberUtils.formatNumberLocal(123.4251, 1));
		Assertions.assertEquals("123.43", NumberUtils.formatNumberLocal(123.4251, 2));
		Assertions.assertEquals("123.425", NumberUtils.formatNumberLocal(123.4251, 3));
		Assertions.assertEquals("123.4251", NumberUtils.formatNumberLocal(123.4251, 4));
		Assertions.assertEquals("123.42510", NumberUtils.formatNumberLocal(123.4251, 5));
		Assertions.assertEquals("123.425100", NumberUtils.formatNumberLocal(123.4251, 6));
	}

	@Test
	public void testFormatNumberToMatch()
	{
		Assertions.assertEquals("102", NumberUtils.formatNumberLocalToMatch(102.4, "5"));
		// Round up
		Assertions.assertEquals("103", NumberUtils.formatNumberLocalToMatch(102.51, "5"));
		// Depends on local formatting
		Assertions.assertEquals("123.4", NumberUtils.formatNumberLocalToMatch(123.425, "-18.1"));
	}

	@Test
	public void testParsingNonsense()
	{
		Assertions.assertNull(NumberUtils.parseDoubleUsingLocale(null));
		Assertions.assertNull(NumberUtils.parseDoubleUsingLocale(""));
		Assertions.assertNull(NumberUtils.parseDoubleUsingLocale("    "));
		Assertions.assertNull(NumberUtils.parseDoubleUsingLocale("abcde"));
	}

	@Test
	public void testParsingNormal()
	{
		Assertions.assertEquals(11.0, NumberUtils.parseDoubleUsingLocale("11"));
		Assertions.assertEquals(-211.0, NumberUtils.parseDoubleUsingLocale("-211"));
		Assertions.assertEquals(11.5, NumberUtils.parseDoubleUsingLocale("11.5"));
	}

	@Test
	public void testParsingLocal()
	{
		Locale originalLocale = Locale.getDefault();
		try
		{
			// Using an english Locale, the comma isn't recognised because the decimal character is dot
			Locale.setDefault(Locale.ENGLISH);
			Assertions.assertNull(NumberUtils.parseDoubleUsingLocale("11,5"));

			// With a german locale, both the dot and the comma are recognised
			Locale.setDefault(Locale.GERMAN);
			Assertions.assertEquals(11.5, NumberUtils.parseDoubleUsingLocale("11.5"));
			Assertions.assertEquals(-71.25, NumberUtils.parseDoubleUsingLocale("-71,25"));
			// Not allowed because of the extra apostrophe
			Assertions.assertNull(NumberUtils.parseDoubleUsingLocale("4'111,5"));
		}
		finally {
			Locale.setDefault(originalLocale);
		}
	}

	@Test
	public void testIntParsing()
	{
		Assertions.assertEquals(0, NumberUtils.getIntOrZero(null));
		Assertions.assertEquals(0, NumberUtils.getIntOrZero(""));
		Assertions.assertEquals(0, NumberUtils.getIntOrZero("abc"));
		Assertions.assertEquals(0, NumberUtils.getIntOrZero(" 123"));
		Assertions.assertEquals(0, NumberUtils.getIntOrZero("1234.0"));

		Assertions.assertEquals(-1, NumberUtils.getIntOrZero("-1"));
		Assertions.assertEquals(1234, NumberUtils.getIntOrZero("1234"));
	}

	@Test
	public void testDoubleParsing()
	{
		Assertions.assertEquals(0.0, NumberUtils.getDoubleOrZero(null));
		Assertions.assertEquals(0.0, NumberUtils.getDoubleOrZero(""));
		Assertions.assertEquals(0.0, NumberUtils.getDoubleOrZero("abc"));
		Assertions.assertEquals(123.0, NumberUtils.getDoubleOrZero(" 123"));
		Assertions.assertEquals(0.0, NumberUtils.getDoubleOrZero("1234a.0"));

		Assertions.assertEquals(-1.0, NumberUtils.getDoubleOrZero("-1"));
		Assertions.assertEquals(-100.25, NumberUtils.getDoubleOrZero("-100.250"));
		Assertions.assertEquals(1234.5, NumberUtils.getDoubleOrZero("1234.5"));
	}
}
