package tim.prune.data;

import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tim.prune.data.Coordinate.Cardinal;

/** Tests the parsing of Coordinate objects from Strings and double values */
public class CoordinateParsingTest
{
	@BeforeEach
	public void setup() {
		Locale.setDefault(Locale.ENGLISH);
	}

	@Test
	public void testFromPositiveDouble()
	{
		Coordinate oneAndAHalfDegrees = new Coordinate(1.5, Coordinate.Cardinal.NORTH);
		Assertions.assertEquals(1.5, oneAndAHalfDegrees.getDouble());
		Assertions.assertEquals("1.50", oneAndAHalfDegrees.output(Coordinate.Format.DECIMAL_FORCE_POINT, 2));
		Assertions.assertEquals("N001°30.0'", oneAndAHalfDegrees.output(Coordinate.Format.DEG_MIN, 1));
		Assertions.assertEquals("N001°30'00.0\"", oneAndAHalfDegrees.output(Coordinate.Format.DEG_MIN_SEC, 1));
		Assertions.assertEquals("1 30 0.0", oneAndAHalfDegrees.output(Coordinate.Format.DEG_MIN_SEC_WITH_SPACES, 1));
	}

	@Test
	public void testFromPositiveDoubleWithAxis()
	{
		Coordinate angle = Latitude.make(1.5);
		Assertions.assertEquals(1.5, angle.getDouble());
		Assertions.assertEquals("1.50", angle.output(Coordinate.Format.DECIMAL_FORCE_POINT, 2));
		Assertions.assertEquals("N001°30.0'", angle.output(Coordinate.Format.DEG_MIN, 1));
		Assertions.assertEquals("N001°30'00.0\"", angle.output(Coordinate.Format.DEG_MIN_SEC, 1));
		Assertions.assertEquals("1 30 0.0", angle.output(Coordinate.Format.DEG_MIN_SEC_WITH_SPACES, 1));

		angle = Longitude.make(102.25);
		Assertions.assertEquals(102.25, angle.getDouble());
		Assertions.assertEquals("102.250", angle.output(Coordinate.Format.DECIMAL_FORCE_POINT, 3));
		Assertions.assertEquals("E102°15.0'", angle.output(Coordinate.Format.DEG_MIN, 1));
		Assertions.assertEquals("E102°15'00.0\"", angle.output(Coordinate.Format.DEG_MIN_SEC, 1));
		Assertions.assertEquals("102 15 0.0", angle.output(Coordinate.Format.DEG_MIN_SEC_WITH_SPACES, 1));
	}

	@Test
	public void testFromNegativeDouble()
	{
		Coordinate oneAndAHalfDegrees = new Coordinate(-1.5, Coordinate.Cardinal.SOUTH);
		Assertions.assertEquals(-1.5, oneAndAHalfDegrees.getDouble());
		Assertions.assertEquals("-1.50", oneAndAHalfDegrees.output(Coordinate.Format.DECIMAL_FORCE_POINT, 2));
		Assertions.assertEquals("S001°30.0'", oneAndAHalfDegrees.output(Coordinate.Format.DEG_MIN, 1));
		Assertions.assertEquals("S001°30'00.0\"", oneAndAHalfDegrees.output(Coordinate.Format.DEG_MIN_SEC, 1));
		Assertions.assertEquals("1 30 0.0", oneAndAHalfDegrees.output(Coordinate.Format.DEG_MIN_SEC_WITH_SPACES, 1));
	}

	@Test
	public void testFromNegativeDoubleWithAxis()
	{
		Coordinate angle = Latitude.make(-1.5);
		Assertions.assertEquals(-1.5, angle.getDouble());
		Assertions.assertEquals("-1.50", angle.output(Coordinate.Format.DECIMAL_FORCE_POINT, 2));
		Assertions.assertEquals("S001°30.0'", angle.output(Coordinate.Format.DEG_MIN, 1));
		Assertions.assertEquals("S001°30'00.0\"", angle.output(Coordinate.Format.DEG_MIN_SEC, 1));
		Assertions.assertEquals("1 30 0.0", angle.output(Coordinate.Format.DEG_MIN_SEC_WITH_SPACES, 1));

		angle = Longitude.make(-2.25);
		Assertions.assertEquals(-2.25, angle.getDouble());
		Assertions.assertEquals("-2.250", angle.output(Coordinate.Format.DECIMAL_FORCE_POINT, 3));
		Assertions.assertEquals("W002°15.0'", angle.output(Coordinate.Format.DEG_MIN, 1));
		Assertions.assertEquals("W002°15'00.0\"", angle.output(Coordinate.Format.DEG_MIN_SEC, 1));
		Assertions.assertEquals("2 15 0.0", angle.output(Coordinate.Format.DEG_MIN_SEC_WITH_SPACES, 1));
	}

