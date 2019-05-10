package tim.prune.data;

import java.util.Set;
import tim.prune.UpdateMessageBroker;

/**
 * Class to hold all track information, including data
 * and the selection information
 */
public class TrackInfo
{
	private Track _track = null;
	private Selection _selection = null;
	private FileInfo _fileInfo = null;
	private PhotoList _photoList = null;
	private AudioList _audioList = null;


	/**
	 * Constructor
	 * @param inTrack Track object
	 */
	public TrackInfo(Track inTrack)
	{
		_track = inTrack;
		_selection = new Selection(_track);
		_fileInfo = new FileInfo();
		_photoList = new PhotoList();
		_audioList = new AudioList();
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
	public FileInfo getFileInfo() {
		return _fileInfo;
	}

	/**
	 * Replace the file info with a previously made clone
	 * @param inInfo cloned file info
	 */
	public void setFileInfo(FileInfo inInfo) {
		_fileInfo = inInfo;
	}

	/**
	 * @return the PhotoList object
	 */
	public PhotoList getPhotoList() {
		return _photoList;
	}

	/**
	 * @return the AudioList object
	 */
	public AudioList getAudioList() {
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
		return _photoList.getPhoto(_selection.getCurrentPhotoIndex());
	}

	/**
	 * Get the currently selected audio clip, if any
	 * @return AudioClip if selected, otherwise null
	 */
	public AudioClip getCurrentAudio() {
		return _audioList.getAudio(_selection.getCurrentAudioIndex());
	}


	/**
	 * Add a Set of Photos
	 * @param inSet Set containing Photo objects
	 * @return array containing number of photos and number of points added
	 */
	public int[] addPhotos(Set<Photo> inSet)
	{
		// Firstly count number of points and photos to add
		int numPhotosToAdd = 0;
		int numPointsToAdd = 0;
		if (inSet != null && !inSet.isEmpty())
		{
			for (Photo photo : inSet)
			{
				if (photo != null && !_photoList.contains(photo))
				{
					numPhotosToAdd++;
					if (photo.getDataPoint() != null) {
						numPointsToAdd++;
					}
				}
			}
		}
		// If there are any photos to add, add them
		if (numPhotosToAdd > 0)
		{
			DataPoint[] dataPoints = new DataPoint[numPointsToAdd];
			int pointNum = 0;
			boolean hasAltitude = false;
			// Add each Photo in turn
			for (Photo photo : inSet)
			{
				if (photo != null && !_photoList.contains(photo))
				{
					// Add photo
					_photoList.addPhoto(photo);
					// Add point if there is one
					if (photo.getDataPoint() != null)
					{
						dataPoints[pointNum] = photo.getDataPoint();
						// Check if any points have altitudes
						hasAltitude |= (photo.getDataPoint().getAltitude() != null);
						pointNum++;
					}
				}
			}
			if (numPointsToAdd > 0)
			{
				// add points to track
				_track.appendPoints(dataPoints);
				// modify track field list
				_track.getFieldList().extendList(Field.LATITUDE);
				_track.getFieldList().extendList(Field.LONGITUDE);
				if (hasAltitude) {_track.getFieldList().extendList(Field.ALTITUDE);}
			}
		}
		int[] result = {numPhotosToAdd, numPointsToAdd};
		return result;
	}

	/**
	 * Add a Set of Audio objects
	 * @param inSet Set containing Audio objects
	 * @return number of audio objects added
	 */
	public int addAudios(Set<AudioClip> inSet)
	{
		int numAudiosAdded = 0;
		if (inSet != null && !inSet.isEmpty())
		{
			for (AudioClip audio : inSet)
			{
				if (audio != null && !_audioList.contains(audio))
				{
					// Add audio object
					_audioList.addAudio(audio);
					numAudiosAdded++;
					// audio objects never have points when they're loaded
				}
			}
		}
		return numAudiosAdded;
	}

	/**
	 * Delete the currently selected point
	 * @return true if point deleted
	 */
	public boolean deletePoint()
	{
		if (_track.deletePoint(_selection.getCurrentPointIndex()))
		{
			_selection.modifyPointDeleted();
			return true;
		}
		return false;
	}


	/**
	 * Delete the currently selected photo and optionally its point too
	 * @param inPointToo true to also delete associated point
	 * @return true if delete successful
	 */
	public boolean deleteCurrentPhoto(boolean inPointToo)
	{
		int photoIndex = _selection.getCurrentPhotoIndex();
		if (photoIndex >= 0)
		{
			Photo photo = _photoList.getPhoto(photoIndex);
			_photoList.deletePhoto(photoIndex);
			// has it got a point?
			if (photo.getDataPoint() != null)
			{
				if (inPointToo)
				{
					// delete point
					int pointIndex = _track.getPointIndex(photo.getDataPoint());
					_track.deletePoint(pointIndex);
				}
				else
				{
					// disconnect point from photo
					photo.getDataPoint().setPhoto(null);
					photo.setDataPoint(null);
				}
			}
			// update subscribers
			_selection.modifyPointDeleted();
			UpdateMessageBroker.informSubscribers();
		}
		return true;
	}

	/**
	 * Delete the currently selected audio item and optionally its point too
	 * @param inPointToo true to also delete associated point
	 * @return true if delete successful
	 */
	public boolean deleteCurrentAudio(boolean inPointToo)
	{
		int audioIndex = _selection.getCurrentAudioIndex();
		if (audioIndex >= 0)
		{
			AudioClip audio = _audioList.getAudio(audioIndex);
			_audioList.deleteAudio(audioIndex);
			// has it got a point?
			if (audio.getDataPoint() != null)
			{
				if (inPointToo)
				{
					// delete point
					int pointIndex = _track.getPointIndex(audio.getDataPoint());
					_track.deletePoint(pointIndex);
				}
				else
				{
					// disconnect point from audio
					audio.getDataPoint().setAudio(null);
					audio.setDataPoint(null);
				}
			}
			// update subscribers
			_selection.modifyPointDeleted();
			UpdateMessageBroker.informSubscribers();
		}
		return true;
	}


	/**
	 * Delete all the points which have been marked for deletion
	 * @param inSplitSegments true to split segments at deleted points
	 * @return number of points deleted
	 */
	public int deleteMarkedPoints(boolean inSplitSegments)
	{
		int numDeleted = _track.deleteMarkedPoints(inSplitSegments);
		if (numDeleted > 0)
		{
			_selection.clearAll();
			UpdateMessageBroker.informSubscribers();
		}
		return numDeleted;
	}


	/**
	 * Clone the selected range of data points
	 * @return shallow copy of DataPoint objects
	 */
	public DataPoint[] cloneSelectedRange()
	{
		return _track.cloneRange(_selection.getStart(), _selection.getEnd());
	}

	/**
	 * Merge the track segments within the given range
	 * @param inStart start index
	 * @param inEnd end index
	 * @return true if successful
	 */
	public boolean mergeTrackSegments(int inStart, int inEnd)
	{
		boolean firstTrackPoint = true;
		// Loop between start and end
		for (int i=inStart; i<=inEnd; i++)
		{
			DataPoint point = _track.getPoint(i);
			// Set all segments to false apart from first track point
			if (point != null && !point.isWaypoint()) {
				point.setSegmentStart(firstTrackPoint);
				firstTrackPoint = false;
			}
		}
		// Find following track point, if any
		DataPoint nextPoint = _track.getNextTrackPoint(inEnd+1);
		if (nextPoint != null) {
			nextPoint.setSegmentStart(true);
		}
		_selection.markInvalid();
		UpdateMessageBroker.informSubscribers();
		return true;
	}


	/**
	 * Average selected points to create a new one
	 * @return true if successful
	 */
	public boolean average()
	{
		boolean success = _track.average(_selection.getStart(), _selection.getEnd());
		if (success) {
			selectPoint(_selection.getEnd()+1);
		}
		return success;
	}


	/**
	 * Select the given DataPoint
	 * @param inPoint DataPoint object to select
	 */
	public void selectPoint(DataPoint inPoint)
	{
		selectPoint(_track.getPointIndex(inPoint));
	}

	/**
	 * Increment the selected point index by the given increment
	 * @param inPointIncrement +1 for next point, -1 for previous etc
	 */
	public void incrementPointIndex(int inPointIncrement)
	{
		int index = _selection.getCurrentPointIndex() + inPointIncrement;
		if (index < 0)
			index = 0;
		else if (index >= _track.getNumPoints())
			index = _track.getNumPoints()-1;
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
			photoIndex = _photoList.getPhotoIndex(selectedPoint.getPhoto());
		}
		else if (photoIndex < 0 || _photoList.getPhoto(photoIndex).isConnected()) {
			// selected point hasn't got a photo - deselect photo if necessary
			photoIndex = -1;
		}
		// Check if point has an audio item or not
		int audioIndex = _selection.getCurrentAudioIndex();
		boolean pointHasAudio = inPointIndex >= 0 && selectedPoint.getAudio() != null;
		if (pointHasAudio) {
			audioIndex = _audioList.getAudioIndex(selectedPoint.getAudio());
		}
		else if (audioIndex < 0 || _audioList.getAudio(audioIndex).isConnected()) {
			// deselect current audio clip
			audioIndex = -1;
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
		// Find Photo object
		Photo photo = _photoList.getPhoto(inPhotoIndex);
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
		else {
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
			audioIndex = _audioList.getAudioIndex(selectedPoint.getAudio());
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
		AudioClip audio = _audioList.getAudio(inAudioIndex);
		int pointIndex = _selection.getCurrentPointIndex();
		DataPoint currPoint = getCurrentPoint();
		if (audio != null)
		{
			// Find point object and its index
			if (audio.isConnected()) {
				pointIndex = _track.getPointIndex(audio.getDataPoint());
			}
			else {
				// Check whether to deselect current point or not if audio not correlated
				if (pointIndex >= 0 && _track.getPoint(pointIndex).getAudio() != null) {
					pointIndex = -1;
				}
			}
		}
		else {
			// check if current point has audio or not
			if (currPoint != null && currPoint.getAudio() != null) {
				pointIndex = -1;
			}
		}
		// Has the new point got a photo?
		DataPoint selectedPoint = _track.getPoint(pointIndex);
		int photoIndex = _selection.getCurrentPhotoIndex();
		if (selectedPoint != null && selectedPoint.getPhoto() != null) {
			// New point has a photo, so select it
			photoIndex = _photoList.getPhotoIndex(selectedPoint.getPhoto());
		}
		else if (currPoint != null && selectedPoint != currPoint && currPoint.getPhoto() != null) {
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
}
