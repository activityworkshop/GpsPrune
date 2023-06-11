package tim.prune.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.cmd.PointReference;
import tim.prune.cmd.RearrangePointsCmd;
import tim.prune.data.Checker;
import tim.prune.data.DataPoint;
import tim.prune.data.SortMode;
import tim.prune.data.Track;


/**
 * Class to provide the function for rearranging waypoints
 */
public class RearrangeWaypointsFunction extends RearrangeFunction
{
	/**
	 * Constructor
	 * @param inApp app object
	 */
	public RearrangeWaypointsFunction(App inApp) {
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
		// Figure out what is required from the radio buttons
		Rearrange rearrangeOption = getRearrangeOption();
		SortMode sortOption = getSortMode();

		final List<PointReference> result;
		if (rearrangeOption == Rearrange.TO_START || rearrangeOption == Rearrange.TO_END)
		{
			// Collect the waypoints to the start or end of the track
			result = collectWaypoints(rearrangeOption, sortOption);
		}
		else
		{
			// Interleave the waypoints into track order
			result = interleaveWaypoints();
		}

		if (result == null || isResultANop(result)) {
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("error.rearrange.noop"),
					I18nManager.getText("error.function.noop.title"), JOptionPane.WARNING_MESSAGE);
		}
		else
		{
			RearrangePointsCmd command = RearrangePointsCmd.from(result);
			command.setDescription(getName());
			command.setConfirmText("confirm.rearrangewaypoints");
			_app.execute(command);
		}
	}


	/**
	 * Do the collection and sorting of the waypoints
	 * @param inRearrangeOption beginning or end
	 * @param inSortOption optional sort criterion
	 * @return list of point references
	 */
	private List<PointReference> collectWaypoints(Rearrange inRearrangeOption, SortMode inSortOption)
	{
		Track track = _app.getTrackInfo().getTrack();
		final int numPoints = track.getNumPoints();
		ArrayList<PointReference> waypoints = new ArrayList<>();
		ArrayList<PointReference> nonWaypoints = new ArrayList<>();
		boolean wayAfterNon = false, nonAfterWay = false;
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = track.getPoint(i);
			PointReference pointReference = new PointReference(i,
					inSortOption == SortMode.SORTBY_NAME ? point.getWaypointName() : null,
					inSortOption == SortMode.SORTBY_TIME ? point.getTimestamp() : null);
			if (point.isWaypoint())
			{
				waypoints.add(pointReference);
				wayAfterNon |= !nonWaypoints.isEmpty();
			}
			else
			{
				nonWaypoints.add(pointReference);
				nonAfterWay |= !waypoints.isEmpty();
			}
		}

		// Exit if the data is already in the specified order
		final boolean wpsToStart = (inRearrangeOption == Rearrange.TO_START);
		final boolean doSort = (inSortOption != SortMode.DONT_SORT);
		if (waypoints.isEmpty()
			|| (wpsToStart && !wayAfterNon && nonAfterWay && !doSort)
			|| (!wpsToStart && wayAfterNon && !nonAfterWay && !doSort)
			|| inRearrangeOption == Rearrange.TO_NEAREST)
		{
			return null;
		}
		// Note: it could still be that the rearrange and sort has no effect, but we don't know yet

		if (doSort) {
			Collections.sort(waypoints);
		}

		// Combine the two lists into a single one
		List<PointReference> result = new ArrayList<>();
		if (wpsToStart)
		{
			result.addAll(waypoints);
			result.addAll(nonWaypoints);
		}
		else
		{
			result.addAll(nonWaypoints);
			result.addAll(waypoints);
		}
		return result;
	}

	/**
	 * Interleave all waypoints by each nearest track point
	 * @return list of point references
	 */
	private List<PointReference> interleaveWaypoints()
	{
		Track track = _app.getTrackInfo().getTrack();
		final int numPoints = track.getNumPoints();
		ArrayList<PointReference> result = new ArrayList<>();
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = track.getPoint(i);
			if (point.isWaypoint()) {
				result.add(new PointReference(i, track.getNearestTrackPointIndex(i)));
			}
			else {
				result.add(new PointReference(i, i));
		}
		}
		Collections.sort(result);
		return result;
	}
}
