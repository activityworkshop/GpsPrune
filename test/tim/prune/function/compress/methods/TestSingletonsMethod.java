package tim.prune.function.compress.methods;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import tim.prune.data.DataPoint;
import tim.prune.data.Distance;
import tim.prune.data.MarkingData;
import tim.prune.data.Track;
import tim.prune.data.UnitSetLibrary;
import tim.prune.function.PointUtils;
import tim.prune.function.compress.CompressionMethodType;
import tim.prune.function.compress.TrackDetails;

public class TestSingletonsMethod
{
	@Test
	public void testSerialize_inactive()
	{
		SingletonsMethod method = new SingletonsMethod(20);
		Assertions.assertFalse(method.isActive());
		Assertions.assertEquals(CompressionMethodType.SINGLETONS, method.getType());
		Assertions.assertEquals("oSIN:20.0", method.getTotalSettingsString());
		Assertions.assertEquals("20.0", method.getParam());
	}

	@Test
	public void testDeserialize_inactive()
	{
		String fromConfig = "oSIN:10001";
		CompressionMethod method = CompressionMethod.fromSettingsString(fromConfig);
		Assertions.assertNotNull(method);
		Assertions.assertTrue(method instanceof SingletonsMethod);
		Assertions.assertFalse(method.isActive());
		Assertions.assertEquals("oSIN:10001.0", method.getTotalSettingsString());
	}

	@Test
	public void testSerialize_active()
	{
		SingletonsMethod method = new SingletonsMethod(1500);
		method.setActive(true);
		Assertions.assertEquals("xSIN:1500.0", method.getTotalSettingsString());
	}

	@Test
	public void testDeserialize_active()
	{
		String fromConfig = "xSIN:1234.5";
		CompressionMethod method = CompressionMethod.fromSettingsString(fromConfig);
		Assertions.assertNotNull(method);
		Assertions.assertTrue(method instanceof SingletonsMethod);
		Assertions.assertTrue(method.isActive());
		Assertions.assertEquals("xSIN:1234.5", method.getTotalSettingsString());
	}

	@Test
	public void testDeserialize_tooLong()
	{
		CompressionMethod method = CompressionMethod.fromSettingsString("NSIN:881");
		Assertions.assertTrue(method instanceof SingletonsMethod);
		Assertions.assertFalse(method.isActive());
	}

	@Test
	public void testDeserialize_fail()
	{
		Assertions.assertNull(CompressionMethod.fromSettingsString(null));
		Assertions.assertNull(CompressionMethod.fromSettingsString(""));
		Assertions.assertNull(CompressionMethod.fromSettingsString("SIN:"));
		Assertions.assertNull(CompressionMethod.fromSettingsString("SIN:abcd"));
	}

	@Test
	public void testDeleteOneSingletonAtEnd()
	{
		Track track = new Track();
		for (int i=0; i<14; i++) {
			track.appendPoint(new DataPoint(1.0, 1.0 + 0.01 * i));
		}
		track.getPoint(0).setSegmentStart(true);
		DataPoint extra = new DataPoint(1.2, 1.2);
		extra.setSegmentStart(true);
		track.appendPoint(extra);
		TrackDetails details = new TrackDetails(track);
		MarkingData markings = new MarkingData(track);
		new SingletonsMethod(0.001).compress(track, details, markings);
		Assertions.assertEquals(1, markings.getNumDeleted());
		for (int i=0; i<15; i++)
		{
			Assertions.assertEquals(i == 14, markings.isPointMarkedForDeletion(i));
			Assertions.assertFalse(markings.isPointMarkedForSegmentBreak(i));
		}
	}

	@Test
	public void testDeleteOneOfTwoSingletons()
	{
		Track track = new Track();
		for (int i=0; i<14; i++)
		{
			DataPoint point = new DataPoint(1.0, 1.0 + 0.01 * i);
			point.setSegmentStart(i == 0 || i == 7);
			track.appendPoint(point);
			if (i == 6)
			{
				// Add two singletons, one close and one far away
				track.appendPoint(makeSingleton(point, 20, 40.0));
				track.appendPoint(makeSingleton(point, 30, 400.0));
			}
		}
		TrackDetails details = new TrackDetails(track);
		MarkingData markings = new MarkingData(track);
		new SingletonsMethod(0.1).compress(track, details, markings);
		Assertions.assertEquals(1, markings.getNumDeleted());
		for (int i=0; i<16; i++)
		{
			Assertions.assertEquals(i == 8, markings.isPointMarkedForDeletion(i));
			Assertions.assertFalse(markings.isPointMarkedForSegmentBreak(i));
		}
	}

	private static DataPoint makeSingleton(DataPoint inPoint, double inDegrees, double inMetres)
	{
		final double radiansAngle = inDegrees / 180.0 * Math.PI;
		final double distRadians = Distance.convertDistanceToRadians(inMetres, UnitSetLibrary.UNITS_METRES);
		DataPoint extra = PointUtils.projectPoint(inPoint, radiansAngle, distRadians);
		extra.setSegmentStart(true);
		return extra;
	}
}
