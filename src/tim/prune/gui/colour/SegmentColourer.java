package tim.prune.gui.colour;

import java.awt.Color;

import tim.prune.config.Config;
import tim.prune.data.DataPoint;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;

/**
 * Point colourer using the segment indices
 */
public class SegmentColourer extends DiscretePointColourer
{
	/**
	 * Constructor
	 * @param inStartColour start colour of scale
	 * @param inEndColour end colour of scale
	 * @param inWrapLength number of unique colours before wrap
	 * @param inWideHues true for wide mode, false for narrow
	 */
	public SegmentColourer(Color inStartColour, Color inEndColour,
		int inWrapLength, boolean inWideHues)
	{
		super(inStartColour, inEndColour, inWrapLength, inWideHues);
	}

	/**
	 * Calculate the colours for each of the points in the given track
	 * @param inTrackInfo track info object
	 * @param inConfig config object (not needed here)
	 */
	@Override
	public void calculateColours(TrackInfo inTrackInfo, Config inConfig)
	{
		// initialise the array to the right size
		Track track = inTrackInfo == null ? null : inTrackInfo.getTrack();
		final int numPoints = track == null ? 0 : track.getNumPoints();
		init(numPoints);
		// loop over track points
		int c = -1; // first track point will increment this to 0
		for (int i=0; i<numPoints; i++)
		{
			DataPoint p = track.getPoint(i);
			if (p != null && !p.isWaypoint())
			{
				if (p.getSegmentStart() || c < 0) {
					c++;
				}
				setColour(i, c);
			}
		}
		// generate the colours needed
		generateDiscreteColours(c+1);
	}
}
