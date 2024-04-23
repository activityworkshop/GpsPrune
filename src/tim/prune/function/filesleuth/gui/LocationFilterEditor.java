package tim.prune.function.filesleuth.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.Unit;
import tim.prune.data.UnitSetLibrary;
import tim.prune.data.Coordinate.Format;
import tim.prune.function.filesleuth.data.LocationFilter;
import tim.prune.gui.GuiGridLayout;
import tim.prune.gui.RadioButtonGroup;
import tim.prune.gui.WholeNumberField;

/** Dialog to define a location filter for the search */
public class LocationFilterEditor extends JDialog
{
	private final LocationFilterUser _parent;
	private DataPoint _selectedPoint;
	private JRadioButton _noFilterRadio = null, _pointFilterRadio = null;
	private JComboBox<String> _unitsDropdown = null;
	private WholeNumberField _distanceField = null;
	private JTextField _pointField = null;
	private JButton _okButton = null;


	public LocationFilterEditor(JDialog inDialog, LocationFilterUser inUser)
	{
		super(inDialog, I18nManager.getText("dialog.findfile.locationfilter"), true); // modal
		_parent = inUser;
		setLocationRelativeTo(inDialog);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		getContentPane().add(makeDialogComponents());
		pack();
	}

	/**
	 * Create dialog components
	 * @return Panel containing all gui elements in dialog
	 */
	private Component makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout(0, 10));
		dialogPanel.add(new JLabel(I18nManager.getText("dialog.locationfilter.desc")), BorderLayout.NORTH);

		// central panel for limits
		JPanel limitsPanel = new JPanel();
		limitsPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		GuiGridLayout grid = new GuiGridLayout(limitsPanel, new double[] {0.5, 1.0},
			new boolean[] {false, false});
		// radio buttons grouped together
		_noFilterRadio = new JRadioButton(I18nManager.getText("dialog.locationfilter.nofilter"));
		_noFilterRadio.setSelected(true);
		_pointFilterRadio = new JRadioButton(I18nManager.getText("dialog.locationfilter.distance") + ": ");
		new RadioButtonGroup(_noFilterRadio, _pointFilterRadio);
		_noFilterRadio.addItemListener(e -> preview());
		_pointFilterRadio.addItemListener(e -> preview());

		grid.add(_noFilterRadio);
		grid.nextRow();
		grid.add(_pointFilterRadio);
		JPanel distancePanel = new JPanel();
		distancePanel.setLayout(new FlowLayout());
		_distanceField = new WholeNumberField(3);
		_distanceField.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				super.keyReleased(e);
				_pointFilterRadio.setSelected(true);
				preview();
			}
		});
		distancePanel.add(_distanceField);
		String[] distUnitsOptions = {I18nManager.getText("units.kilometres"), I18nManager.getText("units.metres"),
			I18nManager.getText("units.miles")};
		_unitsDropdown = new JComboBox<String>(distUnitsOptions);
		distancePanel.add(_unitsDropdown);
		grid.add(distancePanel);
		grid.add(new JLabel(""));
		JPanel pointPanel = new JPanel();
		pointPanel.setLayout(new FlowLayout());
		pointPanel.add(new JLabel(I18nManager.getText("dialog.locationfilter.frompoint")));
		_pointField = new JTextField(15);
		_pointField.setEditable(false);
		pointPanel.add(_pointField);
		grid.add(pointPanel);
		dialogPanel.add(limitsPanel, BorderLayout.CENTER);

		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		_okButton = new JButton(I18nManager.getText("button.ok"));
		ActionListener okListener = e -> finish();
		_okButton.addActionListener(okListener);
		_okButton.setEnabled(false);
		buttonPanel.add(_okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(e -> dispose());
		buttonPanel.add(cancelButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		dialogPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 15));
		return dialogPanel;
	}

	public void show(LocationFilter inFilter, DataPoint inPoint)
	{
		_selectedPoint = inPoint;
		final boolean hasFilter = inFilter != null && inPoint != null;
		_noFilterRadio.setSelected(!hasFilter);
		_pointFilterRadio.setSelected(hasFilter);
		_pointField.setText(describePoint(inPoint));
		preview();
		setVisible(true);
	}

	private void preview()
	{
		final boolean pointSelected = _selectedPoint != null;
		_pointFilterRadio.setEnabled(pointSelected);
		_distanceField.setEnabled(pointSelected);
		_unitsDropdown.setEnabled(pointSelected);
		_pointField.setEnabled(pointSelected);
		if (!pointSelected) {
			_noFilterRadio.setSelected(true);
		}
		boolean ok = _noFilterRadio.isSelected()
			|| (pointSelected && _distanceField.getValue() > 0);
		_okButton.setEnabled(ok);
	}

	private LocationFilter getFilter()
	{
		if (_noFilterRadio.isSelected() || _selectedPoint == null) {
			return null;
		}
		final Unit[] distUnits = {UnitSetLibrary.UNITS_KILOMETRES,
			UnitSetLibrary.UNITS_METRES, UnitSetLibrary.UNITS_MILES};
		Unit distUnit = distUnits[_unitsDropdown.getSelectedIndex()];
		return new LocationFilter(_selectedPoint, describePoint(_selectedPoint),
			_distanceField.getValue(), distUnit);
	}

	/** Ok button has been pressed, so close dialog and pass the filter back to parent */
	private void finish()
	{
		_parent.updateLocationFilter(getFilter());
		dispose();
	}

	static String describePoint(DataPoint inPoint)
	{
		if (inPoint == null) {
			return "";
		}
		if (inPoint.isWaypoint()) {
			return inPoint.getWaypointName();
		}
		return inPoint.getLatitude().output(Format.DEG, 3) + ", "
				+ inPoint.getLongitude().output(Format.DEG, 3);
	}
}
