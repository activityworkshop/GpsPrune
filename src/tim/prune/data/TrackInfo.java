package tim.prune.data;

import java.util.List;

/**
 * Class to hold all track information, including data
 * and the selection information
 */
public class TrackInfo
{
	private final Track _track;
	private final Selection _selection;
	private FileInfo _fileInfo = null;
	private final MediaList<Photo> _photoList = new MediaList<>();
	private final MediaList<AudioClip> _audioList = new MediaList<>();
	private MarkingData _markingData = null;


	/**
	 * Constructor
	 * @param inTrack Track object
	 */
	public TrackInfo(Track inTrack)
	{
		_track = inTrack;
		_selection = new Selection(_track);
	}


	/**
	 * @return the Track object
	 */
	public Track getTrack() {
		return _track;
	}


	/**
	 * @return the Selection object
	 */
	public Selection getSelection() {
		return _selection;
	}


	/**
	 * @return the FileInfo object
	 */
	public FileInfo getFileInfo()
	{
		if (_fileInfo == null)
		{
			_fileInfo = new FileInfo();
			for (int i = 0; i < _track.getNumPoints(); i++) {
				_fileInfo.addSource(_track.getPoint(i).getSourceInfo());
			}
		}
		return _fileInfo;
	}

	/** Delete the current file information so that it will be regenerated */
	public void clearFileInfo() {
		_fileInfo = null;
	}

	/**
	 * @return the PhotoList object
	 */
	public MediaList<Photo> getPhotoList() {
		return _photoList;
	}

	/**
	 * @return the AudioList object
	 */
	public MediaList<AudioClip> getAudioList() {
		return _audioList;
	}

	/**
	 * Get the currently selected point, if any
	 * @return DataPoint if single point selected, otherwise null
	 */
	public DataPoint getCurrentPoint() {
		return _track.getPoint(_selection.getCurrentPointIndex());
	}

	/**
	 * Get the currently selected photo, if any
	 * @return Photo if selected, otherwise null
	 */
	public Photo getCurrentPhoto() {
		return _photoList.get(_selection.getCurrentPhotoIndex());
	}

	/**
	 * Get the currently selected audio clip, if any
	 * @return AudioClip if selected, otherwise null
	 */
	public AudioClip getCurrentAudio() {
		return _audioList.get(_selection.getCurrentAudioIndex());
	}

	/**
	 * Delete the specified point and modify the selection accordingly
	 * @param inIndex index of point to delete
	 * @return true if point deleted
	 */
	public boolean deletePoint(int inIndex)
	{
		if (_track.deletePoint(inIndex))
		{
			_selection.modifyPointDeleted(inIndex);
			return true;
		}
		return false;
	}

	/**
	 * Select the given DataPoint
	 * @param inPoint DataPoint object to select
	 */
	public void selectPoint(DataPoint inPoint) {
		selectPoint(_track.getPointIndex(inPoint));
	}

	/**
	 * Increment the selected point index by the given increment
	 * @param inPointIncrement +1 for next point, -1 for previous etc
	 */
	public void incrementPointIndex(int inPointIncrement)
	{
		int index = _selection.getCurrentPointIndex() + inPointIncrement;
		if (index < 0) {
			index = 0;
		}
		else if (index >= _track.getNumPoints()) {
			index = _track.getNumPoints()-1;
		}
		selectPoint(index);
	}

	/**
	 * Select the data point with the given index
	 * @param inPointIndex index of DataPoint to select, or -1 for none
	 */
	public void selectPoint(int inPointIndex)
	{
		if (_selection.getCurrentPointIndex() == inPointIndex || inPointIndex >= _track.getNumPoints()) {
			return;
		}
		DataPoint selectedPoint = _track.getPoint(inPointIndex);
		// get the index of the current photo
		int photoIndex = _selection.getCurrentPhotoIndex();
		// Check if point has photo or not
		boolean pointHasPhoto = inPointIndex >= 0 && selectedPoint.getPhoto() != null;
		if (pointHasPhoto) {
			photoIndex = _photoList.getIndexOf(selectedPoint.getPhoto());
		}
		else
		{
			Photo photo = _photoList.get(photoIndex);
			if (photo == null || photo.isConnected()) {
				// selected point hasn't got a photo - deselect photo if necessary
				photoIndex = -1;
			}
		}
		// Check if point has an audio item or not
		int audioIndex = _selection.getCurrentAudioIndex();
		boolean pointHasAudio = inPointIndex >= 0 && selectedPoint.getAudio() != null;
		if (pointHasAudio) {
			audioIndex = _audioList.getIndexOf(selectedPoint.getAudio());
		}
		else {
			AudioClip audio = _audioList.get(audioIndex);
			if (audio == null || audio.isConnected()) {
				// deselect current audio clip
				audioIndex = -1;
			}
		}
		// give to selection
		_selection.selectPointPhotoAudio(inPointIndex, photoIndex, audioIndex);
	}

