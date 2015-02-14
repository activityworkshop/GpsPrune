package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.data.TrackInfo;

/**
 * Operation to undo a load photos operation
 */
public class UndoLoadPhotos implements UndoOperation
{
	private int _numLoaded = -1;

	// TODO: Handle possibility of photos not having datapoints (yet)

	/**
	 * Constructor
	 * @param inNumLoaded number of photos loaded
	 */
	public UndoLoadPhotos(int inNumLoaded)
	{
		_numLoaded = inNumLoaded;
	}


	/**
	 * @return description of operation including number of photos loaded
	 */
	public String getDescription()
	{
		String desc = I18nManager.getText("undo.loadphotos");
		if (_numLoaded > 0)
			desc = desc + " (" + _numLoaded + ")";
		return desc;
	}


	/**
	 * Perform the undo operation on the given Track
	 * Delete both track points and Photo objects
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		// crop track to previous size
		int cropIndex = inTrackInfo.getTrack().getNumPoints() - _numLoaded;
		inTrackInfo.getTrack().cropTo(cropIndex);
		// crop photo list to previous size
		// (currently it is assumed that the number of points is the same as number of photos)
		cropIndex = inTrackInfo.getPhotoList().getNumPhotos() - _numLoaded;
		inTrackInfo.getPhotoList().cropTo(cropIndex);
		// clear selection
		inTrackInfo.getSelection().clearAll();
	}
}