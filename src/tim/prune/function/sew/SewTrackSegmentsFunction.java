package tim.prune.function.sew;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.cmd.PointFlag;
import tim.prune.cmd.SewSegmentsCmd;
import tim.prune.data.DataPoint;
import tim.prune.data.Track;
import tim.prune.function.Describer;
import tim.prune.gui.ProgressDialog;
import tim.prune.gui.ProgressIndicator;

/**
 * Function to sew the track segments together if possible,
 * reversing and moving as required
 */
public class SewTrackSegmentsFunction extends GenericFunction
{
	/** Cancel flag */
	private boolean _cancelled = false;

	/** Constructor */
	public SewTrackSegmentsFunction(App inApp) {
		super(inApp);
	}

	/** @return name key */
	public String getNameKey() {
		return "function.sewsegments";
	}

	/**
	 * Execute the function
	 */
	public void begin()
	{
		// Run in separate thread, with progress bar
		new Thread(this::run).start();
	}

	/**
	 * Run the function in a separate thread
	 */
	public void run()
	{
		// Make a progress bar
		ProgressDialog progressDialog = new ProgressDialog(_parentFrame, getNameKey(), null,
			() -> _cancelled = true);
		progressDialog.show();

		Track track = _app.getTrackInfo().getTrack();
		SewSegmentsCmd command = null;
		try {
			command = getSewSegmentsCommand(track, progressDialog);
			command.setDescription(getName());
			command.setConfirmText(I18nManager.getTextWithNumber("confirm.sewsegments", command.getNumJoins()));
		} catch (NothingDoneException e)
		{
			Describer describer = new Describer("error.sewsegments.nothingdone.single",
				"error.sewsegments.nothingdone");
			_app.showErrorMessageNoLookup(getNameKey(),
				describer.getDescriptionWithCount(e.numSegments));
		}
		progressDialog.close();
		if (command != null) {
			_app.execute(command);
		}
	}

	/**
	 * Prepare the command to do the actual sewing function
	 * @param inTrack track object
	 * @param inProgress progress indicator
	 * @return prepared command, or null if cancelled
	 * @throws NothingDoneException if nothing can be done
	 */
	SewSegmentsCmd getSewSegmentsCommand(Track inTrack, ProgressIndicator inProgress) throws NothingDoneException
	{
		// Build list of segments
		List<Segment> segments = buildSegmentList(inTrack);
		if (segments.size() <= 1) {
			throw new NothingDoneException(segments.size());
		}
		List<SegmentChain> chains = new LinkedList<>();
		int segmentNum = 0;
		inProgress.showProgress(1, segments.size() + 1);

		for (Segment segment : segments)
		{
			// Try to add the segment to one of the existing chains, if possible
			boolean segmentDone = false;
			for (SegmentChain chain : chains)
			{
				if (chain.append(segment)) {
					segmentDone = true;
					break;
				}
			}
			if (segmentDone)
			{
				// Now, try and merge the existing chains
				boolean merged = true;
				while (merged)
				{
					merged = false;
					for (SegmentChain chain1 : chains)
					{
						for (SegmentChain chain2 : chains)
						{
							if (chain1 == chain2) {continue;}
							if (chain1.append(chain2))
							{
								merged = true;
								chains.remove(chain2);
							}
							if (merged) {break;}
						}
						if (merged) {break;}
					}
				}
			}
			else {
				chains.add(new SegmentChain(inTrack, segment));
			}
			segmentNum++;
			inProgress.showProgress(segmentNum+1, segments.size() + 1);
			if (_cancelled) {
				break;
			}
		}

		if (_cancelled) {
			return null;
		}
		if (chains.size() == segments.size()) {
			throw new NothingDoneException(segments.size());
		}

		// Start off with just the waypoints, then add all chains
		List<Integer> pointIndexes = findWaypointIndexes(inTrack);
		for (SegmentChain chain : chains) {
			pointIndexes.addAll(chain.getPointIndexes());
		}
		// Construct the command
		return new SewSegmentsCmd(pointIndexes, getDiscardedPoints(pointIndexes, inTrack.getNumPoints()),
			getSegmentFlags(chains, inTrack));
	}

	/**
	 * Build a list of all the track segments (ignoring waypoints)
	 * @param inTrack track object
	 * @return list of track segments, in order
	 */
	private List<Segment> buildSegmentList(Track inTrack)
	{
		ArrayList<Segment> segments = new ArrayList<>();
		final int numPoints = inTrack.getNumPoints();
		int prevTrackPointIndex = -1;
		int segmentStartIndex = -1;
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = inTrack.getPoint(i);
			if (!point.isWaypoint())
			{
				if (point.getSegmentStart())
				{
					// Start of new segment - does previous one need to be saved?
					if (segmentStartIndex >= 0) {
						segments.add(new Segment(segmentStartIndex, prevTrackPointIndex));
					}
					segmentStartIndex = i;
				}
				prevTrackPointIndex = i;
			}
		}
		// Last segment
		if (segmentStartIndex >= 0 && prevTrackPointIndex >= segmentStartIndex) {
			segments.add(new Segment(segmentStartIndex, prevTrackPointIndex));
		}
		return segments;
	}

	/**
	 * Build a list of all the waypoint indexes
	 * @param inTrack track object
	 * @return list of indexes of the waypoints, in order
	 */
	private List<Integer> findWaypointIndexes(Track inTrack)
	{
		ArrayList<Integer> pointRefs = new ArrayList<>();
		final int numPoints = inTrack.getNumPoints();
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = inTrack.getPoint(i);
			if (point.isWaypoint()) {
				pointRefs.add(i);
			}
		}
		return pointRefs;
	}

	/**
	 * @param inUsedPoints list of the used point indexes
	 * @param inNumPoints total number of points before sewing
	 * @return list of the indexes of the discarded points
	 */
	private List<Integer> getDiscardedPoints(List<Integer> inUsedPoints, int inNumPoints)
	{
		boolean[] usedFlags = new boolean[inNumPoints];
		for (int i : inUsedPoints) {
			usedFlags[i] = true;
		}
		List<Integer> discardedPoints = new ArrayList<>();
		for (int i=0; i<inNumPoints; i++)
		{
			if (!usedFlags[i]) {
				discardedPoints.add(i);
			}
		}
		return discardedPoints;
	}

	private List<PointFlag> getSegmentFlags(List<SegmentChain> inChains, Track inTrack)
	{
		List<PointFlag> result = new ArrayList<PointFlag>();
		for (SegmentChain chain : inChains)
		{
			boolean firstInChain = true;
			for (int index : chain.getPointIndexes())
			{
				result.add(new PointFlag(inTrack.getPoint(index), firstInChain));
				firstInChain = false;
			}
		}
		return result;
	}
}
