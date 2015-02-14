package tim.prune.undo;

import tim.prune.data.DataPoint;
import tim.prune.data.TrackInfo;

/**
 * Operation to undo the connection of a photo to a point
 * where the point had to be cloned
 */
public class UndoConnectPhotoWithClone extends UndoConnectPhoto
{
	/** Additional undo object for removing inserted point */
	private UndoInsert _undoInsert = null;


	/**
	 * Constructor
	 * @param inPoint data point
	 * @param inFilename filename of photo
	 * @param inIndex index of cloned point
	 */
	public UndoConnectPhotoWithClone(DataPoint inPoint, String inFilename, int inIndex)
	{
		super(inPoint, inFilename);
		// Make an undo object for the insert
		_undoInsert = new UndoInsert(inIndex, 1);
	}

	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		//System.out.println("Performing undo: (" + super.getDescription() + ", " + _undoInsert.getDescription() + ")");
		// Firstly, undo connect
		super.performUndo(inTrackInfo);
		// Next, undo insert to remove cloned point
		_undoInsert.performUndo(inTrackInfo);
	}
}
