package tim.prune.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import tim.prune.I18nManager;
import tim.prune.function.Cancellable;

/**
 * Class to show a progress dialog for various time-consuming functions
 */
public class ProgressDialog implements ProgressIndicator
{
	private JDialog _dialog = null;
	private final String _titleKey;
	private final String _labelKey;
	private JProgressBar _progressBar = null;
	private final JFrame _parentFrame;
	private final Cancellable _function;
	private boolean _cancelled = false;
	private int _maxValue = -1;

	/**
	 * Constructor
	 * @param inParentFrame parent frame for creating dialog
	 * @param inTitleKey key for dialog title text
	 * @param inLabelKey key for label text
	 * @param inFunction function which can be cancelled
	 */
	public ProgressDialog(JFrame inParentFrame, String inTitleKey,
		String inLabelKey, Cancellable inFunction)
	{
		_titleKey = inTitleKey;
		_labelKey = inLabelKey == null ? "confirm.running" : inLabelKey;
		_parentFrame = inParentFrame;
		_function = inFunction;
	}

	/**
	 * Constructor without cancel button
	 * @param inParentFrame parent frame
	 * @param inTitleKey key for dialog title
	 */
	public ProgressDialog(JFrame inParentFrame, String inTitleKey) {
		this(inParentFrame, inTitleKey, null, null);
	}

	/**
	 * Create the dialog to show the progress
	 */
	private JDialog createProgressDialog()
	{
		JDialog dialog = new JDialog(_parentFrame, I18nManager.getText(_titleKey));
		dialog.setLocationRelativeTo(_parentFrame);
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(new JLabel(I18nManager.getText(_labelKey)), BorderLayout.NORTH);
		// Centre panel with an empty border
		JPanel centrePanel = new JPanel();
		centrePanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		centrePanel.setLayout(new BorderLayout());
		_progressBar = new JProgressBar();
		_progressBar.setPreferredSize(new Dimension(300, 30));
		_progressBar.setValue(0);
		_progressBar.setStringPainted(true);
		_progressBar.setString("");
		centrePanel.add(_progressBar, BorderLayout.CENTER);
		panel.add(centrePanel, BorderLayout.CENTER);
		if (_function != null)
		{
			// Cancel button at the bottom
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
			cancelButton.addActionListener(e -> {_cancelled = true; _function.cancel();});
			buttonPanel.add(cancelButton);
			panel.add(buttonPanel, BorderLayout.SOUTH);
		}
		dialog.getContentPane().add(panel);
		dialog.pack();
		return dialog;
	}

	/**
	 * Show the dialog in indeterminate mode, before limits are calculated
	 */
	public void show()
	{
		_cancelled = false;
		if (_dialog == null) {
			_dialog = createProgressDialog();
		}
		_dialog.setVisible(true);
		setMaximumValue(-1);
	}

	/** Set the maximum value of the progress bar */
	public void setMaximumValue(int inMax)
	{
		_maxValue = inMax;
		_progressBar.setMaximum(inMax);
		_progressBar.setMinimum(0);
		_progressBar.setIndeterminate(inMax <= 1);
		setValue(0);
	}

	/** Set the current value of the progress bar */
	public void setValue(int inValue)
	{
		if (_maxValue > 0)
		{
			_progressBar.setString("" + inValue + " / " + _maxValue);
			_progressBar.setValue(inValue);
		}
		else {
			_progressBar.setString("" + inValue);
		}
	}

	/**
	 * Close the dialog
	 */
	public void close()
	{
		if (_dialog != null) {
			_dialog.dispose();
		}
	}

	/**
	 * @return true if cancel button was pressed
	 */
	public boolean wasCancelled() {
		return _cancelled;
	}

	@Override
	public void showProgress(int inCurrent, int inMax)
	{
		if (inMax != _maxValue) {
			setMaximumValue(inMax);
		}
		setValue(inCurrent);
	}
}
