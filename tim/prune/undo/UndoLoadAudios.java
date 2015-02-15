package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.data.TrackInfo;

/**
 * Operation to undo a load audios operation
 */
public class UndoLoadAudios implements UndoOperation
{
	/** Number of audio clips added */
	private int _numAudios = -1;


	/**
	 * Constructor
	 * @param inNumAudios number of audios loaded
	 */
	public UndoLoadAudios(int inNumAudios)
	{
		_numAudios = inNumAudios;
	}


	/**
	 * @return description of operation including number of audios loaded
	 */
	public String getDescription()
	{
		String desc = I18nManager.getText("undo.loadaudios");
		if (_numAudios > 0)
			desc = desc + " (" + _numAudios + ")";
		return desc;
	}


	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		// crop audio list to previous size
		int cropIndex = inTrackInfo.getAudioList().getNumAudios() - _numAudios;
		inTrackInfo.getAudioList().cropTo(cropIndex);
		// clear selection
		inTrackInfo.getSelection().clearAll();
	}
}
