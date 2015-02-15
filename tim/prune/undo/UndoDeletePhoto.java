package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.DataPoint;
import tim.prune.data.Photo;
import tim.prune.data.TrackInfo;

/**
 * Operation to undo a delete of a single photo, either with or without point
 */
public class UndoDeletePhoto extends UndoDeleteOperation
{
	private int _photoIndex = -1;
	private Photo _photo = null;
	private int _pointIndex = -1;
	private DataPoint _point = null;


	/**
	 * Constructor
	 * @param inPhoto photo
	 * @param inPhotoIndex index number of photo within photo list
	 * @param inPoint data point
	 * @param inPointIndex index number of point within track
	 */
	public UndoDeletePhoto(Photo inPhoto, int inPhotoIndex, DataPoint inPoint, int inPointIndex)
	{
		_photo = inPhoto;
		_photoIndex = inPhotoIndex;
		_point = inPoint;
		_pointIndex = inPointIndex;
	}


	/**
	 * @return description of operation including photo name
	 */
	public String getDescription()
	{
		String desc = I18nManager.getText("undo.removephoto") + " " + _photo.getName();
		return desc;
	}


	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		// restore photo
		inTrackInfo.getPhotoList().addPhoto(_photo, _photoIndex);
		// if there's a point to restore, restore it
		if (_point != null)
		{
			if (!inTrackInfo.getTrack().insertPoint(_point, _pointIndex))
			{
				throw new UndoException(getDescription());
			}
			// Change the current point/range selection if required
			modifySelection(inTrackInfo, _pointIndex, _pointIndex);
		}
		else
		{
			// update needed if not already triggered by track update
			UpdateMessageBroker.informSubscribers();
		}
		// Ensure that photo is associated with point and vice versa
		_photo.setDataPoint(_point);
		if (_point != null) {
			_point.setPhoto(_photo);
		}
	}
}
