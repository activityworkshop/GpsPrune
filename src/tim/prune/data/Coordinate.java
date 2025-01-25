package tim.prune.data;

import java.text.DecimalFormat;
import java.text.NumberFormat;


/**
 * Class to represent a lat/long coordinate
 * and provide conversion functions
 */
public class Coordinate
{
	private static final int NUMDIGITS_DEFAULT = -1;
	private static final CoordFormatters _coordFormatters = new CoordFormatters();

	public enum Format {DEG_MIN_SEC, DEG_MIN, DEG, DEG_WITHOUT_CARDINAL, DEG_MIN_SEC_WITH_SPACES, JUST_CARDINAL, DECIMAL_FORCE_POINT, NONE}

	public enum Cardinal
	{
		NORTH("N"), EAST("E"), SOUTH("S"), WEST("W"), NO_CARDINAL("");
		public final String printable;
		Cardinal(String inChar) {
			printable = inChar;
		}
		public Cardinal getOpposite()
		{
			switch (this)
			{
				case NORTH: return SOUTH;
				case SOUTH: return NORTH;
				case EAST: return WEST;
				case WEST: return EAST;
				case NO_CARDINAL:
				default:
					throw new IllegalArgumentException("Cannot find opposite cardinal of " + this);
			}
		}
	}

	// Instance variables
	private final Cardinal _cardinal;
	private final FractionalSeconds _value;
	private final String _originalString;
	private final Format _originalFormat;
	private final double _asDouble;

	private static char _localDecimalChar = 0;


	/** Private constructor */
	private Coordinate(Cardinal inCardinal, String inString, Format inFormat, double inDouble, FractionalSeconds inValue)
	{
		_cardinal = inCardinal;
		_originalString = inString;
		_originalFormat = inFormat;
		_asDouble = inDouble;
		_value = inValue;
	}

	/**
	 * Constructor given String
	 * @param inString string to parse
	 * @param inPositiveCardinal default cardinal to use if value is positive
	 * @param inNegativeCardinal default cardinal to use if value is negative
	 */
	public static Coordinate parse(String inString, Cardinal inPositiveCardinal, Cardinal inNegativeCardinal)
	{
		final String source = (inString == null ? "" : inString.trim());
		final int strLen = source.length();
		if (strLen == 0) {
			return null;
		}
		// Check for cardinal character either at beginning or end
		Cardinal cardinal = getCardinal(source, inPositiveCardinal, inNegativeCardinal);
		final boolean cardinalSpecified = (cardinal != Cardinal.NO_CARDINAL);

		// count numeric fields - 1=d, 2=dm, 3=dm.m/dms, 4=dms.s
		long[] fields = new long[4]; // needs to be long for lengthy decimals
		int[] lengths = new int[4];
		boolean[] otherDelims = new boolean[5]; // remember whether delimiters have non-decimal chars
		ParseResult result = parseString(source, fields, lengths, otherDelims);
		final int numFields = result.numFields;
		final boolean isNegative = result.isNegative && cardinal == Cardinal.NO_CARDINAL;
		if (!cardinalSpecified) {
			cardinal = isNegative ? inNegativeCardinal : inPositiveCardinal;
		}

		// parse fields according to number found
		final FractionalSeconds seconds;
		final Format originalFormat;
		if (numFields == 2)
		{
			if (!otherDelims[1])
			{
				// String is just decimal degrees
				originalFormat = cardinalSpecified ? Format.DEG : Format.DEG_WITHOUT_CARDINAL;
				seconds = new FractionalSeconds(fields[0], fields[1], lengths[1]);
			}
			else
			{
				// String is degrees and minutes (due to non-decimal separator)
				originalFormat = Format.DEG_MIN;
				if (fields[1] >= 60) {
					return null;
				}
				seconds = new FractionalSeconds(fields[0], fields[1], 0L, 0);
			}
		}
		// Check for exponential degrees like 1.3E-6
		else if (numFields == 3 && !otherDelims[1] && otherDelims[2] && isJustNumber(source))
		{
			double asDouble = Math.abs(Double.parseDouble(source)); // must succeed if isJustNumber has given true
			// Don't need to modify sign of double, because it will be done by the parseDouble
			return new Coordinate(cardinal, source, Format.DEG, asDouble, null);
		}
		// Differentiate between d-m.f and d-m-s using . or ,
		else if (numFields == 3 && !otherDelims[2])
		{
			// String is degrees-minutes.fractions
			if (fields[1] >= 60) {
				return null;
			}
			originalFormat = Format.DEG_MIN;
			seconds = new FractionalSeconds(fields[0], fields[1], fields[2], lengths[2]);
		}
		else if (numFields == 4 || numFields == 3)
		{
			// String is degrees-minutes-seconds.fractions
			if (fields[1] >= 60 || fields[2] >= 60) {
				return null;
			}
			originalFormat = Format.DEG_MIN_SEC;
			seconds = new FractionalSeconds(fields[0], fields[1], fields[2], fields[3], lengths[3]);
		}
		else {
			// Number of fields is wrong
			return null;
		}
		double asDouble = seconds.getDouble();
		if (cardinal == inNegativeCardinal) {
			asDouble = -asDouble;
		}
		return new Coordinate(cardinal, source, originalFormat, asDouble, seconds);
	}

