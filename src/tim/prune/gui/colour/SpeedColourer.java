package tim.prune.gui.colour;

import java.awt.Color;

import tim.prune.config.Config;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;
import tim.prune.gui.profile.SpeedData;

/**
 * Colourer based on speed values
 */
public class SpeedColourer extends ProfileDataColourer
{
	/**
	 * Constructor
	 * @param inStartColour start colour
	 * @param inEndColour end colour
	 * @param inWideHues true for wide mode, false for narrow
	 */
	public SpeedColourer(Color inStartColour, Color inEndColour, boolean inWideHues)
	{
		super(inStartColour, inEndColour, inWideHues);
	}

	@Override
	public void calculateColours(TrackInfo inTrackInfo, Config inConfig)
	{
		Track track = inTrackInfo == null ? null : inTrackInfo.getTrack();
		// Calculate speed value for each point
		SpeedData data = new SpeedData(track);
		calculateColours(track, inConfig, data);
	}
}
