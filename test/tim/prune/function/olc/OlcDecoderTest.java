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
		assertEquals(area, null, "Decoding null gives null");
		area = OlcDecoder.decode("");
		assertEquals(area, null, "Decoding \"\" gives null");
		area = OlcDecoder.decode("9");
		assertEquals(area, null, "Decoding \"9\" gives null");
		area = OlcDecoder.decode("9999999");
		assertEquals(area, null, "Decoding \"9999999\" gives null");
	}

	@Test
	void testDecodeStringsInvalid()
	{
		OlcArea area = OlcDecoder.decode("11111111");
		assertEquals(area, null, "Decoding lots of 1s gives null");
		area = OlcDecoder.decode("99999991");
		assertEquals(area, null, "Decoding with a single 1 gives null");
		area = OlcDecoder.decode("99999999");
		assertNotEquals(area, null, "Decoding with all 9s gives non-null");
		area = OlcDecoder.decode("00000000");
		assertEquals(area, null, "Decoding with all padding gives null");
		area = OlcDecoder.decode("99000000");
		assertNotEquals(area, null, "Decoding with some padding gives non-null");
	}

	@Test
	void testDecodeZeroes()
	{
		OlcArea area = OlcDecoder.decode("22000000");
		assertNotEquals(area, null, "Decoding with padding gives non-null");
		assertEquals(-90.0, area.minLat, 0.0, "South 90");
		assertEquals(-70.0, area.maxLat, 0.0, "South 70");
		assertEquals(-180.0, area.minLon, 0.0, "West 180");
		assertEquals(-160.0, area.maxLon, 0.0, "West 160");
	}

	@Test
	void testDecodeZeroes2()
	{
		OlcArea area = OlcDecoder.decode("22220000");
		assertNotEquals(area, null, "Decoding with padding gives non-null");
		assertEquals(-90.0, area.minLat, 0.0, "South 90");
		assertEquals(-89.0, area.maxLat, 0.0, "South 89");
		assertEquals(-180.0, area.minLon, 0.0, "West 180");
		assertEquals(-179.0, area.maxLon, 0.0, "West 179");
	}

	@Test
	void testMountainView()
	{
		OlcArea area = OlcDecoder.decode("6PH57VP3+PR6");
		assertNotEquals(area, null, "Decoding with separator gives non-null");
		System.out.println("Min lat: " + area.minLat);
		System.out.println("Max lat: " + area.maxLat);
		System.out.println("Min lon: " + area.minLon);
		System.out.println("Max lon: " + area.maxLon);
		assertTrue(area.maxLat > area.minLat, "latitude range");
		assertTrue(area.maxLon > area.minLon, "longitude range");
	}
}
