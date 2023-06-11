package tim.prune.function.edit;

import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
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
import tim.prune.cmd.EditPointCmd;
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

	private enum CaseOperation {LOWER_CASE, UPPER_CASE, TITLE_CASE}


	/**
	 * Constructor
	 * @param inApp application object to inform of success
	 */
	public PointNameEditor(App inApp) {
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
			_dialog = new JDialog(_parentFrame, getName(), true);
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
		_nameField = new JTextField("", 12);
		_nameField.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e)
			{
				// close dialog if escape pressed
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					_dialog.dispose();
				}
				// Enable ok button if name changed
				_okButton.setEnabled(hasNameChanged());
			}
		});
		_nameField.addActionListener(e -> confirmEdit());
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
		upperButton.addActionListener(e -> changeCase(CaseOperation.UPPER_CASE));
		rightPanel.add(upperButton);
		JButton lowerButton = new JButton(I18nManager.getText("dialog.pointnameedit.lowercase"));
		lowerButton.addActionListener(e -> changeCase(CaseOperation.LOWER_CASE));
		rightPanel.add(lowerButton);
		JButton titleButton = new JButton(I18nManager.getText("dialog.pointnameedit.titlecase"));
		titleButton.addActionListener(e -> changeCase(CaseOperation.TITLE_CASE));
		rightPanel.add(titleButton);
		panel.add(rightPanel, BorderLayout.EAST);
		// Bottom panel for OK, cancel buttons
		JPanel lowerPanel = new JPanel();
		lowerPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener((e) -> _dialog.dispose());
		lowerPanel.add(cancelButton);
		_okButton = new JButton(I18nManager.getText("button.ok"));
		_okButton.setEnabled(false);
		_okButton.addActionListener(e -> confirmEdit());
		lowerPanel.add(_okButton);
		panel.add(lowerPanel, BorderLayout.SOUTH);
		return panel;
	}


	/**
	 * Change the case of the name
	 * @param caseOp operation to perform, upper/lower/title
	 */
	private void changeCase(CaseOperation caseOp)
	{
		String name = _nameField.getText();
		switch (caseOp) {
			case UPPER_CASE:
				name = name.toUpperCase();
				break;
			case LOWER_CASE:
				name = name.toLowerCase();
				break;
			case TITLE_CASE:
				name = titleCase(name);
				break;
		}
		_nameField.setText(name);
		_okButton.setEnabled(true);
		_nameField.requestFocus();
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
	static String titleCase(String inString)
	{
		// Check first for empty strings
		if (inString == null || inString.equals("")) {
			return "";
		}
		StringBuilder buffer = new StringBuilder();
		// loop through characters
		char lastChar = ' ', currChar = ' ';
		for (int i=0; i<inString.length(); i++)
		{
			currChar = inString.charAt(i);
			buffer.append(lastChar == ' ' ? Character.toUpperCase(currChar) : Character.toLowerCase(currChar));
			lastChar = currChar;
		}
		return buffer.toString();
	}


	/**
	 * Confirm the edit and execute the command
	 */
	private void confirmEdit()
	{
		// Check whether name has really changed
		if (!hasNameChanged()) {
			return;
		}
		String newName = _nameField.getText().trim();
		String displayName = (newName.isEmpty() ? _point.getWaypointName() : newName);
		int pointIndex = _app.getTrackInfo().getSelection().getCurrentPointIndex();
		EditPointCmd command = new EditPointCmd(pointIndex, new FieldEdit(Field.WAYPT_NAME, newName));
		command.setDescription(I18nManager.getText("undo.editpoint.withname", displayName));
		command.setConfirmText(I18nManager.getText("confirm.point.edit"));
		_app.execute(command);

		_dialog.dispose();
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
