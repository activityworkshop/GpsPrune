package tim.prune.function.olc;

class ParseException extends Exception {}

/**
 * Pair of coordinates
 */
class CoordPair
{
	/** Alphabet of allowed characters */
	private static final String ALPHABET = "23456789CFGHJMPQRVWX";

	public double lat = 0.0;
	public double lon = 0.0;

	/** Constructor */
	public CoordPair(double inLat, double inLon)
	{
		lat = inLat;
		lon = inLon;
	}

	/** Constant pair to represent padding */
	public static CoordPair PADDING = new CoordPair(-1.0, -1.0);

	/**
	 * Try to parse the given pair of characters into a CoordPair
	 * @param inFirst first character of pair
	 * @param inSecond second character of pair
	 * @return CoordPair from (0, 0) to (19/20, 19/20)
	 * @throws ParseException
	 */
	public static CoordPair decode(char inFirst, char inSecond) throws ParseException
	{
		final boolean isFirstPadding = (inFirst == '0');
		final boolean isSecondPadding = (inSecond == '0');
		if (isFirstPadding && isSecondPadding) {return CoordPair.PADDING;}
		if (isFirstPadding || isSecondPadding) {throw new ParseException();}
		// Try to turn these characters into numbers
		final double lat = decodeChar(inFirst);
		final double lon = decodeChar(inSecond);
		return new CoordPair(lat / 20.0, lon / 20.0);
	}

	/**
	 * Try to parse the given single character into a CoordPair
	 * @param inChar single character from level 11
	 * @return CoordPair from (0, 0) to (19/20, 19/20)
	 * @throws ParseException
	 */
	public static CoordPair decode(char inChar) throws ParseException
	{
		// Try to turn this character into a number
		final int charIndex = decodeChar(inChar);
		final int lat = charIndex / 4;
		final int lon = charIndex % 4;
		return new CoordPair(lat / 5.0, lon / 4.0);
	}

	/**
	 * Get the index from the given character
	 * @param inChar character from OLC
	 * @return index from 0 to 19
	 * @throws ParseException if character not found
	 */
	private static int decodeChar(char inChar) throws ParseException
	{
		final int index = ALPHABET.indexOf(inChar);
		if (index < 0)
		{
			throw new ParseException();
		}
		return index;
	}
}
