package tim.prune.gui.colour;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JPanel;

import tim.prune.config.ColourUtils;

/**
 * Class to act as a colour patch to illustrate a chosen colour
 */
public class ColourPatch extends JPanel
{
	/**
	 * Constructor
	 * @param inColour starting colour
	 */
	public ColourPatch(Color inColour)
	{
		Dimension size = new Dimension(80, 50);
		setMinimumSize(size);
		setPreferredSize(size);
		setColour(inColour);
	}

	/**
	 * Set the colour of the patch
	 * @param inColour Color to use
	 */
	public void setColour(Color inColour)
	{
		super.setBackground(inColour);
		setToolTipText(ColourUtils.makeHexCode(inColour));
	}
}
