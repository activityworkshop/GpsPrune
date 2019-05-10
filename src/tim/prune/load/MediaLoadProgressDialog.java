package tim.prune.load;

import javax.swing.JFrame;

import tim.prune.function.Cancellable;
import tim.prune.gui.GenericProgressDialog;

/**
 * Class to show a progress dialog for loading media.
 * Used for regular photo / audio loads plus the async loading function.
 * Maybe this class isn't really needed...
 */
public class MediaLoadProgressDialog extends GenericProgressDialog
{
	/**
	 * Constructor
	 * @param inParentFrame parent frame for creating dialog
	 * @param inFunction function which can be cancelled
	 */
	public MediaLoadProgressDialog(JFrame inParentFrame, Cancellable inFunction)
	{
		super("dialog.jpegload.progress.title", "dialog.jpegload.progress", inParentFrame, inFunction);
	}
}
