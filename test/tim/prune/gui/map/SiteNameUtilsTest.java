package tim.prune.gui.map;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;

import org.junit.jupiter.api.Test;

/**
 * JUnit tests for site name utils
 */
class SiteNameUtilsTest
{

	@Test
	void testPickServerNameWithoutWildcards()
	{
		testPickSingleUrl("abc", "abc");
		testPickSingleUrl("ab[]c", "abc");
		testPickSingleUrl("[]abc", "abc");
		testPickSingleUrl("abc[]", "abc");
	}

	/**
	 * Test a pattern without wildcards which should always produce the expected result
	 * @param inPattern pattern for site name
	 * @param inExpected expected resolved name
	 */
	private void testPickSingleUrl(String inPattern, String inExpected)
	{
		for (int i=0; i<20; i++)
		{
			String resolved = SiteNameUtils.pickServerUrl(inPattern);
			assertEquals(inExpected, resolved, "Failed: " + inPattern);
		}
	}

	@Test
	void testPickUsingWildcards()
	{
		testRandomPick("ab[123]c", new String[]{"ab1c", "ab2c", "ab3c"});
		testRandomPick("1234.[abcd]", new String[]{"1234.a", "1234.b", "1234.c", "1234.d"});
	}

	/**
	 * Test a pattern with wildcards which should produce several different results randomly
	 * @param inPattern pattern for site name
	 * @param inExpected array of expected resolved names
	 */
	private void testRandomPick(String inPattern, String[] inExpected)
	{
		HashSet<String> results = new HashSet<String>();
		for (int i=0; i<30; i++)
		{
			results.add(SiteNameUtils.pickServerUrl(inPattern));
		}
		// Check that all expected results were returned
		assertEquals(inExpected.length, results.size());
		for (String expec : inExpected) {
			assertTrue(results.contains(expec));
		}
	}

	@Test
	void testDirectorySpec()
	{
		assertEquals(null, SiteNameUtils.convertUrlToDirectory(""));
		assertEquals("acme.com/tiles/", SiteNameUtils.convertUrlToDirectory("http://www.acme.com/tiles/"));
		assertEquals("acme.com/tiles/", SiteNameUtils.convertUrlToDirectory("https://acme.com/tiles/"));

		assertEquals("acme.com/tiles/", SiteNameUtils.convertUrlToDirectory("https://[abcd].acme.com/tiles/"));
		assertEquals("acme.com/tiles/", SiteNameUtils.convertUrlToDirectory("https://www.[abcd].acme.com/tiles/"));

		assertEquals("m-f-f.com/layer/water/zz/rowy/", SiteNameUtils.convertUrlToDirectory("m-f-f.com/layer/water/z{z}/row{y}/{z}_{x}-{y}.gif"));
	}
}
