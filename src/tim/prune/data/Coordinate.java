package tim.prune.data;

import java.text.NumberFormat;

/**
 * Class to represent a lat/long coordinate
 * and provide conversion functions
 */
public abstract class Coordinate
{
	public static final int NO_CARDINAL = -1;
	public static final int NORTH = 0;
	public static final int EAST = 1;
	public static final int SOUTH = 2;
	public static final int WEST = 3;
	private static final char[] PRINTABLE_CARDINALS = {'N', 'E', 'S', 'W'};
	public static final int FORMAT_DEG_MIN_SEC = 10;
	public static final int FORMAT_DEG_MIN = 11;
	public static final int FORMAT_DEG = 12;
	public static final int FORMAT_DEG_WITHOUT_CARDINAL = 13;
	public static final int FORMAT_DEG_WHOLE_MIN = 14;
	public static final int FORMAT_DEG_MIN_SEC_WITH_SPACES = 15;
	public static final int FORMAT_CARDINAL = 16;
	public static final int FORMAT_DECIMAL_FORCE_POINT = 17;
	public static final int FORMAT_NONE = 19;

	private static final int NUMDIGITS_DEFAULT = -1;

	// Instance variables
	private boolean _valid = false;
	private boolean _cardinalGuessed = false;
	protected int _cardinal = NORTH;
	private int _degrees = 0;
	private int _minutes = 0;
	private int _seconds = 0;
	private int _fracs = 0;
	private int _fracDenom = 0;
	private String _originalString = null;
	private int _originalFormat = FORMAT_NONE;
	private double _asDouble = 0.0;


