package tim.prune.config;

import java.awt.Color;

/**
 * Class to hold a colour scheme for Prune, including
 * colours for background, points, selections and texts
 */
public class ColourScheme
{
	// Current colours
	private Color[] _colours = new Color[NUM_COLOURS];

	// Default colours
	private static final Color[] DEFAULT_COLOURS = {Color.WHITE, Color.BLUE, Color.GREEN,
		Color.BLACK, Color.RED, Color.ORANGE, Color.BLACK, Color.GRAY};

	// Colour indices
	public static final int IDX_BACKGROUND = 0;
	public static final int IDX_POINT      = 1;
	public static final int IDX_SELECTION  = 2;
	public static final int IDX_TEXT       = 3;
	public static final int IDX_PRIMARY    = 4;
	public static final int IDX_SECONDARY  = 5;
	public static final int IDX_BORDERS    = 6;
	public static final int IDX_LINES      = 7;
	// Number of colours
	private static final int NUM_COLOURS = 8;


	/**
	 * Load the colour scheme from the given String
	 * @param inCodes comma-separated hex codes describing colours
	 */
	public void loadFromHex(String inCodes)
	{
		if (inCodes != null && inCodes.length() > 5)
		{
			String[] codes = inCodes.split(",");
			final int numCodes = (codes.length > NUM_COLOURS ? NUM_COLOURS : codes.length);
			for (int i=0; i<numCodes; i++) {
				_colours[i] = ColourUtils.colourFromHex(codes[i]);
			}
		}
	}

	/**
	 * @return colour to use for given index
	 */
	public Color getColour(int inIndex)
	{
		assert (inIndex >= 0 && inIndex < NUM_COLOURS);
		Color currColour = _colours[inIndex];
		return (currColour != null ? currColour : DEFAULT_COLOURS[inIndex]);
	}

	/**
	 * @return default colour for given index
	 */
	public static Color getDefaultColour(int inIndex)
	{
		assert (inIndex >= 0 && inIndex < NUM_COLOURS);
		return DEFAULT_COLOURS[inIndex];
	}

	/**
	 * Edit one of the colours to a new value
	 * @param inIndex index of colour
	 * @param inColour colour to set
	 */
	public void setColour(int inIndex, Color inColour)
	{
		assert (inIndex >= 0 && inIndex < NUM_COLOURS);
		_colours[inIndex] = inColour;
	}

	/**
	 * @return colour scheme as string of concatenated hex codes
	 */
	public String toString()
	{
		StringBuffer buff = new StringBuffer();
		for (int i=0; i<NUM_COLOURS; i++) {
			buff.append(ColourUtils.makeHexCode(getColour(i)));
			buff.append(',');
		}
		return buff.toString();
	}
}
