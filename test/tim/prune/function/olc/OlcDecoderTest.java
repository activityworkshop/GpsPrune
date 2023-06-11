package tim.prune.function.olc;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * JUnit tests for decoding of Open Location Codes (Pluscodes)
 */
class OlcDecoderTest
{

	@Test
	void testDecodeStringsTooShort()
	{
		OlcArea area = OlcDecoder.decode(null);
		assertNull(area, "Decoding null gives null");
		area = OlcDecoder.decode("");
		assertNull(area, "Decoding \"\" gives null");
		area = OlcDecoder.decode("9");
		assertNull(area, "Decoding \"9\" gives null");
		area = OlcDecoder.decode("9999999");
		assertNull(area, "Decoding \"9999999\" gives null");
	}

	@Test
	void testDecodeStringsInvalid()
	{
		OlcArea area = OlcDecoder.decode("11111111");
		assertNull(area, "Decoding lots of 1s gives null");
		area = OlcDecoder.decode("99999991");
		assertNull(area, "Decoding with a single 1 gives null");
		area = OlcDecoder.decode("99999999");
		assertNotNull(area, "Decoding with all 9s gives non-null");
		area = OlcDecoder.decode("00000000");
		assertNull(area, "Decoding with all padding gives null");
		area = OlcDecoder.decode("99000000");
		assertNotNull(area, "Decoding with some padding gives non-null");
	}

	@Test
	void testDecodeZeroes()
	{
		OlcArea area = OlcDecoder.decode("22000000");
		assertNotNull(area, "Decoding with padding gives non-null");
		assertEquals(-90.0, area.minLat, 0.0, "South 90");
		assertEquals(-70.0, area.maxLat, 0.0, "South 70");
		assertEquals(-180.0, area.minLon, 0.0, "West 180");
		assertEquals(-160.0, area.maxLon, 0.0, "West 160");
	}

	@Test
	void testDecodeZeroes2()
	{
		OlcArea area = OlcDecoder.decode("22220000");
		assertNotNull(area, "Decoding with padding gives non-null");
		assertEquals(-90.0, area.minLat, 0.0, "South 90");
		assertEquals(-89.0, area.maxLat, 0.0, "South 89");
		assertEquals(-180.0, area.minLon, 0.0, "West 180");
		assertEquals(-179.0, area.maxLon, 0.0, "West 179");
	}

	@Test
	void testMountainView()
	{
		OlcArea area = OlcDecoder.decode("849VCWC8+R9");
		assertNotNull(area, "Decoding with separator gives non-null");
//		System.out.println("Lat: " + area.minLat + " to " + area.maxLat);
//		System.out.println("lon: " + area.minLon + " to " + area.maxLon);
		assertTrue(area.maxLat > area.minLat, "latitude range");
		assertTrue(area.maxLon > area.minLon, "longitude range");
	}

	@Test
	void testShortForms()
	{
		OlcArea area = OlcDecoder.decode("7VP3+PR6");
		assertNull(area); // not a valid long code
		// Now try as a short form
		area = OlcDecoder.decode("7VP3+PR6", 1.0, 104.0);
		assertNotNull(area);
		assertTrue(area.code.startsWith("6PH5"), "Singapore");
		assertTrue(area.minLat > 0.0);
		// same code but different reference location, gives different code and latitude
		area = OlcDecoder.decode("7VP3+", -19.0, 48.0);
		assertNotNull(area);
		assertTrue(area.code.startsWith("5HH9"), "Madagaskar");
		assertTrue(area.minLat < -18.0);
	}

	@Test
	void testLengthChecks()
	{
		assertFalse(OlcDecoder.isValidLongForm("123456"));
		assertFalse(OlcDecoder.isValidShortForm("123456"));
		assertTrue(OlcDecoder.isValidLongForm("12345678")); // even though 1 isn't valid
		assertTrue(OlcDecoder.isValidLongForm("ABCDEFGH+")); // even though A isn't valid
		assertFalse(OlcDecoder.isValidLongForm("ABCDEFG+H")); // + in wrong position
		assertTrue(OlcDecoder.isValidLongForm("ABCDEFGH+IJ"));
		assertTrue(OlcDecoder.isValidLongForm("ABCDEFGH+IJK"));
		assertTrue(OlcDecoder.isValidLongForm("ABCDEFGH+IJKL")); // even though too long
		assertTrue(OlcDecoder.isValidLongForm("ABCD0000+IJ+")); // despite second +

		assertFalse(OlcDecoder.isValidLongForm("ABCD"));
		assertFalse(OlcDecoder.isValidShortForm("ABCD"));
		assertFalse(OlcDecoder.isValidLongForm("ABCD+"));
		assertTrue(OlcDecoder.isValidShortForm("ABCD+")); // even though A isn't valid
		assertTrue(OlcDecoder.isValidShortForm("ABCD+EF"));
		assertTrue(OlcDecoder.isValidShortForm("ABCD+EFG"));

		assertTrue(OlcDecoder.isValidShortForm("ABCD+E+")); // even though second + is invalid
	}
}
