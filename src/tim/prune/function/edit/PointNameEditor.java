package tim.prune.function.edit;

import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;

/**
 * Function to manage the display and editing of waypoint names
 */
public class PointNameEditor extends GenericFunction
{
	private JDialog _dialog = null;
	private DataPoint _point = null;
	private JTextField _nameField = null;
	private JButton _okButton = null;


	/**
	 * Constructor
	 * @param inApp application object to inform of success
	 */
	public PointNameEditor(App inApp)
	{
		super(inApp);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.editwaypointname";
	}

	/**
	 * Begin the function by showing the edit point name dialog
	 */
	public void begin()
	{
		_point = _app.getTrackInfo().getCurrentPoint();
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, I18nManager.getText(getNameKey()), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			// Create Gui and show it
			_dialog.getContentPane().add(makeDialogComponents());
			_dialog.pack();
		}
		// Check current waypoint name, if any
		String name = _point.getWaypointName();
		resetDialog(name);
		_dialog.setVisible(true);
	}


	/**
	 * Make the dialog components
	 * @return the GUI components for the dialog
	 */
	private Component makeDialogComponents()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		// Create GUI layout for point name editor
		JPanel centrePanel = new JPanel();
		centrePanel.setLayout(new BorderLayout(8, 8));
		centrePanel.add(new JLabel(I18nManager.getText("dialog.pointnameedit.name") + ": "), BorderLayout.WEST);
		// Make listener to react to ok being pressed
		ActionListener okActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				// update App with edit
				confirmEdit();
				_dialog.dispose();
			}
		};
		_nameField = new JTextField("", 12);
		_nameField.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e)
			{
				// close dialog if escape pressed
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					_dialog.dispose();
				}
				// Enable ok button if name changed
				_okButton.setEnabled(hasNameChanged());
			}
		});
		_nameField.addActionListener(okActionListener);
		centrePanel.add(_nameField, BorderLayout.CENTER);
		// holder panel to stop the text box from being stretched
		JPanel holderPanel = new JPanel();
		holderPanel.setLayout(new BorderLayout());
		holderPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		holderPanel.add(centrePanel, BorderLayout.NORTH);
		panel.add(holderPanel, BorderLayout.CENTER);
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
		JButton titleButton = new JButton(I18nManager.getText("dialog.pointnameedit.titlecase"));
		titleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_nameField.setText(titleCase(_nameField.getText()));
				_okButton.setEnabled(true);
				_nameField.requestFocus();
			}
		});
		rightPanel.add(titleButton);
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
	 * Reset the dialog with the given name
	 * @param inName waypoint name
	 */
	private void resetDialog(String inName)
	{
		_nameField.setText(inName);
		_okButton.setEnabled(false);
	}

	/**
	 * Turn a String into title case by capitalizing each word
	 * @param inString String to convert
	 * @return capitalized String
	 */
	private static String titleCase(String inString)
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
		if (hasNameChanged())
		{
			// Make lists for edit and undo, and add the changed field
			FieldEditList editList = new FieldEditList();
			FieldEditList undoList = new FieldEditList();
			editList.addEdit(new FieldEdit(Field.WAYPT_NAME, _nameField.getText().trim()));
			undoList.addEdit(new FieldEdit(Field.WAYPT_NAME, _point.getWaypointName()));

			// Pass back to App to perform edit
			_app.completePointEdit(editList, undoList);
		}
	}

	/**
	 * Check whether the name has been changed or not
	 * @return true if the new name is different
	 */
	private boolean hasNameChanged()
	{
		String prevName = _point.getWaypointName();
		String newName = _nameField.getText().trim();
		boolean prevNull = (prevName == null || prevName.equals(""));
		boolean newNull = (newName == null || newName.equals(""));
		return (prevNull && !newNull)
			|| (!prevNull && newNull)
			|| (!prevNull && !newNull && !prevName.equals(newName));
	}
}
