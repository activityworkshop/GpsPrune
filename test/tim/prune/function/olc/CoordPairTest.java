package tim.prune.function.olc;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the CoordPair class
 */
class CoordPairTest
{
	@Test
	void testParseCode() throws ParseException
	{
		// 0, 0
		CoordPair pair = CoordPair.decode('0', '0');
		assertTrue(pair == CoordPair.PADDING);
		// 2, 2
		pair = CoordPair.decode('2', '2');
		assertFalse(pair == CoordPair.PADDING);
		assertEquals(0.0, pair.lat);
		assertEquals(0.0, pair.lon);

		// 3, 4
		pair = CoordPair.decode('3', '4');
		assertFalse(pair == CoordPair.PADDING);
		assertEquals(1.0/20.0, pair.lat);
		assertEquals(2.0/20.0, pair.lon);
	}

	@Test
	void testInvalidCode()
	{
		assertThrows(ParseException.class, () -> {
			CoordPair pair = CoordPair.decode('2', 'A');
			assertNull(pair);
		});
		assertThrows(ParseException.class, () -> {
			CoordPair pair = CoordPair.decode('A', '2');
			assertNull(pair);
		});
		assertThrows(ParseException.class, () -> {
			CoordPair pair = CoordPair.decode('4', '0'); // both must be padding or neither
			assertNull(pair);
		});
	}

	@Test
	void testEncode()
	{
		assertEquals('2', CoordPair.encode(0));
		assertEquals('2', CoordPair.encode(20));
		assertEquals('2', CoordPair.encode(40));
		assertEquals('2', CoordPair.encode(-20));
		assertEquals('3', CoordPair.encode(1));
		assertEquals('X', CoordPair.encode(-1));
	}
}
