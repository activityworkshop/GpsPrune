package tim.prune.gui.colour;

import java.awt.Color;

import tim.prune.config.Config;
import tim.prune.data.TrackInfo;

/** Colourer which always gives the same colour to every point */
public class StaticColourer extends PointColourer
{
	public StaticColourer(Color inColour) {
		super(inColour, inColour, 1, false);
	}

	public void calculateColours(TrackInfo inTrackInfo, Config inConfig) {
	}

	public Color getColour(int inPointIndex) {
		return getStartColour();
	}

	public double getColourIndex(int inPointIndex) {
		return 0.0;
	}

	public Color getColourForColourIndex(double inColourIndex) {
		return getStartColour();
	}

	public int getNumDistinctColours() {
		return 1;
	}
}
