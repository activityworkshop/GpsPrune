package tim.prune.function.filesleuth;

import tim.prune.function.filesleuth.data.TrackFile;
import tim.prune.function.filesleuth.data.TrackFileStatus;

public class SearchResult
{
	private final TrackFile _trackFile;
	private TrackFileStatus _statusWhenChecked;
	private boolean _match = false;
	private boolean _needsRecheck = true;
	private String _contents = "";
	// TODO: for distance filter, store distance from search point


	public SearchResult(TrackFile inTrackFile)
	{
		_trackFile = inTrackFile;
		_statusWhenChecked = inTrackFile.getStatus();
	}

	public TrackFile getTrackFile() {
		return _trackFile;
	}

	public String getFilename() {
		return _trackFile.getFile().getName();
	}

	public TrackFileStatus getFileStatusWhenChecked() {
		return _statusWhenChecked;
	}

	public void setContents(String inString) {
		_contents = (inString == null ? "" : inString.trim());
	}

	public String getContents() {
		return _contents;
	}

	public void setIsMatch(boolean isMatch) {
		_match = isMatch;
		_needsRecheck = false;
	}

	public boolean isMatch() {
		return _match;
	}

	public void setNeedsRecheck() {
		_needsRecheck = true;
	}

	public boolean needsRecheck() {
		return _needsRecheck;
	}
}
