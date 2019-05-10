package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;

/**
 * Operation to undo a conversion from names to times
 */
public class UndoConvertNamesToTimes implements UndoOperation
{
	/** Start and end indices of section */
	private int _startIndex, _endIndex;
	/** Waypoint names before operation */
	private String[] _waypointNames = null;
	/** Timestamp strings before operation */
	private String[] _timestamps = null;

	/**
	 * Constructor
	 * @param inTrackInfo track info object to copy values from
	 */
	public UndoConvertNamesToTimes(TrackInfo inTrackInfo)
	{
		_startIndex = inTrackInfo.getSelection().getStart();
		_endIndex = inTrackInfo.getSelection().getEnd();
		final int numPoints = _endIndex - _startIndex + 1;
		_waypointNames = new String[numPoints];
		_timestamps = new String[numPoints];
		// Loop over points in selection, and copy names and timestamps
		for (int i=_startIndex; i<=_endIndex; i++)
		{
			DataPoint point = inTrackInfo.getTrack().getPoint(i);
			if (point.isWaypoint())
			{
				_waypointNames[i-_startIndex] = point.getWaypointName();
				_timestamps[i-_startIndex] = point.getFieldValue(Field.TIMESTAMP);
			}
		}
	}


	/**
	 * @return description of operation
	 */
	public String getDescription()
	{
		return I18nManager.getText("undo.convertnamestotimes");
	}


	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		// Sanity check
		Track track = inTrackInfo.getTrack();
		if (track.getNumPoints() <= _endIndex || _endIndex <= _startIndex) {
			throw new UndoException("Cannot undo conversion, track length doesn't match");
		}
		// Loop over points in selection and replace names and timestamps
		for (int i=_startIndex; i<=_endIndex; i++)
		{
			String storedName = _waypointNames[i-_startIndex];
			if (storedName != null)
			{
				// point had a name before the operation, so might have been converted
				DataPoint point = track.getPoint(i);
				point.setFieldValue(Field.WAYPT_NAME, storedName, true);
				point.setFieldValue(Field.TIMESTAMP, _timestamps[i-_startIndex], true);
			}
		}
		track.requestRescale();
		UpdateMessageBroker.informSubscribers();
	}
}
