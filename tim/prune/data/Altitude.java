package tim.prune.data;

/**
 * Class to hold an altitude and provide conversion functions
 */
public class Altitude
{
	private boolean _valid = false;
	private int _value = 0;
	private Format _format = Format.NO_FORMAT;
	private String _stringValue = null;

	/** Altitude formats */
	public enum Format {
		/** No format */
		NO_FORMAT,
		/** Metres */
		METRES,
		/** Feet */
		FEET
	}

	/** Constants for conversion */
	private static final double CONVERT_FEET_TO_METRES = 0.3048;
	private static final double CONVERT_METRES_TO_FEET = 3.28084;

	/** Constant for no altitude value */
	public static final Altitude NONE = new Altitude(null, Format.NO_FORMAT);


	/**
	 * Constructor using String
	 * @param inString string to parse
	 * @param inFormat format of altitude, either metres or feet
	 */
	public Altitude(String inString, Format inFormat)
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
	 * Constructor with int value
	 * @param inValue int value of altitude
	 * @param inFormat format of altitude, either metres or feet
	 */
	public Altitude(int inValue, Format inFormat)
	{
		_value = inValue;
		_format = inFormat;
		_valid = true;
	}

	/**
	 * @return an exact copy of this Altitude object
	 */
	public Altitude clone()
	{
		return new Altitude(_stringValue, _format);
	}

	/**
	 * Reset the altitude parameters to the same as the given object
	 * @param inClone clone object to copy
	 */
	public void reset(Altitude inClone)
	{
		_value = inClone._value;
		_format = inClone._format;
		_valid = inClone._valid;
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
	public Format getFormat()
	{
		return _format;
	}


	/**
	 * Get the altitude value in the specified format
	 * @param inFormat desired format, either FORMAT_METRES or FORMAT_FEET
	 * @return value as an int
	 */
	public int getValue(Format inFormat)
	{
		// Note possible rounding errors here if converting to/from units
		if (inFormat == _format)
			return _value;
		if (inFormat == Format.METRES)
			return (int) (_value * CONVERT_FEET_TO_METRES);
		if (inFormat == Format.FEET)
			return (int) (_value * CONVERT_METRES_TO_FEET);
		return _value;
	}

	/**
	 * Get a string version of the value
	 * @param inFormat specified format
	 * @return string value, if possible the original one
	 */
	public String getStringValue(Format inFormat)
	{
		if (!_valid) {return "";}
		// Return string value if the same format or "no format" was requested
		if ((inFormat == _format || inFormat == Format.NO_FORMAT)
		 && _stringValue != null && !_stringValue.equals("")) {
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
			return Altitude.NONE;
		// Use altitude format of first point
		Format altFormat = inStart.getFormat();
		int startValue = inStart.getValue();
		int endValue = inEnd.getValue(altFormat);
		// interpolate between start and end
		int newValue = startValue + (int) ((endValue - startValue) * inFrac);
		return new Altitude(newValue, altFormat);
	}

	/**
	 * Add the given offset to the current altitude
	 * @param inOffset offset as double
	 * @param inFormat format of offset, feet or metres
	 * @param inDecimals number of decimal places
	 */
	public void addOffset(double inOffset, Format inFormat, int inDecimals)
	{
		// Use the maximum number of decimal places from current value and offset
		int numDecimals = NumberUtils.getDecimalPlaces(_stringValue);
		if (numDecimals < inDecimals) {numDecimals = inDecimals;}
		// Convert offset to correct units
		double offset = inOffset;
		if (inFormat != _format)
		{
			if (inFormat == Format.FEET)
				offset = inOffset * CONVERT_FEET_TO_METRES;
			else
				offset = inOffset * CONVERT_METRES_TO_FEET;
		}
		// Add the offset
		double newValue = Double.parseDouble(_stringValue.trim()) + offset;
		_value = (int) newValue;
		_stringValue = NumberUtils.formatNumber(newValue, numDecimals);
	}
}