	/**
	 * @return a new Coordinate object wrapped to the given range (but still with the original string)
	 */
	public Coordinate wrapTo180Degrees()
	{
		if (_value == null)
		{
			if (_asDouble >= -180.0 && _asDouble <= 180.0) {
				return this;
			}
			double wrappedDouble = (_asDouble + 180.0) % 360.0 - 180.0;
			final boolean startPositive = (_asDouble > 0.0);
			final boolean endPositive = (wrappedDouble > 0.0);
			Cardinal wrappedCardinal = (startPositive == endPositive ? _cardinal : _cardinal.getOpposite());
			return new Coordinate(wrappedDouble, wrappedCardinal);
		}
		if (_value.isWithinOneEightyDegrees()) {
			return this;
		}
		FractionalSeconds wrappedValue = _value.wrapToThreeSixtyDegrees();
		if (wrappedValue.isWithinOneEightyDegrees()) {
			return new Coordinate(_cardinal, _originalString, _originalFormat, wrappedValue.getDouble(), wrappedValue);
		}
		// we need to flip to the opposite cardinal
		boolean isPositive = (_asDouble > 0.0);
		wrappedValue = wrappedValue.invert();
		double doubleValue = isPositive ? -wrappedValue.getDouble() : wrappedValue.getDouble();
		return new Coordinate(_cardinal.getOpposite(), _originalString, _originalFormat, doubleValue, wrappedValue);
	}

	/** Class to hold the results of the string parsing */
	private static class ParseResult
	{
		public final int numFields;
		public final boolean isNegative;
		ParseResult(int inNumFields, boolean inNegative)
		{
			numFields = inNumFields;
			isNegative = inNegative;
		}
	}

	/**
	 * @param inSource source string to parse
	 * @param inFields array of field values to populate
	 * @param inLengths array of field lengths to populate
	 * @param inOtherDelims array of flags to populate
	 */
	private static ParseResult parseString(String inSource, long[] inFields, int[] inLengths, boolean[] inOtherDelims)
	{
		int numFields = 0;
		boolean isNegative = false;
		try
		{
			final int strLen = inSource.length();
			boolean isNumeric = false;
			// Loop over characters in input string, populating fields array
			for (int i=0; i<strLen; i++)
			{
				char currChar = inSource.charAt(i);
				if (currChar >= '0' && currChar <= '9')
				{
					if (!isNumeric)
					{
						isNumeric = true;
						numFields++;
						inLengths[numFields-1] = 0;
					}
					if (inLengths[numFields-1] < 18) // ignore trailing characters if too big for long
					{
						inFields[numFields-1] = inFields[numFields-1] * 10 + (currChar - '0');
						inLengths[numFields-1] += 1;
					}
				}
				else if (currChar == '-' && numFields == 0 && !isNumeric)
				{
					// Found a minus sign before any of the numbers
					isNegative = true;
				}
				else
				{
					isNumeric = false;
					// Remember delimiters
					if (currChar != ',' && currChar != '.') {
						inOtherDelims[numFields] = true;
					}
				}
			}
		}
		catch (ArrayIndexOutOfBoundsException ignored) {}
		return new ParseResult(numFields, isNegative);
	}

