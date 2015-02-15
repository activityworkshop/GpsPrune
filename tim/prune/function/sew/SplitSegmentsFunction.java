package tim.prune.function.sew;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.DataPoint;
import tim.prune.data.Distance;
import tim.prune.data.Field;
import tim.prune.data.Unit;
import tim.prune.data.UnitSetLibrary;
import tim.prune.gui.WholeNumberField;
import tim.prune.undo.UndoSplitSegments;

/**
 * Function to split a track into segments using
 * either a distance limit or a time limit
 */
public class SplitSegmentsFunction extends GenericFunction
{
	/** Dialog */
	private JDialog _dialog = null;
	/** Radio buttons for splitting by distance and time */
	private JRadioButton _distLimitRadio = null, _timeLimitRadio = null;
	/** Dropdown for selecting distance units */
	private JComboBox<String> _distUnitsDropdown = null;
	/** Text field for entering distance */
	private WholeNumberField _distanceField = null;
	/** Text fields for entering distance */
	private WholeNumberField _limitHourField = null, _limitMinField = null;
	/** Ok button */
	private JButton _okButton = null;


	/**
	 * React to item changes and key presses
	 */
	private abstract class ChangeListener extends KeyAdapter implements ItemListener
	{
		/** Method to be implemented */
		public abstract void optionsChanged();

		/** Item changed in ItemListener */
		public void itemStateChanged(ItemEvent arg0) {
			optionsChanged();
		}

		/** Key released in KeyListener */
		public void keyReleased(KeyEvent arg0) {
			optionsChanged();
		}
	}

	/**
	 * Constructor
	 */
	public SplitSegmentsFunction(App inApp) {
		super(inApp);
	}

	/**
	 * @return name key
	 */
	public String getNameKey() {
		return "function.splitsegments";
	}

