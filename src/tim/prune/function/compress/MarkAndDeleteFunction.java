package tim.prune.function.compress;

import javax.swing.JOptionPane;

import tim.prune.App;
import tim.prune.FunctionLibrary;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;

/**
 * Superclass of those functions which mark points for deletion
 * (and optionally delete them automatically)
 */
public abstract class MarkAndDeleteFunction extends GenericFunction
{
	/** flag to remember whether the automatic deletion has been set to always */
	private boolean _automaticallyDelete = false;


	/**
	 * Constructor
	 * @param inApp App object
	 */
	public MarkAndDeleteFunction(App inApp)
	{
		super(inApp);
	}

	/**
	 * optionally delete the marked points
	 */
	protected void optionallyDeleteMarkedPoints(int inNumMarked)
	{
		// Allow calling of delete function with one click
		final String[] buttonTexts = {I18nManager.getText("button.yes"), I18nManager.getText("button.no"),
			I18nManager.getText("button.always")};
		int answer = _automaticallyDelete ? JOptionPane.YES_OPTION :
			JOptionPane.showOptionDialog(_parentFrame,
			I18nManager.getTextWithNumber("dialog.compress.confirm", inNumMarked),
			I18nManager.getText(getNameKey()), JOptionPane.YES_NO_CANCEL_OPTION,
			JOptionPane.WARNING_MESSAGE, null, buttonTexts, buttonTexts[1]);
		if (answer == JOptionPane.CANCEL_OPTION) {_automaticallyDelete = true;} // "always" is third option

		// Make sure function knows what to do, whether we'll call it now or later
		FunctionLibrary.FUNCTION_DELETE_MARKED_POINTS.setParentFunction(
				getNameKey(), getShouldSplitSegments());
		if (_automaticallyDelete || answer == JOptionPane.YES_OPTION)
		{
			new Thread(new Runnable() {
				public void run()
				{
					FunctionLibrary.FUNCTION_DELETE_MARKED_POINTS.begin();
				}
			}).start();
		}
	}

	/** by default, segments are not split at deleted points */
	protected boolean getShouldSplitSegments() {
		return false;
	}
}
