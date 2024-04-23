package tim.prune.function.filesleuth.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import tim.prune.I18nManager;
import tim.prune.function.filesleuth.data.DateRange;
import tim.prune.gui.GuiGridLayout;
import tim.prune.gui.IconManager;
import tim.prune.gui.StatusIcon;
import tim.prune.gui.WholeNumberField;

/** Dialog to make it easier to define a date range for the search */
public class DateRangeEditor extends JDialog
{
	private final DateRangeUser _parent;
	private JTabbedPane _tabs = null;
	private WholeNumberField _yearField0 = null, _yearField1 = null, _yearField2 = null;
	private MonthCombo _monthField0 = null, _monthField1 = null, _monthField2 = null;
	private WholeNumberField _dayField0 = null, _dayField1 = null, _dayField2 = null;
	private JTextField _resultField0 = null, _resultField1 = null;
	private StatusIcon _statusIcon0 = null, _statusIcon1 = null;
	private JButton _okButton = null;

	/** Listener to trigger a preview when any inputs are changed */
	private class PreviewListener implements KeyListener, ItemListener
	{
		public void itemStateChanged(ItemEvent e) {preview();}
		public void keyPressed(KeyEvent e) {}
		public void keyReleased(KeyEvent e) {preview();}
		public void keyTyped(KeyEvent e) {}
	}

	public DateRangeEditor(JDialog inDialog, DateRangeUser inUser,
		IconManager inIconManager)
	{
		super(inDialog, I18nManager.getText("dialog.editdaterange.title"), true); // modal
		_parent = inUser;
		setLocationRelativeTo(inDialog);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		getContentPane().add(makeDialogComponents(inIconManager));
		pack();
	}

