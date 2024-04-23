package tim.prune.function;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.cmd.EditSingleFieldCmd;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.Timestamp;
import tim.prune.function.edit.PointEdit;
import tim.prune.gui.WholeNumberField;

/**
 * Class to provide the function to add a time offset to a track range
 */
public class AddTimeOffset extends GenericFunction
{
	private JDialog _dialog = null;
	private JRadioButton _addRadio = null, _subtractRadio = null;
	private WholeNumberField _weekField = null, _dayField = null;
	private WholeNumberField _hourField = null, _minuteField = null;
	private JButton _okButton = null;


	/**
	 * Constructor
	 * @param inApp application object for callback
	 */
	public AddTimeOffset(App inApp) {
		super(inApp);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.addtimeoffset";
	}

	/**
	 * Begin the function
	 */
	public void begin()
	{
		int selStart = _app.getTrackInfo().getSelection().getStart();
		int selEnd = _app.getTrackInfo().getSelection().getEnd();
		if (!_app.getTrackInfo().getTrack().hasData(Field.TIMESTAMP, selStart, selEnd))
		{
			_app.showErrorMessage(getNameKey(), "dialog.addtimeoffset.notimestamps");
			return;
		}
		// Make dialog window
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, getName(), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			_dialog.getContentPane().add(makeDialogComponents());
			_dialog.pack();
		}
		_dialog.setVisible(true);
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

		// Make a central panel with the text boxes
		JPanel descPanel = new JPanel();
		descPanel.setLayout(new GridLayout(0, 2));
		descPanel.add(makeRightLabel("dialog.addtimeoffset.weeks"));
		_weekField = new WholeNumberField(4);
		descPanel.add(_weekField);
		descPanel.add(makeRightLabel("dialog.addtimeoffset.days"));
		_dayField = new WholeNumberField(4);
		descPanel.add(_dayField);
		descPanel.add(makeRightLabel("dialog.addtimeoffset.hours"));
		_hourField = new WholeNumberField(3);
		descPanel.add(_hourField);
		descPanel.add(makeRightLabel("dialog.addtimeoffset.minutes"));
		_minuteField = new WholeNumberField(3);
		descPanel.add(_minuteField);
		mainPanel.add(descPanel);
		dialogPanel.add(mainPanel, BorderLayout.CENTER);

		// Listeners to enable/disable ok button
		KeyAdapter keyListener = new KeyAdapter() {
			/** Key typed */
			public void keyTyped(KeyEvent event) {
				final boolean isNumber = "1234567890".indexOf(event.getKeyChar()) >= 0;
				_okButton.setEnabled(isNumber || getOffsetSecs() != 0L);
			}
		};
		MouseAdapter mouseListener = new MouseAdapter() {
			public void mouseReleased(java.awt.event.MouseEvent arg0) {
				_okButton.setEnabled(getOffsetSecs() != 0L);
			}
		};
		_weekField.addKeyListener(keyListener);
		_dayField.addKeyListener(keyListener);
		_hourField.addKeyListener(keyListener);
		_minuteField.addKeyListener(keyListener);
		_dayField.addMouseListener(mouseListener);
		_hourField.addMouseListener(mouseListener);
		_minuteField.addMouseListener(mouseListener);

		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		_okButton = new JButton(I18nManager.getText("button.ok"));
		_okButton.addActionListener(e -> finish());
		_okButton.setEnabled(false);
		buttonPanel.add(_okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(e -> _dialog.dispose());
		buttonPanel.add(cancelButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		dialogPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 15));
		return dialogPanel;
	}


	/**
	 * @param inKey text key
	 * @return right-aligned label
	 */
	private static JLabel makeRightLabel(String inKey)
	{
		JLabel label = new JLabel(I18nManager.getText(inKey) + " : ");
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		return label;
	}

	/**
	 * @return the number of offset seconds entered by the user
	 */
	private long getOffsetSecs()
	{
		long offsetSecs = _minuteField.getValue() * 60L
		  + _hourField.getValue() * 60L * 60L
		  + _dayField.getValue() * 60L * 60L * 24L
		  + _weekField.getValue() * 7L * 60L * 60L * 24L;
		if (_subtractRadio.isSelected()) {offsetSecs = -offsetSecs;}
		return offsetSecs;
	}

	/**
	 * Finish the dialog when OK pressed
	 */
	private void finish()
	{
		// Calculate offset to add or subtract
		long offsetSecs = getOffsetSecs();
		if (offsetSecs == 0L) {
			return;
		}
		// Make list of edits
		ArrayList<PointEdit> edits = new ArrayList<>();
		final int selStart = _app.getTrackInfo().getSelection().getStart();
		final int selEnd = _app.getTrackInfo().getSelection().getEnd();
		for (int i=selStart; i<=selEnd; i++)
		{
			DataPoint point = _app.getTrackInfo().getTrack().getPoint(i);
			if (point.hasTimestamp())
			{
				Timestamp stamp = point.getTimestamp().addOffsetSeconds(offsetSecs);
				String stampText = stamp.getText(Timestamp.Format.ISO8601, null);
				edits.add(new PointEdit(i, stampText));
			}
		}
		if (!edits.isEmpty())
		{
			EditSingleFieldCmd command = new EditSingleFieldCmd(Field.TIMESTAMP, edits, null);
			command.setDescription(getName());
			command.setConfirmText(I18nManager.getText("confirm.addtimeoffset"));
			_app.execute(command);
			_dialog.dispose();
		}
	}
}
