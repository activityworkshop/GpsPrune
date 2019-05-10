package tim.prune.correlate;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Helper class to listen for changed options on the Correlators
 * Tightly coupled but only to ok button and preview function
 */
public class OptionsChangedListener implements KeyListener, ActionListener, ItemListener, Runnable
{
	/** Correlator object for callbacks */
	private Correlator _correlator;
	/** Thread counter */
	private int _threadCount = 0;

	/** Default delay time from change to preview trigger */
	private static final long PREVIEW_DELAY_TIME = 2500L;


	/**
	 * Constructor
	 * @param inCorrelator correlator object for callbacks
	 */
	public OptionsChangedListener(Correlator inCorrelator)
	{
		_correlator = inCorrelator;
	}

	/**
	 * Respond to actions performed on control
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent inEvent)
	{
		optionsChanged();
	}

	/**
	 * Run method, called by separate thread(s)
	 * @see java.lang.Runnable#run()
	 */
	public void run()
	{
		// Wait for a certain time
		try {
			Thread.sleep(PREVIEW_DELAY_TIME);
		}
		catch (InterruptedException ie) {}
		_threadCount--;
		if (_threadCount == 0) {
			// trigger preview (false means automatic)
			_correlator.createPreview(false);
		}
	}

	/**
	 * Respond to key pressed event
	 * @param inEvent event
	 */
	public void keyPressed(KeyEvent inEvent)
	{
		optionsChanged();
	}

	/** Ignore key released events */
	public void keyReleased(KeyEvent inEvent) {}

	/** Ignore key typed events */
	public void keyTyped(KeyEvent e) {}

	/**
	 * Respond to item change events (eg dropdown)
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent inEvent)
	{
		if (inEvent.getStateChange() == ItemEvent.SELECTED) {
			optionsChanged();
		}
	}

	/**
	 * Trigger that an option has changed, whatever type
	 */
	private void optionsChanged()
	{
		// disable ok button
		_correlator.disableOkButton();
		// start new thread to trigger preview
		_threadCount++;
		new Thread(this).start();
	}
}
