package tim.prune.function.settings;

import tim.prune.*;
import tim.prune.config.Config;
import tim.prune.gui.GuiGridLayout;
import tim.prune.gui.colour.WaypointColours;
import tim.prune.gui.map.WpIconLibrary;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;

/**
 * Class to show the dialog for setting the waypoint display settings
 * like icons, colours
 */
public class SetWaypointSettings extends GenericFunction
{
	/**
	 * Inner class to render waypoint icons
	 */
	static class IconComboRenderer extends JLabel implements ListCellRenderer<Integer>
	{
		/** Cached icons for each waypoint type */
		private final ImageIcon[] _icons = new ImageIcon[WpIconLibrary.WAYPT_NUMBER_OF_ICONS];

		/** Constructor */
		IconComboRenderer() {
			setOpaque(true);
		}

		/** Get the label text at the given index */
		private String getLabel(int inIndex) {
			return I18nManager.getText("dialog.displaysettings.wpicon." + WpIconLibrary.getIconName(inIndex));
		}

		/** Get the image icon at the given index */
		private ImageIcon getIcon(int inIndex)
		{
			if (_icons[inIndex] == null) {
				_icons[inIndex] = WpIconLibrary.getIconDefinition(inIndex, 1).getImageIcon();
			}
			return _icons[inIndex];
		}

		/** @return a label to display the combo box entry */
		public Component getListCellRendererComponent(
			JList<? extends Integer> inList, Integer inValue, int inIndex,
			boolean inSelected, boolean inFocus)
		{
			if (inSelected) {
				setBackground(inList.getSelectionBackground());
				setForeground(inList.getSelectionForeground());
			}
			else {
				setBackground(inList.getBackground());
				setForeground(inList.getForeground());
			}
			setIcon(getIcon(inValue));
			setText(getLabel(inValue));
			return this;
		}
	}


	// Members of SetDisplaySettings
	private JDialog _dialog = null;
	private JComboBox<Integer> _wpIconCombobox = null;
	private JRadioButton[] _sizeRadioButtons = null;
	private JCheckBox _coloursCheckbox = null;
	/** Slider for salt */
	private JSlider _saltSlider = null;
	private JLabel _saltLabel = null;


	/**
	 * Constructor
	 * @param inApp app object
	 */
	public SetWaypointSettings(App inApp) {
		super(inApp);
	}

