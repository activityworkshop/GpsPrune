package tim.prune.data;

import java.io.File;

/**
 * Class to represent a photo and link to DataPoint
 */
public class Photo
{
	/** File where photo is stored */
	private File _file = null;
	/** Associated DataPoint if correlated */
	private DataPoint _dataPoint = null;


	/**
	 * Constructor
	 * @param inFile File object for photo
	 */
	public Photo(File inFile)
	{
		_file = inFile;
		// TODO: Cache photo file contents to allow thumbnail preview
	}


	/**
	 * @return File object where photo resides
	 */
	public File getFile()
	{
		return _file;
	}


	/**
	 * Set the data point associated with the photo
	 * @param inPoint DataPoint with coordinates etc
	 */
	public void setDataPoint(DataPoint inPoint)
	{
		_dataPoint = inPoint;
	}

	/**
	 * @return the DataPoint object
	 */
	public DataPoint getDataPoint()
	{
		return _dataPoint;
	}

	/**
	 * Check if a Photo object refers to the same File as another
	 * @param inOther other Photo object
	 * @return true if the Files are the same
	 */
	public boolean equals(Photo inOther)
	{
		return (inOther != null && inOther.getFile() != null && getFile() != null
			&& inOther.getFile().equals(getFile()));
	}
}