	/**
	 * Create dialog components
	 * @param inIconManager icon manager for status icon
	 * @return Panel containing all gui elements in dialog
	 */
	private JPanel makeDialogComponents(IconManager inIconManager)
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout(0, 10));
		dialogPanel.add(new JLabel(I18nManager.getText("dialog.editdaterange.desc")), BorderLayout.NORTH);
		_tabs = new JTabbedPane();
		dialogPanel.add(_tabs, BorderLayout.CENTER);

		final PreviewListener previewListener = new PreviewListener();
		JPanel ymdPanel = new JPanel();
		ymdPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		GuiGridLayout grid = new GuiGridLayout(ymdPanel);
		grid.add(new JLabel(I18nManager.getText("dialog.editdaterange.year")));
		_yearField0 = new WholeNumberField(4);
		_yearField0.addKeyListener(previewListener);
		grid.add(_yearField0);
		grid.add(new JLabel(I18nManager.getText("dialog.editdaterange.month")));
		_monthField0 = new MonthCombo();
		_monthField0.addKeyListener(previewListener);
		_monthField0.addItemListener(previewListener);
		grid.add(_monthField0);
		grid.add(new JLabel(I18nManager.getText("dialog.editdaterange.day")));
		_dayField0 = new WholeNumberField(2);
		_dayField0.addKeyListener(previewListener);
		grid.add(_dayField0);
		grid.addVerticalGap(8);
		grid.add(new JLabel(I18nManager.getText("dialog.editdaterange.result")));
		_resultField0 = new JTextField(10);
		_resultField0.setEditable(false);
		_statusIcon0 = new StatusIcon(inIconManager);
		grid.add(bundleIntoPanel(_resultField0, _statusIcon0));
		_tabs.add(I18nManager.getText("dialog.editdaterange.yearmonthday"), ymdPanel);

		JPanel startEndPanel = new JPanel();
		startEndPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		double[] weights = new double[] {0.5, 1.0, 0.0, 1.0};
		boolean[] aligns = new boolean[] {true, false, true, false};
		grid = new GuiGridLayout(startEndPanel, weights, aligns);
		grid.add(new JLabel(I18nManager.getText("dialog.editdaterange.year")));
		_yearField1 = new WholeNumberField(4);
		_yearField1.addKeyListener(previewListener);
		grid.add(_yearField1);
		grid.add(new JLabel(I18nManager.getText("dialog.editdaterange.to")));
		_yearField2 = new WholeNumberField(4);
		_yearField2.addKeyListener(previewListener);
		grid.add(_yearField2);
		grid.add(new JLabel(I18nManager.getText("dialog.editdaterange.month")));
		_monthField1 = new MonthCombo();
		grid.add(_monthField1);
		grid.add(new JLabel(I18nManager.getText("dialog.editdaterange.to")));
		_monthField2 = new MonthCombo();
		grid.add(_monthField2);
		_monthField1.addItemListener(e -> monthChanged(_monthField1, _monthField2));
		_monthField2.addItemListener(e -> monthChanged(_monthField2, _monthField1));
		grid.add(new JLabel(I18nManager.getText("dialog.editdaterange.day")));
		_dayField1 = new WholeNumberField(2);
		_dayField1.addKeyListener(previewListener);
		grid.add(_dayField1);
		grid.add(new JLabel(I18nManager.getText("dialog.editdaterange.to")));
		_dayField2 = new WholeNumberField(2);
		_dayField2.addKeyListener(previewListener);
		grid.add(_dayField2);
		grid.addVerticalGap(8);
		grid.add(new JLabel(I18nManager.getText("dialog.editdaterange.result")));
		_resultField1 = new JTextField(22);
		_resultField1.setEditable(false);
		_statusIcon1 = new StatusIcon(inIconManager);
		grid.add(bundleIntoPanel(_resultField1, _statusIcon1), 3, false);
		_tabs.add(I18nManager.getText("dialog.editdaterange.daterange"), startEndPanel);
		_tabs.addChangeListener(e -> preview());

		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		_okButton = new JButton(I18nManager.getText("button.ok"));
		ActionListener okListener = e -> finish();
		_okButton.addActionListener(okListener);
		_okButton.setEnabled(false);
		buttonPanel.add(_okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(e -> dispose());
		buttonPanel.add(cancelButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		dialogPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 15));
		return dialogPanel;
	}

	private JPanel bundleIntoPanel(JTextField inTextField, StatusIcon inIcon)
	{
		JPanel subpanel = new JPanel();
		subpanel.setLayout(new BorderLayout());
		subpanel.add(inTextField, BorderLayout.CENTER);
		subpanel.add(inIcon, BorderLayout.EAST);
		return subpanel;
	}

	private void monthChanged(MonthCombo inCombo1, MonthCombo inCombo2)
	{
		final boolean thisEmpty = inCombo1.getSelectedIndex() == 0;
		final boolean otherEmpty = inCombo2.getSelectedIndex() == 0;
		if (thisEmpty != otherEmpty) {
			inCombo2.setSelectedIndex(inCombo1.getSelectedIndex());
		}
		preview();
	}

	public void show(String inCurrentValue)
	{
		DateRange currRange = DateRange.parseString(inCurrentValue);
		if (currRange == null || currRange.isEmpty() || !currRange.isValid())
		{
			int currentYear = Calendar.getInstance().get(Calendar.YEAR);
			_yearField0.setText("" + currentYear);
			_yearField1.setText("" + currentYear);
			_yearField2.setText("" + currentYear);
			_tabs.setSelectedIndex(0);
		}
		else if (currRange.isYearMonthDay())
		{
			final String shortString = currRange.toShortString();
			final boolean hasMonth = shortString.length() > 4;
			final boolean hasDay = shortString.length() == 10;
			_yearField0.setValue(currRange.getDateFrom().getYear());
			_monthField0.setSelectedIndex(hasMonth ? currRange.getDateFrom().getMonth() : 0);
			_dayField0.setText(hasDay ? String.valueOf(currRange.getDateFrom().getDay()) : "");
			_tabs.setSelectedIndex(0);
		}
		else
		{
			_yearField1.setValue(currRange.getDateFrom().getYear());
			_yearField2.setValue(currRange.getDateTo().getYear());
			_monthField1.setSelectedIndex(currRange.getDateFrom().getMonth());
			_monthField2.setSelectedIndex(currRange.getDateTo().getMonth());
			_dayField1.setText(String.valueOf(currRange.getDateFrom().getDay()));
			_dayField2.setText(String.valueOf(currRange.getDateTo().getDay()));
			_tabs.setSelectedIndex(1);
		}
		preview();
		setVisible(true);
	}

	private void preview()
	{
		DateRange dateRange = getDateRange();
		boolean ok = dateRange != null && dateRange.isValid();
		if (_tabs.getSelectedIndex() == 0)
		{
			boolean isWholeYear = _monthField0.isAllMonths();
			_dayField0.setEnabled(!isWholeYear);
			if (isWholeYear) {
				_dayField0.setText("");
				ok = dateRange != null && dateRange.isValid();
			}
			_resultField0.setText(dateRange == null ? "" : dateRange.toShortString());
			if (ok) {
				_statusIcon0.setStatusValid();
			}
			else {
				_statusIcon0.setStatusInvalid();
			}
		}
		else
		{
			boolean isWholeYear = _monthField1.isAllMonths();
			_dayField1.setEnabled(!isWholeYear);
			if (isWholeYear)
			{
				_dayField1.setText("");
				_dayField2.setText("");
			}
			_dayField2.setEnabled(!isWholeYear);
			_resultField1.setText(dateRange == null ? "" : dateRange.toString());
			if (ok) {
				_statusIcon1.setStatusValid();
			}
			else {
				_statusIcon1.setStatusInvalid();
			}
		}
		_okButton.setEnabled(ok);
	}

	private DateRange getDateRange()
	{
		if (_tabs.getSelectedIndex() == 0)
		{
			int year = _yearField0.getValue();
			int month = _monthField0.getMonthNum();
			int day = _dayField0.getValue();
			return DateRange.parseValues(year, month, day);
		}
		else
		{
			int year1 = _yearField1.getValue();
			int month1 = _monthField1.getMonthNum();
			int day1 = _dayField1.getValue();
			int year2 = _yearField2.getValue();
			int month2 = _monthField2.getMonthNum();
			int day2 = _dayField2.getValue();
			return DateRange.parseValues(year1, month1, day1, year2, month2, day2);
		}
	}

	/** Ok button has been pressed, so close dialog and pass range back to parent */
	private void finish()
	{
		DateRange range = getDateRange();
		if (range != null && range.isValid())
		{
			_parent.updateDateRange(getDateRange());
			dispose();
		}
	}
}
