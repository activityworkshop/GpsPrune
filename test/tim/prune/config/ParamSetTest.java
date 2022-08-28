package tim.prune.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ParamSetTest
{
	@Test
	void testEmptyStrings()
	{
		// Empty or missing strings just give an endless set of empty params
		ParamSet params = new ParamSet(null);
		for (int i=0; i<10; i++) {
			assertEquals("", params.getNext());
		}
		params = new ParamSet("");
		for (int i=0; i<10; i++) {
			assertEquals("", params.getNext());
		}
		params = new ParamSet(";;;");
		for (int i=0; i<10; i++) {
			assertEquals("", params.getNext());
		}
	}

	@Test
	void testSingleString()
	{
		// Single param on its own with no delimiters
		ParamSet params = new ParamSet("Abc");
		assertEquals("Abc", params.getNext());
		for (int i=0; i<10; i++) {
			assertEquals("", params.getNext());
		}

		// Empty strings with just one non-empty one in the middle
		params = new ParamSet(";;;Abc;;");
		for (int i=0; i<3; i++) {
			assertEquals("", params.getNext());
		}
		assertEquals("Abc", params.getNext());
		for (int i=0; i<10; i++) {
			assertEquals("", params.getNext());
		}
	}

	@Test
	void testMultipleStrings()
	{
		ParamSet params = new ParamSet("first;2;3;4;last");
		assertEquals("first", params.getNext());
		assertEquals("2", params.getNext());
		assertEquals("3", params.getNext());
		assertEquals("4", params.getNext());
		assertEquals("last", params.getNext());
	}
}
