package tim.prune.gui.colour;

import java.awt.Color;

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
	 */
	public SegmentColourer(Color inStartColour, Color inEndColour, int inWrapLength)
	{
		super(inStartColour, inEndColour, inWrapLength);
	}

	/**
	 * Calculate the colours for each of the points in the given track
	 * @param inTrackInfo track info object
	 */
	@Override
	public void calculateColours(TrackInfo inTrackInfo)
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
				if (p.getSegmentStart()) {
					c++;
				}
				setColour(i, c);
			}
		}
		// generate the colours needed
		generateDiscreteColours(c+1);
	}
}
