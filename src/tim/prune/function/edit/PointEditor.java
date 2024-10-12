package tim.prune.function.edit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.cmd.EditPointCmd;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.FieldList;
import tim.prune.data.Unit;
import tim.prune.function.Describer;

/**
 * Function to manage the display and editing of point data
 */
public class PointEditor extends GenericFunction
{
	private JDialog _dialog = null;
	private JTable _table = null;
	private JLabel _fieldnameLabel = null;
	private JTextField _valueField = null;
	private JTextArea _valueArea = null;
	private JScrollPane _valueAreaPane = null;
	private FieldList _fieldList = null;
	private DataPoint _point = null;
	private EditFieldsTableModel _model = null;
	private JButton _cancelButton = null;
	private int _prevRowIndex = -1;


	/**
	 * Constructor
	 * @param inApp application object to inform of success
	 */
	public PointEditor(App inApp) {
		super(inApp);
	}

	@Override
	public String getNameKey() {
		return "menu.point.editpoint";
	}

	/**
	 * Begin the function by showing the edit point dialog
	 */
	public void begin()
	{
		_point = _app.getTrackInfo().getCurrentPoint();
		_dialog = new JDialog(_parentFrame, I18nManager.getText("dialog.pointedit.title"), true);
		_dialog.setLocationRelativeTo(_parentFrame);
		// Check field list
		FieldList trackFieldList = _app.getTrackInfo().getTrack().getFieldList();
		_fieldList = makeFieldList(trackFieldList, _point);
		int numFields = _fieldList.getNumFields();
		// Create table model for point editor
		_model = new EditFieldsTableModel(numFields);
		for (int i=0; i<numFields; i++)
		{
			Field field = _fieldList.getField(i);
			_model.addFieldInfo(field.getName(), _point.getFieldValue(field), i);
		}
		// Create Gui
		_dialog.getContentPane().add(makeDialogComponents());
		_dialog.pack();
		// Init right-hand side
		SwingUtilities.invokeLater(() -> {
			_valueField.setVisible(false);
			_valueAreaPane.setVisible(false);
			_cancelButton.requestFocus();
		});
		_prevRowIndex = -1;
		_dialog.setVisible(true);
	}


	private FieldList makeFieldList(FieldList inFieldList, DataPoint inPoint)
	{
		FieldList result = new FieldList();
		ArrayList<Field> blankFields = new ArrayList<>();
		// Add the fields which the point has values for
		for (int i=0; i<inFieldList.getNumFields(); i++)
		{
			Field field = inFieldList.getField(i);
			String value = inPoint.getFieldValue(field);
			if (value == null || value.isEmpty()) {
				blankFields.add(field);
			}
			else {
				result.addField(field);
			}
		}
		// Now add the fields which are blank
		for (Field field : blankFields) {
			result.addField(field);
		}
		// Now add additional ones which may not be in the master field list
		result.addFields(Field.ALTITUDE, Field.COMMENT, Field.DESCRIPTION, Field.TIMESTAMP,
			Field.WAYPT_NAME, Field.WAYPT_TYPE, Field.SYMBOL);
		return result;
	}

