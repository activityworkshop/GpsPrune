package tim.prune.cmd;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.Latitude;
import tim.prune.data.Longitude;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;

/**
 * Tests for the commands which insert or append a single point,
 * and the undo of these commands
 */
class InsertSinglePointTest
{
	@Test
	public void testSingleAppend()
	{
		Track track = new Track();
		TrackInfo info = new TrackInfo(track);

		DataPoint point = new DataPoint(new Latitude("1.234"), new Longitude("2.345"), null);
		Command command = new InsertPointCmd(point, -1);
		assertTrue(command.execute(info));
		assertEquals(1, track.getNumPoints());
		// undo
		assertTrue(command.getInverse().execute(info));
		assertEquals(0, track.getNumPoints());
	}

	@Test
	public void testMultipleAppends()
	{
		Track track = new Track();
		TrackInfo info = new TrackInfo(track);
		final int numPoints = 5;
		for (int i=1; i<=numPoints; i++)
		{
			DataPoint point = new DataPoint(new Latitude(i + ".234"), new Longitude(i + ".345"), null);
			point.setFieldValue(Field.WAYPT_NAME, "Point" + i, false);
			Command appendCommand = new InsertPointCmd(point, -1);
			assertTrue(appendCommand.execute(info));
		}
		assertEquals(numPoints, track.getNumPoints());
		// Check the order of the points
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = track.getPoint(i);
			assertEquals("Point" + (i+1), point.getWaypointName());
		}
	}

	@Test
	public void testSingleInsert()
	{
		Track track = new Track();
		TrackInfo info = new TrackInfo(track);
		for (int i=0; i<10; i++) {
			track.appendPoint(new DataPoint(new Latitude("1.23"), new Longitude("2.34"), null));
		}
		// Now use a command to insert at position 4
		final int insertPosition = 4;
		DataPoint point = new DataPoint(new Latitude("5.234"), new Longitude("6.345"), null);
		point.setFieldValue(Field.WAYPT_NAME, "waypoint", false);
		Command command = new InsertPointCmd(point, insertPosition);
		assertTrue(command.execute(info));
		assertEquals(11, track.getNumPoints());
		// Check the order of the points
		for (int i=0; i<track.getNumPoints(); i++)
		{
			String expectedName = (i == insertPosition ? "waypoint" : null);
			assertEquals(expectedName, track.getPoint(i).getWaypointName());
		}
		// undo
		assertTrue(command.getInverse().execute(info));
		assertEquals(10, track.getNumPoints());
		for (int i=0; i<track.getNumPoints(); i++) {
			assertNull(track.getPoint(i).getWaypointName());
		}
	}

	@Test
	public void testSelectionAfterInsert()
	{
		Track track = new Track();
		TrackInfo info = new TrackInfo(track);
		for (int i=0; i<10; i++) {
			track.appendPoint(new DataPoint(new Latitude("1.23"), new Longitude("2.34"), null));
		}
		for (int insertPos = 0; insertPos < 9; insertPos++)
		{
			info.getSelection().selectRange(3,  5);
			DataPoint point = new DataPoint(new Latitude("5.234"), new Longitude("6.345"), null);
			Command command = new InsertPointCmd(point, insertPos);
			assertTrue(command.execute(info));
			assertEquals(11, track.getNumPoints());
			final int expectedStartPos = (insertPos <= 3 ? 4 : 3);
			final int expectedEndPos = (insertPos <= 5 ? 6 : 5);
			assertEquals(expectedStartPos, info.getSelection().getStart());
			assertEquals(expectedEndPos, info.getSelection().getEnd());
			// undo to delete the inserted point
			assertTrue(command.getInverse().execute(info));
			assertEquals(10, track.getNumPoints());
			assertEquals(3, info.getSelection().getStart()); // unchanged
			assertEquals(5, info.getSelection().getEnd()); // back to where it was
		}
	}
}

