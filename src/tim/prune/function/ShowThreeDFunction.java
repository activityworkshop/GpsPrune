package tim.prune.function;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.config.Config;
import tim.prune.data.SymbolScaleFactor;
import tim.prune.gui.BaseImageDefinitionPanel;
import tim.prune.gui.DecimalNumberField;
import tim.prune.gui.GuiGridLayout;
import tim.prune.gui.TerrainDefinitionPanel;
import tim.prune.threedee.TerrainDefinition;
import tim.prune.threedee.ThreeDException;
import tim.prune.threedee.ThreeDWindow;
import tim.prune.threedee.WindowFactory;
import tim.prune.tips.TipManager;

/**
 * Class to show the 3d window
 */
public class ShowThreeDFunction extends GenericFunction
{
	/** Dialog for input parameters */
	private JDialog _dialog = null;
	/** Field for altitude exaggeration value */
	private DecimalNumberField _exaggField = null;
	/** Field for symbol scaling value */
	private DecimalNumberField _symbolScaleField = null;
	/** Component for defining the base image */
	private BaseImageDefinitionPanel _baseImagePanel = null;
	/** Component for defining the terrain */
	private TerrainDefinitionPanel _terrainPanel = null;

	/**
	 * Constructor
	 * @param inApp app object
	 */
	public ShowThreeDFunction(App inApp) {
		super(inApp);
	}

	/**
	 * Get the name key
	 */
	public String getNameKey() {
		return "function.show3d";
	}

	/**
	 * Begin the function
	 */
	public void begin()
	{
		ThreeDWindow window = WindowFactory.getWindow(_parentFrame);
		if (window == null)
		{
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("error.function.nojava3d"),
				I18nManager.getText("error.function.notavailable.title"), JOptionPane.WARNING_MESSAGE);
		}
		else
		{
			// See if the track has any altitudes at all - if not, show a tip to use SRTM
			if (!_app.getTrackInfo().getTrack().hasAltitudeData()) {
				_app.showTip(TipManager.Tip_UseSrtmFor3d);
			}
			// Show a dialog to get the parameters
			if (_dialog == null)
			{
				_dialog = new JDialog(_app.getFrame(), getName(), true);
				_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				_dialog.getContentPane().add(makeDialogComponents());
				_dialog.pack();
			}
			final int exaggFactor = Config.getConfigInt(Config.KEY_HEIGHT_EXAGGERATION);
			if (exaggFactor > 0) {
				_exaggField.setValue(exaggFactor / 100.0);
			}
			_baseImagePanel.updateBaseImageDetails();
			_dialog.setLocationRelativeTo(_app.getFrame());
			_dialog.setVisible(true);
		}
	}

	/**
	 * Make the dialog components to select the options
	 * @return JPanel holding the gui elements
	 */
	private JPanel makeDialogComponents()
	{
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout(4, 4));

		JPanel innerPanel = new JPanel();
		innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
		innerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		// grid
		JPanel gridPanel = new JPanel();
		GuiGridLayout grid = new GuiGridLayout(gridPanel, new double[] {3.0, 1.0},
			new boolean[] {true, false});
		// Row for altitude exaggeration
		final JLabel altLabel = new JLabel(I18nManager.getText("dialog.3d.altitudefactor") + ": ");
		altLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		grid.add(altLabel);
		_exaggField = new DecimalNumberField(); // don't allow negative numbers
		_exaggField.setValue(5.0);
		grid.add(_exaggField);
		// Row for symbol scaling
		JLabel scaleLabel = new JLabel(I18nManager.getText("dialog.3d.symbolscalefactor") + ": ");
		scaleLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		grid.add(scaleLabel);
		_symbolScaleField = new DecimalNumberField(); // don't allow negative numbers
		_symbolScaleField.setValue(1.0);
		grid.add(_symbolScaleField);
		innerPanel.add(gridPanel);
		innerPanel.add(Box.createVerticalStrut(4));

		// Panel for terrain
		_terrainPanel = new TerrainDefinitionPanel();
		innerPanel.add(_terrainPanel);
		mainPanel.add(innerPanel, BorderLayout.NORTH);
		innerPanel.add(Box.createVerticalStrut(4));

		// Panel for base image (null because we don't need callback)
		_baseImagePanel = new BaseImageDefinitionPanel(null, _dialog, _app.getTrackInfo().getTrack());
		innerPanel.add(_baseImagePanel);

		// OK, Cancel buttons
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton okButton = new JButton(I18nManager.getText("button.ok"));
		okButton.addActionListener((e) -> {
			_dialog.dispose();
			new Thread(() -> {finish();}).start();
		});
		buttonPanel.add(okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener((e) -> _dialog.dispose());
		buttonPanel.add(cancelButton);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);

		return mainPanel;
	}

	/**
	 * All parameters have been selected in the input dialog, now we can go to the 3d window
	 */
	private void finish()
	{
		// Store exaggeration factor and grid size in config
		Config.setConfigInt(Config.KEY_HEIGHT_EXAGGERATION, (int) (_exaggField.getValue() * 100));
		int terrainGridSize = _terrainPanel.getGridSize();
		if (terrainGridSize < 20) {terrainGridSize = 20;}
		Config.setConfigInt(Config.KEY_TERRAIN_GRID_SIZE, terrainGridSize);

		ThreeDWindow window = WindowFactory.getWindow(_parentFrame);
		if (window != null)
		{
			try
			{
				// Pass the parameters to use and show the window
				window.setTrack(_app.getTrackInfo().getTrack());
				window.setAltitudeFactor(_exaggField.getValue());
				if (_symbolScaleField.isEmpty()) {
					_symbolScaleField.setValue(1.0);
				}
				final double symbolScaleFactor = SymbolScaleFactor.validateFactor(_symbolScaleField.getValue());
				window.setSymbolScalingFactor(symbolScaleFactor);
				_symbolScaleField.setValue(symbolScaleFactor);
				// Also pass the base image parameters from input dialog
				window.setBaseImageParameters(_baseImagePanel.getImageDefinition());
				window.setTerrainParameters(new TerrainDefinition(_terrainPanel.getUseTerrain(), _terrainPanel.getGridSize()));
				window.setDataStatus(_app.getCurrentDataStatus());
				window.show();
			}
			catch (ThreeDException e)
			{
				_app.showErrorMessageNoLookup(getNameKey(), I18nManager.getText("error.3d") + ": " + e.getMessage());
			}
		}
	}
}