	/**
	 * Begin the function
	 */
	public void begin()
	{
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, I18nManager.getText(getNameKey()), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			_dialog.getContentPane().add(makeDialogComponents());
			_dialog.pack();
		}
		enableOkButton();
		// TODO: Maybe set distance units according to current Config setting?
		final boolean hasTimestamps = _app.getTrackInfo().getTrack().hasData(Field.TIMESTAMP);
		_timeLimitRadio.setEnabled(hasTimestamps);
		_dialog.setVisible(true);
	}

	/**
	 * Create dialog components
	 * @return Panel containing all gui elements in dialog
	 */
	private Component makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout(5, 5));

		// Make radio buttons for three different options
		_distLimitRadio = new JRadioButton(I18nManager.getText("dialog.correlate.options.distancelimit") + ": ");
		_timeLimitRadio = new JRadioButton(I18nManager.getText("dialog.correlate.options.timelimit") + ": ");
		ButtonGroup radioGroup = new ButtonGroup();
		radioGroup.add(_distLimitRadio);
		radioGroup.add(_timeLimitRadio);

		// central panel for limits
		JPanel limitsPanel = new JPanel();
		limitsPanel.setLayout(new BoxLayout(limitsPanel, BoxLayout.Y_AXIS));
		limitsPanel.add(Box.createVerticalStrut(8));
		ChangeListener optionsChangedListener = new ChangeListener() {
			public void optionsChanged() {
				enableOkButton();
			}
		};
		// distance limits
		JPanel distLimitPanel = new JPanel();
		distLimitPanel.setLayout(new FlowLayout());
		_distLimitRadio.setSelected(true);
		_distLimitRadio.addItemListener(optionsChangedListener);
		distLimitPanel.add(_distLimitRadio);
		_distanceField = new WholeNumberField(3);
		_distanceField.addKeyListener(optionsChangedListener);
		distLimitPanel.add(_distanceField);
		String[] distUnitsOptions = {I18nManager.getText("units.kilometres"), I18nManager.getText("units.metres"),
			I18nManager.getText("units.miles")};
		_distUnitsDropdown = new JComboBox<String>(distUnitsOptions);
		_distUnitsDropdown.addItemListener(optionsChangedListener);
		distLimitPanel.add(_distUnitsDropdown);
		distLimitPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		limitsPanel.add(distLimitPanel);

		// time limit panel
		JPanel timeLimitPanel = new JPanel();
		timeLimitPanel.setLayout(new FlowLayout());
		_timeLimitRadio.addItemListener(optionsChangedListener);
		timeLimitPanel.add(_timeLimitRadio);
		_limitHourField = new WholeNumberField(2);
		_limitHourField.addKeyListener(optionsChangedListener);
		timeLimitPanel.add(_limitHourField);
		timeLimitPanel.add(new JLabel(I18nManager.getText("dialog.correlate.options.offset.hours")));
		_limitMinField = new WholeNumberField(3);
		_limitMinField.addKeyListener(optionsChangedListener);
		timeLimitPanel.add(_limitMinField);
		timeLimitPanel.add(new JLabel(I18nManager.getText("units.minutes")));
		timeLimitPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		limitsPanel.add(timeLimitPanel);

		dialogPanel.add(limitsPanel, BorderLayout.NORTH);

		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		// OK button
		_okButton = new JButton(I18nManager.getText("button.ok"));
		_okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				performSplit();
			}
		});
		buttonPanel.add(_okButton);
		// Cancel button
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_dialog.dispose();
			}
		});
		cancelButton.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent inE) {
				if (inE.getKeyCode() == KeyEvent.VK_ESCAPE) {_dialog.dispose();}
			}
		});
		buttonPanel.add(cancelButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		return dialogPanel;
	}

	/**
	 * Enable or disable the OK button according to the inputs
	 */
	private void enableOkButton()
	{
		boolean enabled = false;
		if (_distLimitRadio.isSelected()) {
			enabled = _distanceField.getValue() > 0;
		}
		else if (_timeLimitRadio.isSelected()) {
			enabled = _limitHourField.getValue() > 0 || _limitMinField.getValue() > 0;
		}
		_okButton.setEnabled(enabled);

		// Also enable/disable the other fields
		_distanceField.setEnabled(_distLimitRadio.isSelected());
		_distUnitsDropdown.setEnabled(_distLimitRadio.isSelected());
		_limitHourField.setEnabled(_timeLimitRadio.isSelected());
		_limitMinField.setEnabled(_timeLimitRadio.isSelected());
	}

	/**
	 * The dialog has been completed and OK pressed, so do the split
	 */
	private void performSplit()
	{
		// Split either by distance or time
		boolean checkTimeLimit = _timeLimitRadio.isSelected()
			&& (_limitHourField.getValue() > 0 || _limitMinField.getValue() > 0);
		int timeLimitSeconds = 0;
		if (checkTimeLimit)
		{
			timeLimitSeconds = _limitHourField.getValue() * 60 * 60
				+ _limitMinField.getValue() * 60;
			if (timeLimitSeconds <= 0) {checkTimeLimit = false;}
		}
		double distLimitRadians = 0.0;
		final boolean checkDistLimit = _distLimitRadio.isSelected()
			&& _distanceField.getValue() > 0;
		if (checkDistLimit)
		{
			final Unit[] distUnits = {UnitSetLibrary.UNITS_KILOMETRES,
				UnitSetLibrary.UNITS_METRES, UnitSetLibrary.UNITS_MILES};
			Unit distUnit = distUnits[_distUnitsDropdown.getSelectedIndex()];
			distLimitRadians = Distance.convertDistanceToRadians(_distanceField.getValue(), distUnit);
		}
		if (!checkTimeLimit && !checkDistLimit) {
			return; // neither option selected
		}

		// Make undo object
		UndoSplitSegments undo = new UndoSplitSegments(_app.getTrackInfo().getTrack());
		final int numPoints = _app.getTrackInfo().getTrack().getNumPoints();
		DataPoint currPoint = null, prevPoint = null;
		int numSplitsMade = 0;

		// Now actually do it, looping through the points in the track
		for (int i=0; i<numPoints; i++)
		{
			currPoint = _app.getTrackInfo().getTrack().getPoint(i);
			if (!currPoint.isWaypoint())
			{
				boolean splitHere = (prevPoint != null)
					&& ((checkDistLimit && DataPoint.calculateRadiansBetween(prevPoint, currPoint) > distLimitRadians)
						|| (checkTimeLimit && currPoint.hasTimestamp() && prevPoint.hasTimestamp()
							&& currPoint.getTimestamp().getSecondsSince(prevPoint.getTimestamp()) > timeLimitSeconds));
				if (splitHere && !currPoint.getSegmentStart())
				{
					currPoint.setSegmentStart(true);
					numSplitsMade++;
				}
				prevPoint = currPoint;
			}
		}

		if (numSplitsMade > 0)
		{
			_app.completeFunction(undo, I18nManager.getTextWithNumber("confirm.splitsegments", numSplitsMade));
			UpdateMessageBroker.informSubscribers();
			_dialog.dispose();
		}
		else
		{
			// Complain that no split was made
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("error.tracksplit.nosplit"),
				I18nManager.getText("error.function.noop.title"), JOptionPane.WARNING_MESSAGE);
		}
	}
}
