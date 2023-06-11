package tim.prune.function;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.cmd.InsertPointCmd;
import tim.prune.data.DataPoint;

/**
 * Class to provide the function to duplicate
 * the current point and add to the end of the track
 */
public class DuplicatePoint extends GenericFunction
{
	/**
	 * Constructor
	 * @param inApp application object for callback
	 */
	public DuplicatePoint(App inApp) {
		super(inApp);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.duplicatepoint";
	}

	/**
	 * Begin the function
	 */
	public void begin()
	{
		DataPoint point = _app.getTrackInfo().getCurrentPoint();
		if (point != null)
		{
			InsertPointCmd command = new InsertPointCmd(point.clonePoint(), -1);
			command.setDescription(I18nManager.getText("undo.createpoint"));
			command.setConfirmText(I18nManager.getText("confirm.pointadded"));
			_app.execute(command);
		}
	}
}
