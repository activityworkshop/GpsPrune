package tim.prune.data;

import java.io.File;

/**
 * Class to hold the source of the point data, including the original file
 * and file type, and references to each of the point objects
 */
public class SourceInfo
{
	/** File type of source file */
	public enum FILE_TYPE {TEXT, GPX, KML, NMEA, GPSBABEL, GPSIES};

	/** Source file */
	private File _sourceFile = null;
	/** Name of source */
	private String _sourceName = null;
	/** File type */
	private FILE_TYPE _fileType = null;

	/** Array of datapoints */
	private DataPoint[] _points = null;
	/** Number of points */
	private int _numPoints = 0;
	/** Array of point indices (if necessary) */
	private int[] _pointIndices = null;


	/**
	 * Constructor
	 * @param inFile source file
	 * @param inType type of file
	 */
	public SourceInfo(File inFile, FILE_TYPE inType)
	{
		_sourceFile = inFile;
		_sourceName = inFile.getName();
		_fileType = inType;
	}

	/**
	 * Constructor
	 * @param inName name of source (without file)
	 * @param inType type of file
	 */
	public SourceInfo(String inName, FILE_TYPE inType)
	{
		_sourceFile = null;
		_sourceName = inName;
		_fileType = inType;
	}

	/**
	 * @return source file
	 */
	public File getFile()
	{
		return _sourceFile;
	}

	/**
	 * @return source name
	 */
	public String getName()
	{
		return _sourceName;
	}

	/**
	 * @return file type of source
	 */
	public FILE_TYPE getFileType()
	{
		return _fileType;
	}

	/**
	 * @return number of points from this source
	 */
	public int getNumPoints()
	{
		return _numPoints;
	}

	/**
	 * Set the indices of the points selected out of a loaded track
	 * @param inSelectedFlags array of booleans showing whether each point in the original data was loaded or not
	 */
	public void setPointIndices(boolean[] inSelectedFlags)
	{
		_numPoints = inSelectedFlags.length;
		_pointIndices = new int[_numPoints];
		int p=0;
		for (int i=0; i<_numPoints; i++) {
			if (inSelectedFlags[i]) {_pointIndices[p++] = i;}
		}
		// Now the point indices array holds the index of each of the selected points
	}

	/**
	 * Take the points from the given track and store
	 * @param inTrack track object containing points
	 * @param inNumPoints number of points loaded
	 */
	public void populatePointObjects(Track inTrack, int inNumPoints)
	{
		if (_numPoints == 0) {_numPoints = inNumPoints;}
		if (inNumPoints > 0)
		{
			_points = new DataPoint[inNumPoints];
			int trackLen = inTrack.getNumPoints();
			System.arraycopy(inTrack.cloneContents(), trackLen-inNumPoints, _points, 0, inNumPoints);
			// Note data copied twice here but still more efficient than looping
		}
	}

	/**
	 * Look for the given point in the array
	 * @param inPoint point to look for
	 * @return index, or -1 if not found
	 */
	public int getIndex(DataPoint inPoint)
	{
		int idx = -1;
		for (int i=0; i<_points.length && (idx < 0); i++) {
			if (_points[i] == inPoint) {idx = i;}
		}
		if (_pointIndices == null) {return idx;} // All points loaded
		return _pointIndices[idx]; // use point index mapping
	}
}
