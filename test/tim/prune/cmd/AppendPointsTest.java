package tim.prune.cmd;

import org.junit.jupiter.api.Test;
import tim.prune.data.*;

import java.util.ArrayList;
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

		DataPoint point = new DataPoint(Latitude.make("1.23"), Longitude.make("2.34"));
		Command command = new AppendRangeCmd(makeListOf(point));
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
		DataPoint point = new DataPoint(Latitude.make("1.23"), Longitude.make("2.34"));
		point.setSegmentStart(true);
		point.setWaypointName("Something");
		track.appendPoint(point);
		DataPoint point2 = new DataPoint(Latitude.make("2.23"), Longitude.make("3.34"));
		DataPoint point3 = new DataPoint(Latitude.make("3.23"), Longitude.make("4.34"));

		Command command = new AppendRangeCmd(makeListOf(point2, point3));
		assertTrue(command.execute(info));
		assertEquals(3, track.getNumPoints());
		// undo
		assertTrue(command.getInverse().execute(info));
		assertEquals(1, track.getNumPoints());
		assertEquals("Something", track.getPoint(0).getWaypointName());
	}

	/** Equivalent of List.of for points */
	private static List<DataPoint> makeListOf(DataPoint... inPoints)
	{
		ArrayList<DataPoint> result = new ArrayList<>();
		for (DataPoint point : inPoints) {
			result.add(point);
		}
		return result;
	}
}
