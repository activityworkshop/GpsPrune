package tim.prune.function.filesleuth.data;

import org.junit.jupiter.api.Test;

import tim.prune.function.filesleuth.SearchResult;

import java.io.File;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TestTrackFile
{
	@Test
	public void testFilenameFiltersContents()
	{
		TrackFile file = new TrackFile(new File("blah.txt"));
		assertTrue(file.matchesFilename(""));
		assertTrue(file.matchesFilename("blah"));
		assertFalse(file.matchesFilename("apple"));
	}

	@Test
	public void testTextFilters()
	{
		TrackFile file = new TrackFile(new File("blah.txt"));
		assertFalse(file.matchesFilename("apple"));
		TrackContents contents = new TrackContents(null);
		contents.addString("APPLETREE");
		file.setContents(contents);
		SearchResult result = new SearchResult(file);
		assertTrue(file.matchesStringFilter("apple", result));
		assertEquals("appletree", result.getContents());
		assertTrue(file.matchesStringFilter("letr", result));
		assertEquals("appletree", result.getContents());
		assertFalse(file.matchesStringFilter("macaroni", result));
		assertEquals("", result.getContents());
	}

	@Test
	public void testDateFiltersWithoutContents()
	{
		TrackFile file = new TrackFile(new File("blah.txt"));
		assertTrue(file.matchesDateFilter(DateRange.EMPTY_RANGE));
		DateRange range = DateRange.parseString("2001");
		assertTrue(file.matchesDateFilter(range));
	}

	@Test
	public void testDateFiltersWithSingleDate()
	{
		TrackFile file = new TrackFile(new File("blah.txt"));
		DateRange range2001 = DateRange.parseString("2001");
		assertTrue(file.matchesDateFilter(range2001));
		TrackContents contents = new TrackContents(TimeZone.getTimeZone("GMT"));
		contents.addString("APPLETREE");
		contents.addDateString("2002-02-02 13:12:11");
		file.setContents(contents);
		assertFalse(file.matchesDateFilter(range2001));
		assertTrue(file.matchesDateFilter(DateRange.parseString("2002")));
		assertTrue(file.matchesDateFilter(DateRange.parseString("2002 02 02")));
		assertTrue(file.matchesDateFilter(DateRange.parseString("2002 01 01 2002 03 03")));
	}

	@Test
	public void testDateFiltersWithDateRange()
	{
		TrackFile file = new TrackFile(new File("blah.txt"));
		TrackContents contents = new TrackContents(TimeZone.getTimeZone("GMT"));
		contents.addDateString("2002-02-02 13:12:11");
		contents.addDateString("2002-02-02 10:11:12");
		contents.addDateString("2002-02-03 14:10:02");
		file.setContents(contents);
		assertFalse(file.matchesDateFilter(DateRange.parseString("2001")));
		assertTrue(file.matchesDateFilter(DateRange.parseString("2002")));
		assertTrue(file.matchesDateFilter(DateRange.parseString("2002 02")));
		assertFalse(file.matchesDateFilter(DateRange.parseString("2002 02 01")));
		assertTrue(file.matchesDateFilter(DateRange.parseString("2002 02 02")));
		assertTrue(file.matchesDateFilter(DateRange.parseString("2002 02 02-2002 02 05")));
		assertTrue(file.matchesDateFilter(DateRange.parseString("2002 01 01-2002 02 02")));
		assertFalse(file.matchesDateFilter(DateRange.parseString("2002 02 04-2002 03 03")));
	}
}
