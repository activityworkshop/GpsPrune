package tim.prune.gui.map;

import javax.swing.ImageIcon;

/**
 * Definition of a waypoint icon including offsets
 */
public class WpIconDefinition
{
	/** X offset of marker point in image */
	private final int    _xOffset;
	/** Y offset of marker point in image */
	private final int    _yOffset;
	/** icon */
	private final ImageIcon _icon;


	/**
	 * Constructor
	 * @param inIcon icon to set
	 */
	public WpIconDefinition(ImageIcon inIcon)
	{
		_icon = inIcon;
		_xOffset = inIcon.getIconWidth() / 2;
		_yOffset = inIcon.getIconHeight() / 2;
	}

	/** @return x offset */
	public int getXOffset() {return _xOffset;}
	/** @return y offset */
	public int getYOffset() {return _yOffset;}

	/** @return image icon to display */
	public ImageIcon getImageIcon() {
		return _icon;
	}
}
