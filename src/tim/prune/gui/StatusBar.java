package tim.prune.gui;

import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import tim.prune.DataSubscriber;

/**
 * Class to act as a status bar for the application
 */
public class StatusBar extends JPanel implements Runnable, DataSubscriber
{
	/** Label for displaying the text */
	private JLabel _label = null;
	/** timer for clearing the status */
	private long _timer = 0L;
	/** thread for clearing the status */
	private Thread _thread = null;

	/** Number of milliseconds until status text cleared */
	private static final long DEFAULT_CLEAR_INTERVAL = 1000L * 4;


	/**
	 * Constructor
	 */
	public StatusBar()
	{
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		setBorder(BorderFactory.createLoweredBevelBorder());
		_label = new JLabel(" ");
		_label.setFont(_label.getFont().deriveFont(8));
		add(_label);
	}

	/**
	 * Run method, to check if text should be deleted
	 * @see java.lang.Runnable#run()
	 */
	public void run()
	{
		while (System.currentTimeMillis() < _timer) {
			try {
				Thread.sleep(500);
			}
			catch (InterruptedException ie) {} // ignore
		}
		_label.setText(" ");
	}

	/**
	 * Accept notification that an action has been completed
	 * @param inMessage message to display
	 */
	public void actionCompleted(String inMessage)
	{
		_label.setText(" " + inMessage);
		_timer = System.currentTimeMillis() + DEFAULT_CLEAR_INTERVAL;
		// If necessary, start a new checker thread
		if (_thread == null || !_thread.isAlive())
		{
			_thread = new Thread(this);
			_thread.start();
		}
	}

	/**
	 * Ignore signals about updated data
	 * @param inUpdateType update type
	 */
	public void dataUpdated(byte inUpdateType)
	{
	}
}
