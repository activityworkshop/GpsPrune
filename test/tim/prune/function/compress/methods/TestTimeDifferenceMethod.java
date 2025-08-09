package tim.prune.function.compress.methods;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.MarkingData;
import tim.prune.data.Track;
import tim.prune.function.compress.CompressionMethodType;
import tim.prune.function.compress.TrackDetails;

public class TestTimeDifferenceMethod
{
	@Test
	public void testSerialize_inactive()
	{
		TooSoonMethod method = new TooSoonMethod(20);
		Assertions.assertFalse(method.isActive());
		Assertions.assertEquals(CompressionMethodType.TIME_DIFFERENCE, method.getType());
		Assertions.assertEquals("oTSA:20", method.getTotalSettingsString());
		Assertions.assertEquals("20", method.getParam());
	}

	@Test
	public void testDeserialize_inactive()
	{
		String fromConfig = "oTSA:10001";
		CompressionMethod method = CompressionMethod.fromSettingsString(fromConfig);
		Assertions.assertNotNull(method);
		Assertions.assertTrue(method instanceof TooSoonMethod);
		Assertions.assertFalse(method.isActive());
		Assertions.assertEquals("oTSA:10001", method.getTotalSettingsString());
	}

	@Test
	public void testSerialize_active()
	{
		TooSoonMethod method = new TooSoonMethod(1500);
		method.setActive(true);
		Assertions.assertEquals("xTSA:1500", method.getTotalSettingsString());
	}

	@Test
	public void testDeserialize_active()
	{
		String fromConfig = "xTSA:1234";
		CompressionMethod method = CompressionMethod.fromSettingsString(fromConfig);
		Assertions.assertNotNull(method);
		Assertions.assertTrue(method instanceof TooSoonMethod);
		Assertions.assertTrue(method.isActive());
		Assertions.assertEquals("xTSA:1234", method.getTotalSettingsString());
	}

	@Test
	public void testDeserialize_tooLong()
	{
		CompressionMethod method = CompressionMethod.fromSettingsString("NTSA:881");
		Assertions.assertTrue(method instanceof TooSoonMethod);
		Assertions.assertFalse(method.isActive());
	}

	@Test
	public void testDeserialize_fail()
	{
		Assertions.assertNull(CompressionMethod.fromSettingsString(null));
		Assertions.assertNull(CompressionMethod.fromSettingsString(""));
		Assertions.assertNull(CompressionMethod.fromSettingsString("TSA:"));
		Assertions.assertNull(CompressionMethod.fromSettingsString("TSA:abcd"));
	}

	@Test
	public void testDeleteSome()
	{
		Track track = new Track();
		for (int i=0; i<14; i++)
		{
			final DataPoint point = new DataPoint(1.0, 1.0 + 0.01 * i);
			final long timestamp = 123456L + 2L * i;
			point.setFieldValue(Field.TIMESTAMP, "" + timestamp, false);
			track.appendPoint(point);
		}
		track.getPoint(0).setSegmentStart(true);

		TrackDetails details = new TrackDetails(track);
		MarkingData markings = new MarkingData(track);
		new TooSoonMethod(10).compress(track, details, markings);
		Assertions.assertEquals(10, markings.getNumDeleted());
		for (int i=0; i<15; i++)
		{
			boolean shouldDelete = i == 1 || i == 2 || i == 3 || i == 4
					|| i == 6 || i == 7 || i == 8 || i == 9
					|| i == 11 || i == 12;
			Assertions.assertEquals(shouldDelete, markings.isPointMarkedForDeletion(i), "point " + i);
			Assertions.assertFalse(markings.isPointMarkedForSegmentBreak(i));
		}
	}
}
