package tim.prune.function.comparesegments;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tim.prune.data.DataPoint;


public class SegmentLooperTest
{
	@Test
	public void test_lineAlongEquator()
	{
		// Line to be cut goes along the equator
		DataPoint lineFrom = new DataPoint(0.0, 5.0);
		DataPoint lineTo = new DataPoint(0.0, 5.1);
		// Perpendicular line goes north/south at 5.025 degrees
		DataPoint perpFrom = new DataPoint(-0.1, 5.025);
		DataPoint perpTo = new DataPoint(0.01, 5.025);
		double result = SegmentLooper.calculateLineFraction(perpFrom, perpTo, lineFrom, lineTo);
		Assertions.assertEquals(0.25, result, 0.000001);
	}

	@Test
	public void test_lineAlongMeridian()
	{
		// Line to be cut goes along the meridian
		DataPoint lineFrom = new DataPoint(40.0, 0.0);
		DataPoint lineTo = new DataPoint(40.1, 0.0);
		// Perpendicular line goes east/west at 40.0625
		DataPoint perpFrom = new DataPoint(40.0625, -1.0);
		DataPoint perpTo = new DataPoint(40.0625, 0.25);
		double result = SegmentLooper.calculateLineFraction(perpFrom, perpTo, lineFrom, lineTo);
		Assertions.assertEquals(0.625, result, 0.000001);
	}

	@Test
	public void test_lineWithIntersection()
	{
		// Line to be cut goes northwest
	   DataPoint lineFrom = new DataPoint(0.0, 0.0);
	   DataPoint lineTo = new DataPoint(0.1, 0.1);
	   // Cutter line should intersect
	   DataPoint perpFrom = new DataPoint(0.02, 0.08);
	   DataPoint perpTo = new DataPoint(1.0, 0.0);
	   double result = SegmentLooper.calculateLineFraction(perpFrom, perpTo, lineFrom, lineTo);
	   Assertions.assertEquals(0.7547, result, 0.0001);

		// Same but the cutting line goes the opposite way (t should be the same though)
		double result2 = SegmentLooper.calculateLineFraction(perpTo, perpFrom, lineFrom, lineTo);
		Assertions.assertEquals(result, result2, 0.0000001);

		// Same but the segment line goes the opposite way (t should be the complement)
		result2 = SegmentLooper.calculateLineFraction(perpFrom, perpTo, lineTo, lineFrom);
		Assertions.assertEquals(result, 1.0 - result2, 0.0000001);

		// And again with the opposite cutter line (t should still be the complement)
		result2 = SegmentLooper.calculateLineFraction(perpTo, perpFrom, lineTo, lineFrom);
		Assertions.assertEquals(result, 1.0 - result2, 0.0000001);
	}

	@Test
	public void test_overlapButNoIntersection()
	{
		DataPoint lineFrom = new DataPoint(0.0, 0.0);
		DataPoint lineTo = new DataPoint(0.1, 0.1);
		// Lines do intersect, within the main line but beyond the end of the cutter line
		DataPoint perpFrom = new DataPoint(0.02, 0.08);
		DataPoint perpTo = new DataPoint(0.08, 0.09);
		double result = SegmentLooper.calculateLineFraction(perpFrom, perpTo, lineFrom, lineTo);
		Assertions.assertEquals(-1.0, result, 0.0001);

		// and in the opposite direction we should get the same result
		result = SegmentLooper.calculateLineFraction(perpFrom, perpTo, lineTo, lineFrom);
		Assertions.assertEquals(-1.0, result, 0.0001);
	}

	@Test
	public void test_linesParallel()
	{
		DataPoint lineFrom = new DataPoint(0.0, 0.0);
		DataPoint lineTo = new DataPoint(0.1, 0.1);
		// Cutter line is now parallel
		DataPoint perpFrom = new DataPoint(0.03, 0.02);
		DataPoint perpTo = new DataPoint(0.05, 0.04);
		double result = SegmentLooper.calculateLineFraction(perpFrom, perpTo, lineFrom, lineTo);
		Assertions.assertEquals(-1.0, result, 0.0001);
	}

	@Test
	public void test_lineWithMatchOnLineExtension()
	{
		// Case 6 - intersection on extension of segment line
		// (similar to test_overlapButNoIntersection but here s is OK and t is out of range)
		DataPoint lineFrom = new DataPoint(0.0, 0.0);
		DataPoint lineTo = new DataPoint(0.09, 0.09);
		DataPoint perpFrom = new DataPoint(0.06, 0.085);
		DataPoint perpTo = new DataPoint(0.11, 0.1);
		double result = SegmentLooper.calculateLineFraction(perpFrom, perpTo, lineFrom, lineTo);
		Assertions.assertEquals(-1.0, result, 0.0001);
		result = SegmentLooper.calculateLineFraction(perpFrom, perpTo, lineTo, lineFrom);
		Assertions.assertEquals(-1.0, result, 0.0001);
	}
}
