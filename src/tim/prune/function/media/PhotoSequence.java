package tim.prune.function.media;

import java.util.ArrayList;
import java.util.HashMap;

import tim.prune.App;
import tim.prune.DataStatus;
import tim.prune.data.DataPoint;
import tim.prune.data.MediaList;
import tim.prune.data.Photo;
import tim.prune.data.Track;

/**
 * Manages the sequence of Photos in the current track,
 * in order to provide references for next and previous photos
 */
public class PhotoSequence
{
	/** The App object from which to get the points and photos */
	private final App _app;
	/** The list of photo indexes */
	private final ArrayList<Integer> _photoIndexes = new ArrayList<>();
	/** The previously obtained data status */
	private DataStatus _lastDataStatus = null;
	/** The names of the photos according to index */
	private final HashMap<Integer, String> _photoNames = new HashMap<>();
	private int _previousPhotoIndex = -1;
	private int _nextPhotoIndex = -1;
	/** only required for testing */
	int _numRecalculations = 0;


	/** Constructor */
	public PhotoSequence(App inApp) {
		_app = inApp;
	}

	/** Called once to recalculate everything if required */
	public void recalculate()
	{
		DataStatus currentStatus = _app.getCurrentDataStatus();
		if (currentStatus.hasDataChanged(_lastDataStatus)) {
			rebuildLists();
		}
		_lastDataStatus = currentStatus;
		// find out currently selected photo index (if any), set previous and next
		findIndexes();
	}

	private synchronized void rebuildLists()
	{
		_photoIndexes.clear();
		_photoNames.clear();
		final Track track = _app.getTrackInfo().getTrack();
		final int numPoints = track.getNumPoints();
		final MediaList<Photo> photoList = _app.getTrackInfo().getPhotoList();
		// Loop over the photos which are connected to points
		for (int i=0; i<numPoints; i++)
		{
			final DataPoint point = track.getPoint(i);
			final Photo photo = point.getPhoto();
			if (photo != null)
			{
				final int photoIndex = photoList.getIndexOf(photo);
				_photoIndexes.add(photoIndex);
			}
		}
		// Now collect the indexes which aren't connected
		final int numPhotos = photoList.getCount();
		for (int p=0; p<numPhotos; p++)
		{
			_photoNames.put(p, photoList.get(p).getName());
			if (!_photoIndexes.contains(p)) {
				_photoIndexes.add(p);
			}
		}
		_numRecalculations++;
	}

	private synchronized void findIndexes()
	{
		final int currentPhotoIndex = _app.getTrackInfo().getSelection().getCurrentPhotoIndex();
		_previousPhotoIndex = -1;
		_nextPhotoIndex = -1;
		int lastIndexRead = -1;
		boolean foundCurrentIndex = false;
		if (currentPhotoIndex != -1)
		{
			for (int p : _photoIndexes)
			{
				if (p == currentPhotoIndex)
				{
					_previousPhotoIndex = lastIndexRead;
					foundCurrentIndex = true;
				}
				else if (foundCurrentIndex && _nextPhotoIndex == -1)
				{
					_nextPhotoIndex = p;
					break;
				}
				lastIndexRead = p;
			}
		}
	}

	public boolean hasPrevious() {
		return _previousPhotoIndex >= 0;
	}

	public int getPreviousIndex() {
		return _previousPhotoIndex;
	}

	public String getPreviousName() {
		return hasPrevious() ? _photoNames.get(getPreviousIndex()) : null;
	}

	public boolean hasNext() {
		return _nextPhotoIndex >= 0;
	}

	public int getNextIndex() {
		return _nextPhotoIndex;
	}

	public String getNextName() {
		return hasNext() ? _photoNames.get(getNextIndex()) : null;
	}
}