	/**
	 * Get the cardinal from the given character
	 * @param inSource source string
	 */
	static Cardinal getCardinal(String inSource, Cardinal inPositiveCardinal, Cardinal inNegativeCardinal)
	{
		String source = inSource == null ? "" : inSource.trim();
		if (source.equals("")) {
			return Cardinal.NO_CARDINAL;
		}
		final char firstChar = source.charAt(0);
		final char lastChar = source.charAt(source.length() - 1);
		// Try leading character first
		Cardinal cardinal = getCardinal(firstChar, inPositiveCardinal, inNegativeCardinal);
		// if not there, try trailing character
		if (cardinal == Cardinal.NO_CARDINAL) {
			cardinal = getCardinal(lastChar, inPositiveCardinal, inNegativeCardinal);
		}
		return cardinal;
	}

	/**
	 * Get the cardinal from the given character
	 * @param inChar character from file
	 */
	static Cardinal getCardinal(char inChar, Cardinal inPositiveCardinal, Cardinal inNegativeCardinal)
	{
		String givenCardinal = "" + Character.toUpperCase(inChar);
		if (givenCardinal.equals(inPositiveCardinal.printable)) {
			return inPositiveCardinal;
		}
		if (givenCardinal.equals(inNegativeCardinal.printable)) {
			return inNegativeCardinal;
		}
		return Cardinal.NO_CARDINAL;
	}


	/**
	 * Constructor using numeric value
	 * @param inValue value of coordinate
	 * @param inCardinal cardinal
	 */
	Coordinate(double inValue, Cardinal inCardinal)
	{
		_asDouble = inValue;
		_value = null;
		_cardinal = inCardinal;
		NumberFormat degFormatter = _coordFormatters.getLocalFormatter(6);
		_originalString = degFormatter.format(_asDouble);
		_originalFormat = Format.DEG_WITHOUT_CARDINAL;
	}

	/**
	 * @return coordinate as a double
	 */
	public double getDouble() {
		return _asDouble;
	}

	/**
	 * Compares two Coordinates for equality
	 * @param inOther other Coordinate object with which to compare
	 * @return true if the two objects are equal
	 */
	public boolean equals(Object inOther)
	{
		return inOther instanceof Coordinate
				&& _asDouble == ((Coordinate) inOther)._asDouble;
	}

	/**
	 * Output the Coordinate in the given format
	 * @param inFormat format to use, eg FORMAT_DEG_MIN_SEC
	 * @return String for output
	 */
	public String output(Format inFormat) {
		return output(inFormat, NUMDIGITS_DEFAULT);
	}

	/**
	 * Output the Coordinate in the given format
	 * @param inFormat format to use, eg FORMAT_DEG_MIN_SEC
	 * @param inNumDigits number of digits, or -1 for default
	 * @return String for output
	 */
	public String output(Format inFormat, int inNumDigits)
	{
		if (inFormat == Format.NONE) {
			return _originalString;
		}
		if (inFormat == _originalFormat && inNumDigits == NUMDIGITS_DEFAULT) {
			return _originalString;
		}
		if (inFormat == Format.DECIMAL_FORCE_POINT && _originalFormat == Format.DEG_WITHOUT_CARDINAL
			&& inNumDigits == NUMDIGITS_DEFAULT)
		{
			if (_originalString.indexOf('.') > 0 && _originalString.indexOf(',') < 0) {
				return _originalString;
			}
		}
		int numDigits = (inNumDigits == NUMDIGITS_DEFAULT ? getNumDigits(inFormat) : inNumDigits);

		FractionalSeconds value = (_value != null ? _value : FractionalSeconds.fromDouble(_asDouble, numDigits));

		// format as specified
		switch (inFormat)
		{
			case DEG_MIN_SEC:
				value = value.roundToSeconds(numDigits);
				return _cardinal.printable
						+ threeDigitString(value.getWholeDegrees()) + '\u00B0'
						+ twoDigitString(value.getWholeMinutes()) + '\''
						+ twoDigitString(value.getWholeSeconds()) + getLocalDecimalChar()
						+ value.getFractionSeconds() + '"';

			case DEG_MIN:
				value = value.roundToMinutes(numDigits);
				return _cardinal.printable
						+ threeDigitString(value.getWholeDegrees()) + '\u00B0'
						+ twoDigitString(value.getWholeMinutes()) + getLocalDecimalChar()
						+ value.getFractionMinutes() + '\'';

			case DEG:
			case DEG_WITHOUT_CARDINAL:
			case DECIMAL_FORCE_POINT:
				// value = value.roundToDegrees(numDigits);
				NumberFormat degFormatter = (inFormat == Format.DECIMAL_FORCE_POINT ?
						_coordFormatters.getUkFormatter(numDigits) :
						_coordFormatters.getLocalFormatter(numDigits));
				if (inFormat == Format.DEG) {
					return _cardinal.printable + ' ' + degFormatter.format(Math.abs(_asDouble));
				}
				else {
					return degFormatter.format(_asDouble);
				}

			case DEG_MIN_SEC_WITH_SPACES:
				// Note: cardinal not needed as this format is only for exif, which has cardinal separately
				value = value.roundToSeconds(numDigits);
				return "" + value.getWholeDegrees() + " "
					+ value.getWholeMinutes() + " "
					+ value.getWholeSeconds() + '.' // force decimal dot always, don't use local char
					+ value.getFractionSeconds();

			case JUST_CARDINAL:
				return _cardinal.printable;

			case NONE:
			default:
				return _originalString;
		}
	}

