package tim.prune.function.compress;

import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.FieldList;

/**
 * Comparison of waypoints for duplicate identification
 */
public class WaypointComparer
{
	/** Possible results of comparison */
	public enum CompareResult {
		IDENTICAL, FIRST_BETTER, SECOND_BETTER, COMPLEMENTARY, DIFFERENT
	}

	/**
	 * Compare two waypoints
	 * @param inFirst first waypoint
	 * @param inSecond second waypoint
	 * @return enum value to describe if the points are identical / complementary or if one is better
	 */
	public static CompareResult compare(DataPoint inFirst, DataPoint inSecond)
	{
		if (!inFirst.isWaypoint() || !inSecond.isWaypoint()
				|| !inFirst.getWaypointName().equals(inSecond.getWaypointName()))
		{
			// Comparing waypoint with non-waypoint or differently named waypoints
			return CompareResult.DIFFERENT;
		}
		boolean firstHasExtraField = false, secondHasExtraField = false;

		FieldList allFields = inFirst.getFieldList().merge(inSecond.getFieldList());
		for (int f=0; f<allFields.getNumFields(); f++)
		{
			Field field = allFields.getField(f);
			String firstValue = inFirst.getFieldValue(field);
			String secondValue = inSecond.getFieldValue(field);
			boolean hasFirst = hasValue(firstValue);
			boolean hasSecond = hasValue(secondValue);
			if (hasFirst && !hasSecond) {
				firstHasExtraField = true;
			}
			else if (!hasFirst && hasSecond) {
				secondHasExtraField = true;
			}
			else if (hasFirst && hasSecond
					&& !firstValue.equals(secondValue)) {
				return CompareResult.DIFFERENT;
			}
		}

		// Now check which point has more fields
		if (!firstHasExtraField && !secondHasExtraField) {
			return CompareResult.IDENTICAL;
		}
		if (firstHasExtraField && !secondHasExtraField) {
			return CompareResult.FIRST_BETTER;
		}
		if (!firstHasExtraField && secondHasExtraField) {
			return CompareResult.SECOND_BETTER;
		}
		return CompareResult.COMPLEMENTARY; // both have fields which the other doesn't
	}

	/**
	 * @return true if the given string has something in it
	 */
	private static boolean hasValue(String inValue) {
		return inValue != null && !inValue.isEmpty();
	}
}
