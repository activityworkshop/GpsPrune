package tim.prune.function.media;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LinkedMediaLoaderTest
{
	@Test
	void testSplitUrl_unchanged()
	{
		Assertions.assertEquals("http://abc.com", LinkedMediaLoader.splitUrl("http://abc.com"));
		Assertions.assertEquals("https://abc.com", LinkedMediaLoader.splitUrl("https://abc.com"));
		Assertions.assertEquals("ftp://abc.com/photo.jpg", LinkedMediaLoader.splitUrl("ftp://abc.com/photo.jpg"));
		Assertions.assertEquals("random", LinkedMediaLoader.splitUrl("  random "));
	}

	@Test
	void testSplitUrl_onesplit()
	{
		Assertions.assertEquals("123456789_123456789_123456789_123456789_123456789_\na", LinkedMediaLoader.splitUrl("123456789_123456789_123456789_123456789_123456789_a"));
		Assertions.assertEquals("123456789_123456789_123456789_123456789_123456789_\nabc1", LinkedMediaLoader.splitUrl("  123456789_123456789_123456789_123456789_123456789_abc1"));
	}

	@Test
	void testSplitUrl_twosplits()
	{
		String fiftyChars = "123456789_123456789_123456789_123456789_123456789_";
		Assertions.assertEquals(fiftyChars + "\n" + fiftyChars + "\n" + "thirdline", LinkedMediaLoader.splitUrl(fiftyChars + fiftyChars + "thirdline"));
	}
}
