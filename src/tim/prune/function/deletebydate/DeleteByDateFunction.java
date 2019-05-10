package tim.prune.function.deletebydate;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

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
	/** Ok button */
	private JButton _okButton = null;
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

		JTable infoTable = new JTable(new DeletionTableModel(_infoList));
		JScrollPane pane = new JScrollPane(infoTable);
		pane.setPreferredSize(new Dimension(300, 80));
		pane.setBorder(BorderFactory.createEmptyBorder(2, 50, 2, 50));
		dialogPanel.add(pane, BorderLayout.CENTER);

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
			// Do nothing   //System.out.println("Nothing selected to delete!");
			// delete flags might have been reset, so refresh display
			UpdateMessageBroker.informSubscribers(DataSubscriber.SELECTION_CHANGED);
		}
		_dialog.dispose();
	}
}
