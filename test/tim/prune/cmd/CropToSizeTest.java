package tim.prune.cmd;

import org.junit.jupiter.api.Test;
import tim.prune.data.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the deletion of the end of a track, together with its undo
 */
class CropToSizeTest
{
	@Test
	public void testWrongParameters()
	{
		Track track = new Track();
		TrackInfo info = new TrackInfo(track);
		track.appendPoint(new DataPoint(new Latitude("1.23"), new Longitude("2.34"), null));
		assertEquals(1, track.getNumPoints());
		// try to delete -1 points, should fail
		DeleteFinalRangeCmd command = new DeleteFinalRangeCmd(-1);
		assertFalse(command.execute(info));
		assertEquals(1, track.getNumPoints());
		// try to keep 7 points, should fail
		command = new DeleteFinalRangeCmd(7);
		assertFalse(command.execute(info));
		assertEquals(1, track.getNumPoints());
	}

	@Test
	public void testDeleteOnlyPoint()
	{
		Track track = new Track();
		TrackInfo info = new TrackInfo(track);
		track.appendPoint(new DataPoint(new Latitude("1.23"), new Longitude("2.34"), null));
		assertEquals(1, track.getNumPoints());
		// delete
		DeleteFinalRangeCmd command = new DeleteFinalRangeCmd(1);
		assertTrue(command.execute(info));
		assertEquals(0, track.getNumPoints());
		// undo
		assertTrue(command.getInverse().execute(info));
		assertEquals(1, track.getNumPoints());
	}

	@Test
	public void testLastTwo()
	{
		Track track = new Track();
		TrackInfo info = new TrackInfo(track);
		for (int i=0; i<3; i++) {
			track.appendPoint(new DataPoint(new Latitude(i + ".23"), new Longitude("1" + i + ".34"), null));
		}
		assertEquals(3, track.getNumPoints());
		// delete
		DeleteFinalRangeCmd command = new DeleteFinalRangeCmd(2);
		assertTrue(command.execute(info));
		assertEquals(1, track.getNumPoints());
		// undo
		assertTrue(command.getInverse().execute(info));
		assertEquals(3, track.getNumPoints());
	}
}
