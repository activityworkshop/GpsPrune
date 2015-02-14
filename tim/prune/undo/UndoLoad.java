package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.TrackInfo;

/**
 * Operation to undo a load operation
 */
public class UndoLoad implements UndoOperation
{
	private int _cropIndex = -1;
	private int _numLoaded = -1;
	private DataPoint[] _contents = null;
	private String _previousFilename = null;


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
		_previousFilename = null;
	}


	/**
	 * Constructor for replacing
	 * @param inOldTrack track being replaced
	 * @param inNumLoaded number of points loaded
	 */
	public UndoLoad(TrackInfo inOldTrackInfo, int inNumLoaded)
	{
		_cropIndex = -1;
		_numLoaded = inNumLoaded;
		_contents = inOldTrackInfo.getTrack().cloneContents();
		if (inOldTrackInfo.getFileInfo().getNumFiles() == 1)
			_previousFilename = inOldTrackInfo.getFileInfo().getFilename();
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
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		// remove file from fileinfo
		inTrackInfo.getFileInfo().removeFile();
		if (_previousFilename != null)
		{
			inTrackInfo.getFileInfo().setFile(_previousFilename);
		}
		// Crop / replace
		if (_contents == null)
		{
			// crop track to previous size
			inTrackInfo.getTrack().cropTo(_cropIndex);
		}
		else
		{
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