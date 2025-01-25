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
	/** Listener to be informed of changes */
	private final ColourChangeListener _listener;

	/**
	 * Constructor
	 * @param inColour starting colour
	 */
	public ColourPatch(Color inColour) {
		this(inColour, null);
	}

	/**
	 * Constructor
	 * @param inColour starting colour
	 * @param inListener optional listener
	 */
	public ColourPatch(Color inColour, ColourChangeListener inListener)
	{
		Dimension size = new Dimension(80, 50);
		setMinimumSize(size);
		setPreferredSize(size);
		setColour(inColour);
		_listener = inListener;
	}

	/**
	 * Set the colour of the patch
	 * @param inColour Color to use
	 */
	public void setColour(Color inColour)
	{
		if (inColour != null)
		{
			super.setBackground(inColour);
			setToolTipText(ColourUtils.makeHexCode(inColour));
			if (_listener != null) {
				_listener.colourChanged();
			}
		}
	}
}
