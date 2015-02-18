package tim.prune.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tim.prune.I18nManager;
import tim.prune.config.Config;
import tim.prune.threedee.TerrainDefinition;

/**
 * Gui component for defining the 3d terrain,
 * including whether to use one or not, and if so
 * what resolution to use for the grid
 */
public class TerrainDefinitionPanel extends JPanel
{
	/** Checkbox to use a terrain or not */
	private JCheckBox _useCheckbox = null;
	/** Field for entering the grid size */
	private WholeNumberField _gridSizeField = null;


	/**
	 * Constructor
	 */
	public TerrainDefinitionPanel()
	{
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		// Components
		_useCheckbox = new JCheckBox(I18nManager.getText("dialog.3d.useterrain"));
		_useCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				activateGridField();
			}
		});
		add(_useCheckbox);
		add(Box.createHorizontalGlue());
		JLabel label = new JLabel(I18nManager.getText("dialog.3d.terraingridsize") + ": ");
		add(label);
		_gridSizeField = new WholeNumberField(4);
		_gridSizeField.setValue(Config.getConfigInt(Config.KEY_TERRAIN_GRID_SIZE)); // default grid size
		_gridSizeField.setMaximumSize(new Dimension(100, 50));
		_gridSizeField.setEnabled(false);
		add(_gridSizeField);
	}

	/**
	 * @param inDefinition terrain parameters to set
	 */
	public void initTerrainParameters(TerrainDefinition inDefinition)
	{
		_useCheckbox.setSelected(inDefinition != null && inDefinition.getUseTerrain());
		if (inDefinition != null && inDefinition.getGridSize() > 0) {
			_gridSizeField.setValue(inDefinition.getGridSize());
		}
		activateGridField();
	}

	/**
	 * @return true if the terrain is selected
	 */
	public boolean getUseTerrain() {
		return _useCheckbox.isSelected() && getGridSize() > 2;
	}

	/**
	 * @return number of nodes along each side of the grid
	 */
	public int getGridSize() {
		return _gridSizeField.getValue();
	}

	/**
	 * Set the grid field to be enabled or not based on the checkbox
	 */
	private void activateGridField() {
		_gridSizeField.setEnabled(_useCheckbox.isSelected());
	}
}
