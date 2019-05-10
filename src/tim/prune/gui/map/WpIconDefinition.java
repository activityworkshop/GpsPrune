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
	private ImageIcon _icon = null;


	/**
	 * Constructor
	 * @param inName name of icon
	 * @param inX x offset
	 * @param inY y offset
	 */
	public WpIconDefinition(String inName, int inX, int inY)
	{
		_name = inName;
		_xOffset = inX;
		_yOffset = inY;
	}

	/** @return name of icon */
	public String getName() {return _name;}
	/** @return x offset */
	public int getXOffset() {return _xOffset;}
	/** @return y offset */
	public int getYOffset() {return _yOffset;}

	/** @param inIcon icon to set */
	public void setIcon(ImageIcon inIcon) {_icon = inIcon;}

	/** @return image icon to display */
	public ImageIcon getImageIcon()
	{
		return _icon;
	}
}
