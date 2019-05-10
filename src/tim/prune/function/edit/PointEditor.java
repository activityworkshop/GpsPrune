package tim.prune.function.edit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.config.Config;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.FieldList;
import tim.prune.data.Track;
import tim.prune.data.Unit;

/**
 * Class to manage the display and editing of point data
 */
public class PointEditor
{
	private App _app = null;
	private JFrame _parentFrame = null;
	private JDialog _dialog = null;
	private JTable _table = null;
	private JLabel _fieldnameLabel = null;
	private JTextField _valueField = null;
	private JTextArea _valueArea = null;
	private JScrollPane _valueAreaPane = null;
	private Track _track = null;
	private DataPoint _point = null;
	private EditFieldsTableModel _model = null;
	private JButton _cancelButton = null;
	private int _prevRowIndex = -1;


	/**
	 * Constructor
	 * @param inApp application object to inform of success
	 * @param inParentFrame parent frame
	 */
	public PointEditor(App inApp, JFrame inParentFrame)
	{
		_app = inApp;
		_parentFrame = inParentFrame;
	}


	/**
	 * Show the edit point dialog
	 * @param inTrack track object
	 * @param inPoint point to edit
	 */
	public void showDialog(Track inTrack, DataPoint inPoint)
	{
		_track = inTrack;
		_point = inPoint;
		_dialog = new JDialog(_parentFrame, I18nManager.getText("dialog.pointedit.title"), true);
		_dialog.setLocationRelativeTo(_parentFrame);
		// Check field list
		FieldList fieldList = _track.getFieldList();
		int numFields = fieldList.getNumFields();
		// Create table model for point editor
		_model = new EditFieldsTableModel(numFields);
		for (int i=0; i<numFields; i++)
		{
			Field field = fieldList.getField(i);
			_model.addFieldInfo(field.getName(), _point.getFieldValue(field), i);
		}
		// Create Gui
		_dialog.getContentPane().add(makeDialogComponents());
		_dialog.pack();
		// Init right-hand side
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				_valueField.setVisible(false);
				_valueAreaPane.setVisible(false);
				_cancelButton.requestFocus();
			}
		});
		_dialog.setVisible(true);
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
		_table = new JTable(_model)
		{
			// Paint the changed fields orange
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
			{
				Component comp = super.prepareRenderer(renderer, row, column);
				boolean changed = ((EditFieldsTableModel) getModel()).getChanged(row);
				comp.setBackground(changed ? Color.orange : getBackground());
				return comp;
			}
		};
		_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_table.getSelectionModel().clearSelection();
		_table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e)
			{
				fieldSelected();
			}
		});
		_table.setPreferredScrollableViewportSize(new Dimension(_table.getWidth() * 2, _table.getRowHeight() * 6));
		JScrollPane tablePane = new JScrollPane(_table);
		tablePane.setPreferredSize(new Dimension(150, 100));

		// Label at top
		JLabel topLabel = new JLabel(I18nManager.getText("dialog.pointedit.intro"));
		topLabel.setBorder(BorderFactory.createEmptyBorder(8, 6, 3, 6));
		panel.add(topLabel, BorderLayout.NORTH);

		// listener for ok event
		ActionListener okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				// update App with edit
				confirmEdit();
				_dialog.dispose();
			}
		};

		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BorderLayout());
		JPanel rightiPanel = new JPanel();
		rightiPanel.setLayout(new BoxLayout(rightiPanel, BoxLayout.Y_AXIS));
		// Add GUI elements to rhs
		_fieldnameLabel = new JLabel(I18nManager.getText("dialog.pointedit.nofield"));
		rightiPanel.add(_fieldnameLabel);
		_valueField = new JTextField(11);
		// Add listener for enter button
		_valueField.addActionListener(okListener);
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
		_cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_dialog.dispose();
			}
		});
		_cancelButton.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent inE) {
				if (inE.getKeyCode() == KeyEvent.VK_ESCAPE) {_dialog.dispose();}
			}
		});
		lowerPanel.add(_cancelButton);
		JButton okButton = new JButton(I18nManager.getText("button.ok"));
		okButton.addActionListener(okListener);
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
			Field prevField = _track.getFieldList().getField(_prevRowIndex);
			boolean littleField = prevField.isBuiltIn() && prevField != Field.DESCRIPTION;
			String newValue = littleField ? _valueField.getText() : _valueArea.getText();
			// Update the model from the current GUI values
			_model.updateValue(_prevRowIndex, newValue);
		}

		if (rowNum < 0)
		{
			_fieldnameLabel.setText("");
		}
		else
		{
			String currValue = _model.getValue(rowNum);
			Field  field     = _track.getFieldList().getField(rowNum);
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
	private static String makeFieldLabel(Field inField, DataPoint inPoint)
	{
		String label = I18nManager.getText("dialog.pointedit.table.field") + ": " + inField.getName();
		// Add units if the field is altitude / speed / vspeed
		if (inField == Field.ALTITUDE)
		{
			label += makeUnitsLabel(inPoint.hasAltitude() ? inPoint.getAltitude().getUnit() : Config.getUnitSet().getAltitudeUnit());
		}
		else if (inField == Field.SPEED)
		{
			label += makeUnitsLabel(inPoint.hasHSpeed() ? inPoint.getHSpeed().getUnit() : Config.getUnitSet().getSpeedUnit());
		}
		else if (inField == Field.VERTICAL_SPEED)
		{
			label += makeUnitsLabel(inPoint.hasVSpeed() ? inPoint.getVSpeed().getUnit() : Config.getUnitSet().getVerticalSpeedUnit());
		}
		return label;
	}

	/**
	 * @param inUnit units for altitude / speed
	 * @return addition to the field label to describe the units
	 */
	private static String makeUnitsLabel(Unit inUnit)
	{
		if (inUnit == null) return "";
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
			Field currField = _track.getFieldList().getField(rowNum);
			boolean littleField = currField.isBuiltIn() && currField != Field.DESCRIPTION;
			String newValue = littleField ? _valueField.getText() : _valueArea.getText();
			_model.updateValue(_prevRowIndex, newValue);
		}

		// Package the modified fields into an object
		FieldList fieldList = _track.getFieldList();
		int numFields = fieldList.getNumFields();
		// Make lists for edit and undo, and add each changed field in turn
		FieldEditList editList = new FieldEditList();
		FieldEditList undoList = new FieldEditList();
		for (int i=0; i<numFields; i++)
		{
			if (_model.getChanged(i))
			{
				Field field = fieldList.getField(i);
				editList.addEdit(new FieldEdit(field, _model.getValue(i)));
				undoList.addEdit(new FieldEdit(field, _point.getFieldValue(field)));
			}
		}
		_app.completePointEdit(editList, undoList);
	}
}
