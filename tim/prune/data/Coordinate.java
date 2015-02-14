package tim.prune.data;

/**
 * Class to represent a lat/long coordinate
 * and provide conversion functions
 */
public abstract class Coordinate
{
	public static final int NORTH = 0;
	public static final int EAST = 1;
	public static final int SOUTH = 2;
	public static final int WEST = 3;
	public static final char[] PRINTABLE_CARDINALS = {'N', 'E', 'S', 'W'};
	public static final int FORMAT_DEG_MIN_SEC = 10;
	public static final int FORMAT_DEG_MIN = 11;
	public static final int FORMAT_DEG = 12;
	public static final int FORMAT_DEG_WITHOUT_CARDINAL = 13;
	public static final int FORMAT_NONE = 19;

	// Instance variables
	private boolean _valid = false;
	protected int _cardinal = NORTH;
	private int _degrees = 0;
	private int _minutes = 0;
	private int _seconds = 0;
	private int _fracs = 0;
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
		if (strLen > 1)
		{
			// Check for leading character NSEW
			_cardinal = getCardinal(inString.charAt(0));
			// count numeric fields - 1=d, 2=dm, 3=dm.m/dms, 4=dms.s
			int numFields = 0;
			boolean inNumeric = false;
			char currChar;
			long[] fields = new long[4];
			long[] denoms = new long[4];
			try
			{
				for (int i=0; i<strLen; i++)
				{
					currChar = inString.charAt(i);
					if (currChar >= '0' && currChar <= '9')
					{
						if (!inNumeric)
						{
							inNumeric = true;
							numFields++;
							denoms[numFields-1] = 1;
						}
						fields[numFields-1] = fields[numFields-1] * 10 + (currChar - '0');
						denoms[numFields-1] *= 10;
					}
					else
					{
						inNumeric = false;
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
			_originalFormat = FORMAT_DEG;
			if (numFields == 2)
			{
				// String is just decimal degrees
				double numMins = fields[1] * 60.0 / denoms[1];
				_minutes = (int) numMins;
				double numSecs = (numMins - _minutes) * 60.0;
				_seconds = (int) numSecs;
				_fracs = (int) ((numSecs - _seconds) * 10);
			}
			else if (numFields == 3)
			{
				// String is degrees-minutes.fractions
				_originalFormat = FORMAT_DEG_MIN;
				_minutes = (int) fields[1];
				double numSecs = fields[2] * 60.0 / denoms[2];
				_seconds = (int) numSecs;
				_fracs = (int) ((numSecs - _seconds) * 10);
			}
			else if (numFields == 4)
			{
				_originalFormat = FORMAT_DEG_MIN_SEC;
				// String is degrees-minutes-seconds.fractions
				_minutes = (int) fields[1];
				_seconds = (int) fields[2];
				_fracs = (int) fields[3];
			}
			_asDouble = 1.0 * _degrees + (_minutes / 60.0) + (_seconds / 3600.0) + (_fracs / 36000.0);
			if (_cardinal == WEST || _cardinal == SOUTH || inString.charAt(0) == '-')
				_asDouble = -_asDouble;
		}
		else _valid = false;
	}


	/**
	 * Get the cardinal from the given character
	 * @param inChar character from file
	 */
	protected abstract int getCardinal(char inChar);


	/**
	 * Constructor
	 * @param inValue value of coordinate
	 * @param inFormat format to use
	 */
	protected Coordinate(double inValue, int inFormat)
	{
		_asDouble = inValue;
		// Calculate degrees, minutes, seconds
		_degrees = (int) inValue;
		double numMins = (Math.abs(_asDouble)-Math.abs(_degrees)) * 60.0;
		_minutes = (int) numMins;
		double numSecs = (numMins - _minutes) * 60.0;
		_seconds = (int) numSecs;
		_fracs = (int) ((numSecs - _seconds) * 10);
		// Make a string to display on screen
		_originalFormat = FORMAT_NONE;
		if (inFormat == FORMAT_NONE) inFormat = FORMAT_DEG_WITHOUT_CARDINAL;
		_originalString = output(inFormat);
		_originalFormat = inFormat;
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
		return (inOther != null && _cardinal == inOther._cardinal
			&& _degrees == inOther._degrees
			&& _minutes == inOther._minutes
			&& _seconds == inOther._seconds
			&& _fracs == inOther._fracs);
	}


	/**
	 * Output the Coordinate in the given format
	 * @param inOriginalString the original String to use as default
	 * @param inFormat format to use, eg FORMAT_DEG_MIN_SEC
	 * @return String for output
	 */
	public String output(int inFormat)
	{
		String answer = _originalString;
		if (inFormat != FORMAT_NONE && inFormat != _originalFormat)
		{
			// TODO: allow specification of precision for output of d-m and d
			// format as specified
			switch (inFormat)
			{
				case FORMAT_DEG_MIN_SEC: {
					StringBuffer buffer = new StringBuffer();
					buffer.append(PRINTABLE_CARDINALS[_cardinal])
						.append(threeDigitString(_degrees)).append('°')
						.append(twoDigitString(_minutes)).append('\'')
						.append(twoDigitString(_seconds)).append('.')
						.append(_fracs);
					answer = buffer.toString(); break;
				}
				case FORMAT_DEG_MIN: answer = "" + PRINTABLE_CARDINALS[_cardinal] + threeDigitString(_degrees) + "°"
					+ (_minutes + _seconds / 60.0 + _fracs / 600.0); break;
				case FORMAT_DEG:
				case FORMAT_DEG_WITHOUT_CARDINAL: answer = (_asDouble<0.0?"-":"")
				+ (_degrees + _minutes / 60.0 + _seconds / 3600.0 + _fracs / 36000.0); break;
			}
		}
		return answer;
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
		double startValue = inStart.getDouble();
		double endValue = inEnd.getDouble();
		double newValue = startValue + (endValue - startValue) * (inIndex+1) / (inNumPoints + 1);
		Coordinate answer = inStart.makeNew(newValue, inStart._originalFormat);
		return answer;
	}


	/**
	 * Make a new Coordinate according to subclass
	 * @param inValue double value
	 * @param inFormat format to use
	 * @return object of Coordinate subclass
	 */
	protected abstract Coordinate makeNew(double inValue, int inFormat);


	/**
	 * Create a String representation for debug
	 */
	public String toString()
	{
		return "Coord: " + _cardinal + " (" + _degrees + ") (" + _minutes + ") (" + _seconds + "." + _fracs + ") = " + _asDouble;
	}
}
