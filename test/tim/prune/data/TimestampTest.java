package tim.prune.data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.TimeZone;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for timestamp classes
 */
class TimestampTest
{
	@Test
	void testPlusMinusZoneParsing()
	{
		final String stampString = "2018-03-03T10:22:33";
		Timestamp baseStamp = new TimestampUtc(stampString);
		Timestamp withZ = new TimestampUtc(stampString + "Z");
		assertTrue(withZ.isValid());
		assertEquals(0, withZ.getMillisecondsSince(baseStamp));
		// One timezone east
		Timestamp plusOne = new TimestampUtc(stampString + "+01");
		assertTrue(plusOne.isValid());
		// Note: offset is negative, because the timezone is one hour ahead
		assertEquals(-60 * 60 * 1000L, plusOne.getMillisecondsSince(baseStamp));
		// One timezone west
		Timestamp minusOne = new TimestampUtc(stampString + "-01");
		assertTrue(minusOne.isValid());
		assertEquals(60 * 60 * 1000L, minusOne.getMillisecondsSince(baseStamp));
	}

	@Test
	void testMillisecondParsing()
	{
		final String stampString = "2018-03-03T10:22:33";
		Timestamp baseStamp = new TimestampUtc(stampString);
		Timestamp withCentis = new TimestampUtc(stampString + ".24");
		assertTrue(withCentis.isValid());
		assertEquals(240, withCentis.getMillisecondsSince(baseStamp));
		Timestamp withMillis = new TimestampUtc(stampString + ".941");
		assertTrue(withMillis.isValid());
		assertEquals(941, withMillis.getMillisecondsSince(baseStamp));
	}

	@Test
	void testLondonMoscow()
	{
		Timestamp shouldNotChange = new TimestampUtc("2018-03-03T10:22:33");
		long millisLondon = shouldNotChange.getMilliseconds(TimeZone.getTimeZone("GMT"));
		long millisMoscow = shouldNotChange.getMilliseconds(TimeZone.getTimeZone("GMT+03:00"));
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

	@Test
	void testInterpolateTimestamps_notvalid()
	{
		Timestamp first = new TimestampUtc(1611800L);
		Timestamp second = new TimestampUtc("");
		Assertions.assertEquals("", TimestampUtc.interpolate(first, second, 0, 1));
		Assertions.assertEquals("", TimestampUtc.interpolate(first, null, 0, 1));
		// Also fails if second timestamp is before the first
		second = new TimestampUtc(1611700L);
		Assertions.assertEquals("", TimestampUtc.interpolate(first, second, 0, 1));
	}

	@Test
	void testInterpolateTimestamps_halfway()
	{
		Timestamp first = new TimestampUtc(1611800L);
		Timestamp second = new TimestampUtc(1611800L);
		// Equal timestamps are allowed, then halfway is the same too
		Assertions.assertEquals("1611800", TimestampUtc.interpolate(first, second, 0, 1));

		// Halfway between the two timestamps
		second = new TimestampUtc(1612800L);
		Assertions.assertEquals("1612300", TimestampUtc.interpolate(first, second, 0, 1));
	}
}
