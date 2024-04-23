package tim.prune.function.filesleuth.data;

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import tim.prune.function.filesleuth.SearchResult;

public class TestFilter
{
	@Test
	public void testEmptyFiltersCompare()
	{
		Filter filter1 = new Filter("");
		Filter filter2 = new Filter("  ");
		Filter filter3 = Filter.EMPTY_FILTER;
		Assertions.assertEquals(Filter.Comparison.SAME, Filter.compare(filter1, null));
		Assertions.assertEquals(Filter.Comparison.SAME, Filter.compare(null, filter1));
		Assertions.assertEquals(Filter.Comparison.SAME, Filter.compare(filter1, filter2));
		Assertions.assertEquals(Filter.Comparison.SAME, Filter.compare(null, null));
		Assertions.assertEquals(Filter.Comparison.SAME, Filter.compare(filter2, filter1));
		Assertions.assertEquals(Filter.Comparison.SAME, Filter.compare(filter2, filter3));
	}

	@Test
	public void testFiltersCompare()
	{
		Filter filter1 = new Filter("cube");
		Filter filter2 = new Filter(" CUbE ");
		Filter filter3 = new Filter("  ");
		Filter filter4 = new Filter("sphere");
		Assertions.assertEquals(Filter.Comparison.WIDER, Filter.compare(filter1, null));
		Assertions.assertEquals(Filter.Comparison.SAME, Filter.compare(filter1, filter1));
		Assertions.assertEquals(Filter.Comparison.NARROWER, Filter.compare(null, filter1));
		Assertions.assertEquals(Filter.Comparison.SAME, Filter.compare(filter1, filter2));
		Assertions.assertEquals(Filter.Comparison.SAME, Filter.compare(filter2, filter1));
		Assertions.assertEquals(Filter.Comparison.WIDER, Filter.compare(filter1, filter3));
		Assertions.assertEquals(Filter.Comparison.NARROWER, Filter.compare(filter3, filter1));
		Assertions.assertEquals(Filter.Comparison.DIFFERENT, Filter.compare(filter1, filter4));
	}

	@Test
	public void testFiltersNarrowing()
	{
		Filter filter1 = new Filter("great");
		Filter filter2 = new Filter(" Greater ");
		Assertions.assertEquals(Filter.Comparison.NARROWER, Filter.compare(filter1, filter2));
		Assertions.assertEquals(Filter.Comparison.SAME, Filter.compare(filter1, filter1));
		Assertions.assertEquals(Filter.Comparison.WIDER, Filter.compare(filter2, filter1));
		Assertions.assertEquals(Filter.Comparison.NARROWER, Filter.compare(null, filter1));
		Assertions.assertEquals(Filter.Comparison.WIDER, Filter.compare(filter1, null));
	}

	@Test
	public void testFilterFilenameMatch()
	{
		Filter filter = new Filter("lemon");
		TrackFile track = new TrackFile(new File("-lemonade.txt"));
		SearchResult result = new SearchResult(track);
		filter.apply(result);
		Assertions.assertTrue(result.isMatch());
		Assertions.assertEquals("", result.getContents());
		track = new TrackFile(new File("orangeade.txt"));
		result = new SearchResult(track);
		filter.apply(result);
		Assertions.assertFalse(result.isMatch());
		Assertions.assertEquals("", result.getContents());
	}

	@Test
	public void testFilterStringMatch()
	{
		Filter filter = new Filter("lemon");
		TrackFile track = new TrackFile(new File("lemonade.txt"));
		TrackContents contents = new TrackContents(null);
		contents.addString("walrus");
		contents.addString("Lemon Sole");
		track.setContents(contents);
		SearchResult result = new SearchResult(track);
		filter.apply(result);
		Assertions.assertTrue(result.isMatch());
		Assertions.assertEquals("lemon sole", result.getContents());

		track = new TrackFile(new File("nothing.gpx"));
		track.setContents(contents);
		result = new SearchResult(track);
		filter.apply(result);
		Assertions.assertTrue(result.isMatch());
		Assertions.assertEquals("lemon sole", result.getContents());

		filter = new Filter("haddock");
		filter.apply(result);
		Assertions.assertFalse(result.isMatch());
		Assertions.assertEquals("", result.getContents());
	}
}
