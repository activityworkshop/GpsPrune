package tim.prune.data;

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
}
