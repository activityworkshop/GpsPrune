package tim.prune.load;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class FieldGuesserTest
{
	@Test
	void testPossibleCoordinateStrings()
	{
		String[] goodCoords = new String[] {
			"48°24′36″N", "03°54′27″W", "S006°50.10600", " E039°17.62800",
			"S006d50.10600", // two letters ok
			"1.5581", "110.3515"
		};
		for (String coords : goodCoords) {
			assertTrue(FieldGuesser.couldBeCoordinateString(coords), coords);
		}
		// Maybe not expected but matches!
		assertTrue(FieldGuesser.couldBeCoordinateString("48°24′36″N 03°54′27″W"));
	}

	@Test
	void testNotCoordinateStrings()
	{
		String[] badCoords = new String[] {
				"",
				"Stage12", "03°54′27″We", // consecutive letters
				"03d54m27s", // too many letters total
				"1°" // too few numbers
			};
		for (String coords : badCoords) {
			assertFalse(FieldGuesser.couldBeCoordinateString(coords), coords);
		}
	}

	@Test
	void testPossibleLatitudeStrings()
	{
		String[] goodCoords = new String[] {
			"48°24′36″N", "S006°50.10600",
			"03°54′27″W", " E039°17.62800", // unfortunately also ok
			"S006d50.10600", // two letters ok
			"1.5581", "-10.3515"
		};
		for (String coords : goodCoords) {
			assertTrue(FieldGuesser.fieldLooksLikeLatitude(coords, false), coords);
		}
		// and header field
		assertTrue(FieldGuesser.fieldLooksLikeLatitude("Latitude", true));
	}

	@Test
	void testNotLatitudeStrings()
	{
		String[] badCoords = new String[] {
			"93°54′27″W", " E139°17.62800",
			"S1", "",
			"-91.5581", "110.3515"
		};
		for (String coords : badCoords) {
			assertFalse(FieldGuesser.fieldLooksLikeLatitude(coords, false), coords);
		}
		// and header field
		assertFalse(FieldGuesser.fieldLooksLikeLatitude("Lat", true));
		assertFalse(FieldGuesser.fieldLooksLikeLatitude("Longitude", true));
	}

	@Test
	void testPossibleLongitudeStrings()
	{
		String[] goodCoords = new String[] {
			"48°24′36″N", "S006°50.10600", // unfortunately also ok
			"N-91.5581",
			"03°54′27″W", " E039°17.62800",
			"J123K1",
			"E 6d50.10600", // two letters ok
			"1.5581", "-10.3515", "-91.5581", "110.3515"
		};
		for (String coords : goodCoords) {
			assertTrue(FieldGuesser.fieldLooksLikeLongitude(coords, false), coords);
		}
		// and header field
		assertTrue(FieldGuesser.fieldLooksLikeLongitude("Longitude", true));
	}

	@Test
	void testNotLongitudeStrings()
	{
		String[] badCoords = new String[] {
			"S1", "", "45 71 99 22 70",
			"45 71 63.2", "12JK1",
			"-91.5581ab", "1  "
		};
		for (String coords : badCoords) {
			assertFalse(FieldGuesser.fieldLooksLikeLongitude(coords, false), coords);
		}
		// and header field
		assertFalse(FieldGuesser.fieldLooksLikeLongitude("Lon", true));
		assertFalse(FieldGuesser.fieldLooksLikeLongitude("Latitude", true));
	}
}
