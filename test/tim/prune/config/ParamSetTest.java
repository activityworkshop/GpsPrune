package tim.prune.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ParamSetTest
{
	@Test
	void testEmptyStrings()
	{
		// Empty or missing strings just give an empty list
		ParamSet params = new ParamSet(null);
		for (int i=0; i<10; i++) {
			assertEquals("", params.getParam(i));
		}
		params = new ParamSet("");
		for (int i=0; i<10; i++) {
			assertEquals("", params.getParam(i));
		}
		params = new ParamSet(";;;");
		for (int i=0; i<10; i++) {
			assertEquals("", params.getParam(i));
		}
	}

	@Test
	void testSingleString()
	{
		// Single param on its own with no delimiters
		ParamSet params = new ParamSet("Abc");
		assertEquals("Abc", params.getParam(0));
		for (int i=1; i<10; i++) {
			assertEquals("", params.getParam(i));
		}

		// Empty strings with just one non-empty one in the middle
		params = new ParamSet(";;;Abc;;");
		for (int i=0; i<3; i++)
		{
			if (i == 3) {
				assertEquals("Abc", params.getParam(i));
			}
			else {
				assertEquals("", params.getParam(i));
			}
		}
	}

	@Test
	void testMultipleStrings()
	{
		ParamSet params = new ParamSet("first;2;3;4;last");
		assertEquals("first", params.getParam(0));
		assertEquals("2", params.getParam(1));
		assertEquals("3", params.getParam(2));
		assertEquals("4", params.getParam(3));
		assertEquals("last", params.getParam(4));
	}
}
