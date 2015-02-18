package tim.prune.gui.colour;

import java.awt.Color;

import tim.prune.data.Track;
import tim.prune.data.TrackInfo;
import tim.prune.gui.profile.VerticalSpeedData;

/**
 * Colourer based on vertical speed values
 */
public class VertSpeedColourer extends ProfileDataColourer
{
	/**
	 * Constructor
	 * @param inStartColour start colour
	 * @param inEndColour end colour
	 */
	public VertSpeedColourer(Color inStartColour, Color inEndColour)
	{
		super(inStartColour, inEndColour);
	}

	@Override
	public void calculateColours(TrackInfo inTrackInfo)
	{
		Track track = inTrackInfo == null ? null : inTrackInfo.getTrack();
		// Calculate speed value for each point
		VerticalSpeedData data = new VerticalSpeedData(track);
		calculateColours(track, data);
	}
}