	@Test
	public void testFromStringWithJustNumber()
	{
		Coordinate angle = Longitude.make("1.1");
		Assertions.assertEquals(1.1, angle.getDouble());
		Assertions.assertEquals("1.1", angle.output(Coordinate.Format.DEG_WITHOUT_CARDINAL));
		Assertions.assertEquals("E 1.10000000", angle.output(Coordinate.Format.DEG));
		Assertions.assertEquals("1.100", angle.output(Coordinate.Format.DECIMAL_FORCE_POINT, 3));
		Assertions.assertEquals("E001°06.0'", angle.output(Coordinate.Format.DEG_MIN, 1));
		Assertions.assertEquals("E001°06'00.0\"", angle.output(Coordinate.Format.DEG_MIN_SEC, 1));
		Assertions.assertEquals("1 6 0.0", angle.output(Coordinate.Format.DEG_MIN_SEC_WITH_SPACES, 1));

		angle = Latitude.make("-1.2");
		Assertions.assertEquals(-1.2, angle.getDouble());
		Assertions.assertEquals("S 1.20000000", angle.output(Coordinate.Format.DEG));
		Assertions.assertEquals("-1.200", angle.output(Coordinate.Format.DECIMAL_FORCE_POINT, 3));
		Assertions.assertEquals("S001°12.0'", angle.output(Coordinate.Format.DEG_MIN, 1));
		Assertions.assertEquals("S001°12'00.0\"", angle.output(Coordinate.Format.DEG_MIN_SEC, 1));
		Assertions.assertEquals("1 12 0.0", angle.output(Coordinate.Format.DEG_MIN_SEC_WITH_SPACES, 1));
	}

	@Test
	public void testFromStringWithCardinalAndNumber()
	{
		Coordinate angle = Latitude.make("N 1.1");
		Assertions.assertEquals(1.1, angle.getDouble());
		Assertions.assertEquals("N 1.1", angle.output(Coordinate.Format.DEG));
		Assertions.assertEquals("1.100", angle.output(Coordinate.Format.DECIMAL_FORCE_POINT, 3));
		Assertions.assertEquals("N001°06.0'", angle.output(Coordinate.Format.DEG_MIN, 1));
		Assertions.assertEquals("N001°06'00.0\"", angle.output(Coordinate.Format.DEG_MIN_SEC, 1));
		Assertions.assertEquals("1 6 0.0", angle.output(Coordinate.Format.DEG_MIN_SEC_WITH_SPACES, 1));

		angle = Longitude.make("W 1.3");
		Assertions.assertEquals(-1.3, angle.getDouble());
		Assertions.assertEquals("W 1.3", angle.output(Coordinate.Format.DEG));
		Assertions.assertEquals("-1.300", angle.output(Coordinate.Format.DECIMAL_FORCE_POINT, 3));
		Assertions.assertEquals("W001°18.0'", angle.output(Coordinate.Format.DEG_MIN, 1));
		Assertions.assertEquals("W001°18'00.0\"", angle.output(Coordinate.Format.DEG_MIN_SEC, 1));
		Assertions.assertEquals("1 18 0.0", angle.output(Coordinate.Format.DEG_MIN_SEC_WITH_SPACES, 1));
	}

	@Test
	public void testRounding()
	{
		Coordinate angle = Coordinate.parse("1.26518", Cardinal.NORTH, Cardinal.SOUTH);
		Assertions.assertEquals("N 1.26518000", angle.output(Coordinate.Format.DEG, -1));
		Assertions.assertEquals("N 1.265180", angle.output(Coordinate.Format.DEG, 6));
		Assertions.assertEquals("N 1.26518", angle.output(Coordinate.Format.DEG, 5));
		Assertions.assertEquals("N 1.2652", angle.output(Coordinate.Format.DEG, 4));
		Assertions.assertEquals("N 1.265", angle.output(Coordinate.Format.DEG, 3));
		Assertions.assertEquals("N 1.27", angle.output(Coordinate.Format.DEG, 2));
		Assertions.assertEquals("N 1.3", angle.output(Coordinate.Format.DEG, 1));

		angle = Coordinate.parse("N 1.26518", Cardinal.NORTH, Cardinal.SOUTH);
		Assertions.assertEquals("N 1.26518", angle.output(Coordinate.Format.DEG, -1));

		angle = Coordinate.parse("1°26 59.95438", Cardinal.NORTH, Cardinal.SOUTH);
		Assertions.assertEquals("1°26 59.95438", angle.output(Coordinate.Format.DEG_MIN_SEC, -1));
		Assertions.assertEquals("N001°26'59.954380\"", angle.output(Coordinate.Format.DEG_MIN_SEC, 6));
		Assertions.assertEquals("N001°26'59.95438\"", angle.output(Coordinate.Format.DEG_MIN_SEC, 5));
		Assertions.assertEquals("N001°26'59.9544\"", angle.output(Coordinate.Format.DEG_MIN_SEC, 4));
		Assertions.assertEquals("N001°26'59.954\"", angle.output(Coordinate.Format.DEG_MIN_SEC, 3));
		Assertions.assertEquals("N001°26'59.95\"", angle.output(Coordinate.Format.DEG_MIN_SEC, 2));
		Assertions.assertEquals("N001°27'00.0\"", angle.output(Coordinate.Format.DEG_MIN_SEC, 1));

		angle = Coordinate.parse("N 1°26 59.95438", Cardinal.NORTH, Cardinal.SOUTH);
		Assertions.assertEquals("N 1°26 59.95438", angle.output(Coordinate.Format.DEG_MIN_SEC, -1));
		Assertions.assertEquals("N001°26'59.95438\"", angle.output(Coordinate.Format.DEG_MIN_SEC, 5));

		angle = Latitude.make("51°59.883’");
		Assertions.assertEquals("N052°00.'", angle.output(Coordinate.Format.DEG_MIN, 0));
		Assertions.assertEquals("N051°59.883000000'", angle.output(Coordinate.Format.DEG_MIN, 9));
		Assertions.assertEquals("N051°59.8830000000'", angle.output(Coordinate.Format.DEG_MIN, 10));
	}
}
