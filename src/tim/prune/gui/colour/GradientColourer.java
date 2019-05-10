package tim.prune.gui.colour;

import java.awt.Color;

import tim.prune.data.Track;
import tim.prune.data.TrackInfo;
import tim.prune.gui.profile.GradientData;

/**
 * Colourer based on gradient or glide slope values
 */
public class GradientColourer extends ProfileDataColourer
{
	/**
	 * Constructor
	 * @param inStartColour start colour
	 * @param inEndColour end colour
	 */
	public GradientColourer(Color inStartColour, Color inEndColour)
	{
		super(inStartColour, inEndColour);
	}

	@Override
	public void calculateColours(TrackInfo inTrackInfo)
	{
		Track track = inTrackInfo == null ? null : inTrackInfo.getTrack();
		// Calculate gradient value for each point
		GradientData data = new GradientData(track);
		calculateColours(track, data);
	}
}
