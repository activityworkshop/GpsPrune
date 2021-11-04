package tim.prune.data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.TimeZone;

import org.junit.jupiter.api.Test;

class TimestampTest
{

	@Test
	void testLondonMoscow()
	{
		Timestamp shouldntChange = new TimestampUtc("2018-03-03T10:22:33");
		long millisLondon = shouldntChange.getMilliseconds(TimeZone.getTimeZone("GMT"));
		long millisMoscow = shouldntChange.getMilliseconds(TimeZone.getTimeZone("GMT+03:00"));
		// Shouldn't change, millis are the same in Moscow
		assertEquals(millisLondon, millisMoscow);

		// Same again but using local timestamps
		Timestamp alwaysLocal = new TimestampLocal(2018, 3, 3, 10, 22, 33);
		millisLondon = alwaysLocal.getMilliseconds(TimeZone.getTimeZone("GMT"));
		millisMoscow = alwaysLocal.getMilliseconds(TimeZone.getTimeZone("GMT+03:00"));
		// For local timestamp, millis are not the same in Moscow
		assertNotEquals(millisLondon, millisMoscow);
	}

	@Test
	void testTimestampDifferences()
	{
		Timestamp earlier = new TimestampLocal(2018, 4, 6, 8, 10, 12);
		Timestamp later = new TimestampLocal(2018, 4, 6, 8, 10, 14);
		assertEquals(later.getMillisecondsSince(earlier), 2000L);
		assertTrue(later.isAfter(earlier));
		assertFalse(later.isBefore(earlier));

		assertEquals(earlier.getMillisecondsSince(later), -2000L);
		assertFalse(earlier.isAfter(later));
		assertTrue(earlier.isBefore(later));
	}
}
