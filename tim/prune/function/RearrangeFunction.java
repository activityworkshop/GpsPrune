package tim.prune.function;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.data.sort.SortMode;

/**
 * Abstract superclass for the functions which rearrange points,
 * such as waypoints or photo points
 */
public abstract class RearrangeFunction extends GenericFunction
{
	/** Function dialog */
	private JDialog _dialog = null;
	/** Radio buttons for start/end/nearest */
	private JRadioButton[] _positionRadios = null;
	/** Radio buttons for sorting */
	private JRadioButton[] _sortRadios = null;
	/** Is the "nearest" option available? */
	private boolean _nearestAvailable = false;


	/** Enumeration for rearrange commands */
	protected enum Rearrange
	{
		/** Rearrange all waypoints to start */
		TO_START,
		/** Rearrange all waypoints to end */
		TO_END,
		/** Rearrange each waypoint to nearest track point */
		TO_NEAREST
	}


	/**
	 * Constructor
	 * @param inApp app object
	 * @param isNearestAvailable true if nearest option is visible
	 */
	public RearrangeFunction(App inApp, boolean isNearestAvailable)
	{
		super(inApp);
		_nearestAvailable = isNearestAvailable;
	}

	/**
	 * Begin the function by showing the dialog
	 */
	public void begin()
	{
		// Make dialog window
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, I18nManager.getText(getNameKey()), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			_dialog.getContentPane().add(makeDialogComponents());
			_dialog.pack();
		}
		// If sorting by time isn't available, then disable radio button
		_sortRadios[2].setEnabled(isSortByTimeAllowed());
		// Show dialog
		_dialog.setVisible(true);
	}


	/**
	 * Create dialog components
	 * @return Panel containing all gui elements in dialog
	 */
	private JPanel makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout());
		JLabel descLabel = new JLabel(I18nManager.getText(getDescriptionKey()));
		descLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		dialogPanel.add(descLabel, BorderLayout.NORTH);
		// Radios for position (start / end / nearest)
		_positionRadios = new JRadioButton[3];
		final String[] posNames = {"tostart", "toend", "tonearest"};
		ButtonGroup posGroup = new ButtonGroup();
		JPanel posPanel = new JPanel();
		posPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		for (int i=0; i<posNames.length; i++)
		{
			_positionRadios[i] = new JRadioButton(I18nManager.getText("dialog.rearrange." + posNames[i]));
			posGroup.add(_positionRadios[i]);
			posPanel.add(_positionRadios[i]);
		}
		_positionRadios[0].setSelected(true);
		_positionRadios[2].setVisible(_nearestAvailable);

		// Radios for sort (none / filename / time)
		_sortRadios = new JRadioButton[3];
		final String[] sortNames = {"nosort", getSortNameKey(), "sortbytime"};
		ButtonGroup sortGroup = new ButtonGroup();
		JPanel sortPanel = new JPanel();
		sortPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		for (int i=0; i<3; i++)
		{
			_sortRadios[i] = new JRadioButton(I18nManager.getText("dialog.rearrange." + sortNames[i]));
			sortGroup.add(_sortRadios[i]);
			sortPanel.add(_sortRadios[i]);
		}
		_sortRadios[0].setSelected(true);
		// Use listener to disable all sort options if nearest type chosen, re-enable otherwise
		ActionListener rearrListener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				final boolean sortAvailable = !_positionRadios[2].isSelected();
				for (int i=0; i<_sortRadios.length; i++) {
					_sortRadios[i].setEnabled(sortAvailable);
				}
			}
		};
		for (int i=0; i<_positionRadios.length; i++) {
			_positionRadios[i].addActionListener(rearrListener);
		}
		// add to middle of dialog
		JPanel centrePanel = new JPanel();
		centrePanel.setLayout(new BoxLayout(centrePanel, BoxLayout.Y_AXIS));
		centrePanel.add(posPanel);
		centrePanel.add(sortPanel);
		dialogPanel.add(centrePanel, BorderLayout.CENTER);
		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton okButton = new JButton(I18nManager.getText("button.ok"));
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				finish();
				_dialog.dispose();
			}
		});
		buttonPanel.add(okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_dialog.dispose();
			}
		});
		buttonPanel.add(cancelButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		return dialogPanel;
	}

	/**
	 * @return true if sorting by time is allowed, false otherwise
	 */
	protected boolean isSortByTimeAllowed()
	{
		return true;
	}

	/**
	 * @return the selected rearrange option
	 */
	protected Rearrange getRearrangeOption()
	{
		if (_positionRadios[0].isSelected()) {
			return Rearrange.TO_START;
		}
		if (_positionRadios[1].isSelected()) {
			return Rearrange.TO_END;
		}
		return Rearrange.TO_NEAREST;
	}

	/**
	 * @return the selected sort mode
	 */
	protected SortMode getSortMode()
	{
		if (_sortRadios[0].isSelected()) {
			return SortMode.DONT_SORT;
		}
		if (_sortRadios[1].isSelected()) {
			return SortMode.SORTBY_NAME;
		}
		return SortMode.SORTBY_TIME;
	}

	/** @return key for description */
	protected abstract String getDescriptionKey();

	/** @return partial key for the sort by name radio */
	protected abstract String getSortNameKey();

	/**
	 * Perform the rearrange
	 */
	protected abstract void finish();
}
