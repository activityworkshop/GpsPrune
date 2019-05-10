package tim.prune.gui.colour;

import java.awt.Color;

import tim.prune.data.TrackInfo;

/**
 * Abstract class to do the colouring of points,
 * that is holding a colour for each track point
 * in the current track
 */
public abstract class PointColourer
{
	/** default colour */
	private Color _defaultColour = Color.BLUE;
	/** start and end colours */
	private Color _startColour = null, _endColour = null;
	/** max number of unique colours before wrapping */
	private int _maxColours = 1;


	/**
	 * Constructor
	 * @param inStartColour start colour
	 * @param inEndColour end colour
	 * @param inMaxColours max number of colours
	 */
	public PointColourer(Color inStartColour, Color inEndColour, int inMaxColours)
	{
		_startColour = inStartColour;
		_endColour   = inEndColour;
		_maxColours  = inMaxColours;
	}

	/**
	 * Constructor
	 * @param inStartColour start colour
	 * @param inEndColour end colour
	 */
	public PointColourer(Color inStartColour, Color inEndColour)
	{
		this(inStartColour, inEndColour, -1);
	}

	/**
	 * Calculate the colours for each of the points in the given track
	 * @param inTrackInfo track info object
	 */
	public abstract void calculateColours(TrackInfo inTrackInfo);

	/**
	 * Get the colour for the given point index
	 * @param inPointIndex index of point in track
	 * @return colour object
	 */
	public Color getColour(int inPointIndex)
	{
		return _defaultColour;
	}

	/**
	 * @param inColor default colour to use
	 */
	protected void setDefaultColour(Color inColour)
	{
		if (inColour != null) {
			_defaultColour = inColour;
		}
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
		return _startColour;
	}

	/**
	 * @return end colour
	 */
	protected Color getEndColour() {
		return _endColour;
	}

	/**
	 * @return maximum number of colours, or -1
	 */
	protected int getMaxColours() {
		return _maxColours;
	}

	/**
	 * Mix the given colours together using HSB values instead of interpolating RGB
	 * @param inFraction between 0.0 (start) and 1.0 (end)
	 * @return mixed colour
	 */
	protected Color mixColour(float inFraction)
	{
		if (_startColour == null && _endColour == null) return getDefaultColour();
		if (_startColour == null) return _endColour;
		if (_endColour == null || inFraction < 0.0 || inFraction > 1.0) return _startColour;

		// Convert both colours to hsb, and interpolate
		float[] startHSB = Color.RGBtoHSB(_startColour.getRed(), _startColour.getGreen(), _startColour.getBlue(), null);
		float[] endHSB = Color.RGBtoHSB(_endColour.getRed(), _endColour.getGreen(), _endColour.getBlue(), null);
		// Note that if end hue is less than start hue, hue will go backwards rather than forwards with wrap around 0

		return Color.getHSBColor(startHSB[0] + (endHSB[0]-startHSB[0]) * inFraction,
			startHSB[1] + (endHSB[1]-startHSB[1]) * inFraction,
			startHSB[2] + (endHSB[2]-startHSB[2]) * inFraction);
	}
}
