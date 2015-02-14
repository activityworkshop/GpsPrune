package tim.prune.function.edit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.FieldList;
import tim.prune.data.Track;

/**
 * Class to manage the display and editing of point data
 */
public class PointEditor
{
	private App _app = null;
	private JFrame _parentFrame = null;
	private JDialog _dialog = null;
	private JTable _table = null;
	private Track _track = null;
	private DataPoint _point = null;
	private EditFieldsTableModel _model = null;
	private JButton _editButton = null;
	private JButton _okButton = null;


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
		// Create Gui and show it
		_dialog.getContentPane().add(makeDialogComponents());
		_dialog.pack();
		_dialog.setVisible(true);
	}


	/**
	 * Make the dialog components
	 * @return the GUI components for the dialog
	 */
	private Component makeDialogComponents()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(1, 10));
		// Create GUI layout for point editor
		_table = new JTable(_model);
		_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e)
			{
				// enable edit button when row selected
				_editButton.setEnabled(true);
			}
		});
		_table.setPreferredScrollableViewportSize(new Dimension(_table.getWidth(), _table.getRowHeight() * 6));
		panel.add(new JScrollPane(_table), BorderLayout.CENTER);
		// Label at top
		JLabel topLabel = new JLabel(I18nManager.getText("dialog.pointedit.text"));
		topLabel.setBorder(BorderFactory.createEmptyBorder(8, 6, 3, 6));
		panel.add(topLabel, BorderLayout.NORTH);
		_editButton = new JButton(I18nManager.getText("button.edit"));
		_editButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				// Update field value and enable ok button
				String currValue = _model.getValue(_table.getSelectedRow());
				Object newValue = JOptionPane.showInputDialog(_dialog,
					I18nManager.getText("dialog.pointedit.changevalue.text"),
					I18nManager.getText("dialog.pointedit.changevalue.title"),
					JOptionPane.QUESTION_MESSAGE, null, null, currValue);
				if (newValue != null
					&& _model.updateValue(_table.getSelectedRow(), newValue.toString()))
				{
					_okButton.setEnabled(true);
				}
			}
		});
		_editButton.setEnabled(false);
		JPanel rightPanel = new JPanel();
		rightPanel.add(_editButton);
		panel.add(rightPanel, BorderLayout.EAST);
		// Bottom panel for OK, cancel buttons
		JPanel lowerPanel = new JPanel();
		lowerPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_dialog.dispose();
			}
		});
		lowerPanel.add(cancelButton);
		_okButton = new JButton(I18nManager.getText("button.ok"));
		_okButton.setEnabled(false);
		_okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				// update App with edit
				confirmEdit();
				_dialog.dispose();
			}
		});
		lowerPanel.add(_okButton);
		panel.add(lowerPanel, BorderLayout.SOUTH);
		return panel;
	}


	/**
	 * Confirm the edit and inform the app
	 */
	private void confirmEdit()
	{
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
