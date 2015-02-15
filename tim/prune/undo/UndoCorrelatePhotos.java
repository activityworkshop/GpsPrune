package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.Photo;
import tim.prune.data.TrackInfo;

/**
 * Operation to undo an auto-correlation of photos with points
 */
public class UndoCorrelatePhotos implements UndoOperation
{
	private DataPoint[] _contents = null;
	private DataPoint[] _photoPoints = null;
	private int _numPhotosCorrelated = -1;


	/**
	 * Constructor
	 * @param inTrackInfo track information
	 */
	public UndoCorrelatePhotos(TrackInfo inTrackInfo)
	{
		// Copy track contents
		_contents = inTrackInfo.getTrack().cloneContents();
		// Copy points associated with photos before correlation
		int numPhotos = inTrackInfo.getPhotoList().getNumPhotos();
		_photoPoints = new DataPoint[numPhotos];
		for (int i=0; i<numPhotos; i++) {
			_photoPoints[i] = inTrackInfo.getPhotoList().getPhoto(i).getDataPoint();
		}
	}

	/**
	 * @param inNumCorrelated number of photos correlated
	 */
	public void setNumPhotosCorrelated(int inNumCorrelated)
	{
		_numPhotosCorrelated = inNumCorrelated;
	}

	/**
	 * @return description of operation including parameters
	 */
	public String getDescription()
	{
		return I18nManager.getText("undo.correlatephotos") + " (" + _numPhotosCorrelated + ")";
	}


	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		// restore track to previous values
		inTrackInfo.getTrack().replaceContents(_contents);
		// restore photo association
		for (int i=0; i<_photoPoints.length; i++)
		{
			Photo photo = inTrackInfo.getPhotoList().getPhoto(i);
			// Only need to look at connected photos, since correlation wouldn't disconnect
			if (photo.getCurrentStatus() == Photo.Status.CONNECTED)
			{
				DataPoint prevPoint = _photoPoints[i];
				DataPoint currPoint = photo.getDataPoint();
				photo.setDataPoint(prevPoint);
				if (currPoint != null) {
					currPoint.setPhoto(null); // disconnect
				}
				if (prevPoint != null) {
					prevPoint.setPhoto(photo); // reconnect to prev point
				}
			}
		}
		// clear selection
		inTrackInfo.getSelection().clearAll();
	}
}