	/**
	 * Make the dialog components
	 * @return the GUI components for the dialog
	 */
	private Component makeDialogComponents()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(20, 10));
		// Create GUI layout for point editor
		_table = new EditTable(_model);
		_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_table.getSelectionModel().clearSelection();
		_table.getSelectionModel().addListSelectionListener(e -> fieldSelected());
		_table.setPreferredScrollableViewportSize(new Dimension(_table.getWidth() * 2, _table.getRowHeight() * 6));
		JScrollPane tablePane = new JScrollPane(_table);
		tablePane.setPreferredSize(new Dimension(150, 100));

		// Label at top
		JLabel topLabel = new JLabel(I18nManager.getText("dialog.pointedit.intro"));
		topLabel.setBorder(BorderFactory.createEmptyBorder(8, 6, 3, 6));
		panel.add(topLabel, BorderLayout.NORTH);

		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BorderLayout());
		rightPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		JPanel rightiPanel = new JPanel();
		rightiPanel.setLayout(new BoxLayout(rightiPanel, BoxLayout.Y_AXIS));
		// Add GUI elements to rhs
		_fieldnameLabel = new JLabel(I18nManager.getText("dialog.pointedit.nofield"));
		rightiPanel.add(_fieldnameLabel);
		_valueField = new JTextField(11);
		// Add listener for enter button
		_valueField.addActionListener((e) -> confirmEdit());
		rightiPanel.add(_valueField);
		rightPanel.add(rightiPanel, BorderLayout.NORTH);
		_valueArea = new JTextArea(5, 15);
		_valueArea.setLineWrap(true);
		_valueArea.setWrapStyleWord(true);
		_valueAreaPane = new JScrollPane(_valueArea);
		rightPanel.add(_valueAreaPane, BorderLayout.CENTER);

		// Put the table and the right-hand panel together in a grid
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(0, 2, 10, 10));
		mainPanel.add(tablePane);
		mainPanel.add(rightPanel);
		panel.add(mainPanel, BorderLayout.CENTER);

		// Bottom panel for OK, cancel buttons
		JPanel lowerPanel = new JPanel();
		lowerPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		_cancelButton = new JButton(I18nManager.getText("button.cancel"));
		_cancelButton.addActionListener((e) -> _dialog.dispose());
		_cancelButton.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent inE) {
				if (inE.getKeyCode() == KeyEvent.VK_ESCAPE) {_dialog.dispose();}
			}
		});
		lowerPanel.add(_cancelButton);
		JButton okButton = new JButton(I18nManager.getText("button.ok"));
		okButton.addActionListener((e) -> confirmEdit());
		lowerPanel.add(okButton);
		panel.add(lowerPanel, BorderLayout.SOUTH);
		return panel;
	}


	/**
	 * When table selection changes, need to update model and go to the selected field
	 */
	private void fieldSelected()
	{
		int rowNum = _table.getSelectedRow();
		if (rowNum == _prevRowIndex) {return;} // selection hasn't changed
		// Check the current values
		if (_prevRowIndex >= 0)
		{
			Field prevField = _fieldList.getField(_prevRowIndex);
			boolean littleField = prevField.isBuiltIn() && prevField != Field.DESCRIPTION;
			String newValue = littleField ? _valueField.getText() : _valueArea.getText();
			// Update the model from the current GUI values
			_model.updateValue(_prevRowIndex, newValue);
		}

		if (rowNum < 0) {
			_fieldnameLabel.setText("");
		}
		else
		{
			String currValue = _model.getValue(rowNum);
			Field field = _fieldList.getField(rowNum);
			_fieldnameLabel.setText(makeFieldLabel(field, _point));
			_fieldnameLabel.setVisible(true);
			boolean littleField = field.isBuiltIn() && field != Field.DESCRIPTION;
			if (littleField) {
				_valueField.setText(currValue);
			}
			else {
				_valueArea.setText(currValue);
			}
			_valueField.setVisible(littleField);
			_valueAreaPane.setVisible(!littleField);
			if (littleField) {
				_valueField.requestFocus();
			}
			else {
				_valueArea.requestFocus();
			}
		}
		_prevRowIndex = rowNum;
	}

	/**
	 * @param inField field
	 * @param inPoint current point
	 * @return label string for above the entry field / area
	 */
	private String makeFieldLabel(Field inField, DataPoint inPoint)
	{
		String label = I18nManager.getText("dialog.pointedit.table.field") + ": " + inField.getName();
		// Add units if the field is altitude / speed / vspeed
		if (inField == Field.ALTITUDE) {
			label += makeUnitsLabel(inPoint.hasAltitude() ? inPoint.getAltitude().getUnit() : getConfig().getUnitSet().getAltitudeUnit());
		}
		else if (inField == Field.SPEED) {
			label += makeUnitsLabel(inPoint.hasHSpeed() ? inPoint.getHSpeed().getUnit() : getConfig().getUnitSet().getSpeedUnit());
		}
		else if (inField == Field.VERTICAL_SPEED) {
			label += makeUnitsLabel(inPoint.hasVSpeed() ? inPoint.getVSpeed().getUnit() : getConfig().getUnitSet().getVerticalSpeedUnit());
		}
		return label;
	}

	/**
	 * @param inUnit units for altitude / speed
	 * @return addition to the field label to describe the units
	 */
	private static String makeUnitsLabel(Unit inUnit)
	{
		if (inUnit == null) {
			return "";
		}
		return " (" + I18nManager.getText(inUnit.getShortnameKey()) + ")";
	}

	/**
	 * Confirm the edit and inform the app
	 */
	private void confirmEdit()
	{
		// Apply the edits to the current field
		int rowNum = _table.getSelectedRow();
		if (rowNum >= 0)
		{
			Field currField = _fieldList.getField(rowNum);
			boolean littleField = currField.isBuiltIn() && currField != Field.DESCRIPTION;
			String newValue = littleField ? _valueField.getText() : _valueArea.getText();
			_model.updateValue(_prevRowIndex, newValue);
		}

		// Package all the modified fields into an object
		int numFields = _fieldList.getNumFields();
		ArrayList<FieldEdit> edits = new ArrayList<>();
		for (int i=0; i<numFields; i++)
		{
			if (_model.getChanged(i))
			{
				Field field = _fieldList.getField(i);
				edits.add(new FieldEdit(field, _model.getValue(i)));
			}
		}
		if (!edits.isEmpty())
		{
			int pointIndex = _app.getTrackInfo().getSelection().getCurrentPointIndex();
			EditPointCmd command = new EditPointCmd(pointIndex, edits, getConfig().getUnitSet());
			DataPoint point = _app.getTrackInfo().getCurrentPoint();
			String pointName = getPointName(point, edits);
			Describer undoDescriber = new Describer("undo.editpoint", "undo.editpoint.withname");
			command.setDescription(undoDescriber.getDescriptionWithNameOrNot(pointName));
			command.setConfirmText(I18nManager.getText("confirm.point.edit"));
			_app.execute(command);
		}
		_dialog.dispose();
	}

	/**
	 * @return point name, either the one after the edit or the one before it
	 */
	private String getPointName(DataPoint inPoint, ArrayList<FieldEdit> inEdits)
	{
		for (FieldEdit edit : inEdits)
		{
			if (edit.getField() == Field.WAYPT_NAME && !edit.getValue().trim().equals("")) {
				return edit.getValue();
			}
		}
		if (inPoint != null && inPoint.isWaypoint() && !inPoint.getWaypointName().trim().equals("")) {
			return inPoint.getWaypointName();
		}
		return null;
	}
}
