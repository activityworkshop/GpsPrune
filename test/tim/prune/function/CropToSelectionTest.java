package tim.prune.function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import tim.prune.App;
import tim.prune.data.DataPoint;
import tim.prune.data.Latitude;
import tim.prune.data.Longitude;
import tim.prune.data.Track;

public class CropToSelectionTest
{
	@Test
	public void testSimpleCrop()
	{
		App app = new App(null, null);
		Track track = app.getTrackInfo().getTrack();
		for (int i=0; i<10; i++) {
			track.appendPoint(new DataPoint(Latitude.make(i + ".0"), Longitude.make((10+i) + ".5")));
		}
		track.getPoint(0).setSegmentStart(true);
		// Before the crop, point 3 has latitude 3 and is not the start of a segment
		assertEquals(3.0, track.getPoint(3).getLatitude().getDouble());
		assertFalse(track.getPoint(3).getSegmentStart());
		// Select the range 3 to 6 and crop
		app.getTrackInfo().selectPoint(3);
		app.getTrackInfo().extendSelection(6);
		CropToSelection func = new CropToSelection(app);
		func.begin();
		// Track should now have four points
		assertEquals(4, track.getNumPoints());
		// The old point 3 is now point 0, but it is now the segment start
		assertEquals(3.0, track.getPoint(0).getLatitude().getDouble());
		assertTrue(track.getPoint(0).getSegmentStart());
	}

	@Test
	public void testCropFromWaypoint()
	{
		App app = new App(null, null);
		Track track = app.getTrackInfo().getTrack();
		for (int i=0; i<10; i++) {
			track.appendPoint(new DataPoint(Latitude.make(i + ".0"), Longitude.make((10+i) + ".5")));
		}
		track.getPoint(0).setSegmentStart(true);
		track.getPoint(3).setWaypointName("waypoint");
		// Before the crop, point 3 is a waypoint and point 4 is not the start of a segment
		assertEquals(3.0, track.getPoint(3).getLatitude().getDouble());
		assertTrue(track.getPoint(3).isWaypoint());
		assertEquals(4.0, track.getPoint(4).getLatitude().getDouble());
		assertFalse(track.getPoint(4).getSegmentStart());
		// Select the range 3 to 6 and crop
		app.getTrackInfo().selectPoint(3);
		app.getTrackInfo().extendSelection(6);
		CropToSelection func = new CropToSelection(app);
		func.begin();
		// Track should now have four points
		assertEquals(4, track.getNumPoints());
		// The old point 3 is now point 0, but it is a waypoint
		assertEquals(3.0, track.getPoint(0).getLatitude().getDouble());
		assertTrue(track.getPoint(0).isWaypoint());
		// the second point is the first trackpoint
		assertEquals(4.0, track.getPoint(1).getLatitude().getDouble());
		assertTrue(track.getPoint(1).getSegmentStart());
	}

	@Test
	public void testCropOnlyWaypoints()
	{
		App app = new App(null, null);
		Track track = app.getTrackInfo().getTrack();
		for (int i=0; i<10; i++) {
			track.appendPoint(new DataPoint(Latitude.make(i + ".0"), Longitude.make((10+i) + ".5")));
		}
		track.getPoint(0).setSegmentStart(true);
		for (int i=3; i<=6; i++) {
			track.getPoint(i).setWaypointName("waypoint");
		}
		// Select the range 3 to 6 and crop
		app.getTrackInfo().selectPoint(3);
		app.getTrackInfo().extendSelection(6);
		CropToSelection func = new CropToSelection(app);
		func.begin();
		// Track should now have four points, all waypoints
		assertEquals(4, track.getNumPoints());
		for (int i=0; i<4; i++) {
			assertTrue(track.getPoint(i).isWaypoint());
		}
	}
}
