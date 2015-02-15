package tim.prune.function.sew;

import java.util.Comparator;

/**
 * Class to sort the candidates for segment splitting
 */
public class CandidateSorter implements Comparator<SplitPoint>
{
	/**
	 * Sort the objects by distance (greatest first)
	 */
	public int compare(SplitPoint inFirst, SplitPoint inSecond)
	{
		if (inFirst == null)  return 1;
		if (inSecond == null) return -1;
		// First, sort by distance
		final double dist1 = inFirst.getDistanceToPrevPoint();
		final double dist2 = inSecond.getDistanceToPrevPoint();
		if (dist1 > dist2) {
			return -1;
		}
		if (dist1 < dist2) {
			return 1;
		}
		// If the distances are identical, then just sort by point index
		return inFirst.getPointIndex() - inSecond.getPointIndex();
	}
}
