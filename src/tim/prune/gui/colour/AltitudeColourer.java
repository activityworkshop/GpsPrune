package tim.prune.gui.colour;

import java.awt.Color;

import tim.prune.data.DataPoint;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;

/**
 * Colourer based on altitude values
 */
public class AltitudeColourer extends ContinuousPointColourer
{
	/**
	 * Constructor
	 * @param inStartColour start colour
	 * @param inEndColour end colour
	 */
	public AltitudeColourer(Color inStartColour, Color inEndColour)
	{
		super(inStartColour, inEndColour);
	}

	@Override
	public void calculateColours(TrackInfo inTrackInfo)
	{
		Track track = inTrackInfo == null ? null : inTrackInfo.getTrack();
		final int numPoints = track == null ? 0 : track.getNumPoints();
		DataPoint point = null;

		// Figure out altitude range
		double minAltitude = 0.0;
		double maxAltitude = 0.0;
		boolean altFound = false;
		for (int i=0; i<numPoints; i++)
		{
			point = track.getPoint(i);
			if (point != null && point.hasAltitude())
			{
				double altValue = point.getAltitude().getMetricValue();
				if (altValue < minAltitude || !altFound) {minAltitude = altValue;}
				if (altValue > maxAltitude || !altFound) {maxAltitude = altValue;}
				altFound = true;
			}
		}

		if ((maxAltitude - minAltitude) < 1.0)
		{
			// not enough altitude range, set all to null
			init(0);
		}
		else
		{
			// initialise the array to the right size
			init(numPoints);
			// loop over track points to calculate colours
			for (int i=0; i<numPoints; i++)
			{
				point = track.getPoint(i);
				if (point != null && point.hasAltitude() && !point.isWaypoint())
				{
					double altValue = point.getAltitude().getMetricValue();
					double fraction = (altValue - minAltitude) / (maxAltitude - minAltitude);
					setColour(i, mixColour((float) fraction));
				}
				else setColour(i, null);
			}
		}
	}
}
