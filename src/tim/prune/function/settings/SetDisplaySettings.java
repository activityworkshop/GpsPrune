package tim.prune.function.settings;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.Objects;

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
import javax.swing.border.EtchedBorder;

import tim.prune.App;
import tim.prune.DataSubscriber;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.config.Config;
import tim.prune.gui.GuiGridLayout;
import tim.prune.gui.WholeNumberField;


/**
 * Class to show the dialog for setting the display settings
 * like line width, antialiasing
 */
public class SetDisplaySettings extends GenericFunction
{
	private JDialog _dialog = null;
	private WholeNumberField _lineWidthField = null;
	private JCheckBox _antialiasCheckbox = null;
	private JCheckBox _osScalingCheckbox = null;
	private JCheckBox _doubledIconsCheckbox = null;
	private JRadioButton[] _windowStyleRadios = null;
	// settings when entering dialog in order to detect changes
	private String _previousStyle = null;
	private boolean _previousDoubleIcons = false;

	private static final String STYLEKEY_NIMBUS = "javax.swing.plaf.nimbus.NimbusLookAndFeel";
	private static final String STYLEKEY_GTK = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";


	/**
	 * Constructor
	 * @param inApp app object
	 */
	public SetDisplaySettings(App inApp) {
		super(inApp);
	}

	/**
	 * Return the name key for this function
	 */
	public String getNameKey() {
		return "function.setdisplaysettings";
	}

	/**
	 * @return the contents of the window as a Component
	 */
	private Component makeContents()
	{
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout(0, 5));
		JPanel midPanel = new JPanel();
		midPanel.setLayout(new BoxLayout(midPanel, BoxLayout.Y_AXIS));
		midPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

