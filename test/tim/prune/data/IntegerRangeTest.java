package tim.prune.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class IntegerRangeTest
{
	@Test
	void testEmptyRange()
	{
		IntegerRange range = new IntegerRange();
		assertFalse(range.hasValues());
		assertEquals(-1, range.getMinimum());
		assertEquals(-1, range.getMaximum());
	}

	@Test
	void testSingleRange()
	{
		// ten times the same value, min==max
		final int value = 8;
		IntegerRange range = new IntegerRange();
		for (int i=0; i<10; i++) {
			range.addValue(value);
		}
		assertTrue(range.hasValues());
		assertEquals(value, range.getMinimum());
		assertEquals(value, range.getMaximum());
	}

	@Test
	void testSingleNegative()
	{
		// ten times the same value, min==max
		final int value = -4;
		IntegerRange range = new IntegerRange();
		for (int i=0; i<10; i++) {
			range.addValue(value);
		}
		assertTrue(range.hasValues());
		assertEquals(value, range.getMinimum());
		assertEquals(value, range.getMaximum());
	}

	@Test
	void testReset()
	{
		// Set single value followed by clear
		final int value = 123456;
		IntegerRange range = new IntegerRange();
		range.addValue(value);
		assertTrue(range.hasValues());
		assertEquals(value, range.getMinimum());
		assertEquals(value, range.getMaximum());
		range.clear();
		assertFalse(range.hasValues());
		assertEquals(-1, range.getMinimum());
		assertEquals(-1, range.getMaximum());
	}

	@Test
	void testIncreasingSeries()
	{
		// monotonically increasing ints, negative to positive
		final int startVal = -4, endVal = 10;
		IntegerRange range = new IntegerRange();
		for (int i=startVal; i<=endVal; i++) {
			range.addValue(i);
		}
		assertTrue(range.hasValues());
		assertEquals(startVal, range.getMinimum());
		assertEquals(endVal, range.getMaximum());
	}

	@Test
	void testDecreasingSeries()
	{
		// monotonically decreasing ints, positive to negative
		final int startVal = 14, endVal = -1;
		IntegerRange range = new IntegerRange();
		for (int i=startVal; i>=endVal; i--) {
			range.addValue(i);
		}
		assertTrue(range.hasValues());
		assertEquals(endVal, range.getMinimum());
		assertEquals(startVal, range.getMaximum());
	}
}
