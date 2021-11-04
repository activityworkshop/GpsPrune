package tim.prune.function.deletebydate;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import tim.prune.App;
import tim.prune.DataSubscriber;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.config.TimezoneHelper;
import tim.prune.data.DataPoint;
import tim.prune.function.compress.MarkAndDeleteFunction;

/**
 * Function to select a date or dates,
 * and mark the corresponding points for deletion
 */
public class DeleteByDateFunction extends MarkAndDeleteFunction
{
	/** dialog for selecting dates */
	private JDialog _dialog = null;
	/** table */
	private JTable _infoTable = null;
	/** Ok button */
	private JButton _okButton = null;
	// panel for selection buttons
	private JPanel _selButtonPanel = null;
	private JButton _keepSelectedButton = null;
	private JButton _delSelectedButton = null;
	/** date info list */
	private DateInfoList _infoList = new DateInfoList();


	/**
	 * Constructor
	 * @param inApp App object
	 */
	public DeleteByDateFunction(App inApp)
	{
		super(inApp);
	}

	@Override
	public String getNameKey() {
		return "function.deletebydate";
	}

	@Override
	public void begin()
	{
		// Select the current timezone
		DateInfo.setTimezone(TimezoneHelper.getSelectedTimezone());

		// Make a list of which dates are present in the track
		_infoList.clearAll();
		final int numPoints = _app.getTrackInfo().getTrack().getNumPoints();
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = _app.getTrackInfo().getTrack().getPoint(i);
			if (point != null)
			{
				if (point.hasTimestamp()) {
					_infoList.addPoint(point.getTimestamp()
						.getCalendar(TimezoneHelper.getSelectedTimezone()).getTime());
				}
				else {
					_infoList.addPoint(null); // no timestamp available
				}
			}
		}

