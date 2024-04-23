package tim.prune.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.cmd.ShuffleAndCropCmd;
import tim.prune.data.DataPoint;
import tim.prune.data.Track;
import tim.prune.function.compress.WaypointComparer;
import tim.prune.function.compress.WaypointComparer.CompareResult;


/**
 * Class to provide the function for removing duplicate waypoints
 */
public class DedupeWaypointsFunction extends GenericFunction
{
	/**
	 * Constructor
	 * @param inApp app object
	 */
	public DedupeWaypointsFunction(App inApp) {
		super(inApp);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.dedupewaypoints";
	}

	@Override
	public void begin()
	{
		Track track = _app.getTrackInfo().getTrack();
		// Compare the waypoints with each other to find which ones should be ignored
		Map<String, List<Integer>> waypoints = new HashMap<>();
		for (int i=0; i<track.getNumPoints(); i++)
		{
			DataPoint point = track.getPoint(i);
			if (point.isWaypoint())
			{
				String key = getWaypointKey(point);
				waypoints.put(key, reduceWaypoints(waypoints.get(key), track, i));
			}
		}
		// Make lists of indexes to keep and ignore
		ArrayList<Integer> indexesToKeep = new ArrayList<>();
		ArrayList<Integer> indexesToDelete = new ArrayList<>();
		for (int i=0; i<track.getNumPoints(); i++)
		{
			DataPoint point = track.getPoint(i);
			if (!point.isWaypoint()) {
				indexesToKeep.add(i);
			}
			else
			{
				String key = getWaypointKey(point);
				List<Integer> shouldKeep = waypoints.get(key);
				if (shouldKeep.contains(i)) {
					indexesToKeep.add(i);
				}
				else {
					indexesToDelete.add(i);
				}
			}
		}
		if (indexesToDelete.isEmpty()) {
			_app.showErrorMessage(getNameKey(), "dialog.dedupewaypoints.nonefound");
		}
		else
		{
			// Use these lists to create a command
			_app.execute(new ShuffleAndCropCmd(indexesToKeep, indexesToDelete, null));
		}
	}

	/**
	 * Combine the points found so far with the newly received one
	 * @param inFoundPoints indexes of points found so far
	 * @param inTrack track to get points from
	 * @param inCurrIndex index of newly received point
	 * @return combined list of indexes
	 */
	static List<Integer> reduceWaypoints(List<Integer> inFoundPoints, Track inTrack, int inCurrIndex)
	{
		if (inFoundPoints == null || inFoundPoints.isEmpty()) {
			return List.of(inCurrIndex);
		}
		DataPoint nextPoint = inTrack.getPoint(inCurrIndex);
		ArrayList<CompareResult> compareResults = new ArrayList<>();
		if (shouldIgnorePoint(nextPoint, inFoundPoints, inTrack, compareResults)) {
			return inFoundPoints;
		}
		// Should keep this one, so make new list containing inCurrIndex but maybe removing some others
		ArrayList<Integer> newList = new ArrayList<>();
		for (int i=0; i<inFoundPoints.size(); i++)
		{
			CompareResult result = compareResults.get(i);
			if (result != CompareResult.SECOND_BETTER) {
				newList.add(inFoundPoints.get(i));
			}
		}
		newList.add(inCurrIndex);
		return newList;
	}

	/**
	 * Determine whether or not to ignore this next point
	 * @param nextPoint point to consider
	 * @param inFoundPoints indexes of points found until now
	 * @param inTrack track to get points from
	 * @param compareResults array of results to fill
	 * @return true if it's identical to or worse than any existing point, false to keep it
	 */
	static boolean shouldIgnorePoint(DataPoint nextPoint, List<Integer> inFoundPoints, Track inTrack,
			ArrayList<CompareResult> compareResults)
	{
		compareResults.clear();
		for (int pointIndex : inFoundPoints)
		{
			CompareResult result = WaypointComparer.compare(inTrack.getPoint(pointIndex), nextPoint);
			if (result == CompareResult.FIRST_BETTER || result == CompareResult.IDENTICAL) {
				return true; // ignore this point
			}
			compareResults.add(result);
		}
		return false;
	}

	/**
	 * @return key to use for referencing waypoint position and name
	 */
	private static String getWaypointKey(DataPoint inPoint)
	{
		return inPoint.getLatitude().toString() + "*" + inPoint.getLongitude().toString()
		 + "*" + inPoint.getWaypointName();
	}
}
