package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.AudioClip;
import tim.prune.data.DataPoint;
import tim.prune.data.TrackInfo;

/**
 * Operation to undo a delete of a single audio item, either with or without point
 */
public class UndoDeleteAudio extends UndoDeleteOperation
{
	private int _audioIndex = -1;
	private AudioClip _audio = null;
	private int _pointIndex = -1;
	private DataPoint _point = null;


	/**
	 * Constructor
	 * @param inAudio audio item
	 * @param inAudioIndex index number of audio within list
	 * @param inPoint data point
	 * @param inPointIndex index number of point within track
	 */
	public UndoDeleteAudio(AudioClip inAudio, int inAudioIndex, DataPoint inPoint, int inPointIndex)
	{
		_audio = inAudio;
		_audioIndex = inAudioIndex;
		_point = inPoint;
		_pointIndex = inPointIndex;
	}


	/**
	 * @return description of operation including filename
	 */
	public String getDescription() {
		return I18nManager.getText("undo.removeaudio") + " " + _audio.getName();
	}


	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		// restore audio
		inTrackInfo.getAudioList().addAudio(_audio, _audioIndex);
		// if there's a point to restore, restore it
		if (_point != null)
		{
			if (!inTrackInfo.getTrack().insertPoint(_point, _pointIndex)) {
				throw new UndoException(getDescription());
			}
			// Change the current point/range selection if required
			modifySelection(inTrackInfo, _pointIndex, _pointIndex);
		}
		else
		{
			// update needed if not already triggered by track update
			UpdateMessageBroker.informSubscribers();
		}
		// Ensure that audio is associated with point and vice versa
		_audio.setDataPoint(_point);
		if (_point != null) {
			_point.setAudio(_audio);
		}
	}
}
