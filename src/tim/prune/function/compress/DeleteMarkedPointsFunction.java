package tim.prune.function.compress;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.undo.UndoDeleteMarked;

/**
 * Function to delete the marked points in the track
 */
public class DeleteMarkedPointsFunction extends GenericFunction
{
	private boolean _splitSegments = false;
	private String  _parentFunctionKey = null;

	/** Constructor */
	public DeleteMarkedPointsFunction(App inApp) {
		super(inApp);
	}

	@Override
	public String getNameKey() {
		return "function.deletemarked";
	}

	/**
	 * Get notification about parent function
	 * @param inKey parent function name key
	 * @param inSplitSegments true to split segment, false to not
	 */
	public void setParentFunction(String inKey, boolean inSplitSegments)
	{
		_parentFunctionKey = inKey;
		_splitSegments = inSplitSegments;
	}

	@Override
	public void begin()
	{
		UndoDeleteMarked undo = new UndoDeleteMarked(_app.getTrackInfo().getTrack());
		// call track to do the actual delete//
		int numPointsDeleted = _app.getTrackInfo().deleteMarkedPoints(_splitSegments);
		// add to undo stack if successful
		if (numPointsDeleted > 0)
		{
			undo.setNumPointsDeleted(numPointsDeleted);
			_app.completeFunction(undo, "" + numPointsDeleted + " "
				 + (numPointsDeleted==1?I18nManager.getText("confirm.deletepoint.single"):I18nManager.getText("confirm.deletepoint.multi")));
		}
		else
		{
			final String titleKey = (_parentFunctionKey == null ? getNameKey() : _parentFunctionKey);
			_app.showErrorMessage(titleKey, "dialog.deletemarked.nonefound");
		}
	}
}
