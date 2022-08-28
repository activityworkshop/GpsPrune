package tim.prune.gui.colour;

import java.awt.Color;

/**
 * Determining the colours for the waypoint symbols according to type
 */
public class WaypointColours
{
	private int _salt = -1;
	private static final int NUM_SALTS = 10;


	/**
	 * @return maximum salt value (min is always 0)
	 */
	public static int getMaxSalt() {
		return NUM_SALTS - 1;
	}

	/**
	 * @param inSalt salt value to use (or -1 for no salt)
	 */
	public void setSalt(int inSalt) {
		_salt = (inSalt < 0 ? -1 : inSalt % NUM_SALTS);
	}

	/**
	 * @param inType waypoint type (if any)
	 * @return Colour to use for symbol
	 */
	public Color getColourForType(String inType)
	{
		return getColourForType(inType, _salt);
	}

	/**
	 * @param inType waypoint type (if any)
	 * @param inSalt salt value
	 * @return Colour to use for symbol
	 */
	public static Color getColourForType(String inType, int inSalt)
	{
		String type = (inType == null ? "" : inType.strip().toLowerCase());
		if (inSalt < 0 || type.isEmpty()) {
			return null;
		}
		final int hash = makeHash(type, inSalt);
		final float hue = (float) ((hash % 16) / 16.0);
		final int series = hash / 16;
		float[] saturations = new float[] {1.0f, 0.8f, 0.5f, 1.0f, 0.22f};
		float[] values = new float[] {1.0f, 0.8f, 0.9f, 0.6f, 1.0f};

		return Color.getHSBColor(hue, saturations[series], values[series]);
	}

	/**
	 * @param inType point type
	 * @param inSalt salt to use
	 * @return hash from 0 to 79
	 */
	static int makeHash(String inType, int inSalt)
	{
		final int[] multipliers = new int[] {29, 31, 37, 41, 43, 47, 53, 59, 61, 67};
		final int multiplier = multipliers[inSalt % NUM_SALTS];
		final int numChars = inType.length();
		int value = 1;
		for (int i=0; i<numChars; i++) {
			value = (value * multiplier + inType.charAt(i)) % 80;
		}
		return value;
	}
}
