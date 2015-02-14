package tim.prune.data;

/**
 * Class to hold an altitude and provide conversion functions
 */
public class Altitude
{
	private boolean _valid = false;
	private int _value = 0;
	private int _format = -1;
	private String _stringValue = null;

	/** Altitude formats */
	public static final int FORMAT_NONE   = -1;
	public static final int FORMAT_METRES = 0;
	public static final int FORMAT_FEET = 1;

	/** Constants for conversion */
	private static final double CONVERT_FEET_TO_METRES = 0.3048;
	private static final double CONVERT_METRES_TO_FEET = 3.28084;

	/** Constant for no altitude value */
	public static final Altitude NONE = new Altitude(null, FORMAT_NONE);


	/**
	 * Constructor using String
	 * @param inString string to parse
	 * @param inFormat format of altitude, either metres or feet
	 */
	public Altitude(String inString, int inFormat)
	{
		if (inString != null && !inString.equals(""))
		{
			try
			{
				_stringValue = inString;
				_value = (int) Double.parseDouble(inString.trim());
				_format = inFormat;
				_valid = true;
			}
			catch (NumberFormatException nfe) {}
		}
	}


	/**
	 * Constructor with int vaue
	 * @param inValue int value of altitude
	 * @param inFormat format of altitude, either metres or feet
	 */
	public Altitude(int inValue, int inFormat)
	{
		_value = inValue;
		_format = inFormat;
		_valid = true;
	}


	/**
	 * @return true if the value could be parsed
	 */
	public boolean isValid()
	{
		return _valid;
	}


	/**
	 * @return raw value as int
	 */
	public int getValue()
	{
		return _value;
	}


	/**
	 * @return format of number
	 */
	public int getFormat()
	{
		return _format;
	}


	/**
	 * Get the altitude value in the specified format
	 * @param inFormat desired format, either FORMAT_METRES or FORMAT_FEET
	 * @return value as an int
	 */
	public int getValue(int inFormat)
	{
		// Note possible rounding errors here if converting to/from units
		if (inFormat == _format)
			return _value;
		if (inFormat == FORMAT_METRES)
			return (int) (_value * CONVERT_FEET_TO_METRES);
		if (inFormat == FORMAT_FEET)
			return (int) (_value * CONVERT_METRES_TO_FEET);
		return _value;
	}

	/**
	 * Get a string version of the value
	 * @param inFormat specified format
	 * @return string value, if possible the original one
	 */
	public String getStringValue(int inFormat)
	{
		if (inFormat == _format && _stringValue != null && !_stringValue.equals("")) {
			return _stringValue;
		}
		return "" + getValue(inFormat);
	}


	/**
	 * Interpolate a new Altitude object between the given ones
	 * @param inStart start altitude
	 * @param inEnd end altitude
	 * @param inIndex index of interpolated point
	 * @param inNumSteps number of steps to interpolate
	 * @return Interpolated Altitude object
	 */
	public static Altitude interpolate(Altitude inStart, Altitude inEnd, int inIndex, int inNumSteps)
	{
		return interpolate(inStart, inEnd, 1.0 * (inIndex + 1) / (inNumSteps + 1));
	}


	/**
	 * Interpolate a new Altitude object between the given ones
	 * @param inStart start altitude
	 * @param inEnd end altitude
	 * @param inFrac fraction of distance from first point
	 * @return Interpolated Altitude object
	 */
	public static Altitude interpolate(Altitude inStart, Altitude inEnd, double inFrac)
	{
		// Check if altitudes are valid
		if (inStart == null || inEnd == null || !inStart.isValid() || !inEnd.isValid())
			return new Altitude(null, FORMAT_NONE);
		// Use altitude format of first point
		int altFormat = inStart.getFormat();
		int startValue = inStart.getValue();
		int endValue = inEnd.getValue(altFormat);
		// interpolate between start and end
		int newValue = startValue + (int) ((endValue - startValue) * inFrac);
		return new Altitude(newValue, altFormat);
	}
}
