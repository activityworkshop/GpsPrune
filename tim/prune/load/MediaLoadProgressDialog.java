package tim.prune.load;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import tim.prune.I18nManager;
import tim.prune.function.Cancellable;

/**
 * Class to show a progress dialog for loading media.
 * Used for regular photo / audio loads plus the async
 * loading function.
 */
public class MediaLoadProgressDialog
{
	private JDialog _progressDialog   = null;
	private JProgressBar _progressBar = null;
	private JFrame _parentFrame = null;
	private Cancellable _function = null;

	/**
	 * Constructor
	 * @param inParentFrame parent frame for creating dialog
	 * @param inFunction function which can be cancelled
	 */
	public MediaLoadProgressDialog(JFrame inParentFrame, Cancellable inFunction)
	{
		_parentFrame = inParentFrame;
		_function = inFunction;
	}

	/**
	 * Create the dialog to show the progress
	 */
	private void createProgressDialog()
	{
		_progressDialog = new JDialog(_parentFrame, I18nManager.getText("dialog.jpegload.progress.title"));
		_progressDialog.setLocationRelativeTo(_parentFrame);
		_progressBar = new JProgressBar(0, 100);
		_progressBar.setValue(0);
		_progressBar.setStringPainted(true);
		_progressBar.setString("");
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		panel.add(new JLabel(I18nManager.getText("dialog.jpegload.progress")));
		panel.add(_progressBar);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_function.cancel();
			}
		});
		panel.add(cancelButton);
		_progressDialog.getContentPane().add(panel);
		_progressDialog.pack();
		_progressDialog.setVisible(true);
	}

	/**
	 * Show the dialog in indeterminate mode, before limits are calculated
	 */
	public void show()
	{
		if (_progressDialog == null)
		{
			createProgressDialog();
			_progressBar.setIndeterminate(true);
		}
	}

	/**
	 * Update the progress bar
	 * @param inCurrent current value
	 * @param inMax maximum value
	 */
	public void showProgress(int inCurrent, int inMax)
	{
		if (_progressDialog == null)
			createProgressDialog();
		if (_progressBar.isIndeterminate())
			_progressBar.setIndeterminate(false);
		if (inMax > 0)
			_progressBar.setMaximum(inMax);
		_progressBar.setValue(inCurrent);
		_progressBar.setString("" + inCurrent + " / " + _progressBar.getMaximum());
		// TODO: Need to repaint?
	}

	/**
	 * Close the dialog
	 */
	public void close()
	{
		if (_progressDialog != null)
			_progressDialog.dispose();
	}
}
