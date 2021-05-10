package tim.prune.jpeg.drew;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * JUnit tests for the Rational values used by the Exif
 */
class RationalTest
{
	@Test
	void testManyInts()
	{
		testIntVal(0, 0, 0);
		testIntVal(1, 0, 0);
		testIntVal(0, 1, 0);
		for (int i=0; i<16000; i++)
		{
			testIntVal(0, i, 0);
			testIntVal(i, 0, 0);
			testIntVal(i, 1, i);
			testIntVal(-i, 1, -i);
			testIntVal(i*2, 2, i);
			testIntVal(i*2+1, 2, i);	// rounding down the 0.5
			testIntVal(-i*2, 2, -i);
			testIntVal(i*2, -2, -i);
			testIntVal(-i*2, -2, i);
		}
	}

	/**
	 * Check that a rational converts to an integer properly
	 * @param inTop number on top of the rational (numerator)
	 * @param inBottom number on bottom of the rational (denominator)
	 * @param inExpected expected int value
	 */
	private void testIntVal(long inTop, long inBottom, int inExpected)
	{
		Rational value = new Rational(inTop, inBottom);
		assertEquals(inExpected, value.intValue(), "" + inTop + "/" + inBottom);
	}

	@Test
	void testManyDoubles()
	{
		for (int i=0; i<16000; i++)
		{
			testDoubleVal(0, i, 0.0);
			testDoubleVal(i, 0, 0.0);
			testDoubleVal(i, 1, i);
			testDoubleVal(i*2, 2, i);
			testDoubleVal(i*2+1, 2, i+0.5);
			testDoubleVal(i*2, -2, -i);
		}

		testDoubleVal(123, 3, 123.0/3.0);
	}

	/**
	 * Check that a rational converts to a double properly
	 * @param inTop number on top of the rational (numerator)
	 * @param inBottom number on bottom of the rational (denominator)
	 * @param inExpected expected double value (exact)
	 */
	private void testDoubleVal(long inTop, long inBottom, double inExpected)
	{
		Rational value = new Rational(inTop, inBottom);
		assertEquals(inExpected, value.doubleValue(), 0.0, "" + inTop + "/" + inBottom);
	}
}
