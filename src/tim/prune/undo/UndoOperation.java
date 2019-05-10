package tim.prune.undo;

import tim.prune.data.TrackInfo;

/**
 * Interface implemented by all Undo Operations
 */
public interface UndoOperation
{
	/**
	 * Get the description of this operation
	 * @return description of operation including parameters
	 */
	public String getDescription();

	/**
	 * Perform the undo operation on the specified track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 * @throws UndoException when undo fails
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException;
}