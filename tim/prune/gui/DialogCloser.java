package tim.prune.gui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JDialog;

/**
 * Convenience class to close a dialog when the escape key is pressed
 */
public class DialogCloser extends KeyAdapter
{
	/** dialog to close */
	private JDialog _dialog = null;

	/**
	 * Constructor
	 * @param inDialog dialog to close
	 */
	public DialogCloser(JDialog inDialog) {
		_dialog = inDialog;
	}

	/**
	 * React to the release of the escape key
	 */
	public void keyReleased(KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			_dialog.dispose();
		}
	}
}
