package tim.prune.data;

/**
 * Class to represent a Longitude Coordinate
 */
public class Longitude extends Coordinate
{
	/**
	 * Constructor
	 * @param inString string value from file
	 */
	public Longitude(String inString)
	{
		super(inString);
	}


	/**
	 * Constructor
	 * @param inValue value of coordinate
	 * @param inFormat format to use
	 */
	public Longitude(double inValue, int inFormat)
	{
		super(inValue, inFormat, inValue < 0.0 ? WEST : EAST);
	}


	/**
	 * Turn the given character into a cardinal
	 * @see tim.prune.data.Coordinate#getCardinal(char)
	 */
	protected int getCardinal(char inChar)
	{
		// Longitude recognises E, W and -
		// default is no cardinal
		int cardinal = NO_CARDINAL;
		switch (inChar)
		{
			case 'E':
			case 'e':
				cardinal = EAST; break;
			case 'W':
			case 'w':
			case '-':
				cardinal = WEST; break;
			default:
				// no character given
		}
		return cardinal;
	}


	/**
	 * @return default cardinal (East)
	 * @see tim.prune.data.Coordinate#getDefaultCardinal()
	 */
	protected int getDefaultCardinal()
	{
		return EAST;
	}


	/**
	 * Make a new Longitude object
	 * @see tim.prune.data.Coordinate#makeNew(double, int)
	 */
	protected Coordinate makeNew(double inValue, int inFormat)
	{
		return new Longitude(inValue, inFormat);
	}

	/**
	 * @return the maximum degree range for this coordinate
	 */
	protected int getMaxDegrees()
	{
		return 180;
	}
}
