package tim.prune.cmd;

import org.junit.jupiter.api.Test;
import tim.prune.data.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the deletion of all the points in the track
 */
class DeleteAllPointsTest
{
	@Test
	public void testDeleteTwoPoints()
	{
		Track track = new Track();
		TrackInfo info = new TrackInfo(track);
		track.appendPoint(new DataPoint(new Latitude("1.23"), new Longitude("2.34"), null));
		track.appendPoint(new DataPoint(new Latitude("11.23"), new Longitude("12.34"), null));
		assertEquals(2, track.getNumPoints());
		// delete all
		DeleteAllPointsCmd command = new DeleteAllPointsCmd();
		assertTrue(command.execute(info));
		assertEquals(0, track.getNumPoints());
		// undo
		assertTrue(command.getInverse().execute(info));
		assertEquals(2, track.getNumPoints());
	}
}
