package tim.prune.gui.colour;

import java.awt.Color;

public abstract class ContinuousPointColourer extends PointColourer
{
	/** array of colours to use */
	private Color[] _colours = null;
	/** array of scale values (0.0 to 1.0) */
	private double[] _values = null;

	/**
	 * Constructor
	 * @param inStartColour start colour
	 * @param inEndColour end colour
	 * @param inWideHues true for wide mode, false for narrow
	 */
	public ContinuousPointColourer(Color inStartColour, Color inEndColour, boolean inWideHues)
	{
		super(inStartColour, inEndColour, inWideHues);
	}

	/**
	 * Initialise the array to the right size
	 * @param inNumPoints number of points in the track
	 */
	protected void init(int inNumPoints)
	{
		if (_colours == null || _colours.length != inNumPoints)
		{
			// Arrays need to be created or resized
			if (inNumPoints > 0) {
				_colours = new Color[inNumPoints];
				_values = new double[inNumPoints];
			}
			else {
				_colours = null;
				_values = null;
			}
		}
	}

	/**
	 * Set the colour at the given index
	 * @param inPointIndex point index
	 * @param inColour colour to use, or null
	 * @param inValue value to use, or 0.0
	 */
	protected void setColour(int inPointIndex, Color inColour, double inValue)
	{
		if (_colours != null && _colours.length > inPointIndex && inPointIndex >= 0) {
			_colours[inPointIndex] = inColour;
		}
		if (_values != null && _values.length > inPointIndex && inPointIndex >= 0) {
			_values[inPointIndex] = inValue;
		}
	}

	/**
	 * Get the colour for the given point index
	 * @param inPointIndex index of point in track
	 * @return colour object
	 */
	public Color getColour(int inPointIndex)
	{
		Color colour = null;
		if (_colours != null && _colours.length > inPointIndex && inPointIndex >= 0)
		{
			colour = _colours[inPointIndex];
		}
		if (colour == null) {
			// not found, use default
			colour = super.getDefaultColour();
		}
		return colour;
	}

	/** @return the colour fraction to use for the given point */
	public double getColourIndex(int inPointIndex)
	{
		if (_values != null && _values.length > inPointIndex && inPointIndex >= 0) {
			return _values[inPointIndex];
		}
		return 0.0;
	}

	/** @return the Colour to use for the given colour fraction */
	public Color getColourForColourIndex(double inColourIndex) {
		return mixColour((float) inColourIndex);
	}

	public int getNumDistinctColours() {
		return -1;
	}
}
