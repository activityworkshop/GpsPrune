package tim.prune.gui;

import tim.prune.data.Coordinate;

/**
 * Functions for display of coordinates in gui
 */
public abstract class CoordDisplay
{

	/**
	 * Construct an appropriate coordinate label using the selected format
	 * @param inCoordinate coordinate
	 * @param inFormat selected display format
	 * @return language-sensitive string
	 */
	public static String makeCoordinateLabel(Coordinate inCoordinate, int inFormat)
	{
		String coord = inCoordinate.output(inFormat);
		// Fix broken degree signs (due to unicode mangling)
		final char brokenDeg = 65533;
		if (coord.indexOf(brokenDeg) >= 0)
		{
			coord = coord.replaceAll(String.valueOf(brokenDeg), "\u00B0");
		}
		return restrictDP(coord);
	}


	/**
	 * Restrict the given coordinate to a limited number of decimal places for display
	 * @param inCoord coordinate string
	 * @return chopped string
	 */
	private static String restrictDP(String inCoord)
	{
		final int DECIMAL_PLACES = 7;
		if (inCoord == null) return "";
		String result = inCoord;
		final int dotPos = Math.max(inCoord.lastIndexOf('.'), inCoord.lastIndexOf(','));
		if (dotPos >= 0)
		{
			final int chopPos = dotPos + DECIMAL_PLACES;
			if (chopPos < (inCoord.length()-1))
			{
				result = inCoord.substring(0, chopPos);
				// Maybe there's an exponential in there too which needs to be appended
				int expPos = inCoord.toUpperCase().indexOf("E", chopPos);
				if (expPos > 0 && expPos < (inCoord.length()-1))
				{
					result += inCoord.substring(expPos);
				}
			}
		}
		return result;
	}

}
