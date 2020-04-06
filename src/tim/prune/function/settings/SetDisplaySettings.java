package tim.prune.function.settings;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.ListCellRenderer;
import javax.swing.border.EtchedBorder;

import tim.prune.App;
import tim.prune.DataSubscriber;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.config.Config;
import tim.prune.gui.GuiGridLayout;
import tim.prune.gui.WholeNumberField;
import tim.prune.gui.map.WpIconLibrary;

/**
 * Class to show the dialog for setting the display settings
 * like line width, antialiasing, waypoint icons
 */
public class SetDisplaySettings extends GenericFunction
{
	/**
	 * Inner class to render waypoint icons
	 */
	class IconComboRenderer extends JLabel implements ListCellRenderer<Integer>
	{
		/** Cached icons for each waypoint type */
		private ImageIcon[] _icons = new ImageIcon[WpIconLibrary.WAYPT_NUMBER_OF_ICONS];

		/** Constructor */
		IconComboRenderer()
		{
			setOpaque(true);
		}

		/** Get the label text at the given index */
		private String getLabel(int inIndex)
		{
			return I18nManager.getText("dialog.displaysettings.wpicon." + WpIconLibrary.getIconName(inIndex));
		}

		/** Get the image icon at the given index */
		private ImageIcon getIcon(int inIndex)
		{
			if (_icons[inIndex] == null)
			{
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
			} else {
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
	private WholeNumberField _lineWidthField = null;
	private JCheckBox _antialiasCheckbox = null;
	private JComboBox<Integer> _wpIconCombobox = null;
	private JRadioButton[] _sizeRadioButtons = null;
	private JRadioButton[] _windowStyleRadios = null;
	private JButton _okButton = null;

	private static final String STYLEKEY_NIMBUS = "javax.swing.plaf.nimbus.NimbusLookAndFeel";


	/**
	 * Constructor
	 * @param inApp app object
	 */
	public SetDisplaySettings(App inApp)
	{
		super(inApp);
	}

	/**
	 * Return the name key for this function
	 */
	public String getNameKey()
	{
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

		// line width
		JLabel lineWidthLabel = new JLabel(I18nManager.getText("dialog.displaysettings.linewidth"));
		grid.add(lineWidthLabel);
		_lineWidthField = new WholeNumberField(1);
		grid.add(_lineWidthField);
		// Antialiasing
		_antialiasCheckbox = new JCheckBox(I18nManager.getText("dialog.displaysettings.antialias"), false);
		grid.add(_antialiasCheckbox);
		grid.add(new JLabel(""));

		linesPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		midPanel.add(linesPanel);
		midPanel.add(Box.createVerticalStrut(10));

		// Panel for waypoint icons
		JPanel waypointsPanel = new JPanel();
		waypointsPanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(3, 3, 3, 3))
		);
		waypointsPanel.setLayout(new BoxLayout(waypointsPanel, BoxLayout.Y_AXIS));
		// Select which waypoint icon to use
		JPanel iconPanel = new JPanel();
		GuiGridLayout iconGrid = new GuiGridLayout(iconPanel);
		JLabel headerLabel = new JLabel(I18nManager.getText("dialog.displaysettings.waypointicons"));
		headerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		iconGrid.add(headerLabel);
		_wpIconCombobox = new JComboBox<Integer>(new Integer[] {0, 1, 2, 3, 4});
		_wpIconCombobox.setRenderer(new IconComboRenderer());
		iconGrid.add(_wpIconCombobox);
		waypointsPanel.add(iconPanel);
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
		waypointsPanel.add(sizePanel);
		waypointsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		midPanel.add(waypointsPanel);

		// Panel for window style
		JPanel windowStylePanel = new JPanel();
		windowStylePanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(3, 3, 3, 3))
		);
		windowStylePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		windowStylePanel.add(new JLabel(I18nManager.getText("dialog.displaysettings.windowstyle")));
		windowStylePanel.add(Box.createHorizontalStrut(10));
		ButtonGroup styleGroup = new ButtonGroup();
		final String[] styleKeys = {"default", "nimbus"};
		_windowStyleRadios = new JRadioButton[2];
		for (int i=0; i<2; i++)
		{
			_windowStyleRadios[i] = new JRadioButton(
				I18nManager.getText("dialog.displaysettings.windowstyle." + styleKeys[i]));
			styleGroup.add(_windowStyleRadios[i]);
			windowStylePanel.add(_windowStyleRadios[i]);
		}
		midPanel.add(windowStylePanel);
		mainPanel.add(midPanel, BorderLayout.CENTER);

		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		// OK button
		_okButton = new JButton(I18nManager.getText("button.ok"));
		_okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				finish();
			}
		});
		buttonPanel.add(_okButton);
		// Cancel button
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_dialog.dispose();
			}
		});
		buttonPanel.add(cancelButton);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		return mainPanel;
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
		int lineWidth = Config.getConfigInt(Config.KEY_LINE_WIDTH);
		if (lineWidth < 1 || lineWidth > 4) {lineWidth = 2;}
		_lineWidthField.setValue(lineWidth);
		_antialiasCheckbox.setSelected(Config.getConfigBoolean(Config.KEY_ANTIALIAS));
		_wpIconCombobox.setSelectedIndex(Config.getConfigInt(Config.KEY_WAYPOINT_ICONS));
		selectIconSizeRadio(Config.getConfigInt(Config.KEY_WAYPOINT_ICON_SIZE));
		selectWindowStyleRadio(Config.getConfigString(Config.KEY_WINDOW_STYLE));
		_dialog.setVisible(true);
	}

	/**
	 * Select the corresponding radio button according to the numeric value
	 * @param inValue numeric value saved in Config
	 */
	private void selectIconSizeRadio(int inValue)
	{
		if (inValue < 0 || inValue >= _sizeRadioButtons.length)
		{
			inValue = 1;
		}
		if (_sizeRadioButtons[inValue] != null)
		{
			_sizeRadioButtons[inValue].setSelected(true);
		}
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
		_windowStyleRadios[selectedRadio].setSelected(true);
	}

	/**
	 * @return numeric value of selected icon size according to radio buttons
	 */
	private int getSelectedIconSize()
	{
		for (int i=0; i<_sizeRadioButtons.length; i++)
		{
			if (_sizeRadioButtons[i] != null && _sizeRadioButtons[i].isSelected())
			{
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
		int lineWidth = _lineWidthField.getValue();
		if (lineWidth < 1 || lineWidth > 4) {lineWidth = 2;}
		Config.setConfigInt(Config.KEY_LINE_WIDTH, lineWidth);
		Config.setConfigBoolean(Config.KEY_ANTIALIAS, _antialiasCheckbox.isSelected());
		Config.setConfigInt(Config.KEY_WAYPOINT_ICONS, _wpIconCombobox.getSelectedIndex());
		Config.setConfigInt(Config.KEY_WAYPOINT_ICON_SIZE, getSelectedIconSize());
		final String styleString = (_windowStyleRadios[1].isSelected() ? STYLEKEY_NIMBUS : null);
		Config.setConfigString(Config.KEY_WINDOW_STYLE, styleString);
		// refresh display
		UpdateMessageBroker.informSubscribers(DataSubscriber.MAPSERVER_CHANGED);
		_dialog.dispose();
	}
}
