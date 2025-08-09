package tim.prune.function.compress.methods;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import tim.prune.data.DataPoint;
import tim.prune.data.Distance;
import tim.prune.data.Field;
import tim.prune.data.MarkingData;
import tim.prune.data.Track;
import tim.prune.data.UnitSetLibrary;
import tim.prune.function.PointUtils;
import tim.prune.function.compress.CompressionMethodType;
import tim.prune.function.compress.TrackDetails;

public class TestTooFastMethod
{
	@Test
	public void testSerialize_inactive()
	{
		TooFastMethod method = new TooFastMethod(25.5);
		Assertions.assertFalse(method.isActive());
		Assertions.assertEquals(CompressionMethodType.TOO_FAST, method.getType());
		Assertions.assertEquals("oFAS:25.5", method.getTotalSettingsString());
		Assertions.assertEquals("25.5", method.getParam());
	}

	@Test
	public void testDeserialize_inactive()
	{
		String fromConfig = "oFAS:11.0";
		CompressionMethod method = CompressionMethod.fromSettingsString(fromConfig);
		Assertions.assertNotNull(method);
		Assertions.assertTrue(method instanceof TooFastMethod);
		Assertions.assertFalse(method.isActive());
		Assertions.assertEquals("oFAS:11.0", method.getTotalSettingsString());
	}

	@Test
	public void testSerialize_active()
	{
		TooFastMethod method = new TooFastMethod(5);
		method.setActive(true);
		Assertions.assertEquals("xFAS:5.0", method.getTotalSettingsString());
	}

	@Test
	public void testDeserialize_active()
	{
		String fromConfig = "xFAS:1.234";
		CompressionMethod method = CompressionMethod.fromSettingsString(fromConfig);
		Assertions.assertNotNull(method);
		Assertions.assertTrue(method instanceof TooFastMethod);
		Assertions.assertTrue(method.isActive());
		Assertions.assertEquals("xFAS:1.234", method.getTotalSettingsString());
	}

	@Test
	public void testDeserialize_fail()
	{
		Assertions.assertNull(CompressionMethod.fromSettingsString(null));
		Assertions.assertNull(CompressionMethod.fromSettingsString(""));
		Assertions.assertNull(CompressionMethod.fromSettingsString("FAS:"));
		Assertions.assertNull(CompressionMethod.fromSettingsString("FAS:12abcd"));
	}

	@Test
	public void testDeleteSome()
	{
		Track track = new Track();
		final double angleRadians = 25.0 / 180.0 * Math.PI;
		DataPoint point = new DataPoint(47.1, 8.0);
		double speed = 10.0;
		long seconds = 123456L;
		final int numPoints = 34;
		for (int i=0; i<numPoints; i++)
		{
			final double distanceRadians = Distance.convertDistanceToRadians(speed, UnitSetLibrary.UNITS_METRES);
			point = PointUtils.projectPoint(point, angleRadians, distanceRadians);
			point.setSegmentStart(i == 0);
			point.setFieldValue(Field.TIMESTAMP, "" + seconds, false);
			track.appendPoint(point);
			speed += i - i*i/21;
			seconds += 10;
		}

		TrackDetails details = new TrackDetails(track);
		MarkingData markings = new MarkingData(track);
		new TooFastMethod(30.0).compress(track, details, markings);
		Assertions.assertEquals(9, markings.getNumDeleted());
		for (int i=0; i<numPoints; i++)
		{
			boolean shouldDelete = i >= 17 && i <= 25;
			Assertions.assertEquals(shouldDelete, markings.isPointMarkedForDeletion(i), "point " + i);
			// Index 13 isn't deleted because it's a segment end (last point in track)
			Assertions.assertEquals(shouldDelete, markings.isPointMarkedForSegmentBreak(i));
		}
	}
}