		// Complain if there is only one entry in the list - this means all points are on the same day
		if (_infoList.getNumEntries() < 2)
		{
			_app.showErrorMessage(getNameKey(), "dialog.deletebydate.onlyonedate");
		}
		else
		{
			// Create and build dialog if necessary
			if (_dialog == null)
			{
				_dialog = new JDialog(_parentFrame, I18nManager.getText(getNameKey()), true);
				_dialog.setLocationRelativeTo(_parentFrame);
				_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				_dialog.getContentPane().add(makeDialogComponents());
				_dialog.pack();
			}
			_infoTable.clearSelection();
			_selButtonPanel.setVisible(_infoList.getNumEntries() > 4);
			enableButtons();
			// Show dialog
			_dialog.setVisible(true);
		}
	}

	/**
	 * Create dialog components
	 * @return Panel containing all gui elements in dialog
	 */
	private Component makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout(5, 5));
		// Label at top
		JLabel topLabel = new JLabel(I18nManager.getText("dialog.deletebydate.intro"));
		topLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		dialogPanel.add(topLabel, BorderLayout.NORTH);

		// close window if escape pressed
		KeyAdapter escListener = new KeyAdapter() {
			public void keyReleased(KeyEvent inE) {
				if (inE.getKeyCode() == KeyEvent.VK_ESCAPE) {
					_dialog.dispose();
				}
			}
		};

		_infoTable = new JTable(new DeletionTableModel(_infoList));
		_infoTable.getTableHeader().addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent mouseEvent) {
				int index = _infoTable.convertColumnIndexToModel(_infoTable.columnAtPoint(mouseEvent.getPoint()));
				if (index >= 2) {
					modifyAllCheckboxes(index == 3); // parameter is true for delete, false for keep
				}
			}
		});
		_infoTable.getSelectionModel().addListSelectionListener(
			new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					enableButtons();
				}
		});

		JScrollPane pane = new JScrollPane(_infoTable);
		pane.setPreferredSize(new Dimension(300, 180));
		pane.setBorder(BorderFactory.createEmptyBorder(2, 50, 2, 50));
		JPanel middlePanel = new JPanel();
		middlePanel.setLayout(new BorderLayout());
		middlePanel.add(pane, BorderLayout.CENTER);
		_selButtonPanel = new JPanel();
		_selButtonPanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(4, 4, 4, 4))
		);
		_selButtonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		_keepSelectedButton = new JButton(I18nManager.getText("button.keepselected"));
		_keepSelectedButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeSelectedRowsToKeep();
			}
		});
		_selButtonPanel.add(_keepSelectedButton);
		_delSelectedButton = new JButton(I18nManager.getText("button.deleteselected"));
		_delSelectedButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeSelectedRowsToDelete();
			}
		});
		_selButtonPanel.add(_delSelectedButton);
		middlePanel.add(_selButtonPanel, BorderLayout.SOUTH);
		dialogPanel.add(middlePanel, BorderLayout.CENTER);

		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		// OK button
		_okButton = new JButton(I18nManager.getText("button.ok"));
		_okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				performDelete();
			}
		});
		buttonPanel.add(_okButton);
		_okButton.addKeyListener(escListener);
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
	 * Select a column of checkboxes
	 * @param isDelete true for delete all, false for keep all
	 */
	private void modifyAllCheckboxes(boolean isDelete)
	{
		for (int rowIndex=0; rowIndex<_infoList.getNumEntries(); rowIndex++)
		{
			_infoList.getDateInfo(rowIndex).setDeleteFlag(isDelete);
		}
		enableButtons();
	}

	private void changeSelectedRowsToKeep()
	{
		changeSelectedRows(false);
	}
	private void changeSelectedRowsToDelete()
	{
		changeSelectedRows(true);
	}

	/**
	 * Change the selected rows to either all keep or all delete
	 * @param isDelete true for delete, false for keep
	 */
	private void changeSelectedRows(boolean isDelete)
	{
		int firstRow = -1, lastRow = -1;
		for (int rowIndex : _infoTable.getSelectedRows())
		{
			if (firstRow == -1) {firstRow = rowIndex;}
			_infoList.getDateInfo(rowIndex).setDeleteFlag(isDelete);
			lastRow = rowIndex;
		}
		// Make sure all rows between first and last updated ones are updated in the table
		((AbstractTableModel) _infoTable.getModel()).fireTableRowsUpdated(firstRow, lastRow);
		enableButtons();
	}

	/**
	 * Enable or disable the keepSelected and deleteSelected buttons
	 * according to the current selection
	 */
	private void enableButtons()
	{
		boolean hasKeep = false, hasDelete = false;
		for (int rowIndex : _infoTable.getSelectedRows())
		{
			if (_infoList.getDateInfo(rowIndex).getDeleteFlag()) {
				hasDelete = true;
			}
			else {
				hasKeep = true;
			}
		}
		_delSelectedButton.setEnabled(hasKeep);
		_keepSelectedButton.setEnabled(hasDelete);
	}

	/**
	 * Do the actual point deletion according to the
	 * selected rows in the table
	 */
	private void performDelete()
	{
		int numMarked = 0;
		final int numPoints = _app.getTrackInfo().getTrack().getNumPoints();
		final int numDates = _infoList.getNumEntries();
		// Loop over all points to mark each one for deletion or not
		for (int p=0; p<numPoints; p++)
		{
			DataPoint point = _app.getTrackInfo().getTrack().getPoint(p);
			if (point != null)
			{
				final Date date = (point.hasTimestamp() ?
					point.getTimestamp().getCalendar(TimezoneHelper.getSelectedTimezone()).getTime()
					: null);
				boolean pointMarked = false;
				// Try to match each of the date info objects in the list
				for (int d=0; d<numDates; d++)
				{
					DateInfo info = _infoList.getDateInfo(d);
					if ( (info.isDateless() && date == null) // matches dateless
					  || (!info.isDateless() && date != null && info.isSameDate(date)))
					{
						pointMarked = info.getDeleteFlag();
						break;
					}
				}
				point.setMarkedForDeletion(pointMarked);
				if (pointMarked) {
					numMarked++;
				}
			}
		}
		// Now points have been marked, we can ask user to delete them (or delete automatically)
		if (numMarked > 0) {
			optionallyDeleteMarkedPoints(numMarked);
		}
		else
		{
			// Nothing to be deleted
			// Delete flags might have been reset, so refresh display
			UpdateMessageBroker.informSubscribers(DataSubscriber.SELECTION_CHANGED);
		}
		_dialog.dispose();
	}
}
