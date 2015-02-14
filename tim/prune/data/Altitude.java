package tim.prune.data;

/**
 * Class to hold an altitude and provide conversion functions
 */
public class Altitude
{
	private boolean _valid = false;
	private int _value = 0;
	private int _format = -1;
	public static final int FORMAT_NONE   = -1;
	public static final int FORMAT_METRES = 0;
	public static final int FORMAT_FEET = 1;

	private static final double CONVERT_FEET_TO_METRES = 0.3048;
	private static final double CONVERT_METRES_TO_FEET = 3.28084;


	/**
	 * Constructor
	 */
	public Altitude(String inString, int inFormat)
	{
		if (inString != null && !inString.equals(""))
		{
			try
			{
				_value = (int) Double.parseDouble(inString.trim());
				_format = inFormat;
				_valid = true;
			}
			catch (NumberFormatException nfe) {}
		}
	}


	/**
	 * Constructor
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
		// TODO: Fix rounding errors here converting between units - return double?
		if (inFormat == _format)
			return _value;
		if (inFormat == FORMAT_METRES)
			return (int) (_value * CONVERT_FEET_TO_METRES);
		if (inFormat == FORMAT_FEET)
			return (int) (_value * CONVERT_METRES_TO_FEET);
		return _value;
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
		// Check if altitudes are valid
		if (inStart == null || inEnd == null || !inStart.isValid() || !inEnd.isValid())
			return new Altitude(null, FORMAT_NONE);
		// Use altitude format of first point
		int altFormat = inStart.getFormat();
		int startValue = inStart.getValue();
		int endValue = inEnd.getValue(altFormat);
		int newValue = startValue
			+ (int) ((endValue - startValue) * 1.0 / (inNumSteps + 1) * (inIndex + 1));
		return new Altitude(newValue, altFormat);
	}
}
