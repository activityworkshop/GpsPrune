package tim.prune.load;

import javax.swing.JFrame;

import tim.prune.function.Cancellable;
import tim.prune.gui.ProgressDialog;

/**
 * Class to show a progress dialog for loading media.
 * Used for regular photo / audio loads plus the async loading function.
 * Maybe this class isn't really needed...
 */
public class MediaLoadProgressDialog extends ProgressDialog
{
	/**
	 * Constructor
	 * @param inParentFrame parent frame for creating dialog
	 * @param inFunction function which can be cancelled
	 */
	public MediaLoadProgressDialog(JFrame inParentFrame, Cancellable inFunction)
	{
		super(inParentFrame, "dialog.jpegload.progress.title", "dialog.jpegload.progress", inFunction);
	}
}
