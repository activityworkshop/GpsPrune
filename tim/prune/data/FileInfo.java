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
		_sources.remove(_sources.size()-1);
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
		if (getNumFiles() == 1)
			return _sources.get(0).getName();
		return "";
	}

	/**
	 * @param inIndex index number
	 * @return source info object
	 */
	public SourceInfo getSource(int inIndex) {
		return _sources.get(inIndex);
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
