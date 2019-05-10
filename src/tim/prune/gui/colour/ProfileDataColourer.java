package tim.prune.gui.colour;

import java.awt.Color;

import tim.prune.config.Config;
import tim.prune.data.Track;
import tim.prune.gui.profile.ProfileData;

/**
 * Colourer based on speed values
 */
public abstract class ProfileDataColourer extends ContinuousPointColourer
{
	/**
	 * Constructor
	 * @param inStartColour start colour
	 * @param inEndColour end colour
	 */
	public ProfileDataColourer(Color inStartColour, Color inEndColour)
	{
		super(inStartColour, inEndColour);
	}

	/**
	 * Calculate the colours according to the track and the profile data
	 */
	public void calculateColours(Track inTrack, ProfileData inData)
	{
		final int numPoints = inTrack == null ? 0 : inTrack.getNumPoints();

		// Calculate values for each point
		inData.init(Config.getUnitSet());
		// Figure out speed range
		double minValue = inData.getMinValue();
		double maxValue = inData.getMaxValue();
		if (!inData.hasData() || (maxValue - minValue) < 0.1)
		{
			// not enough value range, set all to null
			init(0);
		}
		else
		{
			// initialise the array to the right size
			init(numPoints);
			// loop over track points to calculate colours
			for (int i=0; i<numPoints; i++)
			{
				if (inData.hasData(i))
				{
					double fraction = (inData.getData(i) - minValue) / (maxValue - minValue);
					setColour(i, mixColour((float) fraction));
				}
				else setColour(i, null);
			}
		}
	}
}
