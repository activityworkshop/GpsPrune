package tim.prune.edit;

import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;

/**
 * Class to manage the display and editing of waypoint names
 */
public class PointNameEditor
{
	private App _app = null;
	private JFrame _parentFrame = null;
	private JDialog _dialog = null;
	private DataPoint _point = null;
	private JTextField _nameField = null;
	private JButton _okButton = null;


	/**
	 * Constructor
	 * @param inApp application object to inform of success
	 * @param inParentFrame parent frame
	 */
	public PointNameEditor(App inApp, JFrame inParentFrame)
	{
		_app = inApp;
		_parentFrame = inParentFrame;
	}


	/**
	 * Show the edit point name dialog
	 * @param inPoint point to edit
	 */
	public void showDialog(DataPoint inPoint)
	{
		_point = inPoint;
		_dialog = new JDialog(_parentFrame, I18nManager.getText("dialog.pointnameedit.title"), true);
		_dialog.setLocationRelativeTo(_parentFrame);
		// Check current waypoint name, if any
		String name = _point.getWaypointName();
		// Create Gui and show it
		_dialog.getContentPane().add(makeDialogComponents(name));
		_dialog.pack();
		_dialog.show();
	}


	/**
	 * Make the dialog components
	 * @param inName initial name of point
	 * @return the GUI components for the dialog
	 */
	private Component makeDialogComponents(String inName)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		// Create GUI layout for point name editor
		JPanel centrePanel = new JPanel();
		centrePanel.add(new JLabel(I18nManager.getText("dialog.pointnameedit.name") + ":"));
		// Make listener to react to ok being pressed
		ActionListener okActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				// Check for empty name
				if (_nameField.getText().length() > 0)
				{
					// update App with edit
					confirmEdit();
					_dialog.dispose();
				}
			}
		};
		_nameField = new JTextField(inName, 12);
		_nameField.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e)
			{
				// close dialog if escape pressed
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					_dialog.dispose();
				}
				// Enable ok button if name not empty
				_okButton.setEnabled(_nameField.getText().length() > 0);
			}
		});
		_nameField.addActionListener(okActionListener);
		centrePanel.add(_nameField);
		panel.add(centrePanel);
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		JButton upperButton = new JButton(I18nManager.getText("dialog.pointnameedit.uppercase"));
		upperButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_nameField.setText(_nameField.getText().toUpperCase());
				_okButton.setEnabled(true);
				_nameField.requestFocus();
			}
		});
		rightPanel.add(upperButton);
		JButton lowerButton = new JButton(I18nManager.getText("dialog.pointnameedit.lowercase"));
		lowerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_nameField.setText(_nameField.getText().toLowerCase());
				_okButton.setEnabled(true);
				_nameField.requestFocus();
			}
		});
		rightPanel.add(lowerButton);
		JButton sentenceButton = new JButton(I18nManager.getText("dialog.pointnameedit.sentencecase"));
		sentenceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_nameField.setText(sentenceCase(_nameField.getText()));
				_okButton.setEnabled(true);
				_nameField.requestFocus();
			}
		});
		rightPanel.add(sentenceButton);
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
		_okButton.addActionListener(okActionListener);
		lowerPanel.add(_okButton);
		panel.add(lowerPanel, BorderLayout.SOUTH);
		return panel;
	}


	/**
	 * Turn a String into sentence case by capitalizing each word
	 * @param inString String to convert
	 * @return capitalized String
	 */
	private static String sentenceCase(String inString)
	{
		// Check first for empty strings
		if (inString == null || inString.equals(""))
		{
			return "";
		}
		StringBuffer buffer = new StringBuffer();
		// loop through characters
		char lastChar = ' ', currChar = ' ';
		for (int i=0; i<inString.length(); i++)
		{
			currChar = inString.charAt(i);
			buffer.append(lastChar == ' '?Character.toUpperCase(currChar):Character.toLowerCase(currChar));
			lastChar = currChar;
		}
		return buffer.toString();
	}


	/**
	 * Confirm the edit and inform the app
	 */
	private void confirmEdit()
	{
		// Check whether name has really changed
		String prevName = _point.getWaypointName();
		String newName = _nameField.getText().trim();
		boolean prevNull = (prevName == null || prevName.equals(""));
		boolean newNull = (newName == null || newName.equals(""));
		if ( (prevNull && !newNull)
			|| (!prevNull && newNull)
			|| (!prevNull && !newNull && !prevName.equals(newName)) )
		{
			// Make lists for edit and undo, and add the changed field
			FieldEditList editList = new FieldEditList();
			FieldEditList undoList = new FieldEditList();
			editList.addEdit(new FieldEdit(Field.WAYPT_NAME, newName));
			undoList.addEdit(new FieldEdit(Field.WAYPT_NAME, prevName));

			// Pass back to App to perform edit
			_app.completePointEdit(editList, undoList);
		}
	}
}
