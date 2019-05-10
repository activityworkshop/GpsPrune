package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.AudioClip;
import tim.prune.data.DataPoint;
import tim.prune.data.Photo;
import tim.prune.data.TrackInfo;

/**
 * Operation to undo the connection of a photo and/or audio to a point
 */
public class UndoConnectMedia implements UndoOperation
{
	private DataPoint _point = null;
	private String _photoFilename = null;
	private String _audioFilename = null;


	/**
	 * Constructor
	 * @param inPoint data point
	 * @param inPhotoFilename filename of photo, or null if photo not connected
	 * @param inAudioFilename filename of audio, or null of audio not connected
	 */
	public UndoConnectMedia(DataPoint inPoint, String inPhotoFilename, String inAudioFilename)
	{
		_point = inPoint;
		_photoFilename = inPhotoFilename;
		_audioFilename = inAudioFilename;
	}


	/**
	 * @return description of operation including photo and/or audio filename(s)
	 */
	public String getDescription()
	{
		String desc = I18nManager.getText("undo.connect") + " " + (_photoFilename==null?"":_photoFilename)
		 + (_photoFilename!=null && _audioFilename!=null?", ":"")
		 + (_audioFilename==null?"":_audioFilename);
		return desc;
	}


	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		if (_photoFilename != null)
		{
			// Disconnect photo
			Photo photo = _point.getPhoto();
			if (photo != null)
			{
				_point.setPhoto(null);
				photo.setDataPoint(null);
			}
		}
		if (_audioFilename != null)
		{
			// Disconnect audio
			AudioClip audio = _point.getAudio();
			if (audio != null)
			{
				_point.setAudio(null);
				audio.setDataPoint(null);
			}
		}
		// inform subscribers
		UpdateMessageBroker.informSubscribers();
	}
}