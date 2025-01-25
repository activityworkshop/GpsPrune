package tim.prune.gui.colour;

import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.colorchooser.AbstractColorChooserPanel;

import tim.prune.I18nManager;

/**
 * Class to offer a dialog to choose a colour using a JColorChooser.
 */
public class ColourChooser
{
	/** dialog object */
	private final JDialog _dialog;
	/** Chooser */
	private final JColorChooser _chooser;
	/** colour patch */
	private ColourPatch _patch = null;


	/**
	 * Constructor
	 * @param inParent parent dialog
	 */
	public ColourChooser(JDialog inParent)
	{
		_chooser = new JColorChooser();
		_chooser.setPreviewPanel(new JPanel());
		setPanels(_chooser);
		_dialog = JColorChooser.createDialog(inParent, I18nManager.getText("dialog.colourchooser.title"), true, _chooser,
			e -> chosenColour(), e -> _patch = null);
	}

	/** Remove the Swatch and CMYK panels, they make it look more complicated than it needs to  be */
	private static void setPanels(JColorChooser inChooser)
	{
		AbstractColorChooserPanel[] defaultPanels = inChooser.getChooserPanels();
		for (AbstractColorChooserPanel panel : defaultPanels)
		{
			if (shouldPanelBeRemoved(panel)) {
				inChooser.removeChooserPanel(panel);
			}
		}
	}

	/** @return true if the chooser panel should be removed */
	private static boolean shouldPanelBeRemoved(AbstractColorChooserPanel inPanel)
	{
		return inPanel.getClass().getName().toLowerCase().contains("defaultswatch")
			|| inPanel.getDisplayName().toUpperCase().contains("CMYK");
	}

	/**
	 * Show the dialog to choose a colour
	 * @param inPatch calling patch
	 */
	public void showDialog(ColourPatch inPatch)
	{
		_patch = inPatch;
		_chooser.setColor(inPatch.getBackground());
		_dialog.setLocationRelativeTo(_dialog.getParent());
		_dialog.setVisible(true);
	}

	/** A colour was selected, so give it back to the calling patch */
	private void chosenColour()
	{
		if (_patch != null) {
			_patch.setColour(_chooser.getColor());
		}
		_patch = null;
	}
}
