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
	private enum Size
	{
		SMALL("_s"), MEDIUM("_m"), LARGE("_l"), FIXED("_f");
		final String _suffix;
		Size(String inSuffix) {
			_suffix = inSuffix;
		}
		String getSuffix() {
			return _suffix;
		}
		static Size fromInt(int inIndex)
		{
			switch (inIndex) {
				case 0: return SMALL;
				default:
				case 1: return MEDIUM;
				case 2: return LARGE;
			}
		}
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
	 * @param inIconManager icon manager to use
	 * @return icon definition for the specified icon
	 */
	public static WpIconDefinition getIconDefinition(int inType, int inSize, IconManager inIconManager) {
		return getIconDefinition(inType, Size.fromInt(inSize), inIconManager);
	}

	/**
	 * @param inType icon type
	 * @param inIconManager icon manager to use
	 * @return icon definition for the specified fixed icon
	 */
	public static WpIconDefinition getFixedIconDefinition(int inType, IconManager inIconManager) {
		return getIconDefinition(inType, Size.FIXED, inIconManager);
	}

	/**
	 * @param inType icon type
	 * @param inSize icon size (small/medium/large)
	 * @param inIconManager icon manager to use
	 * @return icon definition for the specified icon
	 */
	private static WpIconDefinition getIconDefinition(int inType, Size inSize, IconManager inIconManager)
	{
		final String iconName = getIconName(inType);
		ImageIcon icon = inIconManager.getImageIcon(IconManager.WAYPOINT_ICON_PREFIX
			+ iconName + inSize.getSuffix());
		return new WpIconDefinition(icon);
	}
}
