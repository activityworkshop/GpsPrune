package tim.prune.cmd;

import org.junit.jupiter.api.Test;
import tim.prune.data.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

/**
 * Tests for the command to shuffle and crop the track
 */
class ShuffleAndCropTest
{
	@Test
	public void testDeleteRangeInMiddle()
	{
		// Delete points 2 and 3
		List<Integer> indexesToKeep = ListUtils.makeListOfInts(0, 1, 4, 5);
		testReduceSixPointTrack(indexesToKeep, ListUtils.makeListOfInts(2, 3));
	}

	@Test
	public void testCropToSelection()
	{
		// Just keep 3 and 4
		List<Integer> indexesToKeep = ListUtils.makeListOfInts(3, 4);
		testReduceSixPointTrack(indexesToKeep, ListUtils.makeListOfInts(0, 1, 2, 5));
	}

	@Test
	public void testCompress()
	{
		// Delete 0 and 4
		List<Integer> indexesToKeep = ListUtils.makeListOfInts(1, 2, 3, 5);
		testReduceSixPointTrack(indexesToKeep, ListUtils.makeListOfInts(0, 4));
	}

	private void testReduceSixPointTrack(List<Integer> inIndexesToKeep,
		List<Integer> inIndexesToDelete)
	{
		TrackInfo info = makeSixPointTrack();
		Track track = info.getTrack();
		Command command = new ShuffleAndCropCmd(inIndexesToKeep, inIndexesToDelete, null);
		assertTrue(command.execute(info));
		assertEquals(inIndexesToKeep.size(), track.getNumPoints());
		// check ordering
		for (int i=0; i<track.getNumPoints(); i++) {
			assertEquals("Point" + inIndexesToKeep.get(i), track.getPoint(i).getWaypointName());
		}
		// and undo it
		assertTrue(command.getInverse().execute(info));
		assertEquals(6, track.getNumPoints());
		for (int i=0; i<track.getNumPoints(); i++) {
			assertEquals("Point" + i, track.getPoint(i).getWaypointName());
		}
	}

	/**
	 * @return a track with six numbered waypoints, used to test the re-ordering
	 */
	private static TrackInfo makeSixPointTrack()
	{
		Track track = new Track();
		for (int i=0; i<6; i++)
		{
			DataPoint point = new DataPoint(Latitude.make("1.23"), Longitude.make("2.34"));
			point.setWaypointName("Point" + i);
			track.appendPoint(point);
		}
		return new TrackInfo(track);
	}
}
