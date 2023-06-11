package tim.prune.function.compress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import tim.prune.data.Altitude;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.Latitude;
import tim.prune.data.Longitude;
import tim.prune.data.UnitSetLibrary;
import tim.prune.function.compress.WaypointComparer.CompareResult;

/**
 * JUnit tests for waypoint comparisons
 */
public class WaypointComparerTest
{
	@Test
	void testNonWaypoints()
	{
		DataPoint trackPoint = new DataPoint(new Latitude("12.34"), new Longitude("1.23"), null);
		DataPoint waypoint = new DataPoint(new Latitude("12.34"), new Longitude("1.23"), null);
		waypoint.setFieldValue(Field.WAYPT_NAME, "POINT", false);

		assertFalse(trackPoint.isWaypoint());
		assertTrue(waypoint.isWaypoint());
		assertEquals(CompareResult.DIFFERENT, WaypointComparer.compare(trackPoint, waypoint));
		assertEquals(CompareResult.DIFFERENT, WaypointComparer.compare(waypoint, trackPoint));

		assertEquals(CompareResult.DIFFERENT, WaypointComparer.compare(trackPoint, trackPoint));
	}

	@Test
	void testIdenticalWaypoints()
	{
		DataPoint waypoint1 = new DataPoint(new Latitude("12.34"), new Longitude("1.23"), null);
		waypoint1.setFieldValue(Field.WAYPT_NAME, "POINT", false);

		DataPoint waypoint2 = new DataPoint(new Latitude("12.34"), new Longitude("1.23"), null);
		waypoint2.setFieldValue(Field.WAYPT_NAME, "POINT", false);

		assertEquals(CompareResult.IDENTICAL, WaypointComparer.compare(waypoint1, waypoint2));
		assertEquals(CompareResult.IDENTICAL, WaypointComparer.compare(waypoint2, waypoint2));
		assertEquals(CompareResult.IDENTICAL, WaypointComparer.compare(waypoint1, waypoint1));
	}

	@Test
	void testComplementaryWaypoints()
	{
		DataPoint waypoint1 = new DataPoint(new Latitude("12.34"), new Longitude("1.23"), null);
		waypoint1.setFieldValue(Field.WAYPT_NAME, "POINT", false);
		waypoint1.setFieldValue(Field.COMMENT, "A comment", false);

		DataPoint waypoint2 = new DataPoint(new Latitude("12.34"), new Longitude("1.23"), null);
		waypoint2.setFieldValue(Field.WAYPT_NAME, "POINT", false);
		waypoint2.setFieldValue(Field.DESCRIPTION, "A description", false);

		// first has comment, second has description so neither is better
		assertEquals(CompareResult.COMPLEMENTARY, WaypointComparer.compare(waypoint1, waypoint2));
	}

	@Test
	void testDifferentWaypoints()
	{
		DataPoint waypoint1 = new DataPoint(new Latitude("12.34"), new Longitude("1.23"), null);
		waypoint1.setFieldValue(Field.WAYPT_NAME, "POINT1", false);
		waypoint1.setFieldValue(Field.COMMENT, "A comment", false);

		DataPoint waypoint2 = new DataPoint(new Latitude("12.34"), new Longitude("1.23"), null);
		waypoint2.setFieldValue(Field.WAYPT_NAME, "POINT2", false);
		waypoint2.setFieldValue(Field.COMMENT, "A comment", false);

		// name is different
		assertEquals(CompareResult.DIFFERENT, WaypointComparer.compare(waypoint1, waypoint2));

		DataPoint waypoint3 = new DataPoint(new Latitude("12.34"), new Longitude("1.23"), null);
		waypoint1.setFieldValue(Field.WAYPT_NAME, "POINT1", false);
		waypoint1.setFieldValue(Field.COMMENT, "Comment", false);

		// first has same name as third but different comment; second has different name
		assertEquals(CompareResult.DIFFERENT, WaypointComparer.compare(waypoint1, waypoint3));
		assertEquals(CompareResult.DIFFERENT, WaypointComparer.compare(waypoint3, waypoint2));
	}

	@Test
	void testBetterWaypoints()
	{
		DataPoint waypoint1 = new DataPoint(new Latitude("12.34"), new Longitude("1.23"), new Altitude(1010, UnitSetLibrary.UNITS_METRES));
		waypoint1.setFieldValue(Field.WAYPT_NAME, "POINT", false);
		waypoint1.setFieldValue(Field.COMMENT, "A comment", false);

		DataPoint waypoint2 = new DataPoint(new Latitude("12.34"), new Longitude("1.23"), null);
		waypoint2.setFieldValue(Field.WAYPT_NAME, "POINT", false);
		waypoint2.setFieldValue(Field.COMMENT, "A comment", false);

		// first has altitude as well so is better than second - order of parameters determines return value
		assertEquals(CompareResult.FIRST_BETTER, WaypointComparer.compare(waypoint1, waypoint2));
		assertEquals(CompareResult.SECOND_BETTER, WaypointComparer.compare(waypoint2, waypoint1));
	}
}
