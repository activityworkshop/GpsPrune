package tim.prune.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the GeocacheCode class
 */
public class GeocacheCodeTest
{
	@Test
	public void testShortCodesNotValid()
	{
		assertFalse(GeocacheCode.isValidCode(null));
		assertFalse(GeocacheCode.isValidCode(""));
		assertFalse(GeocacheCode.isValidCode("1"));
		assertFalse(GeocacheCode.isValidCode("$"));
		assertFalse(GeocacheCode.isValidCode("GC"));
	}

	@Test
	public void testLongCodesNotValid()
	{
		assertFalse(GeocacheCode.isValidCode("GCVERYLONGNAME"));
		assertFalse(GeocacheCode.isValidCode("GC123456789"));
	}

	@Test
	public void testIncorrectCodesNotValid()
	{
		assertFalse(GeocacheCode.isValidCode("abcdef"));
		assertFalse(GeocacheCode.isValidCode("GCA B"));
		assertFalse(GeocacheCode.isValidCode("GClower"));
		assertFalse(GeocacheCode.isValidCode("OCBL,H"));
		assertFalse(GeocacheCode.isValidCode("ABCDE"));
	}

	@Test
	public void testCodesValid()
	{
		assertTrue(GeocacheCode.isValidCode("GCAAAA"));
		assertTrue(GeocacheCode.isValidCode("GC12345"));
		assertTrue(GeocacheCode.isValidCode("OCABC123"));
		assertTrue(GeocacheCode.isValidCode("OCARINA"));
	}

	@Test
	public void testShortCodesNoUrl()
	{
		assertNull(GeocacheCode.getUrl(null));
		assertNull(GeocacheCode.getUrl(""));
		assertNull(GeocacheCode.getUrl("1"));
		assertNull(GeocacheCode.getUrl("$"));
		assertNull(GeocacheCode.getUrl("GC"));
	}

	@Test
	public void testCodesToUrl()
	{
		String url = GeocacheCode.getUrl("GCABC123");
		assertEquals("https://coord.info/GCABC123", url);
		url = GeocacheCode.getUrl("OCABC123");
		assertEquals("https://www.opencaching.de/OCABC123", url);
	}
}
