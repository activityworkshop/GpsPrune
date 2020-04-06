package tim.prune.gui.colour;

import java.awt.Color;
import java.util.ArrayList;

import tim.prune.data.DataPoint;
import tim.prune.data.FileInfo;
import tim.prune.data.SourceInfo;
import tim.prune.data.TrackInfo;

/**
 * Colours points according to which file (or source) they came from
 */
public class FileColourer extends DiscretePointColourer
{
	/**
	 * Constructor
	 * @param inStartColour start colour of scale
	 * @param inEndColour end colour of scale
	 * @param inWrapLength number of unique colours before wrap
	 */
	public FileColourer(Color inStartColour, Color inEndColour, int inWrapLength)
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
		final int numPoints = inTrackInfo.getTrack().getNumPoints();
		init(numPoints);

		// loop over track points
		FileInfo fInfo = inTrackInfo.getFileInfo();
		ArrayList<SourceInfo> sourceList = new ArrayList<SourceInfo>();
		for (int i=0; i<numPoints; i++)
		{
			DataPoint p = inTrackInfo.getTrack().getPoint(i);
			if (p != null && !p.isWaypoint())
			{
				SourceInfo sInfo = fInfo.getSourceForPoint(p);
				// Is this info object already in the list?
				int foundIndex = -1;
				int sIndex = 0;
				for (SourceInfo si : sourceList) {
					if (si == sInfo) {
						foundIndex = sIndex;
						break;
					}
					sIndex++;
				}
				// Add source info to list
				if (foundIndex < 0)
				{
					sourceList.add(sInfo);
					foundIndex = sourceList.size()-1;
				}
				// use this foundIndex to find the colour
				setColour(i, foundIndex);
			}
		}
		// generate the colours needed
		generateDiscreteColours(sourceList.size());
	}
}
