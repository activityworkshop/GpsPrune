package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.Selection;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;

/**
 * Undo application of timestamps to range based on time estimation
 */
public class UndoApplyTimestamps implements UndoOperation
{
	/** Timestamp strings if present */
	private String[] _timestamps;
	/** Start of selected range */
	private int _selectionStart;


	/**
	 * Constructor
	 * @param inTrackInfo track info object
	 */
	public UndoApplyTimestamps(TrackInfo inTrackInfo)
	{
		Track track = inTrackInfo.getTrack();
		Selection selection = inTrackInfo.getSelection();
		_selectionStart = selection.getStart();
		int numPoints = selection.getEnd() - selection.getStart() + 1;
		_timestamps = new String[numPoints];
		for (int i=selection.getStart(); i<=selection.getEnd(); i++)
		{
			DataPoint point = track.getPoint(i);
			if (point.isWaypoint()) continue;
			if (point.hasTimestamp()) {
				_timestamps[i] = point.getFieldValue(Field.TIMESTAMP);
			}
		}
	}


	/**
	 * @return description of operation
	 */
	public String getDescription()
	{
		return I18nManager.getText("undo.applytimestamps");
	}


	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		// Loop through points again, and reset altitudes if they have one
		final int numPoints = _timestamps.length;
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = inTrackInfo.getTrack().getPoint(_selectionStart + i);
			if (point != null && point.hasTimestamp()) {
				point.setFieldValue(Field.TIMESTAMP, _timestamps[i], true);
			}
		}
		UpdateMessageBroker.informSubscribers();
	}
}