	/**
	 * Select the given Photo and its point if any
	 * @param inPhotoIndex index of photo to select
	 */
	public void selectPhoto(int inPhotoIndex)
	{
		if (_selection.getCurrentPhotoIndex() == inPhotoIndex) {
			return;
		}
		// Photo is primary selection here, not as a result of a point selection
		// Therefore the photo selection takes priority, deselecting point if necessary
		Photo photo = _photoList.get(inPhotoIndex);
		int pointIndex = _selection.getCurrentPointIndex();
		DataPoint currPoint = getCurrentPoint();
		if (photo != null)
		{
			// Has the photo got a point?
			if (photo.isConnected()) {
				pointIndex = _track.getPointIndex(photo.getDataPoint());
			}
			else {
				// Check whether to deselect current point or not if photo not correlated
				if (pointIndex >= 0 && _track.getPoint(pointIndex).getPhoto() != null) {
					pointIndex = -1;
				}
			}
		}
		else
		{
			// no photo, but maybe need to deselect point
			if (currPoint != null && currPoint.getPhoto() != null) {
				pointIndex = -1;
			}
		}
		// Has the new point got an audio clip?
		DataPoint selectedPoint = _track.getPoint(pointIndex);
		int audioIndex = _selection.getCurrentAudioIndex();
		if (selectedPoint != null && selectedPoint.getAudio() != null)
		{
			// New point has an audio, so select it
			audioIndex = _audioList.getIndexOf(selectedPoint.getAudio());
		}
		else if (currPoint != null && selectedPoint != currPoint && currPoint.getAudio() != null)
		{
			// Old point had an audio, so deselect it
			audioIndex = -1;
		}
		// give to selection object
		_selection.selectPointPhotoAudio(pointIndex, inPhotoIndex, audioIndex);
	}

	/**
	 * Select the given audio object and its point if any
	 * @param inAudioIndex index of audio item to select
	 */
	public void selectAudio(int inAudioIndex)
	{
		if (_selection.getCurrentAudioIndex() == inAudioIndex) {
			return;
		}
		// Audio selection takes priority, deselecting point if necessary
		AudioClip audio = _audioList.get(inAudioIndex);
		int pointIndex = _selection.getCurrentPointIndex();
		DataPoint currPoint = getCurrentPoint();
		if (audio != null)
		{
			// Find point object and its index
			if (audio.isConnected()) {
				pointIndex = _track.getPointIndex(audio.getDataPoint());
			}
			else
			{
				// Check whether to deselect current point or not if audio not correlated
				if (pointIndex >= 0 && _track.getPoint(pointIndex).getAudio() != null) {
					pointIndex = -1;
				}
			}
		}
		else
		{
			// check if current point has audio or not
			if (currPoint != null && currPoint.getAudio() != null) {
				pointIndex = -1;
			}
		}
		// Has the new point got a photo?
		DataPoint selectedPoint = _track.getPoint(pointIndex);
		int photoIndex = _selection.getCurrentPhotoIndex();
		if (selectedPoint != null && selectedPoint.getPhoto() != null)
		{
			// New point has a photo, so select it
			photoIndex = _photoList.getIndexOf(selectedPoint.getPhoto());
		}
		else if (currPoint != null && selectedPoint != currPoint && currPoint.getPhoto() != null)
		{
			// Old point had a photo, so deselect it
			photoIndex = -1;
		}
		// give to selection object
		_selection.selectPointPhotoAudio(pointIndex, photoIndex, inAudioIndex);
	}


	/**
	 * Extend the current selection to end at the given point, eg by shift-clicking
	 * @param inPointNum index of end point
	 */
	public void extendSelection(int inPointNum)
	{
		// See whether to start selection from current range start or current point
		int rangeStart = _selection.getStart();
		if (rangeStart < 0 || _selection.getCurrentPointIndex() != _selection.getEnd()) {
			rangeStart = _selection.getCurrentPointIndex();
		}
		selectPoint(inPointNum);
		if (rangeStart < inPointNum) {
			_selection.selectRange(rangeStart, inPointNum);
		}
	}


	public boolean appendRange(List<DataPoint> inPoints)
	{
		final int currentNumPoints = getTrack().getNumPoints();
		if (getTrack().appendRange(inPoints))
		{
			// Select the first point added
			selectPoint(currentNumPoints);
			return true;
		}
		return false;
	}

	public boolean hasPointsMarkedForDeletion() {
		return _markingData != null && _markingData.hasMarkedPoints();
	}

	public boolean isPointMarkedForDeletion(int inIndex) {
		return _markingData != null && _markingData.isPointMarkedForDeletion(inIndex);
	}

	public boolean isPointMarkedForSegmentBreak(int inIndex) {
		return _markingData != null && _markingData.isPointMarkedForSegmentBreak(inIndex);
	}

	public void markPointForDeletion(int inIndex) {
		markPointForDeletion(inIndex, true);
	}

	public void markPointForDeletion(int inIndex, boolean inDelete) {
		markPointForDeletion(inIndex, inDelete, false);
	}

	public void markPointForDeletion(int inIndex, boolean inDelete, boolean inSegmentBreak)
	{
		if (_markingData == null) {
			_markingData = new MarkingData(getTrack());
		}
		_markingData.markPointForDeletion(inIndex, inDelete, inSegmentBreak);
	}

	public void clearAllMarkers()
	{
		if (_markingData != null) {
			_markingData.clear();
		}
	}
}
