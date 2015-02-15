package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.TrackInfo;

/**
 * Operation to undo a delete of a single point
 */
public class UndoDeletePoint extends UndoDeleteOperation
{
	private int _pointIndex = -1;
	private DataPoint _point = null;
	private int _photoIndex = -1;
	private int _audioIndex = -1;
	private boolean _segmentStart = false;


	/**
	 * Constructor
	 * @param inPointIndex index number of point within track
	 * @param inPoint data point
	 * @param inPhotoIndex index number of photo within photo list
	 * @param inAudioIndex index number of audio within audio list
	 * @param inSegmentStart true if following track point starts new segment
	 */
	public UndoDeletePoint(int inPointIndex, DataPoint inPoint, int inPhotoIndex, int inAudioIndex,
		boolean inSegmentStart)
	{
		_pointIndex = inPointIndex;
		_point = inPoint;
		_photoIndex = inPhotoIndex;
		_audioIndex = inAudioIndex;
		_segmentStart = inSegmentStart;
	}


	/**
	 * @return description of operation including point name if any
	 */
	public String getDescription()
	{
		String desc = I18nManager.getText("undo.deletepoint");
		String pointName = _point.getWaypointName();
		if (pointName != null && !pointName.equals(""))
			desc = desc + " " + pointName;
		return desc;
	}


	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		// restore point into track
		if (!inTrackInfo.getTrack().insertPoint(_point, _pointIndex))
		{
			throw new UndoException(getDescription());
		}
		// Re-attach / Re-insert photo into list if necessary
		if (_point.getPhoto() != null && _photoIndex > -1)
		{
			// Check if photo is still in list
			if (!inTrackInfo.getPhotoList().contains(_point.getPhoto()))
			{
				// photo has been removed - need to reinsert
				inTrackInfo.getPhotoList().addPhoto(_point.getPhoto(), _photoIndex);
			}
			// Ensure that photo is associated with point
			if (_point.getPhoto().getDataPoint() != _point) {
				_point.getPhoto().setDataPoint(_point);
			}
		}
		// Re-add audio as well if necessary
		if (_point.getAudio() != null && _audioIndex > -1)
		{
			// add audio object to list
			inTrackInfo.getAudioList().addAudio(_point.getAudio(), _audioIndex);
			_point.getAudio().setDataPoint(_point);
		}
		// Restore previous status of following track point if necessary
		if (!_segmentStart)
		{
			// Deletion of point can only set following point to true, so only need to set it back to false
			DataPoint nextTrackPoint = inTrackInfo.getTrack().getNextTrackPoint(_pointIndex + 1);
			if (nextTrackPoint != null) {
				nextTrackPoint.setSegmentStart(false);
			}
		}
		// If there's a current point or range selected, maybe need to adjust start and/or end
		modifySelection(inTrackInfo, _pointIndex, _pointIndex);
	}
}
