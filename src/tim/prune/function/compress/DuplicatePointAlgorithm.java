package tim.prune.function.compress;

import java.awt.Component;
import java.awt.event.ActionListener;

import tim.prune.data.DataPoint;
import tim.prune.data.Track;

/**
 * Algorithm for detecting duplicate points to compress
 */
public class DuplicatePointAlgorithm extends CompressionAlgorithm
{
	/** Number of points before this one to consider as duplicates */
	private static final int NUM_POINTS_TO_BACKTRACK = 20;

	/**
	 * Constructor
	 * @param inTrack track object
	 * @param inDetails track details object
	 * @param inListener listener to attach to activation control
	 */
	public DuplicatePointAlgorithm(Track inTrack, TrackDetails inDetails, ActionListener inListener)
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
		int numPoints = _track.getNumPoints();
		int numDeleted = 0;
		// Loop over all points looking for duplicates
		for (int i=1; i<numPoints; i++)
		{
			// Don't delete points which are already marked as deleted
			if (!inFlags[i])
			{
				DataPoint currPoint = _track.getPoint(i);
				// Don't delete any photo points or audio points
				if (!currPoint.hasMedia())
				{
					// loop over last few points before this one
					for (int j=i-NUM_POINTS_TO_BACKTRACK; j<i; j++)
					{
						if (j<0) {j=0;} // only look at last few points, but not before 0
						if (!inFlags[j] && currPoint.isDuplicate(_track.getPoint(j)))
						{
							inFlags[i] = true;
							numDeleted++;
							break;
						}
					}
				}
			}
		}
		return numDeleted;
	}


	/**
	 * @return specific gui components for dialog
	 */
	protected Component getSpecificGuiComponents()
	{
		// no parameters to set, so no gui components
		return null;
	}

	/**
	 * @return title key for box
	 */
	protected String getTitleTextKey()
	{
		return "dialog.compress.duplicates.title";
	}

}
