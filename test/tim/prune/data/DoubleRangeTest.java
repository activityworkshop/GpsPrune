package tim.prune.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DoubleRangeTest
{
	@Test
	public void testEmptyRange()
	{
		DoubleRange range = new DoubleRange();
		Assertions.assertFalse(range.hasData());
		Assertions.assertFalse(range.includes(0.0));
		Assertions.assertEquals(0.0, range.getMinimum());
		Assertions.assertEquals(0.0, range.getMaximum());
		Assertions.assertEquals(0.0, range.getMidValue());
		Assertions.assertEquals(0.0, range.getRange());
	}

	@Test
	public void testSingleValue()
	{
		DoubleRange range = new DoubleRange();
		final double singleValue = 1.5;
		range.addValue(singleValue);
		Assertions.assertTrue(range.hasData());
		Assertions.assertFalse(range.includes(0.0));
		Assertions.assertTrue(range.includes(singleValue));
		Assertions.assertEquals(singleValue, range.getMinimum());
		Assertions.assertEquals(singleValue, range.getMaximum());
		Assertions.assertEquals(singleValue, range.getMidValue());
		Assertions.assertEquals(0.0, range.getRange());
	}

	@Test
	public void testClearValues()
	{
		DoubleRange range = new DoubleRange();
		final double singleValue = 1.5;
		range.addValue(singleValue);
		Assertions.assertTrue(range.hasData());
		range.clear();
		Assertions.assertFalse(range.hasData());
		Assertions.assertFalse(range.includes(0.0));
		Assertions.assertEquals(0.0, range.getMinimum());
		Assertions.assertEquals(0.0, range.getMaximum());
		Assertions.assertEquals(0.0, range.getMidValue());
		Assertions.assertEquals(0.0, range.getRange());
	}

	@Test
	public void testPairOfValues()
	{
		DoubleRange range = new DoubleRange();
		final double value1 = 5.0;
		final double value2 = -3.0;
		range.addValue(value1);
		range.addValue(value1);
		range.addValue(value2);
		Assertions.assertTrue(range.hasData());
		Assertions.assertTrue(range.includes(0.0));
		Assertions.assertEquals(value2, range.getMinimum());
		Assertions.assertEquals(value1, range.getMaximum());
		Assertions.assertEquals(1.0, range.getMidValue());
		Assertions.assertEquals(8.0, range.getRange());
	}
}
