package tim.prune.function;

import java.util.Arrays;

import javax.swing.JOptionPane;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.data.Checker;
import tim.prune.data.DataPoint;
import tim.prune.data.Track;
import tim.prune.data.sort.SortMode;
import tim.prune.data.sort.WaypointComparer;
import tim.prune.undo.UndoRearrangeWaypoints;

/**
 * Class to provide the function for rearranging waypoints
 */
public class RearrangeWaypointsFunction extends RearrangeFunction
{

	/**
	 * Constructor
	 * @param inApp app object
	 */
	public RearrangeWaypointsFunction(App inApp)
	{
		super(inApp, true);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.rearrangewaypoints";
	}

	/** Get whether sorting by time is allowed or not */
	protected boolean isSortByTimeAllowed() {
		return Checker.haveWaypointsGotTimestamps(_app.getTrackInfo().getTrack());
	}

	/** Get the description key */
	public String getDescriptionKey() {
		return "dialog.rearrangewaypoints.desc";
	}

	/** Sort by name key */
	protected String getSortNameKey() {
		return "sortbyname";
	}

	/**
	 * Perform the rearrange and sort according to the radio buttons
	 */
	protected void finish()
	{
		Track track = _app.getTrackInfo().getTrack();
		// Figure out what is required from the radio buttons
		Rearrange rearrangeOption = getRearrangeOption();
		SortMode sortOption = getSortMode();

		UndoRearrangeWaypoints undo = new UndoRearrangeWaypoints(track);
		boolean success = false;
		if (rearrangeOption == Rearrange.TO_START || rearrangeOption == Rearrange.TO_END)
		{
			// Collect the waypoints to the start or end of the track
			success = collectWaypoints(rearrangeOption, sortOption);
		}
		else
		{
			// Interleave the waypoints into track order
			success = track.interleaveWaypoints();
		}
		if (success)
		{
			_app.getTrackInfo().getSelection().clearAll(); // clear selected point and range
			_app.completeFunction(undo, I18nManager.getText("confirm.rearrangewaypoints"));
		}
		else
		{
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("error.rearrange.noop"),
				I18nManager.getText("error.function.noop.title"), JOptionPane.WARNING_MESSAGE);
		}
	}


	/**
	 * Do the collection and sorting of the waypoints
	 * @param inRearrangeOption beginning or end
	 * @param inSortOption optional sort criterion
	 * @return true on success
	 */
	private boolean collectWaypoints(Rearrange inRearrangeOption, SortMode inSortOption)
	{
		// Check for mixed data, numbers of waypoints & nons
		int numWaypoints = 0, numNonWaypoints = 0;
		boolean wayAfterNon = false, nonAfterWay = false;
		Track track = _app.getTrackInfo().getTrack();
		final int numPoints = track.getNumPoints();
		DataPoint[] waypoints = new DataPoint[numPoints];
		DataPoint[] nonWaypoints = new DataPoint[numPoints];
		DataPoint point = null;
		for (int i=0; i<numPoints; i++)
		{
			point = track.getPoint(i);
			if (point.isWaypoint())
			{
				waypoints[numWaypoints] = point;
				numWaypoints++;
				wayAfterNon |= (numNonWaypoints > 0);
			}
			else
			{
				nonWaypoints[numNonWaypoints] = point;
				numNonWaypoints++;
				nonAfterWay |= (numWaypoints > 0);
			}
		}

		// Exit if the data is already in the specified order
		final boolean wpsToStart = (inRearrangeOption == Rearrange.TO_START);
		final boolean doSort = (inSortOption != SortMode.DONT_SORT);
		if (numWaypoints == 0
			|| (wpsToStart && !wayAfterNon && nonAfterWay && !doSort)
			|| (!wpsToStart && wayAfterNon && !nonAfterWay && !doSort)
			|| inRearrangeOption == Rearrange.TO_NEAREST)
		{
			return false;
		}
		// Note: it could still be that the rearrange and sort has no effect, but we don't know yet
		// Make a copy of the waypoints array first so we can compare it with after the sort
		DataPoint[] origWaypoints = new DataPoint[numPoints];
		System.arraycopy(waypoints, 0, origWaypoints, 0, numPoints);

		if (doSort && numWaypoints > 1)
		{
			// Sort the waypoints array
			WaypointComparer comparer = new WaypointComparer(inSortOption);
			Arrays.sort(waypoints, comparer);
			final boolean sortDidNothing = areArraysSame(origWaypoints, waypoints);
			if (sortDidNothing && (numNonWaypoints == 0
					|| (wpsToStart && !wayAfterNon && nonAfterWay)
					|| (!wpsToStart && wayAfterNon && !nonAfterWay)))
			{
				return false;
			}
		}

		// Copy the arrays into an array in the specified order
		DataPoint[] neworder = new DataPoint[numPoints];
		if (wpsToStart)
		{
			System.arraycopy(waypoints, 0, neworder, 0, numWaypoints);
			System.arraycopy(nonWaypoints, 0, neworder, numWaypoints, numNonWaypoints);
		}
		else
		{
			System.arraycopy(nonWaypoints, 0, neworder, 0, numNonWaypoints);
			System.arraycopy(waypoints, 0, neworder, numNonWaypoints, numWaypoints);
		}
		// Give track the new point order
		return track.replaceContents(neworder);
	}

	/**
	 * Compare two arrays of DataPoints and see if they're identical or not
	 * @param inOriginal original array of points
	 * @param inSorted array of points after sorting
	 * @return true if the two arrays have the same points in the same order
	 */
	private static boolean areArraysSame(DataPoint[] inOriginal, DataPoint[] inSorted)
	{
		if (inOriginal == null && inSorted == null) return true;  // both null
		if (inOriginal == null || inSorted == null) return false; // only one of them null
		if (inOriginal.length != inSorted.length) return false;
		// Loop over all points
		for (int i=0; i<inOriginal.length; i++)
		{
			DataPoint origPoint = inOriginal[i];
			DataPoint sortedPoint = inSorted[i];
			if ((origPoint != null || sortedPoint != null)
				&& (origPoint != sortedPoint))
			{
				return false; // points different
			}
		}
		// Must be all the same
		return true;
	}
}
