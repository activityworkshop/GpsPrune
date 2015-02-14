package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.Photo;
import tim.prune.data.TrackInfo;

/**
 * Operation to undo the disconnection of a photo from a point
 */
public class UndoDisconnectPhoto implements UndoOperation
{
	private DataPoint _point = null;
	private Photo _photo = null;
	private String _filename = null;


	/**
	 * Constructor
	 * @param inPoint data point
	 * @param inFilename filename of photo
	 */
	public UndoDisconnectPhoto(DataPoint inPoint, String inFilename)
	{
		_point = inPoint;
		_photo = inPoint.getPhoto();
		_filename = inFilename;
	}


	/**
	 * @return description of operation including photo filename
	 */
	public String getDescription()
	{
		String desc = I18nManager.getText("undo.disconnectphoto") + " " + _filename;
		return desc;
	}


	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		// Connect again
		if (_point != null && _photo != null)
		{
			_point.setPhoto(_photo);
			_photo.setDataPoint(_point);
			// inform subscribers
			inTrackInfo.triggerUpdate();
		}
		else
		{
			// throw exception if failed
			throw new UndoException(getDescription());
		}
	}
}