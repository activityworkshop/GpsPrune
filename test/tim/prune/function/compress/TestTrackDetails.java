package tim.prune.function.compress;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import tim.prune.data.DataPoint;
import tim.prune.data.MarkingData;
import tim.prune.data.Track;

public class TestTrackDetails
{
	@Test
	public void testSegments_plainTrack()
	{
		Track track = new Track();
		for (int i=0; i<8; i++)
		{
			DataPoint point = new DataPoint(1.0, 1.0 + 0.05 * i);
			point.setSegmentStart(i == 0 || i == 5);
			track.appendPoint(point);
		}
		// Check segment flags
		TrackDetails details = new TrackDetails(track);
		for (int i=0; i<8; i++)
		{
			Assertions.assertEquals(i == 0 || i == 5, details.isSegmentStart(i));
			Assertions.assertEquals(i == 4 || i == 7, details.isSegmentEnd(i));
		}
	}

	@Test
	public void testSegments_withWaypoint()
	{
		Track track = new Track();
		for (int i=0; i<8; i++)
		{
			DataPoint point = new DataPoint(1.0, 1.0 + 0.05 * i);
			point.setSegmentStart(i == 0 || i == 5);
			if (i == 4) {
				point.setWaypointName("waypoint");
			}
			track.appendPoint(point);
		}
		// Check segment flags
		TrackDetails details = new TrackDetails(track);
		for (int i=0; i<8; i++)
		{
			Assertions.assertEquals(i == 0 || i == 5, details.isSegmentStart(i));
			Assertions.assertEquals(i == 3 || i == 7, details.isSegmentEnd(i));
			Assertions.assertEquals(i == 4, details.isWaypoint(i));
		}
	}

	@Test
	public void testSegments_afterDeletion()
	{
		Track track = new Track();
		for (int i=0; i<8; i++)
		{
			DataPoint point = new DataPoint(1.0, 1.0 + 0.05 * i);
			point.setSegmentStart(i == 0);
			track.appendPoint(point);
		}
		// Modify the segment by deleting points 3 and 4
		MarkingData markings = new MarkingData(track);
		markings.markPointForDeletion(3, true, true);
		markings.markPointForDeletion(4, true, false);
		TrackDetails details = new TrackDetails(track);
		details.initialise();
		details = details.modifyUsingMarkings(markings);
		// Check segment flags
		for (int i=0; i<8; i++)
		{
			Assertions.assertEquals(i == 0 || i == 5, details.isSegmentStart(i));
			Assertions.assertEquals(i == 2 || i == 7, details.isSegmentEnd(i));
		}
	}
}
