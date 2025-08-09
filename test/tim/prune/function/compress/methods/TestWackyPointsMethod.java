package tim.prune.function.compress.methods;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import tim.prune.data.DataPoint;
import tim.prune.data.MarkingData;
import tim.prune.data.Track;
import tim.prune.function.compress.CompressionMethodType;
import tim.prune.function.compress.TrackDetails;

public class TestWackyPointsMethod
{
	@Test
	public void testSerialize_inactive()
	{
		WackyPointsMethod method = new WackyPointsMethod(20);
		Assertions.assertFalse(method.isActive());
		Assertions.assertEquals(CompressionMethodType.WACKY_POINTS, method.getType());
		Assertions.assertEquals("oWAC:20.0", method.getTotalSettingsString());
		Assertions.assertEquals("20.0", method.getParam());
	}

	@Test
	public void testDeserialize_inactive()
	{
		String fromConfig = "oWAC:10001";
		CompressionMethod method = CompressionMethod.fromSettingsString(fromConfig);
		Assertions.assertNotNull(method);
		Assertions.assertTrue(method instanceof WackyPointsMethod);
		Assertions.assertFalse(method.isActive());
		Assertions.assertEquals("oWAC:10001.0", method.getTotalSettingsString());
	}

	@Test
	public void testSerialize_active()
	{
		WackyPointsMethod method = new WackyPointsMethod(1500);
		method.setActive(true);
		Assertions.assertEquals("xWAC:1500.0", method.getTotalSettingsString());
	}

	@Test
	public void testDeserialize_active()
	{
		String fromConfig = "xWAC:1234.5";
		CompressionMethod method = CompressionMethod.fromSettingsString(fromConfig);
		Assertions.assertNotNull(method);
		Assertions.assertTrue(method instanceof WackyPointsMethod);
		Assertions.assertTrue(method.isActive());
		Assertions.assertEquals("xWAC:1234.5", method.getTotalSettingsString());
	}

	@Test
	public void testDeserialize_tooLong()
	{
		CompressionMethod method = CompressionMethod.fromSettingsString("NWAC:881");
		Assertions.assertTrue(method instanceof WackyPointsMethod);
		Assertions.assertFalse(method.isActive());
	}

	@Test
	public void testDeserialize_fail()
	{
		Assertions.assertNull(CompressionMethod.fromSettingsString(null));
		Assertions.assertNull(CompressionMethod.fromSettingsString(""));
		Assertions.assertNull(CompressionMethod.fromSettingsString("WAC:"));
		Assertions.assertNull(CompressionMethod.fromSettingsString("WAC:abcd"));
	}

	@Test
	public void testDeleteOneOfTwoWackyPoints()
	{
		Track track = new Track();
		for (int i=0; i<44; i++)
		{
			DataPoint point = new DataPoint(1.0, 1.0 + 0.01 * i);
			point.setSegmentStart(i == 0 || i == 7);
			track.appendPoint(point);
			if (i == 3 || i == 6) {
				point = new DataPoint(1.1, 1.0 + 0.01 * i);
				track.appendPoint(point);
			}
		}
		TrackDetails details = new TrackDetails(track);
		MarkingData markings = new MarkingData(track);
		new WackyPointsMethod(1.5).compress(track, details, markings);
		Assertions.assertEquals(1, markings.getNumDeleted());
		for (int i=0; i<44; i++)
		{
			Assertions.assertEquals(i == 4, markings.isPointMarkedForDeletion(i));
			Assertions.assertFalse(markings.isPointMarkedForSegmentBreak(i));
		}
	}
}
