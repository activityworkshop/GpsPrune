package tim.prune.function.edit;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;

/**
 * Function to toggle the segment flag of the current point
 */
public class ToggleSegmentFlag extends GenericFunction
{
	/**
	 * Constructor
	 * @param inApp application object to inform of success
	 */
	public ToggleSegmentFlag(App inApp)	{
		super(inApp);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.togglesegmentflag";
	}

	/**
	 * Execute the function
	 */
	public void begin()
	{
		DataPoint point = _app.getTrackInfo().getCurrentPoint();
		if (point == null || point.isWaypoint()) {
			return;
		}
		// Check if there is any track point before this one - if not then the segment start flag can't be edited
		final int pointIndex = _app.getTrackInfo().getSelection().getCurrentPointIndex();
		final DataPoint prevTrackPoint = _app.getTrackInfo().getTrack().getPreviousTrackPoint(pointIndex - 1);
		if (prevTrackPoint == null) {
			return;
		}

		boolean currFlag = point.getSegmentStart();
		// Make lists for edit and undo, and add the changed field
		FieldEditList editList = new FieldEditList();
		FieldEditList undoList = new FieldEditList();
		editList.addEdit(new FieldEdit(Field.NEW_SEGMENT, currFlag ? "0" : "1"));
		undoList.addEdit(new FieldEdit(Field.NEW_SEGMENT, currFlag ? "1" : "0"));

		// Pass back to App to perform edit
		_app.completePointEdit(editList, undoList);
	}
}
