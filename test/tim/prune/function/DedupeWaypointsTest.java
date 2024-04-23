package tim.prune.function;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import tim.prune.data.Altitude;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.Latitude;
import tim.prune.data.ListUtils;
import tim.prune.data.Longitude;
import tim.prune.data.Track;
import tim.prune.data.UnitSetLibrary;
import tim.prune.function.compress.WaypointComparer.CompareResult;

/**
 * JUnit tests for the waypoint duplicate detection
 */
public class DedupeWaypointsTest
{
	@Test
	public void testShouldIgnorePoint_identicalPair()
	{
		// Make track with two identical waypoints
		Track track = new Track();
		DataPoint point1 = new DataPoint(Latitude.make("1.81"), Longitude.make("5.44"));
		point1.setWaypointName("abc");
		track.appendPoint(point1);
		DataPoint point2 = new DataPoint(Latitude.make("1.81"), Longitude.make("5.44"));
		point2.setWaypointName("abc");
		track.appendPoint(point2);

		// Shouldn't ignore first waypoint
		ArrayList<CompareResult> resultList = new ArrayList<>();
		boolean ignoreFirst = DedupeWaypointsFunction.shouldIgnorePoint(point1, new ArrayList<Integer>(), track, resultList);
		assertFalse(ignoreFirst);
		assertTrue(resultList.isEmpty());

		// Should ignore second because it's identical
		resultList.clear();
		boolean ignoreSecond = DedupeWaypointsFunction.shouldIgnorePoint(point2, ListUtils.makeListOfInts(0), track, resultList);
		assertTrue(ignoreSecond); // should ignore
		assertTrue(resultList.isEmpty());
	}

	@Test
	public void testShouldIgnorePoint_twoDifferent()
	{
		// Make track with two different waypoints
		Track track = new Track();
		DataPoint point1 = new DataPoint(Latitude.make("0.81"), Longitude.make("5.964"));
		point1.setWaypointName("abc");
		track.appendPoint(point1);
		DataPoint point2 = new DataPoint(Latitude.make("81.1"), Longitude.make("-5.44"), new Altitude(123, UnitSetLibrary.UNITS_FEET));
		point2.setWaypointName("another");
		track.appendPoint(point2);

		// Shouldn't ignore first waypoint
		ArrayList<CompareResult> resultList = new ArrayList<>();
		boolean ignoreFirst = DedupeWaypointsFunction.shouldIgnorePoint(point1, new ArrayList<Integer>(), track, resultList);
		assertFalse(ignoreFirst);
		assertTrue(resultList.isEmpty());

		// Shouldn't ignore second either because it's different
		resultList.clear();
		boolean ignoreSecond = DedupeWaypointsFunction.shouldIgnorePoint(point2, ListUtils.makeListOfInts(0), track, resultList);
		assertFalse(ignoreSecond); // should not ignore
		assertEquals(1, resultList.size());
		assertEquals(CompareResult.DIFFERENT, resultList.get(0));
	}

	@Test
	public void testShouldIgnorePoint_oneBetter()
	{
		// Make track with two different waypoints
		Track track = new Track();
		DataPoint point1 = new DataPoint(Latitude.make("22.33"), Longitude.make("10.98"));
		point1.setWaypointName("abc");
		track.appendPoint(point1);
		DataPoint point2 = new DataPoint(Latitude.make("22.33"), Longitude.make("10.98"), new Altitude(123, UnitSetLibrary.UNITS_FEET));
		point2.setWaypointName("abc");
		point2.setFieldValue(Field.DESCRIPTION, "some description", false);
		track.appendPoint(point2);

		// Shouldn't ignore second waypoint because it's better
		ArrayList<CompareResult> resultList = new ArrayList<>();
		boolean ignoreSecond = DedupeWaypointsFunction.shouldIgnorePoint(point2, ListUtils.makeListOfInts(0), track, resultList);
		assertFalse(ignoreSecond); // should not ignore
		assertEquals(1, resultList.size());
		assertEquals(CompareResult.SECOND_BETTER, resultList.get(0));

		// If it's the other way round, then the worse one should be ignored
		resultList.clear();
		boolean ignoreFirst = DedupeWaypointsFunction.shouldIgnorePoint(point1, ListUtils.makeListOfInts(1), track, resultList);
		assertTrue(ignoreFirst); // should ignore, it's worse
		assertTrue(resultList.isEmpty());
	}

	/** Second one is identical to first, so is ignored */
	@Test
	public void testReduceWaypoints_secondIsIdentical()
	{
		// Make track with two identical waypoints
		Track track = new Track();
		DataPoint point1 = new DataPoint(Latitude.make("1.81"), Longitude.make("5.44"));
		point1.setWaypointName("abc");
		track.appendPoint(point1);
		DataPoint point2 = new DataPoint(Latitude.make("1.81"), Longitude.make("5.44"));
		point2.setWaypointName("abc");
		track.appendPoint(point2);

		// add first one
		List<Integer> afterFirst = DedupeWaypointsFunction.reduceWaypoints(null, track, 0);
		assertEquals(1, afterFirst.size());
		assertEquals(0, afterFirst.get(0));
		// also gives the same if the list is empty at the start
		afterFirst = DedupeWaypointsFunction.reduceWaypoints(new ArrayList<Integer>(), track, 0);
		assertEquals(1, afterFirst.size());
		assertEquals(0, afterFirst.get(0));
		// add second one, should be ignored
		List<Integer> afterSecond = DedupeWaypointsFunction.reduceWaypoints(afterFirst, track, 1);
		assertEquals(1, afterSecond.size());
		assertEquals(0, afterSecond.get(0));
	}

	/** First one is complement of second, but third is better than both */
	@Test
	public void testReduceWaypoints_thirdIsBest()
	{
		// Make track with three waypoints
		Track track = new Track();
		DataPoint point1 = new DataPoint(Latitude.make("22.33"), Longitude.make("10.98"));
		point1.setWaypointName("abc");
		point1.setFieldValue(Field.DESCRIPTION, "some description", false);
		track.appendPoint(point1);
		DataPoint point2 = new DataPoint(Latitude.make("22.33"), Longitude.make("10.98"), new Altitude(123, UnitSetLibrary.UNITS_FEET));
		point2.setWaypointName("abc");
		track.appendPoint(point2);
		DataPoint point3 = new DataPoint(Latitude.make("22.33"), Longitude.make("10.98"), new Altitude(123, UnitSetLibrary.UNITS_FEET));
		point3.setWaypointName("abc");
		point3.setFieldValue(Field.DESCRIPTION, "some description", false);
		track.appendPoint(point3);

		// add first one
		List<Integer> afterFirst = DedupeWaypointsFunction.reduceWaypoints(null, track, 0);
		assertEquals(1, afterFirst.size());
		assertEquals(0, afterFirst.get(0));
		// now add the second
		List<Integer> afterSecond = DedupeWaypointsFunction.reduceWaypoints(afterFirst, track, 1);
		assertEquals(2, afterSecond.size());
		assertEquals(0, afterSecond.get(0));
		assertEquals(1, afterSecond.get(1));
		// and now the third => this causes the first two points to be removed and we're just left with the last one
		List<Integer> afterThird = DedupeWaypointsFunction.reduceWaypoints(afterSecond, track, 2);
		assertEquals(1, afterThird.size());
		assertEquals(2, afterThird.get(0));
	}
}