	/**
	 * Constructor given String
	 * @param inString string to parse
	 */
	public Coordinate(String inString)
	{
		_originalString = inString;
		int strLen = 0;
		if (inString != null)
		{
			inString = inString.trim();
			strLen = inString.length();
		}
		if (strLen > 0)
		{
			// Check for cardinal character either at beginning or end
			boolean hasCardinal = true;
			_cardinal = getCardinal(inString.charAt(0), inString.charAt(strLen-1));
			if (_cardinal == NO_CARDINAL) {
				hasCardinal = false;
				// use default from concrete subclass
				_cardinal = getDefaultCardinal();
				_cardinalGuessed = true;
			}
			else if (isJustNumber(inString)) {
				// it's just a number
				hasCardinal = false;
				_cardinalGuessed = true;
			}

			// count numeric fields - 1=d, 2=dm, 3=dm.m/dms, 4=dms.s
			int numFields = 0;
			boolean isNumeric = false;
			char currChar;
			long[] fields = new long[4]; // needs to be long for lengthy decimals
			long[] denoms = new long[4];
			boolean[] otherDelims = new boolean[5]; // remember whether delimiters have non-decimal chars
			try
			{
				// Loop over characters in input string, populating fields array
				for (int i=0; i<strLen; i++)
				{
					currChar = inString.charAt(i);
					if (currChar >= '0' && currChar <= '9')
					{
						if (!isNumeric)
						{
							isNumeric = true;
							numFields++;
							denoms[numFields-1] = 1;
						}
						if (denoms[numFields-1] < 1E18) // ignore trailing characters if too big for long
						{
							fields[numFields-1] = fields[numFields-1] * 10 + (currChar - '0');
							denoms[numFields-1] *= 10;
						}
					}
					else
					{
						isNumeric = false;
						// Remember delimiters
						if (currChar != ',' && currChar != '.') {otherDelims[numFields] = true;}
					}
				}
				_valid = (numFields > 0);
			}
			catch (ArrayIndexOutOfBoundsException obe)
			{
				// more than four fields found - unable to parse
				_valid = false;
			}
			// parse fields according to number found
			_degrees = (int) fields[0];
			_asDouble = _degrees;
			_originalFormat = hasCardinal ? FORMAT_DEG : FORMAT_DEG_WITHOUT_CARDINAL;
			_fracDenom = 10;
			if (numFields == 2)
			{
				if (!otherDelims[1])
				{
					// String is just decimal degrees
					double numMins = fields[1] * 60.0 / denoms[1];
					_minutes = (int) numMins;
					double numSecs = (numMins - _minutes) * 60.0;
					_seconds = (int) numSecs;
					_fracs = (int) ((numSecs - _seconds) * 10);
					_asDouble = _degrees + 1.0 * fields[1] / denoms[1];
				}
				else
				{
					// String is degrees and minutes (due to non-decimal separator)
					_originalFormat = FORMAT_DEG_MIN;
					_minutes = (int) fields[1];
					_seconds = 0;
					_fracs = 0;
					_asDouble = 1.0 * _degrees + (_minutes / 60.0);
				}
			}
			// Check for exponential degrees like 1.3E-6
			else if (numFields == 3 && !otherDelims[1] && otherDelims[2] && isJustNumber(inString))
			{
				_originalFormat = FORMAT_DEG;
				_asDouble = Math.abs(Double.parseDouble(inString)); // must succeed if isJustNumber has given true
				// now we can ignore the fields and just use this double
				_degrees = (int) _asDouble;
				double numMins = (_asDouble - _degrees) * 60.0;
				_minutes = (int) numMins;
				double numSecs = (numMins - _minutes) * 60.0;
				_seconds = (int) numSecs;
				_fracs = (int) ((numSecs - _seconds) * 10);
			}
			// Differentiate between d-m.f and d-m-s using . or ,
			else if (numFields == 3 && !otherDelims[2])
			{
				// String is degrees-minutes.fractions
				_originalFormat = FORMAT_DEG_MIN;
				_minutes = (int) fields[1];
				double numSecs = fields[2] * 60.0 / denoms[2];
				_seconds = (int) numSecs;
				_fracs = (int) ((numSecs - _seconds) * 10);
				_asDouble = 1.0 * _degrees + (_minutes / 60.0) + (numSecs / 3600.0);
			}
			else if (numFields == 4 || numFields == 3)
			{
				// String is degrees-minutes-seconds.fractions
				_originalFormat = FORMAT_DEG_MIN_SEC;
				_minutes = (int) fields[1];
				_seconds = (int) fields[2];
				_fracs = (int) fields[3];
				_fracDenom = (int) denoms[3];
				if (_fracDenom < 1) {_fracDenom = 1;}
				_asDouble = 1.0 * _degrees + (_minutes / 60.0) + (_seconds / 3600.0) + (_fracs / 3600.0 / _fracDenom);
			}
			if (_cardinal == WEST || _cardinal == SOUTH || inString.charAt(0) == '-')
				_asDouble = -_asDouble;
			// validate fields
			_valid = _valid && (_degrees <= getMaxDegrees() && _minutes < 60 && _seconds < 60 && _fracs < _fracDenom)
				&& Math.abs(_asDouble) <= getMaxDegrees();
		}
		else _valid = false;
	}


	/**
	 * Get the cardinal from the given character
	 * @param inFirstChar first character from string
	 * @param inLastChar last character from string
	 */
	private int getCardinal(char inFirstChar, char inLastChar)
	{
		// Try leading character first
		int cardinal = getCardinal(inFirstChar);
		// if not there, try trailing character
		if (cardinal == NO_CARDINAL) {
			cardinal = getCardinal(inLastChar);
		}
		return cardinal;
	}

	/**
	 * @return true if cardinal was guessed, false if parsed
	 */
	public boolean getCardinalGuessed() {
		return _cardinalGuessed;
	}

	/**
	 * Get the cardinal from the given character
	 * @param inChar character from file
	 */
	protected abstract int getCardinal(char inChar);

	/**
	 * @return the default cardinal for the subclass
	 */
	protected abstract int getDefaultCardinal();

	/**
	 * @return the maximum degree range for this coordinate
	 */
	protected abstract int getMaxDegrees();


