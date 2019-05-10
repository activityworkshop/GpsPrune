package tim.prune.function.compress;

import java.awt.Component;
import java.awt.event.ActionListener;

import tim.prune.data.DataPoint;
import tim.prune.data.Track;

/**
 * Algorithm for detecting close points to compress
 * Only checks distance to previous point, not any earlier point
 */
public class ClosePointsAlgorithm extends SingleParameterAlgorithm
{

	/**
	 * Constructor
	 * @param inTrack track object
	 * @param inDetails track details object
	 * @param inListener listener to attach to activation control
	 */
	public ClosePointsAlgorithm(Track inTrack, TrackDetails inDetails, ActionListener inListener)
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
		// Parse parameter
		double param = getParameter();
		// Use 1/x if x greater than 1
		if (param > 1.0) param = 1.0 / param;
		if (param <= 0.0 || param >= 1.0) {
			// Parameter isn't valid, don't delete any
			return 0;
		}
		double threshold = _trackDetails.getTrackSpan() * param;

		// Loop over all points checking distances to previous point
		// TODO: Maybe this should also check distance to _next_ point as well!
		int numPoints = _track.getNumPoints();
		int prevPointIndex = 0;
		int prevTrackPointIndex = 0;
		double pointDist = 0.0;
		int numDeleted = 0;
		for (int i=1; i<numPoints; i++)
		{
			// don't delete points already deleted
			if (!inFlags[i])
			{
				DataPoint currPoint = _track.getPoint(i);
				// Don't consider waypoints
				if (!currPoint.isWaypoint())
				{
					// Don't delete any photo points or start/end of segments
					if (!currPoint.hasMedia()
						&& !_trackDetails.isSegmentStart(i) && !_trackDetails.isSegmentEnd(i))
					{
						// Check current point against prevPoint
						pointDist = Math.abs(_track.getX(i) - _track.getX(prevPointIndex))
						 + Math.abs(_track.getY(i) - _track.getY(prevPointIndex));
						if (pointDist < threshold) {
							inFlags[i] = true;
							numDeleted++;
						}
						else if (prevTrackPointIndex != prevPointIndex)
						{
							// Check current point against prevTrackPoint
							pointDist = Math.abs(_track.getX(i) - _track.getX(prevTrackPointIndex))
							 + Math.abs(_track.getY(i) - _track.getY(prevTrackPointIndex));
							if (pointDist < threshold) {
								inFlags[i] = true;
								numDeleted++;
							}
						}
					}
					if (!inFlags[i]) {prevTrackPointIndex = i;}
				}
				if (!inFlags[i]) {prevPointIndex = i;}
			}
		}
		return numDeleted;
	}


	/**
	 * @return specific gui components for dialog
	 */
	protected Component getSpecificGuiComponents()
	{
		return getSpecificGuiComponents("dialog.compress.closepoints.paramdesc", "200");
	}

	/**
	 * @return title key for box
	 */
	protected String getTitleTextKey()
	{
		return "dialog.compress.closepoints.title";
	}

}
