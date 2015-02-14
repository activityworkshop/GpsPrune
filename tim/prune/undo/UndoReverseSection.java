package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.data.TrackInfo;

/**
 * Undo reversal of track section
 */
public class UndoReverseSection implements UndoOperation
{
	private int _startIndex, _endIndex;


	/**
	 * Constructor
	 * @param inStart start index of section
	 * @param inEnd end index of section
	 */
	public UndoReverseSection(int inStart, int inEnd)
	{
		_startIndex = inStart;
		_endIndex = inEnd;
	}


	/**
	 * @return description of operation
	 */
	public String getDescription()
	{
		return I18nManager.getText("undo.reverse");
	}


	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		if (!inTrackInfo.getTrack().reverseRange(_startIndex, _endIndex))
		{
			throw new UndoException(getDescription());
		}
	}
}
