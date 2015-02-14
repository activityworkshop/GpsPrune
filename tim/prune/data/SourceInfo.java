package tim.prune.data;

import java.io.File;

/**
 * Class to hold the source of the point data,
 * including original file and file type, and
 * also file offsets for source copying
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
		return _points.length;
	}

	/**
	 * Take the points from the given track and store
	 * @param inTrack track object containing points
	 * @param inNumPoints number of points loaded
	 */
	public void populatePointObjects(Track inTrack, int inNumPoints)
	{
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
		return idx;
	}
}
