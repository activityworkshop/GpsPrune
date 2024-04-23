package tim.prune.function.filesleuth.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;


public class TestDateRange
{
	@Test
	public void testParsingSingle()
	{
		// Single year
		DateRange range = DateRange.parseString("2024");
		assertEquals("2024-01-01 - 2024-12-31", range.toString());
		// Single month
		range = DateRange.parseString("2024-02");
		assertEquals("2024-02-01 - 2024-02-29", range.toString());
		// Single day
		range = DateRange.parseString("2024-04-10");
		assertEquals("2024-04-10", range.toString());
		range = DateRange.parseString("2024-01-31");
		assertEquals("2024-01-31", range.toString());
	}

	@Test
	public void testParsingEmpty()
	{
		checkEmptyDateString("");
		checkEmptyDateString("	");
		checkEmptyDateString(null);
	}

	private void checkEmptyDateString(String date)
	{
		DateRange range = DateRange.parseString(date);
		assertEquals(DateRange.EMPTY_RANGE, range);
		assertEquals("", range.toString());
	}

	@Test
	public void testParsingNotValid()
	{
		// Year value but too short
		checkInvalidDateString("20");
		checkInvalidDateString("202");
		checkInvalidDateString("-555");
		checkInvalidDateString("0898");
		// Year and month but month not valid
		checkInvalidDateString("2021-");
		checkInvalidDateString("2021-4");
		checkInvalidDateString("2021 -1");
		checkInvalidDateString("2021 15");
		checkInvalidDateString("2021-144");
		checkInvalidDateString("2021144");
		// Year, month and day but wrong separators
		checkInvalidDateString("20210101");
		checkInvalidDateString("2021-0101");
		checkInvalidDateString("2021--010");
		checkInvalidDateString("2021 00 00");
		checkInvalidDateString("2021 -1 -1");
		checkInvalidDateString("2021 13 13");
		checkInvalidDateString("2021 02 30");
		checkInvalidDateString("2021 03 40");
		// Alphanumerics
		checkInvalidDateString("banana");
		checkInvalidDateString("pear");
	}

	private void checkInvalidDateString(String date)
	{
		DateRange range = DateRange.parseString(date);
		assertEquals(DateRange.INVALID_RANGE, range);
		assertFalse(range.isValid());
		assertEquals("", range.toString());
	}

	@Test
	public void testParsingDateRange()
	{
		// Mixed separators
		DateRange range = DateRange.parseString("2024:06:22	  ...  2024 08-01");
		assertEquals("2024-06-22 - 2024-08-01", range.toString());
		// Dates in wrong order
		range = DateRange.parseString("2024:08:01:2024 06-22");
		assertEquals("2024-06-22 - 2024-08-01", range.toString());
		// Spaces before and after
		range = DateRange.parseString("  2024:08:01:2024 06-22  ");
		assertEquals("2024-06-22 - 2024-08-01", range.toString());
		range = DateRange.parseString("  2024:12:25:2024 12:25  ");
		assertEquals("2024-12-25", range.toString());
	}

	@Test
	public void testParsingInvalidDateRange() {
		checkInvalidDateString("2001-02-22 12002-03-03");
	}

	@Test
	public void testEquals()
	{
		Date firstMarch = new Date(2001, 3, 1);
		Date tenthMarch = new Date(2001, 3, 10);

		assertTrue(new DateRange(firstMarch, firstMarch).equals(new DateRange(firstMarch, firstMarch)));
		assertTrue(new DateRange(firstMarch, tenthMarch).equals(new DateRange(tenthMarch, firstMarch)));
		assertFalse(new DateRange(firstMarch, tenthMarch).equals(new DateRange(tenthMarch, tenthMarch)));
	}

