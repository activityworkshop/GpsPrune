package tim.prune.gui.map;

import javax.swing.ImageIcon;

/**
 * Definition of a waypoint icon including name and offsets
 */
public class WpIconDefinition
{
	/** Name of icon, used for finding image file */
	private final String _name;
	/** X offset of marker point in image */
	private final int    _xOffset;
	/** Y offset of marker point in image */
	private final int    _yOffset;
	/** icon */
	private final ImageIcon _icon;


	/**
	 * Constructor
	 * @param inName name of icon
	 * @param inIcon icon to set
	 */
	public WpIconDefinition(String inName, ImageIcon inIcon)
	{
		_name = inName;
		_icon = inIcon;
		_xOffset = inIcon.getIconWidth() / 2;
		_yOffset = inIcon.getIconHeight() / 2;
	}

	/** @return name of icon */
	public String getName() {return _name;}
	/** @return x offset */
	public int getXOffset() {return _xOffset;}
	/** @return y offset */
	public int getYOffset() {return _yOffset;}

	/** @return image icon to display */
	public ImageIcon getImageIcon() {
		return _icon;
	}
}
