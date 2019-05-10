package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.Photo;
import tim.prune.data.TrackInfo;

/**
 * Operation to undo a load photos operation
 */
public class UndoLoadPhotos implements UndoOperation
{
	private int _numPhotos = -1;
	private int _numPoints = -1;


	/**
	 * Constructor
	 * @param inNumPhotos number of photos loaded
	 * @param inNumPoints number of points loaded
	 */
	public UndoLoadPhotos(int inNumPhotos, int inNumPoints)
	{
		_numPhotos = inNumPhotos;
		_numPoints = inNumPoints;
	}


	/**
	 * @return description of operation including number of photos loaded
	 */
	public String getDescription()
	{
		String desc = I18nManager.getText("undo.loadphotos");
		if (_numPhotos > 0)
			desc = desc + " (" + _numPhotos + ")";
		return desc;
	}


	/**
	 * Perform the undo operation on the given Track
	 * Delete both track points and Photo objects
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		int cropIndex;
		// crop track to previous size
		if (_numPoints > 0)
		{
			cropIndex = inTrackInfo.getTrack().getNumPoints() - _numPoints;
			inTrackInfo.getTrack().cropTo(cropIndex);
		}
		else
		{
			// Loop through the points (if any) and detach them
			for (int i=0; i<_numPhotos; i++)
			{
				Photo photo = inTrackInfo.getPhotoList().getPhoto(inTrackInfo.getPhotoList().getNumPhotos() - 1 - i);
				if (photo.isConnected()) {
					DataPoint point = photo.getDataPoint();
					if (point != null) {point.setPhoto(null);}
				}
			}
		}
		// crop photo list to previous size
		cropIndex = inTrackInfo.getPhotoList().getNumPhotos() - _numPhotos;
		inTrackInfo.getPhotoList().cropTo(cropIndex);
		// clear selection
		inTrackInfo.getSelection().clearAll();
	}
}