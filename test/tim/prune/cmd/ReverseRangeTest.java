package tim.prune.cmd;

import org.junit.jupiter.api.Test;
import tim.prune.data.*;
import tim.prune.function.ReverseSelectedRange;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the command to reverse a selected range
 */
class ReverseRangeTest
{
	@Test
	public void testReverseSinglePoint()
	{
		Track track = new Track();
		DataPoint point = new DataPoint(new Latitude("1.23"), new Longitude("2.34"),
				new Altitude("515", UnitSetLibrary.UNITS_METRES));
		track.appendPoint(point);

		// Reverse, should be no change
		ReverseRangeCmd command = new ReverseRangeCmd(List.of(0),
			0, 0, null);
		TrackInfo info = new TrackInfo(track);
		assertTrue(command.execute(info));
		assertEquals(1, track.getNumPoints());
		// undo
		assertTrue(command.getInverse().execute(info));
	}

	@Test
	public void testReverseThreePoints()
	{
		Track track = new Track();
		for (int i=0; i<3; i++) {
			DataPoint point = new DataPoint(new Latitude("1.23"), new Longitude("2.34"),
					new Altitude("" + (1000 + i), UnitSetLibrary.UNITS_METRES));
			track.appendPoint(point);
		}
		// check
		for (int i=0; i<3; i++) {
			assertEquals(1000 + i, track.getPoint(i).getAltitude().getMetricValue());
		}

		// Reverse and check
		ReverseRangeCmd command = new ReverseRangeCmd(List.of(2, 1, 0),
				0, 0, null);
		TrackInfo info = new TrackInfo(track);
		assertTrue(command.execute(info));
		for (int i=0; i<3; i++) {
			assertEquals(1000 + 2 - i, track.getPoint(i).getAltitude().getMetricValue());
		}
		// undo
		assertTrue(command.getInverse().execute(info));
		for (int i=0; i<3; i++) {
			assertEquals(1000 + i, track.getPoint(i).getAltitude().getMetricValue());
		}
	}

	@Test
	public void testReverseTwoWaypoints()
	{
		Track track = TrackHelper.makeTwelvePointTrack();
		track.getPoint(0).setSegmentStart(true);
		track.getPoint(5).setFieldValue(Field.WAYPT_NAME, "A", false);
		track.getPoint(6).setFieldValue(Field.WAYPT_NAME, "B", false);
		// check
		assertEquals("S----ww-----", TrackHelper.describeSegments(track));
		assertEquals("A", track.getPoint(5).getWaypointName());
		assertEquals("B", track.getPoint(6).getWaypointName());

		// Reverse and check
		ReverseRangeCmd command = new ReverseRangeCmd(List.of(0, 1, 2, 3, 4, 6, 5, 7, 8, 9, 10, 11),
				0, 0, null);
		TrackInfo info = new TrackInfo(track);
		assertTrue(command.execute(info));
		assertEquals("S----ww-----", TrackHelper.describeSegments(track));
		assertEquals("B", track.getPoint(5).getWaypointName());
		assertEquals("A", track.getPoint(6).getWaypointName());
		// undo
		assertTrue(command.getInverse().execute(info));
		assertEquals("S----ww-----", TrackHelper.describeSegments(track));
		assertEquals("A", track.getPoint(5).getWaypointName());
		assertEquals("B", track.getPoint(6).getWaypointName());
	}

	@Test
	public void testReverseWithSegment()
	{
		Track track = TrackHelper.makeTwelvePointTrack();
		track.getPoint(0).setSegmentStart(true);
		track.getPoint(5).setSegmentStart(true);
		track.getPoint(8).setSegmentStart(true);
		// check
		assertEquals("S----S--S---", TrackHelper.describeSegments(track));

		// Reverse and check
		final int startIndex = 3, endIndex = 9;
		Command command = ReverseSelectedRange.makeReverseCommand(track, startIndex, endIndex);
		TrackInfo info = new TrackInfo(track);
		assertTrue(command.execute(info));
		assertEquals("S--S-S--S-S-", TrackHelper.describeSegments(track));
		// undo
		assertTrue(command.getInverse().execute(info));
		assertEquals("S----S--S---", TrackHelper.describeSegments(track));
	}

	@Test
	public void testReverseWholeSegment()
	{
		Track track = TrackHelper.makeTwelvePointTrack();
		track.getPoint(0).setSegmentStart(true);
		track.getPoint(5).setSegmentStart(true);
		track.getPoint(8).setSegmentStart(true);
		// check
		assertEquals("S----S--S---", TrackHelper.describeSegments(track));

		// Reverse and check
		final int startIndex = 5, endIndex = 7;
		Command command = ReverseSelectedRange.makeReverseCommand(track, startIndex, endIndex);
		TrackInfo info = new TrackInfo(track);
		assertTrue(command.execute(info));
		assertEquals("S----S--S---", TrackHelper.describeSegments(track));
		// undo
		assertTrue(command.getInverse().execute(info));
		assertEquals("S----S--S---", TrackHelper.describeSegments(track));
	}

	@Test
	public void testReverseWholeTrack()
	{
		Track track = TrackHelper.makeTwelvePointTrack();
		track.getPoint(0).setSegmentStart(true);
		track.getPoint(11).setSegmentStart(true);
		track.getPoint(8).setSegmentStart(true);
		String initialState = "S-------S--S";
		assertEquals(initialState, TrackHelper.describeSegments(track));
		// Reverse whole track
		Command command = ReverseSelectedRange.makeReverseCommand(track, 0, 11);
		TrackInfo info = new TrackInfo(track);
		assertTrue(command.execute(info));
		String shiftedState = "SS--S-------";
		assertEquals(shiftedState, TrackHelper.describeSegments(track));
		// undo
		assertTrue(command.getInverse().execute(info));
		assertEquals(initialState, TrackHelper.describeSegments(track));
	}
}
