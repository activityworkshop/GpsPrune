package tim.prune.gui.colour;

import java.awt.Color;

import tim.prune.config.Config;
import tim.prune.data.TrackInfo;

/**
 * Abstract class to do the colouring of points,
 * that is holding a colour for each track point
 * in the current track
 */
public abstract class PointColourer
{
	/** default colour */
	private static final Color _defaultColour = Color.BLUE;
	/** start and end colours */
	private final Color _startColour;
	private final Color _endColour;
	/** max number of unique colours before wrapping */
	private final int _maxColours;
	/** true for wide hue scaling, false for narrow (default) */
	private final boolean _wideHues;


	/**
	 * Constructor
	 * @param inStartColour start colour
	 * @param inEndColour end colour
	 * @param inMaxColours max number of colours
	 * @param inWideHues true for wide mode, false for narrow
	 */
	public PointColourer(Color inStartColour, Color inEndColour,
		int inMaxColours, boolean inWideHues)
	{
		_startColour = inStartColour;
		_endColour = inEndColour;
		_maxColours = inMaxColours;
		_wideHues = inWideHues;
	}

	/**
	 * Constructor
	 * @param inStartColour start colour
	 * @param inEndColour end colour
	 */
	public PointColourer(Color inStartColour, Color inEndColour, boolean inWideHues) {
		this(inStartColour, inEndColour, -1, inWideHues);
	}

	/**
	 * Calculate the colours for each of the points in the given track
	 * @param inTrackInfo track info object
	 * @param inConfig config object for units and dates
	 */
	public abstract void calculateColours(TrackInfo inTrackInfo, Config inConfig);

	/**
	 * Get the colour for the given point index
	 * @param inPointIndex index of point in track
	 * @return colour object
	 */
	public Color getColour(int inPointIndex) {
		return _defaultColour;
	}

	/**
	 * @return default colour
	 */
	protected Color getDefaultColour() {
		return _defaultColour;
	}

	/**
	 * @return start colour
	 */
	protected Color getStartColour() {
		return _startColour == null ? getDefaultColour() : _startColour;
	}

	/**
	 * @return end colour
	 */
	protected Color getEndColour() {
		return _endColour == null ? getDefaultColour() : _endColour;
	}

	/**
	 * @return maximum number of colours, or -1
	 */
	protected int getMaxColours() {
		return _maxColours;
	}

	/**
	 * @return true if wide hue scaling is active, false for normal
	 */
	boolean isWideHueScaling() {
		return _wideHues;
	}

	/**
	 * @return true for discrete, false for continuous
	 */
	boolean isDiscrete() {
		return _maxColours >= 0;
	}

	/**
	 * Mix the given colours together using HSB values instead of interpolating RGB
	 * @param inFraction between 0.0 (start) and 1.0 (end)
	 * @return mixed colour
	 */
	protected Color mixColour(float inFraction)
	{
		if (_startColour == null && _endColour == null) {
			return getDefaultColour();
		}
		if (_startColour == null) {
			return _endColour;
		}
		if (_endColour == null || inFraction < 0.0 || inFraction > 1.0) {
			return _startColour;
		}

		// Convert both colours to hsb, and interpolate (using either 'wide' mode or 'narrow' mode)
		float[] startHSB = Color.RGBtoHSB(_startColour.getRed(), _startColour.getGreen(), _startColour.getBlue(), null);
		float[] endHSB = Color.RGBtoHSB(_endColour.getRed(), _endColour.getGreen(), _endColour.getBlue(), null);
		final float startHue = (startHSB[1] == 0.0 ? endHSB[0] : startHSB[0]);
		final float endHue = (endHSB[1] == 0.0 ? startHSB[0] : endHSB[0]);
		final float hue = calculateHue(startHue, endHue, inFraction, _wideHues);
		return Color.getHSBColor(hue,
			startHSB[1] + (endHSB[1]-startHSB[1]) * inFraction,
			startHSB[2] + (endHSB[2]-startHSB[2]) * inFraction);
	}

	/**
	 * Interpolate a hue value between the given start and end values
	 * @param inStartValue start hue from 0 to 1
	 * @param inEndValue end hue from 0 to 1
	 * @param inFraction fraction of interpolation, from 0 to 1
	 * @param inWide true for a 'wide' interpolation, greater than 180 degrees, false for 'narrow' less than 180
	 * @return interpolated hue value (only the fractional part is relevant)
	 */
	private static float calculateHue(float inStartValue, float inEndValue, float inFraction, boolean inWide)
	{
		final boolean isWide = Math.abs(inEndValue - inStartValue) > 0.5;
		if (inWide == isWide) {
			return interpolate(inStartValue, inEndValue, inFraction);
		}
		// Need to modify values to get the right direction
		if (inStartValue <= inEndValue) {
			return interpolate(inStartValue + 1.0f, inEndValue, inFraction);
		}
		else {
			return interpolate(inStartValue, inEndValue + 1.0f, inFraction);
		}
	}

	/** Simple interpolation between start value and end value */
	private static float interpolate(float inStartValue, float inEndValue, float inFraction) {
		return inStartValue + (inEndValue - inStartValue) * inFraction;
	}
}
