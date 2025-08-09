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

public class TestNearbyDistanceMethod
{
	@Test
	public void testSerialize_inactive()
	{
		NearbyDistMethod method = new NearbyDistMethod(125.5);
		Assertions.assertFalse(method.isActive());
		Assertions.assertEquals(CompressionMethodType.NEARBY_WITH_DISTANCE, method.getType());
		Assertions.assertEquals("oNED:125.5", method.getTotalSettingsString());
		Assertions.assertEquals("125.5", method.getParam());
	}

	@Test
	public void testDeserialize_inactive()
	{
		String fromConfig = "oNED:11.0";
		CompressionMethod method = CompressionMethod.fromSettingsString(fromConfig);
		Assertions.assertNotNull(method);
		Assertions.assertTrue(method instanceof NearbyDistMethod);
		Assertions.assertFalse(method.isActive());
		Assertions.assertEquals("oNED:11.0", method.getTotalSettingsString());
	}

	@Test
	public void testSerialize_active()
	{
		NearbyDistMethod method = new NearbyDistMethod(5);
		method.setActive(true);
		Assertions.assertEquals("xNED:5.0", method.getTotalSettingsString());
	}

	@Test
	public void testDeserialize_active()
	{
		String fromConfig = "xNED:1234";
		CompressionMethod method = CompressionMethod.fromSettingsString(fromConfig);
		Assertions.assertNotNull(method);
		Assertions.assertTrue(method instanceof NearbyDistMethod);
		Assertions.assertTrue(method.isActive());
		Assertions.assertEquals("xNED:1234.0", method.getTotalSettingsString());
	}

	@Test
	public void testDeserialize_tooLong()
	{
		CompressionMethod method = CompressionMethod.fromSettingsString("NNED:881");
		Assertions.assertTrue(method instanceof NearbyDistMethod);
		Assertions.assertFalse(method.isActive());
	}

	@Test
	public void testDeserialize_fail()
	{
		Assertions.assertNull(CompressionMethod.fromSettingsString(null));
		Assertions.assertNull(CompressionMethod.fromSettingsString(""));
		Assertions.assertNull(CompressionMethod.fromSettingsString("NED:"));
		Assertions.assertNull(CompressionMethod.fromSettingsString("NED:12abcd"));
	}

	@Test
	public void testDeleteSome()
	{
		Track track = new Track();
		final double distanceRadians = Distance.convertDistanceToRadians(25.0, UnitSetLibrary.UNITS_METRES);
		final double angleRadians = 25.0 / 180.0 * Math.PI;
		DataPoint point = new DataPoint(47.1, 8.0);
		for (int i=0; i<14; i++)
		{
			point = PointUtils.projectPoint(point, angleRadians, distanceRadians);
			point.setSegmentStart(i == 0);
			track.appendPoint(point);
		}

		TrackDetails details = new TrackDetails(track);
		MarkingData markings = new MarkingData(track);
		new NearbyDistMethod(100.0).compress(track, details, markings);	// Parameter 100 metres
		Assertions.assertEquals(9, markings.getNumDeleted());
		for (int i=0; i<15; i++)
		{
			boolean shouldDelete = i == 1 || i == 2 || i == 3
					|| i == 5 || i == 6 || i == 7
					|| i == 9 || i == 10 || i == 11;
			Assertions.assertEquals(shouldDelete, markings.isPointMarkedForDeletion(i), "point " + i);
			// Index 13 isn't deleted because it's a segment end (last point in track)
			Assertions.assertFalse(markings.isPointMarkedForSegmentBreak(i));
		}
	}

	@Test
	public void testDeleteSomeButNotWaypoints()
	{
		Track track = new Track();
		final double distanceRadians = Distance.convertDistanceToRadians(25.0, UnitSetLibrary.UNITS_METRES);
		final double angleRadians = 25.0 / 180.0 * Math.PI;
		DataPoint point = new DataPoint(47.1, 8.0);
		for (int i=0; i<14; i++)
		{
			point = PointUtils.projectPoint(point, angleRadians, distanceRadians);
			point.setSegmentStart(i == 0);
			if (i == 5 || i == 10) {
				point.setWaypointName("waypoint");
			}
			track.appendPoint(point);
		}

		TrackDetails details = new TrackDetails(track);
		MarkingData markings = new MarkingData(track);
		new NearbyDistMethod(100.0).compress(track, details, markings);	// Parameter 100 metres
		Assertions.assertEquals(7, markings.getNumDeleted());
		for (int i=0; i<15; i++)
		{
			boolean shouldDelete = i == 1 || i == 2 || i == 3
					|| i == 6 || i == 7
					|| i == 9 || i == 11;
			Assertions.assertEquals(shouldDelete, markings.isPointMarkedForDeletion(i), "point " + i);
			// Indexes 5 and 10 aren't deleted because they're now waypoints
			// Index 13 isn't deleted because it's a segment end (last point in track)
			Assertions.assertFalse(markings.isPointMarkedForSegmentBreak(i));
		}
	}
}
