package tim.prune.function.compress;

import java.awt.Component;
import java.awt.event.ActionListener;

import tim.prune.data.DataPoint;
import tim.prune.data.Track;

/**
 * Algorithm for detecting singleton points to compress
 */
public class SingletonAlgorithm extends SingleParameterAlgorithm
{
	/**
	 * Constructor
	 * @param inTrack track object
	 * @param inDetails track details object
	 * @param inListener listener to attach to activation control
	 */
	public SingletonAlgorithm(Track inTrack, TrackDetails inDetails, ActionListener inListener)
	{
		super(inTrack, inDetails, inListener);
	}

	/**
	 * Perform the compression and work out which points should be deleted
	 * @param inFlags deletion flags from previous algorithms
	 * @return number of points deleted
	 */
	protected int compress(boolean[] inFlags)
	{
		double param = getParameter();
		if (param <= 0.0) return 0;
		// System.out.println("Singleton algorithm compressing : " + param + ", " + _trackDetails.getMeanRadians());
		int numPoints = _track.getNumPoints();
		int numDeleted = 0;
		double threshold = param * _trackDetails.getMeanRadians();
		DataPoint currPoint = null, prevPoint = null;
		// Loop over all points looking for points far away from neighbours
		for (int i=0; i<numPoints; i++)
		{
			currPoint = _track.getPoint(i);
			// Don't delete points which are already marked as deleted
			if (!inFlags[i])
			{
				// Don't delete any waypoints or photo points
				// Only interested in start and end of segments
				if (!currPoint.isWaypoint() && !currPoint.hasMedia()
					&& _trackDetails.isSegmentStart(i) && _trackDetails.isSegmentEnd(i))
				{
					// Measure distance from previous track point
					if (DataPoint.calculateRadiansBetween(prevPoint, currPoint) > threshold)
					{
						// Now need to find next track point, and measure distances
						DataPoint nextPoint = _track.getNextTrackPoint(i+1);
						if (nextPoint != null && DataPoint.calculateRadiansBetween(currPoint, nextPoint) > threshold)
						{
							// Found a point to delete (hope that next point hasn't been deleted already)
							inFlags[i] = true;
							numDeleted++;
						}
					}
				}
				// Remember last (not-deleted) track point
				if (!currPoint.isWaypoint()) {prevPoint = currPoint;}
			}
		}
		return numDeleted;
	}

	/**
	 * @return specific gui components for dialog
	 */
	protected Component getSpecificGuiComponents()
	{
		return getSpecificGuiComponents("dialog.compress.singletons.paramdesc", "2");
	}

	/**
	 * @return title key for box
	 */
	protected String getTitleTextKey()
	{
		return "dialog.compress.singletons.title";
	}
}
