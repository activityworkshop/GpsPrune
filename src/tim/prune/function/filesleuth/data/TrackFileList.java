package tim.prune.function.filesleuth.data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import tim.prune.function.filesleuth.TrackListListener;

public class TrackFileList
{
	/** List of track files including their contents */
	private final ArrayList<TrackFile> _tracks = new ArrayList<>();
	private final ArrayList<TrackListListener> _listeners = new ArrayList<>();
	private boolean _listComplete = false;


	public synchronized void clear()
	{
		_tracks.clear();
		_listComplete = false;
	}

	public void addListener(TrackListListener inListener) {
		_listeners.add(inListener);
	}

	public int getCurrentSize() {
		return _tracks.size();
	}

	public boolean isListComplete() {
		return _listComplete;
	}

	public void setListComplete() {
		_listComplete = true;
	}

	/**
	 * A file has been discovered, and should be added to the list
	 * @param inPath path of newly-discovered file
	 */
	public synchronized void foundFile(Path inPath)
	{
		final int index = _tracks.size();
		TrackFile track = new TrackFile(inPath.toFile());
		_tracks.add(track);
		track.setIndex(index);
		triggerListeners(index);
	}

	/** @return true if claim was successful, false if already claimed */
	public synchronized boolean claimTrackFileForScanning(TrackFile inFile)
	{
		if (inFile.getStatus() == TrackFileStatus.FOUND) {
			inFile.setStatus(TrackFileStatus.SCANNING);
			return true;
		}
		return false;
	}

	/** A file has been scanned, so its contents need to be updated */
	public synchronized void scanComplete(TrackFile inFile, TrackContents inContents)
	{
		inFile.setContents(inContents);
		inFile.setStatus(TrackFileStatus.COMPLETE);
		triggerListeners(inFile.getIndex());
	}

	private void triggerListeners(int inIndex)
	{
		for (TrackListListener listener : _listeners) {
			listener.reactToTrackListChange(inIndex);
		}
	}

	/** Make a shallow copy of the list which can be iterated over safely
	 * even though the main list is still being updated */
	public synchronized List<TrackFile> getCurrentContents() {
		return List.copyOf(_tracks);
	}
}
