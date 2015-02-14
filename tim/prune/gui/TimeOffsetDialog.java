package tim.prune.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import tim.prune.App;
import tim.prune.I18nManager;

/**
 * Class to show a dialog for adding a time offset to a track range
 */
public class TimeOffsetDialog
{
	private App _app = null;
	private JFrame _parentFrame = null;
	private JDialog _dialog = null;
	private JRadioButton _addRadio = null, _subtractRadio = null;
	private WholeNumberField _dayField = null, _hourField = null;
	private WholeNumberField _minuteField = null;


	/**
	 * Constructor
	 * @param inApp application object for callback
	 * @param inParentFrame parent frame
	 */
	public TimeOffsetDialog(App inApp, JFrame inParentFrame)
	{
		_app = inApp;
		_parentFrame = inParentFrame;
	}


	/**
	 * Show the dialog to select options and export file
	 */
	public void showDialog()
	{
		// Make dialog window
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, I18nManager.getText("dialog.addtimeoffset.title"), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			_dialog.getContentPane().add(makeDialogComponents());
			_dialog.pack();
		}
		_dialog.show();
	}


	/**
	 * Create dialog components
	 * @return Panel containing all gui elements in dialog
	 */
	private Component makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout());
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		// Make a panel for the two radio buttons
		JPanel radioPanel = new JPanel();
		_addRadio = new JRadioButton(I18nManager.getText("dialog.addtimeoffset.add"));
		_addRadio.setSelected(true);
		radioPanel.add(_addRadio);
		_subtractRadio = new JRadioButton(I18nManager.getText("dialog.addtimeoffset.subtract"));
		_subtractRadio.setSelected(false);
		radioPanel.add(_subtractRadio);
		ButtonGroup radioGroup = new ButtonGroup();
		radioGroup.add(_addRadio);
		radioGroup.add(_subtractRadio);
		mainPanel.add(radioPanel);

		// Make a listener to validate the text boxes during typing (to en/disable OK button)

		// Make a central panel with the text boxes
		JPanel descPanel = new JPanel();
		descPanel.setLayout(new GridLayout(0, 2));
		descPanel.add(makeRightLabel("dialog.addtimeoffset.days"));
		_dayField = new WholeNumberField(3);
		descPanel.add(_dayField);
		descPanel.add(makeRightLabel("dialog.addtimeoffset.hours"));
		_hourField = new WholeNumberField(3);
		descPanel.add(_hourField);
		descPanel.add(makeRightLabel("dialog.addtimeoffset.minutes"));
		_minuteField = new WholeNumberField(3);
		descPanel.add(_minuteField);
		mainPanel.add(descPanel);
		dialogPanel.add(mainPanel, BorderLayout.CENTER);

		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton okButton = new JButton(I18nManager.getText("button.ok"));
		ActionListener okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				finish();
			}
		};
		okButton.addActionListener(okListener);
		buttonPanel.add(okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_dialog.dispose();
			}
		});
		buttonPanel.add(cancelButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		dialogPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 15));
		return dialogPanel;
	}


	/**
	 * @param inKey text key
	 * @return right-aligned label
	 */
	private static final JLabel makeRightLabel(String inKey)
	{
		JLabel label = new JLabel(I18nManager.getText(inKey) + " : ");
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		return label;
	}


	/**
	 * Finish the dialog when OK pressed
	 */
	private void finish()
	{
		// Calculate offset to add or subtract
		long offsetSecs = _minuteField.getValue() * 60L
			+ _hourField.getValue() * 60L * 60L
			+ _dayField.getValue() * 60L * 60L * 24L;
		if (_subtractRadio.isSelected()) {offsetSecs = -offsetSecs;}
		if (offsetSecs != 0L)
		{
			// Pass offset back to app and close dialog
			_app.finishAddTimeOffset(offsetSecs);
			_dialog.dispose();
		}
	}
}
