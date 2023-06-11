package tim.prune.gui.colour;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


/**
 * Listener class to react to patch clicks
 */
public class PatchListener extends MouseAdapter
{
	/** Associated patch */
	private final ColourPatch _patch;
	/** Colour chooser object, shared between listeners */
	private final ColourChooser _colourChooser;

	/**
	 * Constructor
	 * @param inPatch patch object to listen to
	 * @param inChooser colour chooser to use for selection
	 */
	public PatchListener(ColourPatch inPatch, ColourChooser inChooser)
	{
		_patch = inPatch;
		_colourChooser = inChooser;
	}

	/** React to mouse clicks */
	public void mouseClicked(MouseEvent e)
	{
		_colourChooser.showDialog(_patch.getBackground());
		_patch.setColour(_colourChooser.getChosenColour());
	}
}
