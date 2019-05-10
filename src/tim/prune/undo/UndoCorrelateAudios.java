package tim.prune.undo;

import tim.prune.I18nManager;
import tim.prune.data.AudioClip;
import tim.prune.data.DataPoint;
import tim.prune.data.TrackInfo;

/**
 * Operation to undo an auto-correlation of audios with points
 * (very similar to UndoCorrelatePhotos)
 */
public class UndoCorrelateAudios implements UndoOperation
{
	private DataPoint[] _contents = null;
	private DataPoint[] _audioPoints = null;
	private int _numCorrelated = -1;


	/**
	 * Constructor
	 * @param inTrackInfo track information
	 */
	public UndoCorrelateAudios(TrackInfo inTrackInfo)
	{
		// Copy track contents
		_contents = inTrackInfo.getTrack().cloneContents();
		// Copy points associated with audios before correlation
		int numAudios = inTrackInfo.getAudioList().getNumAudios();
		_audioPoints = new DataPoint[numAudios];
		for (int i=0; i<numAudios; i++) {
			_audioPoints[i] = inTrackInfo.getAudioList().getAudio(i).getDataPoint();
		}
	}

	/**
	 * @param inNumCorrelated number of audios correlated
	 */
	public void setNumAudiosCorrelated(int inNumCorrelated)
	{
		_numCorrelated = inNumCorrelated;
	}

	/**
	 * @return description of operation including parameters
	 */
	public String getDescription()
	{
		return I18nManager.getText("undo.correlateaudios") + " (" + _numCorrelated + ")";
	}


	/**
	 * Perform the undo operation on the given Track
	 * @param inTrackInfo TrackInfo object on which to perform the operation
	 */
	public void performUndo(TrackInfo inTrackInfo) throws UndoException
	{
		// restore track to previous values
		inTrackInfo.getTrack().replaceContents(_contents);
		// restore audio association
		for (int i=0; i<_audioPoints.length; i++)
		{
			AudioClip audio = inTrackInfo.getAudioList().getAudio(i);
			// Only need to look at connected ones, since correlation wouldn't disconnect
			if (audio.getCurrentStatus() == AudioClip.Status.CONNECTED)
			{
				DataPoint prevPoint = _audioPoints[i];
				DataPoint currPoint = audio.getDataPoint();
				audio.setDataPoint(prevPoint);
				if (currPoint != null) {
					currPoint.setAudio(null); // disconnect
				}
				if (prevPoint != null) {
					prevPoint.setAudio(audio); // reconnect to prev point
				}
			}
		}
		// clear selection
		inTrackInfo.getSelection().clearAll();
	}
}
