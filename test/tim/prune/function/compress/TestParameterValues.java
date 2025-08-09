package tim.prune.function.compress;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestParameterValues
{
	@Test
	public void testDefaultValues()
	{
		ParameterValues values = new ParameterValues();
		Assertions.assertEquals("", values.getValue(CompressionMethodType.NONE));
		Assertions.assertEquals("", values.getValue(CompressionMethodType.DUPLICATES));
		Assertions.assertEquals("200", values.getValue(CompressionMethodType.NEARBY_WITH_FACTOR));
		Assertions.assertEquals("2", values.getValue(CompressionMethodType.WACKY_POINTS));
		Assertions.assertEquals("2", values.getValue(CompressionMethodType.SINGLETONS));
		Assertions.assertEquals("2000", values.getValue(CompressionMethodType.DOUGLAS_PEUCKER));
		Assertions.assertEquals("10", values.getValue(CompressionMethodType.NEARBY_WITH_DISTANCE));
		Assertions.assertEquals("2", values.getValue(CompressionMethodType.TOO_SLOW));
		Assertions.assertEquals("40", values.getValue(CompressionMethodType.TOO_FAST));
		Assertions.assertEquals("20", values.getValue(CompressionMethodType.TIME_DIFFERENCE));
	}

	@Test
	public void testFromEmptyOldString()
	{
		ParameterValues values = new ParameterValues();
		values.applyOldStyleConfig("");
		Assertions.assertEquals("", values.getValue(CompressionMethodType.NONE));
		Assertions.assertEquals("", values.getValue(CompressionMethodType.DUPLICATES));
		Assertions.assertEquals("200", values.getValue(CompressionMethodType.NEARBY_WITH_FACTOR));
		Assertions.assertEquals("2", values.getValue(CompressionMethodType.WACKY_POINTS));
		Assertions.assertEquals("2", values.getValue(CompressionMethodType.SINGLETONS));
		Assertions.assertEquals("2000", values.getValue(CompressionMethodType.DOUGLAS_PEUCKER));
		Assertions.assertEquals("10", values.getValue(CompressionMethodType.NEARBY_WITH_DISTANCE));
		Assertions.assertEquals("2", values.getValue(CompressionMethodType.TOO_SLOW));
		Assertions.assertEquals("40", values.getValue(CompressionMethodType.TOO_FAST));
		Assertions.assertEquals("20", values.getValue(CompressionMethodType.TIME_DIFFERENCE));
	}

	@Test
	public void testFromOldString()
	{
		ParameterValues values = new ParameterValues();
		values.applyOldStyleConfig("0;10;11;12;13;14");
		Assertions.assertEquals("", values.getValue(CompressionMethodType.NONE));
		Assertions.assertEquals("", values.getValue(CompressionMethodType.DUPLICATES));
		Assertions.assertEquals("10", values.getValue(CompressionMethodType.NEARBY_WITH_FACTOR));
		Assertions.assertEquals("11", values.getValue(CompressionMethodType.WACKY_POINTS));
		Assertions.assertEquals("12", values.getValue(CompressionMethodType.SINGLETONS));
		Assertions.assertEquals("13", values.getValue(CompressionMethodType.DOUGLAS_PEUCKER));
		Assertions.assertEquals("10", values.getValue(CompressionMethodType.NEARBY_WITH_DISTANCE));
		Assertions.assertEquals("2", values.getValue(CompressionMethodType.TOO_SLOW));
		Assertions.assertEquals("40", values.getValue(CompressionMethodType.TOO_FAST));
		Assertions.assertEquals("20", values.getValue(CompressionMethodType.TIME_DIFFERENCE));
	}

	@Test
	public void testFromEmptyNewString()
	{
		ParameterValues values = new ParameterValues();
		values.applyNewStyleConfig("");
		Assertions.assertEquals("", values.getValue(CompressionMethodType.NONE));
		Assertions.assertEquals("", values.getValue(CompressionMethodType.DUPLICATES));
		Assertions.assertEquals("200", values.getValue(CompressionMethodType.NEARBY_WITH_FACTOR));
		Assertions.assertEquals("2", values.getValue(CompressionMethodType.WACKY_POINTS));
		Assertions.assertEquals("2", values.getValue(CompressionMethodType.SINGLETONS));
		Assertions.assertEquals("2000", values.getValue(CompressionMethodType.DOUGLAS_PEUCKER));
		Assertions.assertEquals("10", values.getValue(CompressionMethodType.NEARBY_WITH_DISTANCE));
		Assertions.assertEquals("2", values.getValue(CompressionMethodType.TOO_SLOW));
		Assertions.assertEquals("40", values.getValue(CompressionMethodType.TOO_FAST));
		Assertions.assertEquals("20", values.getValue(CompressionMethodType.TIME_DIFFERENCE));
	}

	@Test
	public void testFromNewString()
	{
		ParameterValues values = new ParameterValues();
		values.applyNewStyleConfig("xDUP:;oDPC:1500;xNEF:120;xWAC:1.8;;");
		Assertions.assertEquals("", values.getValue(CompressionMethodType.NONE));
		Assertions.assertEquals("", values.getValue(CompressionMethodType.DUPLICATES));
		Assertions.assertEquals("120", values.getValue(CompressionMethodType.NEARBY_WITH_FACTOR));
		Assertions.assertEquals("1.8", values.getValue(CompressionMethodType.WACKY_POINTS));
		Assertions.assertEquals("2", values.getValue(CompressionMethodType.SINGLETONS));
		Assertions.assertEquals("1500", values.getValue(CompressionMethodType.DOUGLAS_PEUCKER));
		Assertions.assertEquals("10", values.getValue(CompressionMethodType.NEARBY_WITH_DISTANCE));
		Assertions.assertEquals("2", values.getValue(CompressionMethodType.TOO_SLOW));
		Assertions.assertEquals("40", values.getValue(CompressionMethodType.TOO_FAST));
		Assertions.assertEquals("20", values.getValue(CompressionMethodType.TIME_DIFFERENCE));
	}
}
