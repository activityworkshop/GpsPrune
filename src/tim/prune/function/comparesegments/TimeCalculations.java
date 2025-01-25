package tim.prune.function.comparesegments;

import java.util.List;

import tim.prune.data.DataPoint;
import tim.prune.function.PointUtils;

/** Calculations dealing with time matching between the two segments */
class TimeCalculations
{
	/** Placeholder class to get the information from a time match */
	private static class TimeResult
	{
		private final IntersectionResult _firstResult;
		private final IntersectionResult _resultBefore;
		private final IntersectionResult _resultAfter;
		private final double _fraction;

		public TimeResult(IntersectionResult inFirst, IntersectionResult inBefore,
			IntersectionResult inAfter, double inFraction)
		{
			_firstResult = inFirst;
			_resultBefore = inBefore;
			_resultAfter = inAfter;
			_fraction = inFraction;
		}
	}

	/**
	 * Find the position in the second segment which matches the time from the first segment
	 * @return the distance from the second segment (in radians), or null if no match was found
	 */
	public static Double findSecondDistanceAtSameTime(List<IntersectionResult> inResults, long inFirstSeconds)
	{
		TimeResult timeResult = findResultsAtSameTime(inResults, inFirstSeconds);
		if (timeResult == null || timeResult._fraction < 0.0 || timeResult._fraction > 1.0) {
			return null;
		}
		double distBeforeRadians = timeResult._resultBefore.getSecondDistanceRadians(timeResult._firstResult);
		double spanRadians = timeResult._resultAfter.getSecondDistanceRadians(timeResult._firstResult) - distBeforeRadians;
		if (spanRadians < 0.0) {
			return null;
		}
		return distBeforeRadians + timeResult._fraction;
	}

	/**
	 * Find the position in the second segment which matches the time from the first segment
	 * @return a point from the second segment, or null if no match was found
	 */
	public static DataPoint findSecondPointAtSameTime(List<IntersectionResult> inResults, long inFirstSeconds)
	{
		TimeResult timeResult = findResultsAtSameTime(inResults, inFirstSeconds);
		if (timeResult == null || timeResult._fraction < 0.0 || timeResult._fraction > 1.0) {
			return null;
		}
		final DataPoint pointBefore = timeResult._resultBefore.getSecondPoint();
		final DataPoint pointAfter = timeResult._resultAfter.getSecondPoint();
		return PointUtils.interpolate(pointBefore, pointAfter, timeResult._fraction);
	}

	private static TimeResult findResultsAtSameTime(List<IntersectionResult> inResults, long inFirstSeconds)
	{
		IntersectionResult firstResult = null;
		IntersectionResult latestBefore = null, earliestAfter = null;
		long secsBefore = 0L, secsAfter = 0L;
		for (IntersectionResult result : inResults)
		{
			final long duration2;
			if (firstResult == null)
			{
				firstResult = result;
				duration2 = 0L;
			}
			else {
				duration2 = result.getSecondDurationSeconds(firstResult);
			}
			if (duration2 < inFirstSeconds)
			{
				if (latestBefore == null || (inFirstSeconds - duration2) < secsBefore) {
					latestBefore = result;
					secsBefore = inFirstSeconds - duration2;
				}
			}
			else if (duration2 == inFirstSeconds) {
				return new TimeResult(firstResult, result, result, 0.0);
			}
			else
			{
				if (earliestAfter == null || (duration2 - inFirstSeconds) < secsAfter)
				{
					earliestAfter = result;
					secsAfter = duration2 - inFirstSeconds;
				}
			}
		}
		if (latestBefore == null || earliestAfter == null) {
			return null;
		}
		double distBeforeRadians = latestBefore.getSecondDistanceRadians(firstResult);
		double spanRadians = earliestAfter.getSecondDistanceRadians(firstResult) - distBeforeRadians;
		if (spanRadians < 0.0) {
			return null;
		}
		final long spanSeconds = earliestAfter.getSecondDurationSeconds(latestBefore);
		return new TimeResult(firstResult, latestBefore, earliestAfter, 1.0 * secsBefore / spanSeconds);
	}
}
