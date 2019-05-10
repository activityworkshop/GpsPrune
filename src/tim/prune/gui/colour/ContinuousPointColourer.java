package tim.prune.gui.colour;

import java.awt.Color;

public abstract class ContinuousPointColourer extends PointColourer
{
	/** array of colours to use */
	private Color[] _colours = null;

	/**
	 * Constructor
	 * @param inStartColour start colour
	 * @param inEndColour end colour
	 */
	public ContinuousPointColourer(Color inStartColour, Color inEndColour)
	{
		super(inStartColour, inEndColour);
	}

	/** Continuous colourers don't need a maximum count */
	public static boolean isMaxColoursRequired() {
		return false;
	}

	/**
	 * Initialise the array to the right size
	 * @param inNumPoints number of points in the track
	 */
	protected void init(int inNumPoints)
	{
		if (_colours == null || _colours.length != inNumPoints)
		{
			// Array needs to be created or resized
			if (inNumPoints > 0) {
				_colours = new Color[inNumPoints];
			}
			else {
				_colours = null;
			}
		}
	}

	/**
	 * Set the colour at the given index
	 * @param inPointIndex point index
	 * @param inColour colour to use, or null
	 */
	protected void setColour(int inPointIndex, Color inColour)
	{
		if (_colours != null && _colours.length > inPointIndex && inPointIndex >= 0)
		{
			_colours[inPointIndex] = inColour;
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
}
