package tim.prune.function.distance;

import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tim.prune.I18nManager;

public class BearingDescriberTest
{
	private BearingDescriber _describer = null;

	@BeforeEach
	public void setup() {
		I18nManager.init(Locale.UK);
		_describer = new BearingDescriber();
	}

	@Test
	public void testNorth() {
		testAngle("N", 0.0);
	}

	@Test
	public void testNorthEast() {
		testAngle("NE", 45.0);
	}

	@Test
	public void testEast() {
		testAngle("E", 90.0);
	}

	@Test
	public void testSouthEast() {
		testAngle("SE", 135.0);
	}

	@Test
	public void testSouth() {
		testAngle("S", 180.0);
	}

	@Test
	public void testSouthWest() {
		testAngle("SW", 225.0);
	}

	@Test
	public void testWest() {
		testAngle("W", 270.0);
	}

	@Test
	public void testNorthWest() {
		testAngle("NW", 315.0);
	}

	private void testAngle(String inExpected, double inAngle)
	{
		Assertions.assertEquals(inExpected, _describer.describeBearing(inAngle));
		Assertions.assertEquals(inExpected, _describer.describeBearing(inAngle - 22.0));
		Assertions.assertEquals(inExpected, _describer.describeBearing(inAngle + 22.0));
		Assertions.assertEquals(inExpected, _describer.describeBearing(inAngle + 360.0));
		Assertions.assertEquals(inExpected, _describer.describeBearing(inAngle - 360.0));
	}
}
