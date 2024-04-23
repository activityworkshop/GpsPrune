package tim.prune.function;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.cmd.InsertPointCmd;
import tim.prune.data.Altitude;
import tim.prune.data.Coordinate;
import tim.prune.data.DataPoint;
import tim.prune.data.Latitude;
import tim.prune.data.Longitude;
import tim.prune.data.Unit;
import tim.prune.data.UnitSetLibrary;
import tim.prune.gui.GuiGridLayout;

/**
 * Class to provide the function to paste coordinates
 * - see wikipedia, opencaching.de, waymarking.com etc
 */
public class PasteCoordinates extends GenericFunction
{
	private JDialog _dialog = null;
	private JTextField _nameField = null;
	private JTextField _coordField = null;
	private JButton _okButton = null;
	private JComboBox<String> _altUnitsDropDown;


	/**
	 * Constructor
	 * @param inApp application object for callback
	 */
	public PasteCoordinates(App inApp) {
		super(inApp);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.pastecoordinates";
	}

	/**
	 * Begin the function
	 */
	public void begin()
	{
		// Make dialog window
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, getName(), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			_dialog.getContentPane().add(makeDialogComponents());
			_dialog.pack();
		}
		// MAYBE: Paste clipboard into the edit field
		_coordField.setText("");
		_nameField.setText("");
		boolean useMetres = (getConfig().getUnitSet().getAltitudeUnit() == UnitSetLibrary.UNITS_METRES);
		_altUnitsDropDown.setSelectedIndex(useMetres?0:1);
		enableOK();
		_dialog.setVisible(true);
	}


	/**
	 * Create dialog components
	 * @return Panel containing all gui elements in dialog
	 */
	private Component makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout(0, 10));
		dialogPanel.add(new JLabel(I18nManager.getText("dialog.pastecoordinates.desc")), BorderLayout.NORTH);
		JPanel mainPanel = new JPanel();
		GuiGridLayout grid = new GuiGridLayout(mainPanel);
		_coordField = new JTextField("", 25);
		// Listeners to enable/disable ok button
		KeyAdapter keyListener = new KeyAdapter() {
			/** Key released */
			public void keyReleased(KeyEvent inE) {
				enableOK();
				if (inE.getKeyCode() == KeyEvent.VK_ESCAPE) {
					_dialog.dispose();
				}
			}
		};
		MouseAdapter mouseListener = new MouseAdapter() {
			public void mouseReleased(MouseEvent inE) {
				enableOK();
			}
		};
		_coordField.addKeyListener(keyListener);
		_coordField.addMouseListener(mouseListener);
		JLabel coordLabel = new JLabel(I18nManager.getText("dialog.pastecoordinates.coords"));
		coordLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		grid.add(coordLabel);
		grid.add(_coordField);
		// Altitude format (if any)
		JLabel formatLabel = new JLabel(I18nManager.getText("dialog.openoptions.altitudeunits"));
		formatLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		grid.add(formatLabel);
		final String[] altunits = {I18nManager.getText("units.metres"), I18nManager.getText("units.feet")};
		_altUnitsDropDown = new JComboBox<String>(altunits);
		grid.add(_altUnitsDropDown);
		// Waypoint name
		JLabel nameLabel = new JLabel(I18nManager.getText("dialog.pointnameedit.name"));
		nameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		grid.add(nameLabel);
		_nameField = new JTextField("", 12);
		grid.add(_nameField);
		dialogPanel.add(mainPanel, BorderLayout.CENTER);
		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		_okButton = new JButton(I18nManager.getText("button.ok"));
		ActionListener okListener = e -> {
			if (_okButton.isEnabled()) {finish();}
		};
		_okButton.addActionListener(okListener);
		_okButton.setEnabled(false);
		_coordField.addActionListener(okListener);
		_nameField.addActionListener(okListener);
		buttonPanel.add(_okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(e -> _dialog.dispose());
		buttonPanel.add(cancelButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		dialogPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 15));
		return dialogPanel;
	}

	/**
	 * Enable or disable the OK button based on the contents of the text field
	 */
	private void enableOK()
	{
		String text = _coordField.getText();
		_okButton.setEnabled(text != null && text.length() > 6
			&& (text.indexOf(' ') >= 0 || text.indexOf(',') >= 0));
	}

	/**
	 * Finish the dialog when OK pressed
	 */
	private void finish()
	{
		final Unit altUnits = (_altUnitsDropDown.getSelectedIndex()==0 ?
			UnitSetLibrary.UNITS_METRES : UnitSetLibrary.UNITS_FEET);
		final DataPoint point = makePoint(_coordField.getText().trim(), altUnits);
		if (point == null) {
			JOptionPane.showMessageDialog(_parentFrame,
				I18nManager.getText("dialog.pastecoordinates.nothingfound"),
				getName(), JOptionPane.ERROR_MESSAGE);
		}
		else
		{
			// See if name was entered
			String name = _nameField.getText();
			if (name != null && name.length() > 0) {
				point.setWaypointName(name);
			}
			else {
				point.setSegmentStart(true);
			}

			InsertPointCmd command = new InsertPointCmd(point, -1);
			command.setDescription(I18nManager.getText("undo.createpoint"));
			command.setConfirmText(I18nManager.getText("confirm.pointadded"));
			_app.execute(command);
			_dialog.dispose();
		}
	}

	/**
	 * @param inString the entered coordinates
	 * @param inAltitudeUnits altitude units selected from the dropdown
	 * @return new point, or null if contents couldn't be parsed
	 */
	private static DataPoint makePoint(String inString, Unit inAltitudeUnits)
	{
		if (inString.length() < 3) {
			return null;
		}
		// Try to split using commas
		String[] items = inString.split(",");
		switch (items.length)
		{
			case 2:
				return parseValues(items[0].trim(), items[1].trim(), null, inAltitudeUnits);
			case 3:
				return parseValues(items[0].trim(), items[1].trim(), items[2].trim(), inAltitudeUnits);
		}

		// Try again using semicolons
		items = inString.split(";");
		switch (items.length)
		{
			case 2:
				return parseValues(items[0].trim(), items[1].trim(), null, inAltitudeUnits);
			case 3:
				return parseValues(items[0].trim(), items[1].trim(), items[2].trim(), inAltitudeUnits);
		}

		// Finally, try spaces
		items = inString.split(" ");
		switch (items.length)
		{
			case 2:
				return parseValues(items[0], items[1], null, inAltitudeUnits);
			case 3:
				if (items[1].length() == 1) {
					return parseValues(items[0], items[2], null, inAltitudeUnits);
				}
				break;
			case 4:
				return parseValues(items[0] + " " + items[1],
					items[2] + " " + items[3], null, inAltitudeUnits);
			case 6:
				return parseValues(items[0] + " " + items[1] + " " + items[2],
					items[3] + " " + items[4] + " " + items[5], null, inAltitudeUnits);
			case 8:
				// eight elements, so try four and four
				String firstFour = items[0] + " " + items[1] + " " + items[2] + " " + items[3];
				String secondFour = items[4] + " " + items[5] + " " + items[6] + " " + items[7];
				return parseValues(firstFour, secondFour, null, inAltitudeUnits);
		}
		// Nothing found
		return null;
	}

	/**
	 * Try to parse the three given Strings into lat, lon and alt
	 * @param inValue1 first value (either lat/lon)
	 * @param inValue2 second value (either lon/lat)
	 * @param inValue3 altitude value or null if absent
	 * @param inAltitudeUnits selected altitude units
	 * @return DataPoint object or null if failed
	 */
	private static DataPoint parseValues(String inValue1, String inValue2,
		String inValue3, Unit inAltitudeUnits)
	{
		// Check for parseable altitude
		Altitude alt = null;
		if (inValue3 != null)
		{
			// Use selected altitude units
			alt = new Altitude(inValue3, inAltitudeUnits);
			if (!alt.isValid()) {alt = null;}
		}
		// See if value1 can be lat and value2 lon:
		Coordinate coord1 = Latitude.make(inValue1);
		Coordinate coord2 = Longitude.make(inValue2);
		if (coord1 != null && Latitude.hasCardinal(inValue1)
			&& coord2 != null && Longitude.hasCardinal(inValue2))
		{
			return new DataPoint(coord1, coord2, alt);
		}
		// Now see if lat/lon are reversed
		Coordinate coord3 = Longitude.make(inValue1);
		Coordinate coord4 = Latitude.make(inValue2);
		if (coord3 != null && Longitude.hasCardinal(inValue1)
			&& coord4 != null && Latitude.hasCardinal(inValue2))
		{
			// reversed order
			return new DataPoint(coord4, coord3, alt);
		}
		// Didn't work without guessing cardinals, so accept latitude, longitude order (if valid)
		if (coord1 != null && coord2 != null) {
			return new DataPoint(coord1, coord2, alt);
		}
		// Or accept other order (if valid)
		if (coord3 != null && coord4 != null) {
			// reversed order
			return new DataPoint(coord4, coord3, alt);
		}
		// Couldn't be parsed either way
		return null;
	}
}
