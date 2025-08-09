package tim.prune.function.compress.methods;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import tim.prune.data.DataPoint;
import tim.prune.data.MarkingData;
import tim.prune.data.Track;
import tim.prune.function.PointUtils;
import tim.prune.function.compress.CompressionMethodType;
import tim.prune.function.compress.TrackDetails;

public class TestDouglasPeuckerMethod
{
	@Test
	public void testSerialize_inactive()
	{
		DouglasPeuckerMethod method = new DouglasPeuckerMethod(20);
		Assertions.assertFalse(method.isActive());
		Assertions.assertEquals(CompressionMethodType.DOUGLAS_PEUCKER, method.getType());
		Assertions.assertEquals("oDPC:20", method.getTotalSettingsString());
		Assertions.assertEquals("20", method.getParam());
	}

	@Test
	public void testDeserialize_inactive()
	{
		String fromConfig = "oDPC:10001";
		CompressionMethod method = CompressionMethod.fromSettingsString(fromConfig);
		Assertions.assertNotNull(method);
		Assertions.assertTrue(method instanceof DouglasPeuckerMethod);
		Assertions.assertFalse(method.isActive());
		Assertions.assertEquals("oDPC:10001", method.getTotalSettingsString());
	}

	@Test
	public void testSerialize_active()
	{
		DouglasPeuckerMethod method = new DouglasPeuckerMethod(1500);
		method.setActive(true);
		Assertions.assertEquals("xDPC:1500", method.getTotalSettingsString());
	}

	@Test
	public void testDeserialize_active()
	{
		String fromConfig = "xDPC:1234";
		CompressionMethod method = CompressionMethod.fromSettingsString(fromConfig);
		Assertions.assertNotNull(method);
		Assertions.assertTrue(method instanceof DouglasPeuckerMethod);
		Assertions.assertTrue(method.isActive());
		Assertions.assertEquals("xDPC:1234", method.getTotalSettingsString());
	}

	@Test
	public void testDeserialize_tooLong()
	{
		CompressionMethod method = CompressionMethod.fromSettingsString("NDPC:881");
		Assertions.assertTrue(method instanceof DouglasPeuckerMethod);
		Assertions.assertFalse(method.isActive());
	}

	@Test
	public void testDeserialize_fail()
	{
		Assertions.assertNull(CompressionMethod.fromSettingsString(null));
		Assertions.assertNull(CompressionMethod.fromSettingsString(""));
		Assertions.assertNull(CompressionMethod.fromSettingsString("DPC:"));
		Assertions.assertNull(CompressionMethod.fromSettingsString("DPC:abcd"));
	}

	@Test
	public void testDeleteSome()
	{
		Track track = new Track();
		DataPoint point = new DataPoint(47.1, 8.0);
		double angleDegrees = 20.0;
		for (int i=0; i<44; i++)
		{
			final double angleRadians = angleDegrees / 180.0 * Math.PI;
			point = PointUtils.projectPoint(point, angleRadians, 0.0005);
			point.setSegmentStart(i == 0);
			track.appendPoint(point);
			angleDegrees += (i / 9.0);
		}

		TrackDetails details = new TrackDetails(track);
		MarkingData markings = new MarkingData(track);
		new DouglasPeuckerMethod(1700).compress(track, details, markings);
		Assertions.assertEquals(10, markings.getNumDeleted());
		for (int i=0; i<44; i++)
		{
			boolean expectDelete = i == 1 || i == 2 || i == 3 || i == 4
					|| i == 6 || i == 7 || i == 9
					|| i == 12 || i == 15 || i == 18;
			Assertions.assertEquals(expectDelete, markings.isPointMarkedForDeletion(i), "Point " + i);
			Assertions.assertFalse(markings.isPointMarkedForSegmentBreak(i));
		}
	}
}
