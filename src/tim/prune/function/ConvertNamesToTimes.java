package tim.prune.function;

import tim.prune.App;
import tim.prune.DataSubscriber;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.TimestampUtc;
import tim.prune.data.Track;
import tim.prune.undo.UndoConvertNamesToTimes;

/**
 * Class to provide the function to convert waypoint names to timestamps
 */
public class ConvertNamesToTimes extends GenericFunction
{
	/**
	 * Constructor
	 * @param inApp application object for callback
	 */
	public ConvertNamesToTimes(App inApp)
	{
		super(inApp);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.convertnamestotimes";
	}

	/**
	 * Begin the function
	 */
	public void begin()
	{
		int selStart = _app.getTrackInfo().getSelection().getStart();
		int selEnd = _app.getTrackInfo().getSelection().getEnd();
		final Track track = _app.getTrackInfo().getTrack();
		if (!track.hasData(Field.WAYPT_NAME, selStart, selEnd))
		{
			_app.showErrorMessage(getNameKey(), "error.convertnamestotimes.nonames");
			return;
		}
		UndoConvertNamesToTimes undo = new UndoConvertNamesToTimes(_app.getTrackInfo());
		int numConverted = 0;
		// Loop over all points in selection
		for (int i=selStart; i<=selEnd; i++)
		{
			DataPoint point = track.getPoint(i);
			if (point.isWaypoint())
			{
				TimestampUtc tstamp = new TimestampUtc(point.getWaypointName());
				if (tstamp.isValid())
				{
					// timestamp could be parsed!
					point.setFieldValue(Field.TIMESTAMP, point.getWaypointName(), false);
					// set waypoint name to nothing (track point)
					point.setFieldValue(Field.WAYPT_NAME, null, false);
					// increment counter
					numConverted++;
				}
			}
		}
		if (numConverted > 0)
		{
			_app.getTrackInfo().getTrack().requestRescale();
			UpdateMessageBroker.informSubscribers(DataSubscriber.DATA_EDITED);
			_app.completeFunction(undo, I18nManager.getText("confirm.convertnamestotimes"));
		}
	}

}
