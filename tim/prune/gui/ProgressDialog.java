package tim.prune.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import tim.prune.I18nManager;

/**
 * Class to show a simple progress dialog
 * similar to swing's ProgressMonitor but with a few
 * modifications
 */
public class ProgressDialog
{
	/** Parent frame */
	private JFrame _parentFrame = null;
	/** Key for title text */
	private String _titleKey = null;
	/** function dialog */
	private JDialog _dialog = null;
	/** Progress bar for function */
	private JProgressBar _progressBar = null;
	/** Cancel flag */
	private boolean _cancelled = false;


	/**
	 * Constructor
	 * @param inParentFrame parent frame
	 * @param inNameKey key for title
	 */
	public ProgressDialog(JFrame inParentFrame, String inNameKey)
	{
		_parentFrame = inParentFrame;
		_titleKey = inNameKey;
	}

	public void show()
	{
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, I18nManager.getText(_titleKey), false);
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.getContentPane().add(makeDialogComponents());
			_dialog.pack();
		}
		_progressBar.setMinimum(0);
		_progressBar.setMaximum(100);
		_progressBar.setValue(0);
		_progressBar.setIndeterminate(true);
		_cancelled = false;
		_dialog.setVisible(true);
	}

	/**
	 * Make the dialog components
	 * @return the GUI components for the dialog
	 */
	private Component makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout());
		dialogPanel.add(new JLabel(I18nManager.getText("confirm.running")), BorderLayout.NORTH);
		// Centre panel with an empty border
		JPanel centrePanel = new JPanel();
		centrePanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		centrePanel.setLayout(new BorderLayout());
		_progressBar = new JProgressBar();
		_progressBar.setPreferredSize(new Dimension(250, 30));
		centrePanel.add(_progressBar, BorderLayout.CENTER);
		dialogPanel.add(centrePanel, BorderLayout.CENTER);
		// Cancel button at the bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_cancelled = true;
				_dialog.dispose();
			}
		});
		buttonPanel.add(cancelButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		return dialogPanel;
	}

	/** Set the maximum value of the progress bar */
	public void setMaximum(int inMax) {
		_progressBar.setMaximum(inMax);
		_progressBar.setIndeterminate(inMax <= 1);
	}

	/** Set the current value of the progress bar */
	public void setValue(int inValue) {
		_progressBar.setValue(inValue);
	}

	/** Close the dialog */
	public void dispose() {
		_dialog.dispose();
	}

	/**
	 * @return true if cancel button was pressed
	 */
	public boolean isCancelled() {
		return _cancelled;
	}
}
