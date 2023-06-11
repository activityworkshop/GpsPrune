package tim.prune.gui.map;

import javax.swing.ImageIcon;

import tim.prune.gui.IconManager;

/**
 * Class to provide a library of waypoint icon definitions
 */
public abstract class WpIconLibrary
{
	/** Types of waypoint */
	public static final int WAYPT_DEFAULT = 0;
	public static final int WAYPT_RING_POINT = 1;
	public static final int WAYPT_PLECTRUM = 2;
	public static final int WAYPT_CIRCLE = 3;
	public static final int WAYPT_PIN = 4;
	public static final int WAYPT_FLAG = 5;
	public static final int WAYPT_NUMBER_OF_ICONS = WAYPT_FLAG + 1;

	/** Sizes of icon */
	public static final int SIZE_SMALL = 0;
	public static final int SIZE_MEDIUM = 1;
	public static final int SIZE_LARGE = 2;

	/** Array of x and y offsets for the icons */
	private static int[] _PIXEL_OFFSETS = null;

	/** Static block to initialise offsets */
	static
	{
		_PIXEL_OFFSETS = new int[] {0, 0, 0, 0, 0, 0, // default
			8,  15, 11, 21, 14, 27, // ringpt
			9,  17, 12, 23, 14, 28, // plectrum
			8,  8,  12, 12, 15, 15, // ring
			3,  16, 4,  22, 4,  28, // pin
			2,  17, 3,  22, 4,  28  // flag
		};
	}

	/** @return array of Integers representing waypoint types */
	public static Integer[] getWaypointTypes()
	{
		return new Integer[] {WAYPT_DEFAULT, WAYPT_RING_POINT, WAYPT_PLECTRUM,
				WAYPT_CIRCLE, WAYPT_PIN, WAYPT_FLAG};
	}

	/**
	 * @param inType icon type
	 * @return the name of the specified icon, used for settings dialog
	 */
	public static String getIconName(int inType)
	{
		switch (inType)
		{
			case WAYPT_RING_POINT: return "ringpt";
			case WAYPT_PLECTRUM:   return "plectrum";
			case WAYPT_CIRCLE:     return "ring";
			case WAYPT_PIN:        return "pin";
			case WAYPT_FLAG:       return "flag";
			case WAYPT_DEFAULT:
			default:               return "default";
		}
	}

	/**
	 * @param inType icon type
	 * @param inSize icon size (small/medium/large)
	 * @return icon definition for the specified icon
	 */
	public static WpIconDefinition getIconDefinition(int inType, int inSize)
	{
		String iconName = getIconName(inType);
		String sizeSuffix = null;
		switch (inSize)
		{
			case SIZE_SMALL:  sizeSuffix = "_s"; break;
			case SIZE_MEDIUM: sizeSuffix = "_m"; break;
			case SIZE_LARGE:  sizeSuffix = "_l"; break;
			default:          sizeSuffix = "_m"; inSize = SIZE_MEDIUM; break;
		}
		// Look up offsets in the static array
		int xOffset = 0, yOffset = 0;
		try {
			xOffset = _PIXEL_OFFSETS[inType * 6 + inSize * 2];
			yOffset = _PIXEL_OFFSETS[inType * 6 + inSize * 2 + 1];
		}
		catch (ArrayIndexOutOfBoundsException obe) {} // ignore, leave offsets at 0
		WpIconDefinition iconDef = new WpIconDefinition(iconName, xOffset, yOffset);
		// Get icon
		ImageIcon icon = IconManager.getImageIcon(IconManager.WAYPOINT_ICON_PREFIX
			+ iconDef.getName() + sizeSuffix);
		iconDef.setIcon(icon);
		return iconDef;
	}
}
