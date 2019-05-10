package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;

/**
 * Undo lookup of altitudes from SRTM data
 */
public class UndoLookupSrtm implements UndoOperation
{
	/** DataPoint objects which didn't have altitudes before */
	private DataPoint[] _points;
	/** Altitude strings if present */
	private String[] _altitudes;


	/**
	 * Constructor
	 * @param inTrackInfo track info object
	 */
	public UndoLookupSrtm(TrackInfo inTrackInfo)
	{
		Track track = inTrackInfo.getTrack();
		int numPoints = track.getNumPoints();
		// Make arrays of points and altitudes
		_points = new DataPoint[numPoints];
		_altitudes = new String[numPoints];
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = track.getPoint(i);
			if (!point.hasAltitude() || point.getAltitude().getValue() == 0)
			{
				_points[i] = point;
				if (point.hasAltitude()) {
					_altitudes[i] = point.getFieldValue(Field.ALTITUDE);
				}
			}
		}
	}


	/**
	 * @return description of operation
	 */
	public String getDescription()
	{
		return I18nManager.getText("undo.lookupsrtm");
	}


	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		// Loop through points again, and reset altitudes if they have one
		final int numPoints = _points.length;
		for (int i=0; i<numPoints; i++) {
			DataPoint point = _points[i];
			if (point != null && point.hasAltitude()) {
				if (_altitudes[i] == null) {
					point.setFieldValue(Field.ALTITUDE, null, true);
				}
				else {
					point.setFieldValue(Field.ALTITUDE, _altitudes[i], true);
				}
			}
		}
		_points = null;
		UpdateMessageBroker.informSubscribers();
	}
}