	/** @return decimal character used by local number formatter */
	private static char getLocalDecimalChar()
	{
		if (_localDecimalChar == 0)
		{
			NumberFormat format = _coordFormatters.getLocalFormatter(3);
			if (format instanceof DecimalFormat) {
				_localDecimalChar = ((DecimalFormat) format).getDecimalFormatSymbols().getDecimalSeparator();
			}
			else {
				_localDecimalChar = '.';
			}
		}
		return _localDecimalChar;
	}

	/** @return default number of decimal digits to use, depending on format */
	private static int getNumDigits(Format inFormat)
	{
		switch (inFormat)
		{
			case DEG_MIN_SEC:
			case DEG_MIN_SEC_WITH_SPACES:
				return 3;
			case DEG_MIN:
				return 6;
			case DEG:
			case DEG_WITHOUT_CARDINAL:
			case DECIMAL_FORCE_POINT:
			case NONE:
			default:
				return 8;
			case JUST_CARDINAL:
				return 0;
		}
	}

	/**
	 * Format an integer to a two-digit String
	 * @param inNumber number to format
	 * @return two-character String
	 */
	private static String twoDigitString(int inNumber)
	{
		if (inNumber <= 0) return "00";
		if (inNumber < 10) return "0" + inNumber;
		if (inNumber < 100) return "" + inNumber;
		return "" + (inNumber % 100);
	}

	/**
	 * Format an integer to a three-digit String for degrees
	 * @param inNumber number to format
	 * @return three-character String
	 */
	private static String threeDigitString(int inNumber)
	{
		if (inNumber <= 0) return "000";
		if (inNumber < 10) return "00" + inNumber;
		if (inNumber < 100) return "0" + inNumber;
		return "" + (inNumber % 1000);
	}

	/**
	 * Create a new Coordinate between two others
	 * @param inStart start coordinate
	 * @param inEnd end coordinate
	 * @param inFraction fraction from start to end
	 * @param inPositiveCardinal cardinal to use if value is positive
	 * @param inNegativeCardinal cardinal to use if value is negative
	 * @return new Coordinate object
	 */
	public static Coordinate interpolate(Coordinate inStart, Coordinate inEnd, double inFraction,
										 Cardinal inPositiveCardinal, Cardinal inNegativeCardinal)
	{
		double startValue = inStart.getDouble();
		double endValue = inEnd.getDouble();
		double newValue = startValue + (endValue - startValue) * inFraction;
		return new Coordinate(newValue, newValue >= 0.0 ? inPositiveCardinal : inNegativeCardinal);
	}

	/**
	 * Try to parse the given string
	 * @param inString string to check
	 * @return true if it can be parsed as a number
	 */
	private static boolean isJustNumber(String inString)
	{
		boolean justNum = false;
		try {
			double x = Double.parseDouble(inString);
			justNum = (x >= -180.0 && x <= 360.0);
		}
		catch (NumberFormatException ignored) {} // flag remains false
		return justNum;
	}

	/**
	 * Create a String representation for debug
	 * @return String describing coordinate value
	 */
	public String toString() {
		return _originalString;
	}

	/**
	 * From a saved coordinate format display value, get the corresponding value to use
	 * @param inValue value from config
	 * @return coordinate format
	 */
	public static Format getCoordinateFormatForDisplay(String inValue)
	{
		if (Format.DEG.toString().equals(inValue)) {
			return Format.DEG;
		}
		if (Format.DEG_MIN.toString().equals(inValue)) {
			return Format.DEG_MIN;
		}
		if (Format.DEG_MIN_SEC.toString().equals(inValue)) {
			return Format.DEG_MIN_SEC;
		}
		return Format.NONE;
	}
}
