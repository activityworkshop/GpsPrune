package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.DataPoint;
import tim.prune.data.Photo;
import tim.prune.data.TrackInfo;

/**
 * Operation to undo the connection of a photo to a point
 */
public class UndoConnectPhoto implements UndoOperation
{
	private DataPoint _point = null;
	private String _filename = null;


	/**
	 * Constructor
	 * @param inPoint data point
	 * @param inFilename filename of photo
	 */
	public UndoConnectPhoto(DataPoint inPoint, String inFilename)
	{
		_point = inPoint;
		_filename = inFilename;
	}


	/**
	 * @return description of operation including photo filename
	 */
	public String getDescription()
	{
		String desc = I18nManager.getText("undo.connectphoto") + " " + _filename;
		return desc;
	}


	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		// Disconnect again
		Photo photo = _point.getPhoto();
		if (photo != null)
		{
			_point.setPhoto(null);
			photo.setDataPoint(null);
			// inform subscribers
			UpdateMessageBroker.informSubscribers();
		}
		else
		{
			// throw exception if failed
			throw new UndoException(getDescription());
		}
	}
}