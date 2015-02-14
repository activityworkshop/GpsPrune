package tim.prune.data;

/**
 * Class to hold the information about the file(s)
 * from which the data was loaded from / saved to
 */
public class FileInfo
{
	private String _filename = null;
	private int _numFiles = 0;


	/**
	 * Set the file information to the specified file
	 * @param inFilename filename of file
	 */
	public void setFile(String inFilename)
	{
		_filename = inFilename;
		_numFiles = 1;
	}


	/**
	 * Add a file to the data
	 */
	public void addFile()
	{
		_numFiles++;
	}


	/**
	 * Undo a load file
	 */
	public void removeFile()
	{
		_numFiles--;
	}


	/**
	 * @return the number of files loaded
	 */
	public int getNumFiles()
	{
		return _numFiles;
	}


	/**
	 * @return The filename, if a single file
	 */
	public String getFilename()
	{
		if (_numFiles == 1 && _filename != null)
			return _filename;
		return "";
	}
}
