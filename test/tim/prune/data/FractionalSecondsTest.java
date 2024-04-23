package tim.prune.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FractionalSecondsTest
{
	@Test
	public void testPlainDegrees()
	{
		// 100 degrees + 0.1 seconds
		final double value = 100.0 + 0.1 / 60.0 / 60.0;

		final double seconds = (value - 100.0) * 60.0 * 60.0;
		// seconds should be 0.1, but isn't exactly

		Assertions.assertEquals("0.100000000", String.format("%.9f", seconds));
		Assertions.assertEquals("0.1000000000", String.format("%.10f", seconds));
		Assertions.assertEquals("0.09999999999", String.format("%.11f", seconds));
		Assertions.assertEquals("0.099999999986", String.format("%.12f", seconds));
		Assertions.assertEquals("0.0999999999863", String.format("%.13f", seconds));
		// This shows the problem that the FractionalSeconds class tries to solve
	}

	@Test
	public void testHundredDegTenthSecond()
	{
		// 100 degrees + 0.1 seconds
		FractionalSeconds angle = new FractionalSeconds(100L, 0L, 0L, 1L, 1);
		Assertions.assertEquals(100, angle.getWholeDegrees());
		Assertions.assertEquals(0, angle.getWholeMinutes());
		Assertions.assertEquals(0, angle.getWholeSeconds());
		Assertions.assertEquals("1", angle.getFractionSeconds());

		Assertions.assertEquals("100000000", angle.getFractionSeconds(9));
		Assertions.assertEquals("1000000000", angle.getFractionSeconds(10));
		Assertions.assertEquals("10000000000", angle.getFractionSeconds(11));
		Assertions.assertEquals("100000000000", angle.getFractionSeconds(12));
		Assertions.assertEquals("1000000000000", angle.getFractionSeconds(13));
	}

	@Test
	public void testThousandthDegree()
	{
		// 0.001 degrees
		FractionalSeconds angle = new FractionalSeconds(0L, 1L, 3);
		Assertions.assertEquals(0, angle.getWholeDegrees());
		Assertions.assertEquals("001", angle.getFractionDegrees());

		Assertions.assertEquals("0010", angle.getFractionDegrees(4));
		Assertions.assertEquals("001000000", angle.getFractionDegrees(9));
		Assertions.assertEquals("0010000000000", angle.getFractionDegrees(13));
	}

	@Test
	public void testCropSeconds()
	{
		FractionalSeconds angle = new FractionalSeconds(123L, 45L, 2L, 123456L, 8);
		Assertions.assertEquals(123, angle.getWholeDegrees());
		Assertions.assertEquals(45, angle.getWholeMinutes());
		Assertions.assertEquals(2, angle.getWholeSeconds());
		Assertions.assertEquals("00123456", angle.getFractionSeconds());

		Assertions.assertEquals("0012345600", angle.getFractionSeconds(10));
		Assertions.assertEquals("00123456", angle.getFractionSeconds(8));
		Assertions.assertEquals("0012346", angle.getFractionSeconds(7));
		Assertions.assertEquals("001235", angle.getFractionSeconds(6));
		Assertions.assertEquals("00123", angle.getFractionSeconds(5));
		Assertions.assertEquals("0012", angle.getFractionSeconds(4));
		Assertions.assertEquals("00", angle.getFractionSeconds(2));
	}

	@Test
	public void testDegMinRounding()
	{
		FractionalSeconds angle = new FractionalSeconds(51L, 59L, 883L, 3);
		Assertions.assertEquals(51, angle.getWholeDegrees());
		Assertions.assertEquals(59, angle.getWholeMinutes());
		Assertions.assertEquals("883", angle.getFractionMinutes());

		// Three digits on minutes
		FractionalSeconds threeDigits = angle.roundToMinutes(3);
		Assertions.assertEquals(51, threeDigits.getWholeDegrees());
		Assertions.assertEquals(59, threeDigits.getWholeMinutes());
		Assertions.assertEquals("883", threeDigits.getFractionMinutes());

		// Two digits on minutes
		FractionalSeconds twoDigits = angle.roundToMinutes(2);
		Assertions.assertEquals(51, twoDigits.getWholeDegrees());
		Assertions.assertEquals(59, twoDigits.getWholeMinutes());
		Assertions.assertEquals("88", twoDigits.getFractionMinutes());

		// One digit on minutes
		FractionalSeconds oneDigit = angle.roundToMinutes(1);
		Assertions.assertEquals(51, oneDigit.getWholeDegrees());
		Assertions.assertEquals(59, oneDigit.getWholeMinutes());
		Assertions.assertEquals("9", oneDigit.getFractionMinutes());

		// Zero digits on minutes
		FractionalSeconds zeroDigits = angle.roundToMinutes(0);
		Assertions.assertEquals(52, zeroDigits.getWholeDegrees());
		Assertions.assertEquals(0, zeroDigits.getWholeMinutes());
		Assertions.assertEquals("", zeroDigits.getFractionMinutes());
	}

	@Test
	public void testWrappingTo360NoChange()
	{
		FractionalSeconds angle = new FractionalSeconds(33L, 20L, 3);
		FractionalSeconds wrapped = angle.wrapToThreeSixtyDegrees();
		Assertions.assertEquals(angle.getWholeDegrees(), wrapped.getWholeDegrees());
		Assertions.assertEquals(angle.getWholeMinutes(), wrapped.getWholeMinutes());
		Assertions.assertEquals(angle.getDouble(), wrapped.getDouble());
	}

	@Test
	public void testWrappingTo360()
	{
		FractionalSeconds angle = new FractionalSeconds(400L, 0L, 3);
		FractionalSeconds wrapped = angle.wrapToThreeSixtyDegrees();
		Assertions.assertEquals(angle.getWholeDegrees() - 360, wrapped.getWholeDegrees());
		Assertions.assertEquals(angle.getWholeMinutes(), wrapped.getWholeMinutes());
		Assertions.assertEquals(angle.getDouble() - 360.0, wrapped.getDouble());

		angle = new FractionalSeconds(800L, 0L, 3);
		wrapped = angle.wrapToThreeSixtyDegrees();
		Assertions.assertEquals(angle.getWholeDegrees() - 720, wrapped.getWholeDegrees());
		Assertions.assertEquals(angle.getWholeMinutes(), wrapped.getWholeMinutes());
		Assertions.assertEquals(angle.getDouble() - 720.0, wrapped.getDouble());
	}

	@Test
	public void testInvert()
	{
		FractionalSeconds angle = new FractionalSeconds(226L, 500L, 3);
		FractionalSeconds inverse = angle.invert();
		Assertions.assertEquals(359 - 226, inverse.getWholeDegrees());
		Assertions.assertEquals(30, inverse.getWholeMinutes());
		Assertions.assertEquals(360.0 - 226.5, inverse.getDouble()); // still positive

		angle = new FractionalSeconds(350L, 0L, 2);
		inverse = angle.invert();
		Assertions.assertEquals(10, inverse.getWholeDegrees());
		Assertions.assertEquals(0, inverse.getWholeMinutes());
		Assertions.assertEquals(10.0, inverse.getDouble()); // still positive
	}
}
