package tim.prune.function;

import tim.prune.App;

/**
 * Class to provide the function to delete the currently selected range
 */
public class DeleteSelectedRangeFunction extends DeleteBitOfTrackFunction
{
	/**
	 * Constructor
	 * @param inApp application object for callback
	 */
	public DeleteSelectedRangeFunction(App inApp)
	{
		super(inApp);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.deleterange";
	}

	/**
	 * @return name key for undo operation
	 */
	protected String getUndoNameKey() {
		return "undo.deleterange";
	}

	/**
	 * Begin the function
	 */
	public void begin()
	{
		// Get the currently selected range and pass indexes to parent class
		final int startIndex = _app.getTrackInfo().getSelection().getStart();
		final int endIndex   = _app.getTrackInfo().getSelection().getEnd();
		deleteSection(startIndex, endIndex);
	}
}
