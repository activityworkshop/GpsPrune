package tim.prune.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JProgressBar;

import tim.prune.FunctionLibrary;
import tim.prune.function.PlayAudioFunction;

/**
 * Class to update the supplied progress bar on the basis of
 * the currently playing audio clip (if any)
 */
public class AudioListener implements Runnable, ActionListener
{
	/** progress bar */
	private JProgressBar _progressBar = null;

	/**
	 * Constructor
	 * @param inBar progress bar object to update
	 */
	public AudioListener(JProgressBar inBar) {
		_progressBar = inBar;
	}

	/**
	 * React to button press
	 */
	public void actionPerformed(ActionEvent inEvent) {
		new Thread(this).start();
	}

	/**
	 * Loop and update progress bar
	 */
	public void run()
	{
		int progress = 0;
		while (progress >= 0)
		{
			try {
				Thread.sleep(400);
			}
			catch (InterruptedException e) {}
			progress = ((PlayAudioFunction) FunctionLibrary.FUNCTION_PLAY_AUDIO).getPercentage();
			_progressBar.setVisible(progress >= 0);
			_progressBar.setValue(progress);
		}
	}
}
