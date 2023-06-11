package tim.prune.data;

import java.util.ArrayList;

/**
 * Class to hold the information about the file(s)
 * from which the data was loaded from / saved to
 */
public class FileInfo
{
	/** List of sources */
	private final ArrayList<SourceInfo> _sources = new ArrayList<>();

	/**
	 * Add a data source to the list, if it's not already present
	 * @param inInfo info object to add
	 */
	public void addSource(SourceInfo inInfo)
	{
		if (inInfo != null) {
			for (SourceInfo info : _sources) {
				if (info == inInfo) {
					return;
				}
			}
			_sources.add(inInfo);
		}
	}

	/**
	 * @return the number of files loaded
	 */
	public int getNumFiles() {
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
	public SourceInfo getSource(int inIndex) {
		return _sources.get(inIndex);
	}

	/**
	 * @return the first found source, if any
	 */
	public SourceInfo getFirstSource()
	{
		if (getNumFiles() == 0) {
			return null;
		}
		return getSource(0);
	}

	/**
	 * @return the first found title, if any
	 */
	public String getFirstTitle()
	{
		for (SourceInfo info : _sources)
		{
			if (info != null)
			{
				String title = info.getFileTitle();
				if (title != null && !title.equals("")) {
					return title;
				}
			}
		}
		return null;
	}
}
