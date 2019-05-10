package tim.prune.function;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.data.Checker;
import tim.prune.data.DataPoint;

/**
 * Function to allow the selection of which tracks to load from the file / stream
 */
public class SelectSegmentFunction extends GenericFunction
{

	/**
	 * Constructor
	 * @param inApp app object to use for load
	 */
	public SelectSegmentFunction(App inApp)
	{
		super(inApp);
	}

	/**
	 * Start the function
	 */
	public void begin()
	{
		// If no point selected, or a waypoint is selected, then do nothing
		DataPoint currPoint = _app.getTrackInfo().getCurrentPoint();
		if (currPoint != null && !currPoint.isWaypoint())
		{
			// Find indexes of segment start and end
			final int currIndex = _app.getTrackInfo().getSelection().getCurrentPointIndex();
			final int startIndex = Checker.getPreviousSegmentStart(_app.getTrackInfo().getTrack(), currIndex+1);
			final int endIndex   = Checker.getNextSegmentEnd(_app.getTrackInfo().getTrack(), currIndex);
			// Select this range if there is one
			if (endIndex > startIndex) {
				_app.getTrackInfo().getSelection().selectRange(startIndex, endIndex);
			}
		}
	}

	/** @return name key */
	public String getNameKey() {
		return "function.selectsegment";
	}
}
