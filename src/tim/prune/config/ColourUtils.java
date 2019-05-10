package tim.prune.config;

import java.awt.Color;

/**
 * Class to hold static methods for handling colours
 * including converting to and from hex code Strings
 */
public abstract class ColourUtils
{
	/**
	 * Convert a string into a Color object
	 * @param inValue 6-character hex code
	 * @return corresponding colour
	 */
	public static Color colourFromHex(String inValue)
	{
		Color retVal = null;
		if (inValue != null && inValue.length() == 6)
		{
			try
			{
				final int redness = convertToInt(inValue.substring(0, 2));
				final int greenness = convertToInt(inValue.substring(2, 4));
				final int blueness = convertToInt(inValue.substring(4, 6));
				retVal = new Color(redness, greenness, blueness);
			}
			catch (NumberFormatException nfe) {} // colour stays null
		}
		return retVal;
	}

	/**
	 * @param inPair two-digit String representing hex code
	 * @return corresponding integer (0 to 255)
	 */
	private static int convertToInt(String inPair)
	{
		int val = Integer.parseInt(inPair, 16);
		if (val < 0) val = 0;
		return val;
	}

	/**
	 * Make a hex code string for the given colour
	 * @param inColour colour
	 * @return 6-character hex code
	 */
	public static String makeHexCode(Color inColour)
	{
		return convertToHex(inColour.getRed()) + convertToHex(inColour.getGreen()) + convertToHex(inColour.getBlue());
	}

	/**
	 * @param inValue integer value from 0 to 255
	 * @return two-character hex code
	 */
	private static String convertToHex(int inValue)
	{
		// Uses lower case a-f
		String code = Integer.toHexString(inValue);
		// Pad with leading 0 if necessary
		return (inValue < 16 ? "0" + code : code);
	}
}
