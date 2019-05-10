package tim.prune.function.sew;

import java.util.TreeSet;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.DataPoint;
import tim.prune.data.Track;
import tim.prune.function.Cancellable;
import tim.prune.gui.GenericProgressDialog;
import tim.prune.undo.UndoException;
import tim.prune.undo.UndoSewSegments;

/**
 * Function to sew the track segments together if possible,
 * reversing and moving as required
 */
public class SewTrackSegmentsFunction extends GenericFunction implements Runnable, Cancellable
{
	/** Set of sorted segment endpoints */
	private TreeSet<SegmentEnd> _nodes = null;
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
		new Thread(this).start();
	}

	/**
	 * Run the function in a separate thread
	 */
	public void run()
	{
		// Make a progress bar
		GenericProgressDialog progressDialog = new GenericProgressDialog(getNameKey(), null, _parentFrame, this);
		progressDialog.show();
		// Make an undo object to store the current points and sequence
		UndoSewSegments undo = new UndoSewSegments(_app.getTrackInfo().getTrack());

		// Make list of all the segment ends
		_nodes = buildNodeList(_app.getTrackInfo().getTrack());
		final int numNodes = (_nodes == null ? 0 : _nodes.size());
		if (numNodes < 4)
		{
			System.out.println("Can't do anything with this, not enough segments");
			progressDialog.close();
		}
		else
		{
			progressDialog.showProgress(10, 100); // Say 10% for building the nodes

			// Disable messaging because we're probably doing a lot of reverses and moves
			UpdateMessageBroker.enableMessaging(false);
			// Set now contains all pairs of segment ends, ends at the same location are adjacent
			// Now we're just interested in pairs of nodes, not three or more at the same location
			SegmentEnd firstNode = null, secondNode = null;
			int numJoins = 0, currNode = 0;
			for (SegmentEnd node : _nodes)
			{
				if (!node.isActive()) {continue;}
				if (firstNode == null)
				{
					firstNode = node;
				}
				else if (secondNode == null)
				{
					if (node.atSamePointAs(firstNode)) {
						secondNode = node;
					}
					else {
						firstNode = node;
					}
				}
				else if (node.atSamePointAs(secondNode))
				{
					// Found three colocated nodes, not interested
					firstNode = secondNode = null;
				}
				else
				{
					// Found a pair
					joinSegments(firstNode, secondNode);
					numJoins++;
					firstNode = node; secondNode = null;
				}
				if (_cancelled) {break;}
				final double fractionDone = 1.0 * currNode / numNodes;
				progressDialog.showProgress(10 + (int) (fractionDone * 80), 100);
				currNode++;
			}
			if (firstNode != null && secondNode != null)
			{
				joinSegments(firstNode, secondNode);
				numJoins++;
			}

			progressDialog.showProgress(90, 100); // Say 90%, only duplicate point deletion left

			// Delete the duplicate points
			final int numDeleted = _cancelled ? 0 : deleteSegmentStartPoints(_app.getTrackInfo().getTrack());

			progressDialog.close();
			// Enable the messaging again
			UpdateMessageBroker.enableMessaging(true);
			if (_cancelled) // TODO: Also revert if any of the operations failed
			{
				// try to restore using undo object
				try {
					undo.performUndo(_app.getTrackInfo());
				}
				catch (UndoException ue) {
					_app.showErrorMessage("oops", "CANNOT UNDO");
				}
			}
			else if (numJoins > 0 || numDeleted > 0)
			{
				// Give Undo object back to App to confirm
				final String confirmMessage = (numJoins > 0 ? I18nManager.getTextWithNumber("confirm.sewsegments", numJoins)
					: "" + numDeleted + " " + I18nManager.getText("confirm.deletepoint.multi"));
				_app.completeFunction(undo, confirmMessage);
				UpdateMessageBroker.informSubscribers();
			}
			else
			{
				// Nothing done
				_app.showErrorMessageNoLookup(getNameKey(), I18nManager.getTextWithNumber("error.sewsegments.nothingdone", numNodes/2));
			}
		}
	}

	/**
	 * Build a sorted list of all the segment start points and end points
	 * Creates a TreeSet containing two SegmentEnd objects for each segment
	 * @param inTrack track object
	 * @return sorted list of segment ends
	 */
	private static TreeSet<SegmentEnd> buildNodeList(Track inTrack)
	{
		TreeSet<SegmentEnd> nodes = new TreeSet<SegmentEnd>();
		final int numPoints = inTrack.getNumPoints();
		DataPoint prevTrackPoint = null;
		int       prevTrackPointIndex = -1;
		SegmentEnd segmentStart = null;
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = inTrack.getPoint(i);
			if (!point.isWaypoint() && !point.hasMedia())
			{
				if (point.getSegmentStart())
				{
					// Start of new segment - does previous one need to be saved?
					if (segmentStart != null && prevTrackPointIndex > 0 && prevTrackPointIndex != segmentStart.getPointIndex())
					{
						// Finish previous segment and store in list
						SegmentEnd segmentEnd = new SegmentEnd(prevTrackPoint, prevTrackPointIndex);
						segmentStart.setOtherEnd(segmentEnd);
						segmentEnd.setOtherEnd(segmentStart);
						// Don't add closed loops
						if (!segmentStart.atSamePointAs(segmentEnd))
						{
							nodes.add(segmentStart);
							nodes.add(segmentEnd);
						}
					}
					// Remember segment start
					segmentStart = new SegmentEnd(point, i);
				}
				prevTrackPoint = point;
				prevTrackPointIndex = i;
			}
		}
		// Probably need to deal with segmentStart and prevTrackPoint, prevTrackPointIndex
		if (segmentStart != null && prevTrackPointIndex > 0 && prevTrackPointIndex != segmentStart.getPointIndex())
		{
			// Finish last segment and store in list
			SegmentEnd segmentEnd = new SegmentEnd(prevTrackPoint, prevTrackPointIndex);
			segmentStart.setOtherEnd(segmentEnd);
			segmentEnd.setOtherEnd(segmentStart);
			// Don't add closed loops
			if (!segmentStart.atSamePointAs(segmentEnd))
			{
				nodes.add(segmentStart);
				nodes.add(segmentEnd);
			}
		}
		return nodes;
	}

	/**
	 * Join the two segments together represented by the given nodes
	 * @param inFirstNode first node (order doesn't matter)
	 * @param inSecondNode other node
	 */
	private void joinSegments(SegmentEnd inFirstNode, SegmentEnd inSecondNode)
	{
		final Track track = _app.getTrackInfo().getTrack();
		// System.out.println("Join: (" + inFirstNode.getPointIndex() + "-" + inFirstNode.getOtherPointIndex() + ") with ("
		//	+ inSecondNode.getPointIndex() + "-" + inSecondNode.getOtherPointIndex() + ")");
		// System.out.println("    : " + (inFirstNode.isStart() ? "start" : "end") + " to " + (inSecondNode.isStart() ? "start" : "end"));
		final boolean moveSecondBeforeFirst = inFirstNode.isStart();
		if (inFirstNode.isStart() == inSecondNode.isStart())
		{
			if (track.reverseRange(inSecondNode.getEarlierIndex(), inSecondNode.getLaterIndex()))
			{
				inSecondNode.reverseSegment();
				// System.out.println("    : Reverse segment: " + inSecondNode.getEarlierIndex() + " - " + inSecondNode.getLaterIndex());
			}
			else {
				System.err.println("Oops, reverse range didn't work");
				// TODO: Abort?
			}
		}
		if (moveSecondBeforeFirst)
		{
			if ((inSecondNode.getLaterIndex()+1) != inFirstNode.getPointIndex())
			{
				// System.out.println("    : Move second segment before first");
				cutAndMoveSegment(inSecondNode.getEarlierIndex(), inSecondNode.getLaterIndex(), inFirstNode.getPointIndex());
			}
		}
		else if ((inFirstNode.getLaterIndex()+1) != inSecondNode.getPointIndex())
		{
			// System.out.println("    : Move first segment before second (because " + (inFirstNode.getLaterIndex()+1) + " isn't " + inSecondNode.getPointIndex() + ")");
			cutAndMoveSegment(inFirstNode.getEarlierIndex(), inFirstNode.getLaterIndex(), inSecondNode.getPointIndex());
		}
		// Now merge the SegmentEnds so that they're not split up again
		if (inSecondNode.getEarlierIndex() == (inFirstNode.getLaterIndex()+1)) {
			// System.out.println("second node is now directly after the first node");
		}
		else if (inFirstNode.getEarlierIndex() == (inSecondNode.getLaterIndex()+1)) {
			//System.out.println("first node is now directly after the second node");
		}
		else {
			System.err.println("Why aren't the segments directly consecutive after the join?");
		}
		// Find the earliest and latest ends of these two segments
		SegmentEnd earlierSegmentEnd = (inFirstNode.getEarlierIndex() < inSecondNode.getEarlierIndex() ? inFirstNode : inSecondNode).getEarlierEnd();
		SegmentEnd laterSegmentEnd   = (inFirstNode.getLaterIndex() > inSecondNode.getLaterIndex() ? inFirstNode : inSecondNode).getLaterEnd();
		// Get rid of the inner two segment ends, join the earliest and latest together
		earlierSegmentEnd.getOtherEnd().deactivate();
		laterSegmentEnd.getOtherEnd().deactivate();
		earlierSegmentEnd.setOtherEnd(laterSegmentEnd);
		laterSegmentEnd.setOtherEnd(earlierSegmentEnd);
	}

	/**
	 * Cut and move the segment to a different position
	 * @param inSegmentStart start index of segment
	 * @param inSegmentEnd end index of segment
	 * @param inMoveToPos index before which the segment should be moved
	 */
	private void cutAndMoveSegment(int inSegmentStart, int inSegmentEnd, int inMoveToPos)
	{
		if (!_app.getTrackInfo().getTrack().cutAndMoveSection(inSegmentStart, inSegmentEnd, inMoveToPos))
		{
			System.err.println("   Oops, cut and move didn't work");
			// TODO: Throw exception? Return false?
		}
		else
		{
			// Loop over each node to inform it of the index changes
			for (SegmentEnd node : _nodes) {
				node.adjustPointIndex(inSegmentStart, inSegmentEnd, inMoveToPos);
			}
		}
	}

	/**
	 * The final step of the sewing, removing the duplicate points at the start of each segment
	 * @param inTrack track object
	 * @return number of points deleted
	 */
	private static int deleteSegmentStartPoints(Track inTrack)
	{
		final int numPoints = inTrack.getNumPoints();
		boolean[] deleteFlags = new boolean[numPoints];
		// Loop over points in track, setting delete flags
		int numToDelete = 0;
		DataPoint prevPoint = null;
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = inTrack.getPoint(i);
			if (!point.isWaypoint())
			{
				if (prevPoint != null && point.getSegmentStart() && point.isDuplicate(prevPoint))
				{
					deleteFlags[i] = true;
					numToDelete++;
				}
				prevPoint = point;
			}
		}
		// Make new datapoint array of the right size
		DataPoint[] pointCopies = new DataPoint[numPoints - numToDelete];
		// Loop over points again, keeping the ones we want
		int copyIndex = 0;
		for (int i=0; i<numPoints; i++)
		{
			if (!deleteFlags[i]) {
				pointCopies[copyIndex] = inTrack.getPoint(i);
				copyIndex++;
			}
		}
		// Finally, replace the copied points in the track
		inTrack.replaceContents(pointCopies);
		return numToDelete;
	}

	/** Function cancelled by progress dialog */
	public void cancel() {
		_cancelled = true;
	}
}
