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

public class TestNearbyFactorMethod
{
	@Test
	public void testSerialize_inactive()
	{
		NearbyFactorMethod method = new NearbyFactorMethod(2);
		Assertions.assertFalse(method.isActive());
		Assertions.assertEquals(CompressionMethodType.NEARBY_WITH_FACTOR, method.getType());
		Assertions.assertEquals("oNEF:2", method.getTotalSettingsString());
		Assertions.assertEquals("2", method.getParam());
	}

	@Test
	public void testDeserialize_inactive()
	{
		String fromConfig = "oNEF:11";
		CompressionMethod method = CompressionMethod.fromSettingsString(fromConfig);
		Assertions.assertNotNull(method);
		Assertions.assertTrue(method instanceof NearbyFactorMethod);
		Assertions.assertFalse(method.isActive());
		Assertions.assertEquals("oNEF:11", method.getTotalSettingsString());
	}

	@Test
	public void testSerialize_active()
	{
		NearbyFactorMethod method = new NearbyFactorMethod(5);
		method.setActive(true);
		Assertions.assertEquals("xNEF:5", method.getTotalSettingsString());
	}

	@Test
	public void testDeserialize_active()
	{
		String fromConfig = "xNEF:1234";
		CompressionMethod method = CompressionMethod.fromSettingsString(fromConfig);
		Assertions.assertNotNull(method);
		Assertions.assertTrue(method instanceof NearbyFactorMethod);
		Assertions.assertTrue(method.isActive());
		Assertions.assertEquals("xNEF:1234", method.getTotalSettingsString());
	}

	@Test
	public void testDeserialize_tooLong()
	{
		CompressionMethod method = CompressionMethod.fromSettingsString("NNEF:881");
		Assertions.assertTrue(method instanceof NearbyFactorMethod);
		Assertions.assertFalse(method.isActive());
	}

	@Test
	public void testDeserialize_fail()
	{
		Assertions.assertNull(CompressionMethod.fromSettingsString(null));
		Assertions.assertNull(CompressionMethod.fromSettingsString(""));
		Assertions.assertNull(CompressionMethod.fromSettingsString("NEF:"));
		Assertions.assertNull(CompressionMethod.fromSettingsString("NEF:abcd"));
	}

	@Test
	public void testDeleteSome()
	{
		Track track = new Track();
		final double angleRadians = 25.0 / 180.0 * Math.PI;
		DataPoint point = new DataPoint(47.1, 8.0);
		double speed = 10.0;
		for (int i=0; i<34; i++)
		{
			final double distanceRadians = Distance.convertDistanceToRadians(speed, UnitSetLibrary.UNITS_METRES);
			point = PointUtils.projectPoint(point, angleRadians, distanceRadians);
			point.setSegmentStart(i == 0);
			track.appendPoint(point);
			speed += i - i*i/21;
		}

		TrackDetails details = new TrackDetails(track);
		MarkingData markings = new MarkingData(track);
		new NearbyFactorMethod(50).compress(track, details, markings);	// Parameter 1/50
		Assertions.assertEquals(6, markings.getNumDeleted());
		for (int i=0; i<34; i++)
		{
			boolean shouldDelete = i == 1 || i == 2 || i == 3
					|| i == 5 || i == 7 || i == 32;
			Assertions.assertEquals(shouldDelete, markings.isPointMarkedForDeletion(i), "point " + i);
			// Index 13 isn't deleted because it's a segment end (last point in track)
			Assertions.assertFalse(markings.isPointMarkedForSegmentBreak(i));
		}
	}
}
