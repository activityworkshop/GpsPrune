package tim.prune.cmd;

import org.junit.jupiter.api.Test;
import tim.prune.data.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the deletion of a single point, together with its undo
 */
class DeletePointTest
{
	@Test
	public void testDeleteOnlyPoint()
	{
		Track track = new Track();
		TrackInfo info = new TrackInfo(track);
		track.appendPoint(new DataPoint(Latitude.make("1.23"), Longitude.make("2.34")));
		assertEquals(1, track.getNumPoints());
		// delete
		DeletePointCmd command = new DeletePointCmd(0);
		assertTrue(command.execute(info));
		assertEquals(0, track.getNumPoints());
		// undo
		assertTrue(command.getInverse().execute(info));
		assertEquals(1, track.getNumPoints());
	}

	@Test
	public void testDeleteFromMiddle()
	{
		Track track = new Track();
		TrackInfo info = new TrackInfo(track);
		final int indexToDelete = 1;
		for (int i=0; i<10; i++)
		{
			DataPoint point = new DataPoint(Latitude.make("1.23"), Longitude.make("2.34"));
			point.setSegmentStart(i == 0 || i == 7);
			point.setWaypointName(i == indexToDelete ? "removeme" : null);
			track.appendPoint(point);
		}

		Command command = new DeletePointCmd(indexToDelete);
		assertTrue(command.execute(info));
		assertEquals(9, track.getNumPoints());
		// Only the single waypoint should be deleted, so all the other points should be nameless
		for (int i=0; i<track.getNumPoints(); i++) {
			assertNull(track.getPoint(i).getWaypointName());
		}
		// undo
		assertTrue(command.getInverse().execute(info));
		assertEquals(10, track.getNumPoints());
		for (int i=0; i<track.getNumPoints(); i++) {
			String expectedName = (i == indexToDelete ? "removeme" : null);
			assertEquals(expectedName, track.getPoint(i).getWaypointName());
		}
	}

	@Test
	public void testDeleteStartOfSegment()
	{
		Track track = new Track();
		TrackInfo info = new TrackInfo(track);
		final int indexToDelete = 7;
		for (int i=0; i<10; i++)
		{
			DataPoint point = new DataPoint(Latitude.make("1.23"), Longitude.make("2.34"));
			point.setSegmentStart(i == 0 || i == indexToDelete);
			track.appendPoint(point);
		}
		assertEquals("S------S--", TrackHelper.describeSegments(track));

		// Delete the first point of the second segment
		Command command = new DeletePointCmd(indexToDelete);
		assertTrue(command.execute(info));
		assertEquals(9, track.getNumPoints());
		assertEquals("S------S-", TrackHelper.describeSegments(track));

		// undo
		assertTrue(command.getInverse().execute(info));
		assertEquals(10, track.getNumPoints());
		assertEquals("S------S--", TrackHelper.describeSegments(track));
	}

	@Test
	public void testSelectionAfterDelete()
	{
		Track track = new Track();
		TrackInfo info = new TrackInfo(track);
		for (int i=0; i<10; i++) {
			track.appendPoint(new DataPoint(Latitude.make("1.23"), Longitude.make("2.34")));
		}
		for (int delPos = 0; delPos < 9; delPos++)
		{
			info.getSelection().selectRange(3,  5);
			Command command = new DeletePointCmd(delPos);
			assertTrue(command.execute(info));
			assertEquals(9, track.getNumPoints());
			final int expectedStartPos = (delPos < 3 ? 2 : 3);
			final int expectedEndPos = (delPos <= 5 ? 4 : 5);
			assertEquals(expectedStartPos, info.getSelection().getStart(), "Delete at point index " + delPos);
			assertEquals(expectedEndPos, info.getSelection().getEnd(), "Delete at point index " + delPos);
			// undo to re-insert the deleted point
			assertTrue(command.getInverse().execute(info));
			assertEquals(10, track.getNumPoints());
			// The selection isn't correctly restored when a boundary point is deleted and re-inserted - we live with this for now
			if (delPos != 3) {
				assertEquals(3, info.getSelection().getStart(), "Undo delete at point index " + delPos); // unchanged
			}
			if (delPos != 5) {
				assertEquals(5, info.getSelection().getEnd()); // back to where it was
			}
		}
	}
}
