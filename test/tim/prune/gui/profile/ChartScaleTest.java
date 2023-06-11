package tim.prune.gui.profile;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ChartScaleTest
{
	@Test
	void testEmptyScale()
	{
		ChartScale scale = new ChartScale();
		Assertions.assertEquals(-1, scale.getLineScale(0.0, 0.0, 5));
		Assertions.assertEquals(-1, scale.getLineScale(1.0, 1.0, 5));
	}

	@Test
	void testScaleOne()
	{
		ChartScale scale = new ChartScale();
		Assertions.assertEquals(1, scale.getLineScale(5.0, 7.1, 2));
		Assertions.assertEquals(1, scale.getLineScale(1005.0, 1007.1, 2));
	}

	@Test
	void testScaleTwenty()
	{
		ChartScale scale = new ChartScale();
		Assertions.assertEquals(20, scale.getLineScale(5.0, 47.1, 2));
		Assertions.assertEquals(20, scale.getLineScale(5.0, 147.1, 7));
		Assertions.assertEquals(20, scale.getLineScale(5005.0, 5047.1, 2));
	}

	@Test
	void testScaleHuge()
	{
		ChartScale scale = new ChartScale();
		double base = (double) Integer.MAX_VALUE * 5.0;
		// both bigger than maximum integer but difference is small
		Assertions.assertEquals(20, scale.getLineScale(base, base + 47.1, 2));
		// difference also huge, so fail
		Assertions.assertEquals(-1, scale.getLineScale(base / 1000.0, base, 2));
	}
}
