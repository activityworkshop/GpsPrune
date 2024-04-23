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
		track.appendPoint(new DataPoint(Latitude.make("1.23"), Longitude.make("2.34")));
		track.appendPoint(new DataPoint(Latitude.make("11.23"), Longitude.make("12.34")));
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
