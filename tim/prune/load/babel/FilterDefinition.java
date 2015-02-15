package tim.prune.load.babel;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Superclass of all the filter definition panels, to be added in the cardset
 * of the AddFilterDialog
 */
public abstract class FilterDefinition extends JPanel
{
	/** Parent dialog to inform of parameter changes */
	private AddFilterDialog _parentDialog = null;
	/** Listener for key presses on the parameter entry fields */
	protected KeyListener _paramChangeListener = null;

	/**
	 * Constructor
	 */
	public FilterDefinition(AddFilterDialog inFilterDialog)
	{
		_parentDialog = inFilterDialog;
		_paramChangeListener = new KeyAdapter() {
			public void keyTyped(KeyEvent arg0) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						_parentDialog.filterParamsChanged();
					}
				});
			}
		};
	}

	/**
	 * @return true if the filter definition is valid
	 */
	public abstract boolean isFilterValid();

	/**
	 * @return filter definition to pass to gpsbabel
	 */
	public String getString()
	{
		return "-x " + getFilterName() + getParameters();
	}

	/** @return filter name */
	protected abstract String getFilterName();

	/** Construct the GUI elements and add them to the panel */
	protected abstract void makePanelContents();

	/** @return filter parameters */
	protected abstract String getParameters();
}