		JPanel linesPanel = new JPanel();
		linesPanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(3, 3, 3, 3))
		);
		GuiGridLayout grid = new GuiGridLayout(linesPanel);
		grid.setYPadding(8);

		// line width
		JLabel lineWidthLabel = new JLabel(I18nManager.getText("dialog.displaysettings.linewidth"));
		grid.add(lineWidthLabel);
		_lineWidthField = new WholeNumberField(1);
		grid.add(_lineWidthField);
		// Antialiasing
		_antialiasCheckbox = new JCheckBox(I18nManager.getText("dialog.displaysettings.antialias"), false);
		grid.add(_antialiasCheckbox);
		grid.add(new JLabel(""));
		// OS scaling
		_osScalingCheckbox = new JCheckBox(I18nManager.getText("dialog.displaysettings.allowosscaling"), false);
		grid.add(_osScalingCheckbox);
		grid.add(new JLabel(""));
		// Icon size
		_doubledIconsCheckbox = new JCheckBox(I18nManager.getText("dialog.displaysettings.doublesizedicons"), false);
		grid.add(_doubledIconsCheckbox);
		grid.add(new JLabel(""));

		linesPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		midPanel.add(linesPanel);
		midPanel.add(Box.createVerticalStrut(15));

		// Panel for window style
		JPanel windowStylePanel = new JPanel();
		windowStylePanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(3, 3, 3, 3))
		);
		windowStylePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		windowStylePanel.add(new JLabel(I18nManager.getText("dialog.displaysettings.windowstyle")));
		windowStylePanel.add(Box.createHorizontalStrut(10));
		ButtonGroup styleGroup = new ButtonGroup();
		final String[] styleKeys = {"default", "nimbus", "gtk"};
		_windowStyleRadios = new JRadioButton[3];
		for (int i=0; i<3; i++)
		{
			_windowStyleRadios[i] = new JRadioButton(
				I18nManager.getText("dialog.displaysettings.windowstyle." + styleKeys[i]));
			styleGroup.add(_windowStyleRadios[i]);
			if (i != 2 || platformHasPlaf(STYLEKEY_GTK))
			{
				windowStylePanel.add(_windowStyleRadios[i]);
			}
		}
		midPanel.add(windowStylePanel);
		mainPanel.add(midPanel, BorderLayout.CENTER);

		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		// OK button
		JButton okButton = new JButton(I18nManager.getText("button.ok"));
		okButton.addActionListener(e -> finish());
		buttonPanel.add(okButton);
		// Cancel button
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(e -> _dialog.dispose());
		buttonPanel.add(cancelButton);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		return mainPanel;
	}

	/**
	 * @return true if the specified style name is available on this platform
	 */
	private static boolean platformHasPlaf(String styleName)
	{
		for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
		{
			if (info.getClassName().equals(styleName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Show window
	 */
	public void begin()
	{
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, getName());
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.getContentPane().add(makeContents());
			_dialog.pack();
		}
		// Set values from config
		Config config = getConfig();
		int lineWidth = config.getConfigInt(Config.KEY_LINE_WIDTH);
		if (lineWidth < 1 || lineWidth > 4) {lineWidth = 2;}
		_lineWidthField.setValue(lineWidth);
		_antialiasCheckbox.setSelected(config.getConfigBoolean(Config.KEY_ANTIALIAS));
		_osScalingCheckbox.setSelected(config.getConfigBoolean(Config.KEY_OSSCALING));
		_doubledIconsCheckbox.setSelected(config.getConfigBoolean(Config.KEY_ICONS_DOUBLE_SIZE));
		selectWindowStyleRadio(config.getConfigString(Config.KEY_WINDOW_STYLE));
		// Remember what the current settings are
		_previousStyle = getSelectedStyleString();
		_previousDoubleIcons = _doubledIconsCheckbox.isSelected();
		_dialog.setVisible(true);
	}

	/**
	 * Select the corresponding radio button according to the selected style
	 * @param inValue style string saved in Config
	 */
	private void selectWindowStyleRadio(String inValue)
	{
		int selectedRadio = 0;
		if (inValue != null && inValue.equals(STYLEKEY_NIMBUS))
		{
			selectedRadio = 1;
		}
		else if (inValue != null && inValue.equals(STYLEKEY_GTK) && _windowStyleRadios[2] != null)
		{
			selectedRadio = 2;
		}
		_windowStyleRadios[selectedRadio].setSelected(true);
	}

	/**
	 * @return the style string according to the selected radio button
	 */
	private String getSelectedStyleString()
	{
		if (_windowStyleRadios[1].isSelected()) {
			return STYLEKEY_NIMBUS;
		}
		if (_windowStyleRadios[2] != null && _windowStyleRadios[2].isSelected()) {
			return STYLEKEY_GTK;
		}
		return null;
	}

	/**
	 * Save settings and close
	 */
	public void finish()
	{
		// update config
		int lineWidth = _lineWidthField.getValue();
		if (lineWidth < 1 || lineWidth > 4) {lineWidth = 2;}
		Config config = getConfig();
		config.setConfigInt(Config.KEY_LINE_WIDTH, lineWidth);
		config.setConfigBoolean(Config.KEY_ANTIALIAS, _antialiasCheckbox.isSelected());
		config.setConfigBoolean(Config.KEY_OSSCALING, _osScalingCheckbox.isSelected());
		config.setConfigBoolean(Config.KEY_ICONS_DOUBLE_SIZE, _doubledIconsCheckbox.isSelected());
		config.setConfigString(Config.KEY_WINDOW_STYLE, getSelectedStyleString());
		if (needsRestart())
		{
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("dialog.displaysettings.restart"),
				getName(), JOptionPane.INFORMATION_MESSAGE);
		}
		// refresh display
		UpdateMessageBroker.informSubscribers(DataSubscriber.MAPSERVER_CHANGED);
		_dialog.dispose();
	}

	/** @return true if either the double-icons or window style settings have changed */
	private boolean needsRestart()
	{
		return _doubledIconsCheckbox.isSelected() != _previousDoubleIcons
			|| !Objects.equals(_previousStyle, getSelectedStyleString());
	}
}
