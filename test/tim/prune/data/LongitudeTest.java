package tim.prune.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class LongitudeTest
{
	@Test
	public void testWrappingInRange()
	{
		// unchanged because already in range
		Assertions.assertEquals(0.5, Longitude.wrapTo180(0.5));
		Assertions.assertEquals(120.0, Longitude.wrapTo180(120.0));
		Assertions.assertEquals(-120.0, Longitude.wrapTo180(-120.0));
		Assertions.assertEquals(175.5, Longitude.wrapTo180(175.5));
	}

	@Test
	public void testWrappingTooBig()
	{
		// beyond +180 so get wrapped
		Assertions.assertEquals(-170.0, Longitude.wrapTo180(190.0));
		Assertions.assertEquals(0.0, Longitude.wrapTo180(360.0));
		Assertions.assertEquals(26.0, Longitude.wrapTo180(360.0 + 26.0));
		Assertions.assertEquals(-45.0, Longitude.wrapTo180(180.0 + 360.0 + 135.0));
	}

	@Test
	public void testWrappingTooNegative()
	{
		// beyond -180 so get wrapped
		Assertions.assertEquals(170.0, Longitude.wrapTo180(-190.0));
		Assertions.assertEquals(0.0, Longitude.wrapTo180(-360.0));
		Assertions.assertEquals(25.0, Longitude.wrapTo180(-720.0 + 25.0));
	}

	@Test
	public void testCreateWrapped()
	{
		Coordinate tenDegrees = Longitude.make(370.0);
		Assertions.assertEquals(10.0, tenDegrees.getDouble());
	}

	@Test
	public void testGreaterThan180()
	{
		Coordinate angle = Longitude.make("E 190.1");
		Assertions.assertEquals("E 190.1", angle.output(Coordinate.Format.DEG, -1));
		Assertions.assertEquals("W 169.900000", angle.output(Coordinate.Format.DEG, 6));
		Assertions.assertEquals(-169.9, angle.getDouble());

		angle = Longitude.make("W 200.0");
		Assertions.assertEquals("E 160.00", angle.output(Coordinate.Format.DEG, 2));
		Assertions.assertEquals(160.0, angle.getDouble());
	}
}