	@Test
	public void testOverlaps()
	{
		Date firstMarch = new Date(2001, 3, 1);
		Date tenthMarch = new Date(2001, 3, 10);
		Date secondApril = new Date(2001, 4, 2);

		assertTrue(new DateRange(firstMarch, firstMarch).overlaps(new DateRange(firstMarch, firstMarch)));
		assertTrue(new DateRange(firstMarch, tenthMarch).overlaps(new DateRange(tenthMarch, firstMarch)));
		assertTrue(new DateRange(firstMarch, tenthMarch).overlaps(new DateRange(tenthMarch, tenthMarch)));
		assertTrue(new DateRange(firstMarch, tenthMarch).overlaps(new DateRange(tenthMarch, secondApril)));
		assertFalse(new DateRange(firstMarch, tenthMarch).overlaps(new DateRange(secondApril, secondApril)));
	}

	@Test
	public void testIncludes()
	{
		Date firstMarch = new Date(2001, 3, 1);
		Date tenthMarch = new Date(2001, 3, 10);
		Date secondApril = new Date(2001, 4, 2);
		Date endJuly = new Date(2001, 7, 31);

		assertTrue(new DateRange(firstMarch, firstMarch).includes(new DateRange(firstMarch, firstMarch)));
		assertTrue(new DateRange(firstMarch, tenthMarch).includes(new DateRange(tenthMarch, firstMarch)));
		assertTrue(new DateRange(firstMarch, tenthMarch).includes(new DateRange(tenthMarch, tenthMarch)));
		assertFalse(new DateRange(tenthMarch, tenthMarch).includes(new DateRange(firstMarch, tenthMarch)));
		assertFalse(new DateRange(firstMarch, tenthMarch).includes(new DateRange(tenthMarch, secondApril)));
		assertFalse(new DateRange(firstMarch, tenthMarch).includes(new DateRange(secondApril, secondApril)));
		assertTrue(new DateRange(firstMarch, endJuly).includes(new DateRange(tenthMarch, tenthMarch)));
	}

	@Test
	public void testFromNumbers()
	{
		DateRange range = DateRange.parseValues(2024, -1, 0);
		assertEquals("2024", range.toShortString());
		assertTrue(range.isYearMonthDay());

		range = DateRange.parseValues(2024, 1, 0);
		assertEquals("2024-01", range.toShortString());
		assertTrue(range.isYearMonthDay());

		range = DateRange.parseValues(2024, 1, 1);
		assertEquals("2024-01-01", range.toShortString());
		assertTrue(range.isYearMonthDay());

		range = DateRange.parseValues(2024, 1, 1, 2024, 1, 1);
		assertEquals("2024-01-01", range.toShortString());
		assertTrue(range.isYearMonthDay());

		range = DateRange.parseValues(2020, 1, 1, 2023, 1, 1);
		assertEquals("2020-01-01 - 2023-01-01", range.toShortString());
		assertFalse(range.isYearMonthDay());

		range = DateRange.parseValues(2020, 1, 1, 2020, 12, 31);
		assertEquals("2020", range.toShortString());
		assertTrue(range.isYearMonthDay());
	}

	@Test
	public void testShortString()
	{
		DateRange range = DateRange.parseString("2024:01:01	  ...  2024 12-31");
		assertEquals("2024", range.toShortString());
		assertTrue(range.isYearMonthDay());

		range = DateRange.parseString("2024:01:01	  ...  2024 12-30");
		assertEquals("2024-01-01 - 2024-12-30", range.toShortString());
		assertFalse(range.isYearMonthDay());

		range = DateRange.parseString("2024:02:01	  ...  2024 02-29");
		assertEquals("2024-02", range.toShortString());
		assertTrue(range.isYearMonthDay());

		range = DateRange.parseString("2024:02:01	  ...  2024 02-28");
		assertEquals("2024-02-01 - 2024-02-28", range.toShortString());
		assertFalse(range.isYearMonthDay());

		range = DateRange.parseString("2024:02:01	  ...  2024 02-30");
		assertEquals("", range.toShortString());
		assertTrue(range.isEmpty());
		assertFalse(range.isValid());
		assertTrue(range.isYearMonthDay());

		range = DateRange.parseString("2024:03:02	  ...  2024 03-02");
		assertEquals("2024-03-02", range.toShortString());
		assertTrue(range.isYearMonthDay());

		range = DateRange.parseString("2024:03:04 2024 03-03");
		assertEquals("2024-03-03 - 2024-03-04", range.toShortString());
		assertFalse(range.isYearMonthDay());
	}
}
