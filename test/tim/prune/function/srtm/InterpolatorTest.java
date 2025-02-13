package tim.prune.function.srtm;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.Locale;

class InterpolatorTest extends Interpolator
{
	void testAverageNonVoid(int[] inInputs, double inExpected)
	{
		double result = Interpolator.averageNonVoid(inInputs);
		assertEquals(inExpected, result);
	}

	@Test
	void testAverageNonVoid_expectVoid()
	{
		int[] inputs = new int[0];
		testAverageNonVoid(inputs, SrtmSource.VOID_VAL);
		inputs = new int[] {SrtmSource.VOID_VAL};
		testAverageNonVoid(inputs, SrtmSource.VOID_VAL);
		inputs = new int[] {SrtmSource.VOID_VAL, SrtmSource.VOID_VAL, SrtmSource.VOID_VAL};
		testAverageNonVoid(inputs, SrtmSource.VOID_VAL);
	}

	@Test
	void testAverageNonVoid_expectZero()
	{
		int[] inputs = new int[] {0};
		testAverageNonVoid(inputs, 0.0);
		inputs = new int[] {SrtmSource.VOID_VAL, 0};
		testAverageNonVoid(inputs, 0.0);
		inputs = new int[] {0, SrtmSource.VOID_VAL, SrtmSource.VOID_VAL};
		testAverageNonVoid(inputs, 0.0);
		inputs = new int[] {1, SrtmSource.VOID_VAL, -1, SrtmSource.VOID_VAL};
		testAverageNonVoid(inputs, 0.0);
	}

	@Test
	void testAverageNonVoid_expectTen()
	{
		int[] inputs = new int[] {9, 11};
		testAverageNonVoid(inputs, 10.0);
		inputs = new int[] {9, SrtmSource.VOID_VAL, 11, SrtmSource.VOID_VAL};
		testAverageNonVoid(inputs, 10.0);
	}

	@Test
	void testFixVoids_noChange()
	{
		int[] inputs = new int[] {3, 4, 5};
		int[] result = Interpolator.fixVoid(inputs);
		assertEquals(inputs.length,  result.length);
		for (int i=0; i<inputs.length; i++) {
			assertEquals(inputs[i], result[i]);
		}
	}

	@Test
	void testFixVoids_fillSingle()
	{
		int[] inputs = new int[] {4, 5, SrtmSource.VOID_VAL, 9};
		int[] expected = new int[] {4, 5, 6, 9};
		int[] result = Interpolator.fixVoid(inputs);
		assertEquals(inputs.length,  expected.length);
		for (int i=0; i<inputs.length; i++) {
			assertEquals(expected[i], result[i]);
		}
	}

	@Test
	void testBilinearInterpolate()
	{
		// coords given in order: bottom left, bottom right, top left, top right
		int[] altitudes = new int[] {100, 120, 200, 240};
		double botLeft = Interpolator.bilinearInterpolate(altitudes, 0.0, 1.0);
		assertEquals(100.0, botLeft);
		double topLeft = Interpolator.bilinearInterpolate(altitudes, 0.0, 0.0);
		assertEquals(200.0, topLeft);
		double botRight = Interpolator.bilinearInterpolate(altitudes, 1.0, 1.0);
		assertEquals(120.0, botRight);
		double topRight = Interpolator.bilinearInterpolate(altitudes, 1.0, 0.0);
		assertEquals(240.0, topRight);
		double topMiddle = Interpolator.bilinearInterpolate(altitudes, 0.5, 0.0);
		assertEquals(220.0, topMiddle);
		double middleLeft = Interpolator.bilinearInterpolate(altitudes, 0.0, 0.5);
		assertEquals(150.0, middleLeft);
		double middleMiddle = Interpolator.bilinearInterpolate(altitudes, 0.5, 0.5);
		assertEquals(165.0, middleMiddle);
	}

	@Test
	void testCalculateAltitude()
	{
		int[] altitudes = new int[] {2, 2, 1, 1, 1,
			2, 2, 3, 3, 1,
			1, 3, 5, 3, 1,
			1, 3, 3, 3, 4,
			1, 1, 1, 1, 1};
		double botLeftCorner = Interpolator.calculateAltitude(15.0, 18.0, altitudes, true, 5);
		assertEquals(1.0, botLeftCorner);
		double topLeftCorner = Interpolator.calculateAltitude(15.0, 18.9999, altitudes, true, 5);
		assertEquals(2.0, topLeftCorner);
		double middle = Interpolator.calculateAltitude(19.5, 100.5, altitudes, true, 5);
		assertEquals(5.0, middle);
		// Also should work if lat/longs are negative
		botLeftCorner = Interpolator.calculateAltitude(-15.0, -18.0, altitudes, true, 5);
		assertEquals(1.0, botLeftCorner);
		// middle of the bottom left corner between 1,1,1,3
		double interpolated = Interpolator.calculateAltitude(2.125, 44.125, altitudes, true, 5);
		assertEquals((1.0+1.0+1.0+3.0)/4.0, interpolated);
		interpolated = Interpolator.calculateAltitude(-40.875, 44.125, altitudes, true, 5);
		assertEquals((1.0+1.0+1.0+3.0)/4.0, interpolated);
		interpolated = Interpolator.calculateAltitude(-40.875, -33.875, altitudes, true, 5);
		assertEquals((1.0+1.0+1.0+3.0)/4.0, interpolated);
	}

	@Test
	void testCalculateAltitudeStrip()
	{
		int[] altitudes = new int[] {0, 0, 0, 0, 0, 0, 0,
			1, 1, 1, 1, 1, 1, 1,
			2, 2, 2, 2, 2, 2, 2,
			3, 3, 3, 3, 3, 3, 3,
			4, 4, 4, 4, 4, 4, 4,
			5, 5, 5, 5, 5, 5, 5,
			6, 6, 6, 6, 6, 6, 6};
		StringBuilder builder = new StringBuilder();
		for (int i=0; i<100; i++)
		{
			double height = Interpolator.calculateAltitude(11.2, 58.0 + i/100.0, altitudes, true, 7);
			builder.append(String.format(Locale.US, "%.3f", height));
			builder.append(',');
		}
		String result = builder.toString();
		String expectedResult = "6.000,5.940,5.880,5.820,5.760,5.700,5.640,5.580,5.520,5.460,5.400,5.340," +
			"5.280,5.220,5.160,5.100,5.040,4.980,4.920,4.860,4.800,4.740,4.680,4.620,4.560,4.500,4.440," +
			"4.380,4.320,4.260,4.200,4.140,4.080,4.020,3.960,3.900,3.840,3.780,3.720,3.660,3.600,3.540," +
			"3.480,3.420,3.360,3.300,3.240,3.180,3.120,3.060,3.000,2.940,2.880,2.820,2.760,2.700,2.640," +
			"2.580,2.520,2.460,2.400,2.340,2.280,2.220,2.160,2.100,2.040,1.980,1.920,1.860,1.800,1.740," +
			"1.680,1.620,1.560,1.500,1.440,1.380,1.320,1.260,1.200,1.140,1.080,1.020,0.960,0.900,0.840," +
			"0.780,0.720,0.660,0.600,0.540,0.480,0.420,0.360,0.300,0.240,0.180,0.120,0.060,";
		assertEquals(expectedResult, result);
	}
}
