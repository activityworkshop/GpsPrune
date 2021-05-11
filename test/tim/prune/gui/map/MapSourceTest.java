package tim.prune.gui.map;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * JUnit tests for manipulating base Urls
 */
class MapSourceTest
{
	@Test
	void testFixBaseUrls()
	{
		// Should succeed
		testUrlFix("8bitcities.s3.amazonaws.com", "http://8bitcities.s3.amazonaws.com/");
		testUrlFix("8bitcities.s3.amazonaws.com/", "http://8bitcities.s3.amazonaws.com/");
		testUrlFix("http://8bitcities.s3.amazonaws.com/", "http://8bitcities.s3.amazonaws.com/");
		testUrlFix("something.com/ok", "http://something.com/ok/");

		// These should fail and return null
		testUrlFix("something/wrong", null);
		testUrlFix("protocol://something.com/16/", null);
	}

	private void testUrlFix(String inStart, String inExpected)
	{
		String result = MapSource.fixBaseUrl(inStart);
		assertEquals(inExpected, result);
	}
}
