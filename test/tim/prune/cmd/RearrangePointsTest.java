package tim.prune.cmd;

import org.junit.jupiter.api.Test;
import tim.prune.data.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the command to rearrange all the points in the track
 */
class RearrangePointsTest
{
	@Test
	public void testMoveTwo()
	{
		TrackInfo info = makeSixPointTrack();
		Track track = info.getTrack();
		// Now do a reshuffle to get the following sequence
		List<Integer> desiredOrder = List.of(0, 3, 4, 1, 2, 5);
		Command command = new RearrangePointsCmd(desiredOrder);
		assertTrue(command.execute(info));
		assertEquals(6, track.getNumPoints());
		// check ordering
		for (int i=0; i<track.getNumPoints(); i++) {
			assertEquals("Point" + desiredOrder.get(i), track.getPoint(i).getWaypointName());
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

	@Test
	public void testRearrangeWithDuplicateIndexesFails()
	{
		// A reshuffle with duplicate indexes should fail
		testExpectedFail(List.of(0, 3, 4, 1, 3, 5));
	}

	@Test
	public void testRearrangeWithSmallerSizeFails()
	{
		// A reshuffle with a smaller number of indexes should fail
		testExpectedFail(List.of(0, 3, 4, 1, 2));
	}

	@Test
	public void testRearrangeWithNegativeIndexFails() {
		testExpectedFail(List.of(0, 3, 4, 1, -2, 5));
	}

	@Test
	public void testRearrangeWithTooLargeIndexFails() {
		testExpectedFail(List.of(0, 3, 4, 1, 12, 5));
	}

	public void testExpectedFail(List<Integer> inIndexes)
	{
		TrackInfo info = makeSixPointTrack();
		Command command = new RearrangePointsCmd(inIndexes);
		assertFalse(command.execute(info));
		// check that the point ordering is unchanged from before (command failed and did nothing)
		for (int i=0; i<info.getTrack().getNumPoints(); i++) {
			assertEquals("Point" + i, info.getTrack().getPoint(i).getWaypointName());
		}
	}
}
