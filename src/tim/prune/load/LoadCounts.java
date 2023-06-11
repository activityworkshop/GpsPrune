package tim.prune.load;

/**
 * Placeholder for ints counting the numbers of things found / loaded
 */
public class LoadCounts
{
	private int _numFiles = 0;
	private int _numPhotos = 0;
	private int _numPoints = 0;

	public void foundFile() {
		_numFiles++;
	}

	public void foundPhoto() {
		_numPhotos++;
	}

	public void foundPhotoWithCoords() {
		_numPoints++;
	}

	public int getNumFiles() {
		return _numFiles;
	}

	public int getNumPhotos() {
		return _numPhotos;
	}

	public int getNumPoints() {
		return _numPoints;
	}
}
