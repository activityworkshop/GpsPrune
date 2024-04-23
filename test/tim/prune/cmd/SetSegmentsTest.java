package tim.prune.cmd;

import org.junit.jupiter.api.Test;
import tim.prune.data.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the command to set the segment flags in the given range
 */
class SetSegmentsTest
{
	@Test
	public void testSetRange()
	{
		Track track = TrackHelper.makeTwelvePointTrack();
		track.getPoint(6).setWaypointName("waypoint");
		int[] segmentStarts = new int[] {0, 1, 4, 8};
		for (int s : segmentStarts) {
			track.getPoint(s).setSegmentStart(true);
		}
		String initialState = "SS--S-w-S---";
		assertEquals(initialState, TrackHelper.describeSegments(track));
		// Set
		SetSegmentsCmd command = new SetSegmentsCmd();
		for (int i=1; i<8; i++) {
			command.addSegmentFlag(track.getPoint(i), i%3 == 2);
		}
		TrackInfo info = new TrackInfo(track);
		assertTrue(command.execute(info));
		assertEquals(12, track.getNumPoints());
		// check segment flags after shift
		String newState = "S-S--Sw-S---";
		assertEquals(newState, TrackHelper.describeSegments(track));
		// undo
		assertTrue(command.getInverse().execute(info));
		assertEquals(initialState, TrackHelper.describeSegments(track));
	}
}
