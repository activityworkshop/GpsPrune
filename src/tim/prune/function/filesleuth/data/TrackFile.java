package tim.prune.function.filesleuth.data;

import java.io.File;

import tim.prune.function.filesleuth.SearchResult;

/** Represents a single track file which may or may not have been read yet */
public class TrackFile
{
	private final File _file;
	private final String _pathString;
	private TrackFileStatus _status;
	private TrackContents _contents = null;
	private int _index = 0;


	public TrackFile(File inFile)
	{
		_file = inFile;
		_pathString = inFile.getAbsolutePath().toLowerCase().trim();
		_status = TrackFileStatus.FOUND;
	}

	public File getFile() {
		return _file;
	}

	public TrackFileStatus getStatus() {
		return _status;
	}

	public void setStatus(TrackFileStatus inStatus) {
		_status = inStatus;
	}

	public void setContents(TrackContents inContents) {
		_contents = inContents;
	}

	public boolean hasContents() {
		return _contents != null;
	}

	public String getNameOrDescription() {
		return hasContents() ? _contents.getNameOrDescription() : "";
	}

	public boolean matchesFilename(String inFilter)
	{
		if (inFilter == null || inFilter.equals("")) {
			return true;
		}
		return _pathString.contains(inFilter);
	}

	public boolean matchesStringFilter(String inFilter, SearchResult inResult)
	{
		inResult.setContents(_contents == null ? "" : _contents.getNameOrDescription());
		if (inFilter == null || inFilter.equals("")) {
			return true;
		}
		if (_contents != null && _contents.getNumStrings() > 0)
		{
			for (String s : _contents.getStrings())
			{
				if (s.contains(inFilter))
				{
					inResult.setIsMatch(true);
					inResult.setContents(s);
					return true;
				}
			}
		}
		return false;
	}

	public boolean matchesDateFilter(DateRange inRange) {
		return _contents == null || _contents.matchesDateFilter(inRange);
	}

	public void setIndex(int inIndex) {
		_index = inIndex;
	}

	public int getIndex() {
		return _index;
	}

	public boolean matchesLocationFilter(LocationFilter inFilter) {
		return _contents == null || _contents.matchesLocationFilter(inFilter);
	}
}
