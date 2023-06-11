package tim.prune.cmd;

import org.junit.jupiter.api.Test;
import tim.prune.data.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the command to append several points, together with its undo
 */
class AppendPointsTest
{
	@Test
	public void testAppendSinglePoint()
	{
		Track track = new Track();
		TrackInfo info = new TrackInfo(track);

		DataPoint point = new DataPoint(new Latitude("1.23"), new Longitude("2.34"), null);
		Command command = new AppendRangeCmd(List.of(point));
		assertTrue(command.execute(info));
		assertEquals(1, track.getNumPoints());
		// undo
		assertTrue(command.getInverse().execute(info));
		assertEquals(0, track.getNumPoints());
	}

	@Test
	public void testAppendTwoPoints()
	{
		Track track = new Track();
		TrackInfo info = new TrackInfo(track);
		DataPoint point = new DataPoint(new Latitude("1.23"), new Longitude("2.34"), null);
		point.setSegmentStart(true);
		point.setFieldValue(Field.WAYPT_NAME, "Something", false);
		track.appendPoint(point);
		DataPoint point2 = new DataPoint(new Latitude("2.23"), new Longitude("3.34"), null);
		DataPoint point3 = new DataPoint(new Latitude("3.23"), new Longitude("4.34"), null);

		Command command = new AppendRangeCmd(List.of(point2, point3));
		assertTrue(command.execute(info));
		assertEquals(3, track.getNumPoints());
		// undo
		assertTrue(command.getInverse().execute(info));
		assertEquals(1, track.getNumPoints());
		assertEquals("Something", track.getPoint(0).getWaypointName());
	}
}
