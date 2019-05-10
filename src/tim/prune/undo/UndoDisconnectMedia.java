package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.data.AudioClip;
import tim.prune.data.DataPoint;
import tim.prune.data.Photo;
import tim.prune.data.TrackInfo;

/**
 * Operation to undo the disconnection of a photo or audio from a point
 */
public class UndoDisconnectMedia implements UndoOperation
{
	private DataPoint _point = null;
	private Photo _photo = null;
	private AudioClip _audio = null;
	private String _filename = null;


	/**
	 * Constructor
	 * @param inPoint data point
	 * @param inPhoto true if photo was disconnected
	 * @param inAudio true if audio was disconnected
	 * @param inFilename filename of photo / audio
	 */
	public UndoDisconnectMedia(DataPoint inPoint, boolean inPhoto, boolean inAudio, String inFilename)
	{
		_point = inPoint;
		if (inPhoto) {
			_photo = inPoint.getPhoto();
		}
		if (inAudio) {
			_audio = inPoint.getAudio();
		}
		_filename = inFilename;
	}


	/**
	 * @return description of operation including filename
	 */
	public String getDescription()
	{
		return I18nManager.getText("undo.disconnect") + " " + _filename;
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
		}
		else if (_point != null && _audio != null)
		{
			_point.setAudio(_audio);
			_audio.setDataPoint(_point);
		}
		else {
			// throw exception if failed
			throw new UndoException(getDescription());
		}
		// clear selection
		inTrackInfo.getSelection().clearAll();
	}
}