	/**
	 * Constructor
	 * @param inValue value of coordinate
	 * @param inFormat format to use
	 * @param inCardinal cardinal
	 */
	protected Coordinate(double inValue, int inFormat, int inCardinal)
	{
		_asDouble = inValue;
		// Calculate degrees, minutes, seconds
		_degrees = (int) Math.abs(inValue);
		double numMins = (Math.abs(_asDouble)-_degrees) * 60.0;
		_minutes = (int) numMins;
		double numSecs = (numMins - _minutes) * 60.0;
		_seconds = (int) numSecs;
		_fracs = (int) Math.round((numSecs - _seconds) * 10.);
		_fracDenom = 10; // fixed for now
		// Make a string to display on screen
		_cardinal = inCardinal;
		_originalFormat = FORMAT_NONE;
		if (inFormat == FORMAT_NONE) inFormat = FORMAT_DEG_WITHOUT_CARDINAL;
		_originalString = output(inFormat);
		_originalFormat = inFormat;
		_valid = true;
	}


	/**
	 * @return coordinate as a double
	 */
	public double getDouble()
	{
		return _asDouble;
	}

	/**
	 * @return true if Coordinate is valid
	 */
	public boolean isValid()
	{
		return _valid;
	}

	/**
	 * Compares two Coordinates for equality
	 * @param inOther other Coordinate object with which to compare
	 * @return true if the two objects are equal
	 */
	public boolean equals(Coordinate inOther)
	{
		return (_asDouble == inOther._asDouble);
	}


	/**
	 * Output the Coordinate in the given format
	 * @param inFormat format to use, eg FORMAT_DEG_MIN_SEC
	 * @return String for output
	 */
	public String output(int inFormat)
	{
		return output(inFormat, NUMDIGITS_DEFAULT);
	}

	/**
	 * Output the Coordinate in the given format
	 * @param inFormat format to use, eg FORMAT_DEG_MIN_SEC
	 * @param inNumDigits number of digits, or -1 for default
	 * @return String for output
	 */
	public String output(int inFormat, int inNumDigits)
	{
		String answer = _originalString;
		if (inFormat != FORMAT_NONE && inFormat != _originalFormat)
		{
			// format as specified
			switch (inFormat)
			{
				case FORMAT_DEG_MIN_SEC:
				{
					StringBuilder builder = new StringBuilder();
					builder.append(PRINTABLE_CARDINALS[_cardinal])
						.append(threeDigitString(_degrees)).append('\u00B0')
						.append(twoDigitString(_minutes)).append('\'');
					if (inNumDigits == NUMDIGITS_DEFAULT || inNumDigits == 1)
					{
						builder.append(twoDigitString(_seconds)).append('.')
							.append(formatFraction(_fracs, _fracDenom))
							.append('"');
					}
					else
					{
						NumberFormat secFormatter = CoordFormatters.getFormatter(inNumDigits);
						final double numSecs = ((Math.abs(_asDouble) - _degrees) * 60.0 - _minutes) * 60.0;
						builder.append(secFormatter.format(numSecs)).append('"');
					}
					answer = builder.toString();
					break;
				}
				case FORMAT_DEG_MIN:
				{
					final int numMinuteDigits = (inNumDigits == NUMDIGITS_DEFAULT ? 5 : inNumDigits);
					NumberFormat minFormatter = CoordFormatters.getFormatter(numMinuteDigits);
					answer = "" + PRINTABLE_CARDINALS[_cardinal] + threeDigitString(_degrees) + "\u00B0"
						+ minFormatter.format((Math.abs(_asDouble) - _degrees) * 60.0) + "'";
					break;
				}
				case FORMAT_DEG_WHOLE_MIN:
				{
					int deg = _degrees;
					int min = (int) Math.floor(_minutes + _seconds / 60.0 + _fracs / 60.0 / _fracDenom + 0.5);
					if (min == 60) {
						min = 0; deg++;
					}
					answer = "" + PRINTABLE_CARDINALS[_cardinal] + threeDigitString(deg) + "\u00B0" + min + "'";
					break;
				}
				case FORMAT_DEG:
				case FORMAT_DEG_WITHOUT_CARDINAL:
				{
					if (_originalFormat != FORMAT_DEG_WITHOUT_CARDINAL)
					{
						answer = (_asDouble<0.0?"-":"")
							+ (_degrees + _minutes / 60.0 + _seconds / 3600.0 + _fracs / 3600.0 / _fracDenom);
					}
					break;
				}
				case FORMAT_DECIMAL_FORCE_POINT:
				{
					// Forcing a decimal point instead of system-dependent commas etc
					if (_originalFormat == FORMAT_DEG_WITHOUT_CARDINAL && answer.indexOf('.') >= 0 && inNumDigits == NUMDIGITS_DEFAULT)
					{
						break;
					}
					final int numDegDigits = (inNumDigits == NUMDIGITS_DEFAULT ? 8 : inNumDigits);
					NumberFormat degFormatter = CoordFormatters.getFormatter(numDegDigits);
					answer = degFormatter.format(_asDouble);
					break;
				}
				case FORMAT_DEG_MIN_SEC_WITH_SPACES:
				{
					// Note: cardinal not needed as this format is only for exif, which has cardinal separately
					answer = "" + _degrees + " " + _minutes + " " + _seconds + "." + formatFraction(_fracs, _fracDenom);
					break;
				}
				case FORMAT_CARDINAL:
				{
					answer = "" + PRINTABLE_CARDINALS[_cardinal];
					break;
				}
			}
		}
		return answer;
	}

