package tim.prune.function;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.config.Config;
import tim.prune.gui.BaseImageDefinitionPanel;
import tim.prune.gui.DecimalNumberField;
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

	/** Field for sphere size */
	private DecimalNumberField _sphereSizeField = null;

	/** Field for rod size */
	private DecimalNumberField _rodSizeField = null;

	/** CheckBox for scaling */
	private JCheckBox _scaleButton = null;

	/** RadioButtons for orthographic projection */
	private JRadioButton _orthographicButton = null;

	/** RadioButtons for perspektive projection */
	private JRadioButton _perspectiveButton = null;

	/** Radio buttons for style - balls on sticks or tubes or spheres */
	private JRadioButton _ballsAndSticksButton = null;
	private JRadioButton _spheresButton = null;

	/** Radio button for lighting */
	private JRadioButton _standardLightingButton = null;
	private JRadioButton _cartographicLightingButton = null;

	/** Component for defining the base image */
	private BaseImageDefinitionPanel _baseImagePanel = null;

	/** Component for defining the terrain */
	private TerrainDefinitionPanel _terrainPanel = null;

	/**
	 * Constructor
	 * @param inApp app object
	 */
	public ShowThreeDFunction(App inApp)
	{
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
			JOptionPane.showMessageDialog(
				_parentFrame, I18nManager.getText("error.function.nojava3d"),
				I18nManager.getText("error.function.notavailable.title"),
				JOptionPane.WARNING_MESSAGE);
		}
		else
		{
			// See if the track has any altitudes at all - if not, show a tip
			// to use SRTM
			if (!_app.getTrackInfo().getTrack().hasAltitudeData()) {
				_app.showTip(TipManager.Tip_UseSrtmFor3d);
			}
			// Show a dialog to get the parameters
			if (_dialog == null)
			{
				_dialog = new JDialog(_app.getFrame(),
					I18nManager.getText(getNameKey()), true);
				_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				_dialog.getContentPane().add(makeDialogComponents());
				_dialog.pack();
			}
			final int exaggFactor = Config.getConfigInt(
				Config.KEY_HEIGHT_EXAGGERATION);
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
		// border for panels of dialog
		Border panelBorder = BorderFactory.createEmptyBorder(2, 4, 2, 4);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout(4, 4));

		JPanel innerPanel = new JPanel();
		innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));

		// Panel for altitude exaggeration, sphere size, rod size
		JPanel exaggPanel = new JPanel();
		exaggPanel.setLayout(new GridLayout(0, 2, 10, 4));
		exaggPanel.setBorder(panelBorder);

		JLabel exaggLabel = new JLabel(
			I18nManager.getText("dialog.3d.altitudefactor"));
		exaggLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		exaggPanel.add(exaggLabel);

		_exaggField = new DecimalNumberField(); // don't allow negative numbers
		_exaggField.setText("5.0");
		exaggPanel.add(_exaggField);

		/** TODO ph */
		// Size of spheres
		JLabel spheresSizeLabel = new JLabel(
			I18nManager.getText("dialog.3d.spheresize"));
		spheresSizeLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		exaggPanel.add(spheresSizeLabel);
		_sphereSizeField = new DecimalNumberField();
		_sphereSizeField.setText("0.2");
		exaggPanel.add(_sphereSizeField);

		// Size of rods
		JLabel rodSizeLabel = new JLabel(
			I18nManager.getText("dialog.3d.rodsize"));
		rodSizeLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		exaggPanel.add(rodSizeLabel);
		_rodSizeField = new DecimalNumberField();
		_rodSizeField.setText("0.1");
		exaggPanel.add(_rodSizeField);

		innerPanel.add(exaggPanel);
		innerPanel.add(Box.createVerticalStrut(4));

		// Radio buttons for style - balls on sticks or tubes or spheres
		JPanel stylePanel = new JPanel();
		stylePanel.setLayout(new GridLayout(0, 2, 10, 4));
		JLabel styleLabel = new JLabel(
			I18nManager.getText("dialog.3d.modelstyle"));
		styleLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		stylePanel.add(styleLabel);

		JPanel radioPanel = new JPanel();
		radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS));
		radioPanel.setBorder(panelBorder);
		_ballsAndSticksButton = new JRadioButton(
			I18nManager.getText("dialog.3d.ballsandsticks"));
		_ballsAndSticksButton.setSelected(false);
		radioPanel.add(_ballsAndSticksButton);

		_spheresButton = new JRadioButton(
			I18nManager.getText("dialog.3d.spheres"));
		_spheresButton.setSelected(true);
		radioPanel.add(_spheresButton);

		ButtonGroup group = new ButtonGroup();
		group.add(_ballsAndSticksButton);
		group.add(_spheresButton);
		stylePanel.add(radioPanel);

		innerPanel.add(stylePanel);
		innerPanel.add(Box.createVerticalStrut(4));

		// Panel with radio buttons for projection type
		JPanel projectionPanel = new JPanel();
		projectionPanel.setLayout(new GridLayout(0, 2, 10, 4));
		projectionPanel.setBorder(panelBorder);
		JLabel projectionLabel = new JLabel(
			I18nManager.getText("dialog.3d.projection"));
		projectionLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		projectionPanel.add(projectionLabel);

		JPanel radioPanel2 = new JPanel();
		radioPanel2.setLayout(new BoxLayout(radioPanel2, BoxLayout.Y_AXIS));
		radioPanel2.setBorder(panelBorder);
		_perspectiveButton = new JRadioButton(
			I18nManager.getText("dialog.3d.perspective"));
		_perspectiveButton.setSelected(false);
		radioPanel2.add(_perspectiveButton);

		_orthographicButton = new JRadioButton(
			I18nManager.getText("dialog.3d.orthographic"));
		_orthographicButton.setSelected(true);
		radioPanel2.add(_orthographicButton);

		ButtonGroup group2 = new ButtonGroup();
		group2.add(_perspectiveButton);
		group2.add(_orthographicButton);
		projectionPanel.add(radioPanel2);

		innerPanel.add(projectionPanel);
		innerPanel.add(Box.createVerticalStrut(4));

		// Radio buttons for lighting (cartographic or 3 lights standard)
		JPanel lightingPanel = new JPanel();
		lightingPanel.setLayout(new GridLayout(0, 2, 10, 4));
		lightingPanel.setBorder(panelBorder);
		JLabel lightingLabel = new JLabel(
			I18nManager.getText("dialog.3d.lighting"));
		lightingLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		lightingPanel.add(lightingLabel);

		JPanel radioPanel3 = new JPanel();
		radioPanel3.setLayout(new BoxLayout(radioPanel3, BoxLayout.Y_AXIS));
		radioPanel3.setBorder(panelBorder);
		_standardLightingButton = new JRadioButton(
			I18nManager.getText("dialog.3d.standardlighting"));
		_standardLightingButton.setSelected(true);
		radioPanel3.add(_standardLightingButton);

		_cartographicLightingButton = new JRadioButton(
			I18nManager.getText("dialog.3d.cartographiclighting"));
		_cartographicLightingButton.setSelected(false);
		radioPanel3.add(_cartographicLightingButton);

		ButtonGroup group3 = new ButtonGroup();
		group3.add(_standardLightingButton);
		group3.add(_cartographicLightingButton);
		lightingPanel.add(radioPanel3);

		innerPanel.add(lightingPanel);
		innerPanel.add(Box.createVerticalStrut(4));

		// Button to select scale
		JPanel scalePanel = new JPanel();
		scalePanel.setLayout(new GridLayout(0, 2, 10, 4));
		scalePanel.setBorder(panelBorder);
		JLabel scaleLabel = new JLabel(
			I18nManager.getText("dialog.3d.showscale"));
		scaleLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		scalePanel.add(scaleLabel);
		_scaleButton = new JCheckBox();
		_scaleButton.setSelected(true);
		scalePanel.add(_scaleButton);

		innerPanel.add(scalePanel);
		innerPanel.add(Box.createVerticalStrut(4));

		// Panel for terrain
		_terrainPanel = new TerrainDefinitionPanel();
		innerPanel.add(_terrainPanel);
		mainPanel.add(innerPanel, BorderLayout.NORTH);
		innerPanel.add(Box.createVerticalStrut(4));

		// Panel for base image (null because we don't need callback)
		_baseImagePanel = new BaseImageDefinitionPanel(
			null, _dialog, _app.getTrackInfo().getTrack());
		innerPanel.add(_baseImagePanel);

		// OK, Cancel buttons
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton okButton = new JButton(I18nManager.getText("button.ok"));
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_dialog.dispose();
				new Thread(new Runnable() {
					public void run() {
						finish();  // needs to be in separate thread
					}
				}).start();
			}
		});
		buttonPanel.add(okButton);
		JButton cancelButton = new JButton(
			I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_dialog.dispose();
			}
		});
		buttonPanel.add(cancelButton);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);

		return mainPanel;
	}

	/**
	 * All parameters have been selected in the input dialog, now we can go to
	 * the 3d window.
	 */
	private void finish()
	{
		// Store exaggeration factor and grid size in config
		Config.setConfigInt(Config.KEY_HEIGHT_EXAGGERATION,
			(int) (_exaggField.getValue() * 100));
		int terrainGridSize = _terrainPanel.getGridSize();
		if (terrainGridSize < 20) {terrainGridSize = 20;}
		Config.setConfigInt(Config.KEY_TERRAIN_GRID_SIZE, terrainGridSize);

		ThreeDWindow window = WindowFactory.getWindow(_parentFrame);
		if (window != null)
		{
			try
			{
				/** TODO ph */
				// Pass the parameters to use and show the window
				window.setTrack(_app.getTrackInfo().getTrack());
				window.setAltitudeFactor(_exaggField.getValue());
				window.setSphereSize((float) _sphereSizeField.getValue());
				window.setRodSize((float) _rodSizeField.getValue());
				window.setStyle(_ballsAndSticksButton.isSelected());
				window.setCartographic(
					_cartographicLightingButton.isSelected());
				window.setProjection(_orthographicButton.isSelected());
				window.setShowScale(_scaleButton.isSelected());

				// Also pass the base image parameters from input dialog
				window.setBaseImageParameters(
					_baseImagePanel.getImageDefinition());
				window.setTerrainParameters(new TerrainDefinition(
					_terrainPanel.getUseTerrain(), _terrainPanel.getGridSize()));
				window.setDataStatus(_app.getCurrentDataStatus());
				window.show();
			}
			catch (ThreeDException e)
			{
				_app.showErrorMessageNoLookup(getNameKey(),
					I18nManager.getText("error.3d") + ": " + e.getMessage());
			}
		}
	}
}