	/**
	 * Return the name key for this function
	 */
	public String getNameKey() {
		return "function.setwaypointdisplay";
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

		// Panel for waypoint icons
		JPanel iconsPanel = new JPanel();
		iconsPanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(3, 3, 3, 3))
		);
		iconsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		iconsPanel.setLayout(new BoxLayout(iconsPanel, BoxLayout.Y_AXIS));
		// Select which waypoint icon to use
		JPanel iconPanel = new JPanel();
		GuiGridLayout iconGrid = new GuiGridLayout(iconPanel);
		JLabel headerLabel = new JLabel(I18nManager.getText("dialog.displaysettings.waypointicons"));
		headerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		iconGrid.add(headerLabel);
		_wpIconCombobox = new JComboBox<>(new Integer[]{0, 1, 2, 3, 4});
		_wpIconCombobox.setRenderer(new IconComboRenderer());
		iconGrid.add(_wpIconCombobox);
		iconsPanel.add(iconPanel);
		// Select size of waypoints
		JPanel sizePanel = new JPanel();
		sizePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		_sizeRadioButtons = new JRadioButton[3];
		ButtonGroup sizeRadioGroup = new ButtonGroup();
		final String[] sizeKeys = {"small", "medium", "large"};
		for (int i=0; i<3; i++)
		{
			_sizeRadioButtons[i] = new JRadioButton(I18nManager.getText("dialog.displaysettings.size." + sizeKeys[i]));
			sizeRadioGroup.add(_sizeRadioButtons[i]);
			sizePanel.add(_sizeRadioButtons[i]);
		}
		iconsPanel.add(sizePanel);
		midPanel.add(iconsPanel);
		midPanel.add(Box.createVerticalStrut(15));

		// Panel for colours of waypoint icons
		JPanel coloursPanel = new JPanel();
		coloursPanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(3, 3, 3, 3))
		);
		coloursPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		coloursPanel.setLayout(new BoxLayout(coloursPanel, BoxLayout.Y_AXIS));
		_coloursCheckbox = new JCheckBox(I18nManager.getText("dialog.waypointsettings.usecolours"));
		coloursPanel.add(_coloursCheckbox);
		coloursPanel.add(Box.createVerticalStrut(10));
		_saltSlider = new JSlider(0, WaypointColours.getMaxSalt());
		coloursPanel.add(_saltSlider);
		_saltLabel = new JLabel("some label");
		coloursPanel.add(_saltLabel);
		midPanel.add(coloursPanel);
		// attach signals
		_saltSlider.addChangeListener(changeEvent -> setSaltLabel(_saltSlider.getValue()));
		_coloursCheckbox.addChangeListener(changeEvent -> coloursSwitched());

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
	 * React to enabling / disabling checkbox controlling colours of waypoints
	 */
	private void coloursSwitched()
	{
		final boolean coloursOn = _coloursCheckbox.isSelected();
		setSaltLabel(coloursOn ? Math.max(_saltSlider.getValue(), 0) : -1);
		_saltSlider.setEnabled(coloursOn);
	}

	/**
	 * Show window
	 */
	public void begin()
	{
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, I18nManager.getText(getNameKey()));
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.getContentPane().add(makeContents());
			_dialog.pack();
		}
		// Set values from config
		_wpIconCombobox.setSelectedIndex(Config.getConfigInt(Config.KEY_WAYPOINT_ICONS));
		selectIconSizeRadio(Config.getConfigInt(Config.KEY_WAYPOINT_ICON_SIZE));
		final int salt = Config.getConfigInt(Config.KEY_WPICON_SALT);
		_coloursCheckbox.setSelected(salt >= 0);
		_saltSlider.setValue(Math.max(salt, 0));
		_saltSlider.setEnabled(salt >= 0);
		setSaltLabel(salt);
		_dialog.setVisible(true);
	}

	/**
	 * @param salt current salt value from slider
	 */
	private void setSaltLabel(int salt) {
		final String label = salt < 0 ? "" : (I18nManager.getText("dialog.waypointsettings.saltvalue") + " : " + salt);
		_saltLabel.setText(label);
	}

	/**
	 * Select the corresponding radio button according to the numeric value
	 * @param inValue numeric value saved in Config
	 */
	private void selectIconSizeRadio(int inValue)
	{
		if (inValue < 0 || inValue >= _sizeRadioButtons.length) {
			inValue = 1;
		}
		if (_sizeRadioButtons[inValue] != null) {
			_sizeRadioButtons[inValue].setSelected(true);
		}
	}

	/**
	 * @return numeric value of selected icon size according to radio buttons
	 */
	private int getSelectedIconSize()
	{
		for (int i=0; i<_sizeRadioButtons.length; i++)
		{
			if (_sizeRadioButtons[i] != null && _sizeRadioButtons[i].isSelected()) {
				return i;
			}
		}
		return 1; // default is medium
	}

	/**
	 * Save settings and close
	 */
	public void finish()
	{
		// update config
		Config.setConfigInt(Config.KEY_WAYPOINT_ICONS, _wpIconCombobox.getSelectedIndex());
		Config.setConfigInt(Config.KEY_WAYPOINT_ICON_SIZE, getSelectedIconSize());
		final int saltValue = _coloursCheckbox.isSelected() ? _saltSlider.getValue() : -1;
		Config.setConfigInt(Config.KEY_WPICON_SALT, saltValue);
		// refresh display
		UpdateMessageBroker.informSubscribers(DataSubscriber.MAPSERVER_CHANGED);
		_dialog.dispose();
	}
}