	/**
	 * Format the fraction part of seconds value
	 * @param inFrac fractional part eg 123
	 * @param inDenom denominator of fraction eg 10000
	 * @return String describing fraction, in this case 0123
	 */
	private static final String formatFraction(int inFrac, int inDenom)
	{
		if (inDenom <= 1 || inFrac == 0) {return "" + inFrac;}
		String denomString = "" + inDenom;
		int reqdLen = denomString.length() - 1;
		String result = denomString + inFrac;
		int resultLen = result.length();
		return result.substring(resultLen - reqdLen);
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
	 * @param inIndex index of point
	 * @param inNumPoints number of points to interpolate
	 * @return new Coordinate object
	 */
	public static Coordinate interpolate(Coordinate inStart, Coordinate inEnd,
		int inIndex, int inNumPoints)
	{
		return interpolate(inStart, inEnd, 1.0 * (inIndex+1) / (inNumPoints + 1));
	}


	/**
	 * Create a new Coordinate between two others
	 * @param inStart start coordinate
	 * @param inEnd end coordinate
	 * @param inFraction fraction from start to end
	 * @return new Coordinate object
	 */
	public static Coordinate interpolate(Coordinate inStart, Coordinate inEnd,
		double inFraction)
	{
		double startValue = inStart.getDouble();
		double endValue = inEnd.getDouble();
		double newValue = startValue + (endValue - startValue) * inFraction;
		return inStart.makeNew(newValue, Coordinate.FORMAT_DECIMAL_FORCE_POINT);
	}


	/**
	 * Make a new Coordinate according to subclass
	 * @param inValue double value
	 * @param inFormat format to use
	 * @return object of Coordinate subclass
	 */
	protected abstract Coordinate makeNew(double inValue, int inFormat);

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
		catch (NumberFormatException nfe) {} // flag remains false
		return justNum;
	}

	/**
	 * Create a String representation for debug
	 * @return String describing coordinate value
	 */
	public String toString()
	{
		return "Coord: " + _cardinal + " (" + _degrees + ") (" + _minutes + ") (" + _seconds + "."
			+ formatFraction(_fracs, _fracDenom) + ") = " + _asDouble;
	}

	/**
	 * From a saved coordinate format display value, get the corresponding value to use
	 * @param inValue value from config
	 * @return coordinate format as int
	 */
	public static int getCoordinateFormatForDisplay(int inValue)
	{
		switch(inValue)
		{
			case FORMAT_DEG:
			case FORMAT_DEG_MIN:
			case FORMAT_DEG_MIN_SEC:
				return inValue;
			default:
				return FORMAT_NONE;
		}
	}
}
