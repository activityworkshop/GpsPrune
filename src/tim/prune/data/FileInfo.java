package tim.prune.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
		if (inInfo != null)
		{
			for (SourceInfo info : _sources)
			{
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

	/** @return a list of all the unique titles in the order they appear */
	public List<String> getAllTitles() {
		return getAllValues(i -> i.getFileTitle());
	}

	/** @return a list of all the unique descriptions in the order they appear */
	public List<String> getAllDescriptions() {
		return getAllValues(i -> i.getFileDescription());
	}

	/** Functional interface for getting either title or description out of the SourceInfo */
	private interface ValueTaker {
		String takeValue(SourceInfo inInfo);
	}

	/** @return a list of all the unique values from the SourceInfo in the order they appear */
	public List<String> getAllValues(ValueTaker inTaker)
	{
		ArrayList<String> values = new ArrayList<>();
		HashSet<String> valueSet = new HashSet<>();
		SourceInfo previousSource = null;
		for (SourceInfo source : _sources)
		{
			if (source == previousSource) {
				continue;
			}
			previousSource = source;
			final String value = inTaker.takeValue(source);
			if (value != null && !value.isEmpty() && !valueSet.contains(value))
			{
				values.add(value);
				valueSet.add(value);
			}
		}
		return values;
	}
}
