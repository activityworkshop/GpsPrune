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
		// Should succeed, default protocol added
		testUrlFix("8bitcities.s3.amazonaws.com/", "http://8bitcities.s3.amazonaws.com/");
		// Should succeed, unchanged
		testUrlFixUnchanged("http://8bitcities.s3.amazonaws.com/");
		testUrlFixUnchanged("https://8bitcities.s3.amazonaws.com/");
		// Should succeed but with slash appended
		testUrlFix("8bitcities.s3.amazonaws.com", "http://8bitcities.s3.amazonaws.com/");
		testUrlFix("something.com/ok", "http://something.com/ok/");
		testUrlFix("http://something.com/ok", "http://something.com/ok/");
		testUrlFix("https://something.com/ok", "https://something.com/ok/");

		// These should fail and return null
		testUrlFix("something/wrong", null);
		testUrlFix("something..wrong/tiles", null);
		testUrlFix("http://something/wrong", null);
		testUrlFix("https://something/wrong", null);
		testUrlFix("protocol://something.com/16/", null);

		// Incorrect placeholders
		testUrlFix("http://something.com/{tiles}/", null);
		testUrlFix("http://something.com/}/", null);
		testUrlFix("something.com/}/", null);
		testUrlFix("http://something.com/{x}/{z}.png", null);
		testUrlFix("http://something.com/{y}/{x}/{p}.png", null);
		testUrlFix("http://something.com/{{x}y}/{z}/z.png", null);
		testUrlFix("http://something.com/}{y}/{x}/{z}.png", null);
		// unchanged, without trailing slash
		testUrlFixUnchanged("http://something.com/{x}/{y}/x/{z}.png");
		testUrlFixUnchanged("http://abc.something.com/{z}/{y}/{x}");
		testUrlFixUnchanged("http://abc.something.com/{z}/{y}/{x}/");
		testUrlFixUnchanged("http://something.com/{x}/{y}{z}/{x}.png");
	}

	private void testUrlFixUnchanged(String inStart) {
		testUrlFix(inStart, inStart);
	}

	private void testUrlFix(String inStart, String inExpected)
	{
		String result = MapSource.fixBaseUrl(inStart);
		assertEquals(inExpected, result);
	}
}
