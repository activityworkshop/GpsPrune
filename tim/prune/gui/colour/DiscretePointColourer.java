package tim.prune.gui.colour;

import java.awt.Color;

/**
 * Abstract class to do the discrete colouring of points,
 * using start and end colours and a wrapping index
 */
public abstract class DiscretePointColourer extends PointColourer
{
	/** array of discrete colours to use */
	private Color[] _discreteColours = null;
	/** array of colour indexes */
	private int[] _colourIndexes = null;


	/**
	 * Constructor
	 * @param inStartColour start colour of scale
	 * @param inEndColour end colour of scale
	 * @param inMaxColours number of unique colours before wrap
	 */
	public DiscretePointColourer(Color inStartColour, Color inEndColour, int inMaxColours)
	{
		super(inStartColour, inEndColour, inMaxColours);
	}

	/** max number of colours is required here */
	public static boolean isMaxColoursRequired() {
		return true;
	}

	/**
	 * Initialise the array to the right size
	 * @param inNumPoints number of points in the track
	 */
	protected void init(int inNumPoints)
	{
		if (_colourIndexes == null || _colourIndexes.length != inNumPoints)
		{
			// Array needs to be created or resized
			if (inNumPoints > 0) {
				_colourIndexes = new int[inNumPoints];
			}
			else {
				_colourIndexes = null;
			}
		}
	}

	/**
	 * Set the colour at the given index
	 * @param inPointIndex point index
	 * @param inColourIndex index of colour to use
	 */
	protected void setColour(int inPointIndex, int inColourIndex)
	{
		if (_colourIndexes != null && _colourIndexes.length > inPointIndex && inPointIndex >= 0)
		{
			_colourIndexes[inPointIndex] = inColourIndex;
		}
	}

	/**
	 * Get the colour for the given point index
	 * @param inPointIndex index of point in track
	 * @return colour object
	 */
	public Color getColour(int inPointIndex)
	{
		if (_colourIndexes != null && _colourIndexes.length > inPointIndex && inPointIndex >= 0 && getMaxColours() > 0)
		{
			int colourIndex = _colourIndexes[inPointIndex] % getMaxColours();
			if (colourIndex >= 0 && _discreteColours != null && colourIndex < _discreteColours.length) {
				return _discreteColours[colourIndex];
			}
		}
		// not found, use default
		return super.getDefaultColour();
	}

	/**
	 * Generate the set of discrete colours to use
	 * @param inNumCategories number of different categories found in the data
	 */
	protected void generateDiscreteColours(int inNumCategories)
	{
		int maxColours = getMaxColours();
		if (maxColours <= 1) {maxColours = 2;}
		if (inNumCategories < 1) {inNumCategories = 1;}
		else if (inNumCategories > maxColours) {inNumCategories = maxColours;}

		// Use this number of categories to generate the colours
		_discreteColours = new Color[inNumCategories];
		for (int i=0; i<inNumCategories; i++) {
			_discreteColours[i] = mixColour(i, inNumCategories);
		}
	}

	/**
	 * Mix the given colours together by interpolating H,S,B values
	 * @param inIndex index from 0 to inWrap-1
	 * @param inWrap wrap length
	 * @return mixed colour
	 */
	private Color mixColour(int inIndex, int inWrap)
	{
		float fraction = inWrap < 2 ? 0.0f : (float) inIndex / (float) (inWrap - 1);
		return mixColour(fraction);
	}

	/**
	 * @param inIndex specified colour index
	 * @return precalculated colour at the given index
	 */
	protected Color getDiscreteColour(int inIndex)
	{
		if (_discreteColours == null || inIndex < 0 || getMaxColours() <= 1) {
			return getDefaultColour();
		}
		return _discreteColours[inIndex % getMaxColours()];
	}

}
