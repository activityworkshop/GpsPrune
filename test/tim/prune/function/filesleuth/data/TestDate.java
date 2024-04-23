package tim.prune.function.filesleuth.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class TestDate
{
	@Test
	public void testDaysInMonth()
	{
		assertEquals(31, Date.getDaysInMonth(2024, 1));
		assertEquals(29, Date.getDaysInMonth(2024, 2)); // leap year
		assertEquals(30, Date.getDaysInMonth(2024, 4));
		assertEquals(28, Date.getDaysInMonth(2025, 2)); // not a leap year
	}

	@Test
	public void testParsingSingle()
	{
		Date date = Date.parseString("2024-04-10");
		assertEquals("2024-04-10", date.toString());
		date = Date.parseString("2024-01-31");
		assertEquals("2024-01-31", date.toString());
		date = Date.parseString("   2024 02:11   ");
		assertEquals("2024-02-11", date.toString());
	}

	@Test
	public void testParsingEmpty()
	{
		assertNull(Date.parseString(""));
		assertNull(Date.parseString("          "));
		assertNull(Date.parseString(null));
	}

	@Test
	public void testParsingNotValid()
	{
		// Year, month and day but wrong separators
		checkInvalidDateString("20210101");
		checkInvalidDateString("2021-0101");
		checkInvalidDateString("2021--010");
		// values out of range
		checkInvalidDateString("2021 00 00");
		checkInvalidDateString("2021 -1 -1");
		checkInvalidDateString("2021 13 13");
		checkInvalidDateString("2021 02 30");
		checkInvalidDateString("2021 03 40");
		// Alphanumerics
		checkInvalidDateString("banana");
	}

	private void checkInvalidDateString(String date) {
		assertNull(Date.parseString(date));
	}
}
