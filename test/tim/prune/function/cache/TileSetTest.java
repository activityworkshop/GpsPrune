package tim.prune.function.cache;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * JUnit tests for tile name checks
 */
class TileSetTest
{
	@Test
	void testIsNumeric()
	{
		// not numeric, should be false
		assertFalse(TileSet.isNumeric(null));
		assertFalse(TileSet.isNumeric(""));
		assertFalse(TileSet.isNumeric("a"));
		assertFalse(TileSet.isNumeric(" "));
		assertFalse(TileSet.isNumeric("155a"));
		assertFalse(TileSet.isNumeric("-2"));
		// numeric, should be true
		assertTrue(TileSet.isNumeric("1"));
		assertTrue(TileSet.isNumeric("155"));
	}

	@Test
	void testIsNumericUntilDot()
	{
		// not numeric, should be false
		assertFalse(TileSet.isNumericUntilDot(null));
		assertFalse(TileSet.isNumericUntilDot(""));
		assertFalse(TileSet.isNumericUntilDot("."));
		assertFalse(TileSet.isNumericUntilDot(".abc"));
		assertFalse(TileSet.isNumericUntilDot("a3."));
		assertFalse(TileSet.isNumericUntilDot("4a"));
		assertFalse(TileSet.isNumericUntilDot("215327h.png"));
		// numeric but no dot, should be false
		assertFalse(TileSet.isNumericUntilDot("1234"));
		// numeric, should be true
		assertTrue(TileSet.isNumericUntilDot("44.jpg"));
		assertTrue(TileSet.isNumericUntilDot("0."));
	}
}
