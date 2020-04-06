package tim.prune.data;

import java.util.ArrayList;

/**
 * Class to hold the information about the file(s)
 * from which the data was loaded from / saved to
 */
public class FileInfo
{
	/** List of sources */
	private ArrayList<SourceInfo> _sources = new ArrayList<SourceInfo>();


	/**
	 * Empty constructor
	 */
	public FileInfo()
	{}

	/**
	 * Private constructor for creating clone
	 * @param inList list of sources
	 */
	private FileInfo(ArrayList<SourceInfo> inList)
	{
		_sources = inList;
	}

	/**
	 * Add a data source to the list
	 * @param inInfo info object to add
	 */
	public void addSource(SourceInfo inInfo)
	{
		_sources.add(inInfo);
	}

	/**
	 * Replace the list of data sources with the given source
	 * @param inInfo new source
	 */
	public void replaceSource(SourceInfo inInfo)
	{
		_sources.clear();
		addSource(inInfo);
	}

	/**
	 * remove the last source added
	 */
	public void removeSource()
	{
		if (!_sources.isEmpty()) {
			_sources.remove(_sources.size()-1);
		}
	}

	/**
	 * @return the number of files loaded
	 */
	public int getNumFiles()
	{
		return _sources.size();
	}


	/**
	 * @return The source name, if a single file
	 */
	public String getFilename()
	{
		if (getNumFiles() == 1) {
			return _sources.get(0).getName();
		}
		return "";
	}

	/**
	 * @param inIndex index number, starting from zero
	 * @return source info object
	 */
	public SourceInfo getSource(int inIndex)
	{
		return _sources.get(inIndex);
	}

	/**
	 * Get the SourceInfo object (if any) for the given point
	 * @param inPoint point object
	 * @return SourceInfo object if there is one, otherwise null
	 */
	public SourceInfo getSourceForPoint(DataPoint inPoint)
	{
		for (SourceInfo source : _sources)
		{
			if (source.getIndex(inPoint) >= 0) {
				return source;
			}
		}
		return null;
	}

	/**
	 * @return the info about the last file loaded, if any
	 */
	public SourceInfo getLastFileInfo()
	{
		if (getNumFiles() == 0)
		{
			return null;
		}
		return getSource(getNumFiles()-1);
	}

	/**
	 * @return the most recent file title loaded, if any
	 */
	public String getLastFileTitle()
	{
		final int numFiles = getNumFiles();
		if (numFiles == 0)
		{
			return null;
		}
		for (int i=(numFiles-1); i>=0; i--)
		{
			SourceInfo info = getSource(i);
			if (info != null)
			{
				String title = info.getFileTitle();
				if (title != null && !title.equals(""))
				{
					return title;
				}
			}
		}
		return null;
	}

	/**
	 * Clone contents of file info
	 */
	@SuppressWarnings("unchecked")
	public FileInfo clone()
	{
		// copy source list
		ArrayList<SourceInfo> copy = (ArrayList<SourceInfo>) _sources.clone();
		return new FileInfo(copy);
	}
}
