package tim.prune.data;

import java.io.File;

/**
 * Simple class to represent an entry in the recently-used files list
 */
public class RecentFile
{
	private boolean _regularLoad = true; // false for load via gpsbabel
	private File _file = null;

	/**
	 * Constructor
	 * @param inFile file
	 * @param inRegular true for regular load, false for gpsbabel load
	 */
	public RecentFile(File inFile, boolean inRegular)
	{
		_file = inFile;
		_regularLoad = inRegular;
	}

	/**
	 * Constructor
	 * @param inDesc String from config
	 */
	public RecentFile(String inDesc)
	{
		if (inDesc != null && inDesc.length() > 3)
		{
			_regularLoad = (inDesc.charAt(0) != 'g');
			_file = new File(inDesc.substring(1));
		}
	}

	/**
	 * @return file object
	 */
	public File getFile() {
		return _file;
	}

	/**
	 * @return true for regular load, false for gpsbabel load
	 */
	public boolean isRegularLoad() {
		return _regularLoad;
	}

	/**
	 * @return true if file (still) exists
	 */
	public boolean isValid() {
		return _file != null && _file.exists() && _file.isFile();
	}

	/**
	 * @return string to save in config
	 */
	public String getConfigString()
	{
		if (!isValid()) return "";
		return (_regularLoad?"r":"g") + _file.getAbsolutePath();
	}

	/**
	 * Check for equality
	 * @param inOther other RecentFile object
	 * @return true if they both refer to the same file
	 */
	public boolean isSameFile(RecentFile inOther)
	{
		return inOther != null && isValid() && inOther.isValid()
			&& (_file.equals(inOther._file) || _file.getAbsolutePath().equals(inOther._file.getAbsolutePath()));
		// Note that the file.equals should be sufficient but sometimes it returns false even if the absolute paths are identical
	}
}
