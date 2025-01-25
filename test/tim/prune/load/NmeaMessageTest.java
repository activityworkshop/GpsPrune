package tim.prune.load;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Calendar;

public class NmeaMessageTest
{
	@Test
	public void testFix_false()
	{
		NmeaMessage message = new NmeaMessage("", "", "", "", null);
		Assertions.assertFalse(message.hasFix());
		message = new NmeaMessage("", "", "", "", "");
		Assertions.assertFalse(message.hasFix());
		message = new NmeaMessage("", "", "", "", "0");
		Assertions.assertFalse(message.hasFix());
	}

	@Test
	public void testFix_true()
	{
		NmeaMessage message = new NmeaMessage("", "", "", "", "1");
		Assertions.assertTrue(message.hasFix());
		message = new NmeaMessage("", "", "", "", "2");
		Assertions.assertTrue(message.hasFix());
	}

	@Test
	public void testModifyLatitude_unchanged()
	{
		NmeaMessage message = new NmeaMessage(null, "", "", "", "");
		Assertions.assertNull(message.getLatitude());
		message = new NmeaMessage("123456", "", "", "", "");
		Assertions.assertEquals("123456", message.getLatitude());
		message = new NmeaMessage("1234567", "", "", "", "");
		Assertions.assertEquals("1234567", message.getLatitude());
		message = new NmeaMessage(".123456", "", "", "", "");
		Assertions.assertEquals(".123456", message.getLatitude());
		message = new NmeaMessage("1.23456", "", "", "", "");
		Assertions.assertEquals("1.23456", message.getLatitude());
	}

	@Test
	public void testModifyLatitude_changed()
	{
		NmeaMessage message = new NmeaMessage("123.456", "", "", "", "");
		Assertions.assertEquals("1d23.456", message.getLatitude());
		message = new NmeaMessage("1234.56", "", "", "", "");
		Assertions.assertEquals("12d34.56", message.getLatitude());
		message = new NmeaMessage("12345.6789", "", "", "", "");
		Assertions.assertEquals("123d45.6789", message.getLatitude());
		message = new NmeaMessage("12.3456789", "", "", "", "");
		Assertions.assertEquals("0d12.3456789", message.getLatitude());
		message = new NmeaMessage("-12345.", "", "", "", "");
		Assertions.assertEquals("-123d45.", message.getLatitude());
		// multiple decimal points aren't valid, neither is 123 degrees latitude
		message = new NmeaMessage("12345.6.7", "", "", "", "");
		Assertions.assertEquals("123d45.6.7", message.getLatitude());
	}

	@Test
	public void testModifyLongitude_outofrange()
	{
		NmeaMessage message = new NmeaMessage("", "-23456.7", "", "", "");
		Assertions.assertEquals("-234d56.7", message.getLongitude());
		message = new NmeaMessage("", "23456.7", "", "", "");
		Assertions.assertEquals("234d56.7", message.getLongitude());
	}

	@Test
	public void testTimestamp_none()
	{
		NmeaMessage message = new NmeaMessage("", "", "", null, "");
		Assertions.assertNull(message.getTimestamp());
		message = new NmeaMessage("", "", "", "", "");
		Assertions.assertNull(message.getTimestamp());
		message = new NmeaMessage("", "", "", "notnum", "");
		Assertions.assertNull(message.getTimestamp());
		message = new NmeaMessage("", "", "", "12345.", "");
		Assertions.assertNull(message.getTimestamp());
		message = new NmeaMessage("", "", "", "12345", "");
		Assertions.assertNull(message.getTimestamp());
	}

