package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.Photo;
import tim.prune.data.TrackInfo;

/**
 * Operation to undo the rotation of a photo
 */
public class UndoRotatePhoto implements UndoOperation
{
	private Photo _photo = null;
	private boolean _rightwards = true;


	/**
	 * Constructor
	 * @param inPhoto photo
	 * @param inDir true if original operation was rightwards (clockwise) rotation
	 */
	public UndoRotatePhoto(Photo inPhoto, boolean inDir)
	{
		_photo = inPhoto;
		_rightwards = inDir;
	}


	/**
	 * @return description of operation including photo name
	 */
	public String getDescription()
	{
		return I18nManager.getText("undo.rotatephoto") + " " + _photo.getName();
	}


	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		_photo.rotate(!_rightwards);
		// inform subscribers
		UpdateMessageBroker.informSubscribers();
	}
}