package tim.prune.data;

import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import tim.prune.I18nManager;

public class FieldRecogniserTest
{
	@Test
	public void testMapGeneration()
	{
		Assertions.assertEquals(0, FieldRecogniser.getMappingSize());
		FieldRecogniser.clearMap();
		Assertions.assertEquals(0, FieldRecogniser.getMappingSize());
		FieldRecogniser.getLabel("");
		final int mapSize = FieldRecogniser.getMappingSize();
		Assertions.assertTrue(mapSize > 0);
		FieldRecogniser.getLabel("abc");
		FieldRecogniser.getLabel("hr");
		Assertions.assertEquals(mapSize, FieldRecogniser.getMappingSize());
		FieldRecogniser.clearMap();
		Assertions.assertEquals(0, FieldRecogniser.getMappingSize());
	}

	@Test
	public void testUnknownFields()
	{
		Assertions.assertEquals("abc", FieldRecogniser.getLabel("abc"));
		Assertions.assertEquals("Def", FieldRecogniser.getLabel("Def"));
		// prefixes before colons should be removed
		Assertions.assertEquals("def", FieldRecogniser.getLabel("abc:def"));
		Assertions.assertEquals("Suffix", FieldRecogniser.getLabel("super:prefix:Suffix"));
	}

	@Test
	public void testKnownFields()
	{
		I18nManager.init(Locale.UK);
		Assertions.assertEquals("Heart rate", FieldRecogniser.getLabel("hr"));
		Assertions.assertEquals("Heart rate", FieldRecogniser.getLabel("gpxtpx:hr"));
		Assertions.assertEquals("Heart rate", FieldRecogniser.getLabel("anything:HR"));
		Assertions.assertEquals("Heart rate", FieldRecogniser.getLabel("anything:Heart"));
		Assertions.assertEquals("Cadence", FieldRecogniser.getLabel("anything:caDEnce"));
	}
}