	@Test
	public void testTimestamp_valid()
	{
		NmeaMessage message = new NmeaMessage("", "", "", "000000", "");
		Assertions.assertNotNull(message.getTimestamp());
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(Long.parseLong(message.getTimestamp()));
		Assertions.assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY));
		Assertions.assertEquals(0, calendar.get(Calendar.MINUTE));
		Assertions.assertEquals(0, calendar.get(Calendar.SECOND));

		message = new NmeaMessage("", "", "", "111213", "");
		Assertions.assertNotNull(message.getTimestamp());
		calendar.setTimeInMillis(Long.parseLong(message.getTimestamp()));
		Assertions.assertEquals(11, calendar.get(Calendar.HOUR_OF_DAY));
		Assertions.assertEquals(12, calendar.get(Calendar.MINUTE));
		Assertions.assertEquals(13, calendar.get(Calendar.SECOND));

		message = new NmeaMessage("", "", "", "236464", "");
		Assertions.assertNotNull(message.getTimestamp());
		calendar.setTimeInMillis(Long.parseLong(message.getTimestamp()));
		// 23:64:64 corresponds to 0:05:04 after wrapping the minutes and seconds
		Assertions.assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY));
		Assertions.assertEquals(5, calendar.get(Calendar.MINUTE));
		Assertions.assertEquals(4, calendar.get(Calendar.SECOND));

		// Negative numbers should not occur but they get wrapped anyway
		message = new NmeaMessage("", "", "", "-1-2-3", "");
		Assertions.assertNotNull(message.getTimestamp());
		calendar.setTimeInMillis(Long.parseLong(message.getTimestamp()));
		// -1:-2:-3 corresponds to 22:57:57 after wrapping the minutes and seconds
		Assertions.assertEquals(22, calendar.get(Calendar.HOUR_OF_DAY));
		Assertions.assertEquals(57, calendar.get(Calendar.MINUTE));
		Assertions.assertEquals(57, calendar.get(Calendar.SECOND));
	}

	@Test
	public void testDate_none()
	{
		checkUsesTodaysDate(null);
		checkUsesTodaysDate("");
		checkUsesTodaysDate("12345");
		checkUsesTodaysDate("1234567");
		checkUsesTodaysDate("abcdef");
	}

	private void checkUsesTodaysDate(String dateString)
	{
		Calendar cal = Calendar.getInstance();
		final int todayDay = cal.get(Calendar.DAY_OF_MONTH);
		final int todayMonth = cal.get(Calendar.MONTH);
		final int todayYear = cal.get(Calendar.YEAR);

		NmeaMessage message = new NmeaMessage("", "", "", "123456", "");
		message.setDate(dateString);

		cal.setTimeInMillis(Long.parseLong(message.getTimestamp()));
		Assertions.assertEquals(todayDay, cal.get(Calendar.DAY_OF_MONTH));
		Assertions.assertEquals(todayMonth, cal.get(Calendar.MONTH));
		Assertions.assertEquals(todayYear, cal.get(Calendar.YEAR));
	}

	@Test
	public void testDate_valid()
	{
		checkUsesGivenDate(4, 5, 2006, "040506");
		checkUsesGivenDate(11, 12, 1997, "111297");
		// 30th February is evaluated to 1 March
		checkUsesGivenDate(1, 3, 2024, "300224");
		// Negative numbers get wrapped too
		checkUsesGivenDate(29, 9, 1996, "-1-2-3");
	}

	private void checkUsesGivenDate(int expDay, int expMonth, int expYear, String dateString)
	{
		NmeaMessage message = new NmeaMessage("", "", "", "123456", "");
		message.setDate(dateString);

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(Long.parseLong(message.getTimestamp()));
		Assertions.assertEquals(expDay, cal.get(Calendar.DAY_OF_MONTH));
		Assertions.assertEquals(expMonth - 1, cal.get(Calendar.MONTH)); // months start from 0
		Assertions.assertEquals(expYear, cal.get(Calendar.YEAR));
	}

	@Test
	public void testDate_validButTimeNot()
	{
		NmeaMessage message = new NmeaMessage("", "", "", null, "");
		message.setDate("040524");
		Assertions.assertNull(message.getTimestamp());
	}
}
