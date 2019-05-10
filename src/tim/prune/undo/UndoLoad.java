package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.FileInfo;
import tim.prune.data.PhotoList;
import tim.prune.data.TrackInfo;

/**
 * Operation to undo a load operation
 */
public class UndoLoad implements UndoOperation
{
	private int _cropIndex = -1;
	private int _numLoaded = -1;
	private DataPoint[] _contents = null;
	private PhotoList _photoList = null;
	private FileInfo _oldFileInfo = null;
	// Numbers of each media before operation
	private int _numPhotos = -1, _numAudios = -1;


	/**
	 * Constructor for appending
	 * @param inIndex index number of crop point
	 * @param inNumLoaded number of points loaded
	 */
	public UndoLoad(int inIndex, int inNumLoaded)
	{
		_cropIndex = inIndex;
		_numLoaded = inNumLoaded;
		_contents = null;
	}


	/**
	 * Constructor for replacing
	 * @param inOldTrackInfo track info being replaced
	 * @param inNumLoaded number of points loaded
	 * @param inPhotoList photo list, if any
	 */
	public UndoLoad(TrackInfo inOldTrackInfo, int inNumLoaded, PhotoList inPhotoList)
	{
		_cropIndex = -1;
		_numLoaded = inNumLoaded;
		_contents = inOldTrackInfo.getTrack().cloneContents();
		_oldFileInfo = inOldTrackInfo.getFileInfo().clone();
		_photoList = inPhotoList;
	}


	/**
	 * @return description of operation including number of points loaded
	 */
	public String getDescription()
	{
		String desc = I18nManager.getText("undo.load");
		if (_numLoaded > 0)
			desc = desc + " (" + _numLoaded + ")";
		return desc;
	}

	/**
	 * Set the number of photos and audios before the load operation
	 * @param inNumPhotos number of photos
	 * @param inNumAudios number of audios
	 */
	public void setNumPhotosAudios(int inNumPhotos, int inNumAudios)
	{
		_numPhotos = inNumPhotos;
		_numAudios = inNumAudios;
	}

	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		// remove source from fileinfo
		if (_oldFileInfo == null) {
			inTrackInfo.getFileInfo().removeSource();
		}
		else {
			inTrackInfo.setFileInfo(_oldFileInfo);
		}
		// Crop / replace
		if (_contents == null)
		{
			// crop track to previous size
			inTrackInfo.getTrack().cropTo(_cropIndex);
		}
		else
		{
			// replace photos how they were
			if (_photoList != null) {
				inTrackInfo.getPhotoList().restore(_photoList);
			}
			// Crop media lists to previous size (if specified)
			if (_numPhotos > -1) {inTrackInfo.getPhotoList().cropTo(_numPhotos);}
			if (_numAudios > -1) {inTrackInfo.getAudioList().cropTo(_numAudios);}
			// replace track contents with old
			if (!inTrackInfo.getTrack().replaceContents(_contents))
			{
				throw new UndoException(getDescription());
			}
		}
		// clear selection
		inTrackInfo.getSelection().clearAll();
	}
}
