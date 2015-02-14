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


	/**
	 * Constructor
	 * @param inTrackInfo track info object
	 */
	public UndoLookupSrtm(TrackInfo inTrackInfo)
	{
		Track track = inTrackInfo.getTrack();
		int numPoints = track.getNumPoints();
		// Make array of points without altitudes
		_points = new DataPoint[numPoints];
		for (int i=0; i<numPoints; i++) {
			DataPoint point = track.getPoint(i);
			if (!point.hasAltitude()) {
				_points[i] = point;
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
				point.setFieldValue(Field.ALTITUDE, null, true);
			}
		}
		_points = null;
		UpdateMessageBroker.informSubscribers();
	}
}
