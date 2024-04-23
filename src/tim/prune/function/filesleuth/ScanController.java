package tim.prune.function.filesleuth;

import java.util.*;

import tim.prune.function.filesleuth.data.TrackContents;
import tim.prune.function.filesleuth.data.TrackFile;
import tim.prune.function.filesleuth.data.TrackFileList;
import tim.prune.function.filesleuth.data.TrackFileStatus;

/** Responsible for coordinating the various FileScanners working in parallel */
public class ScanController implements TrackListListener, WorkerCoordinator
{
	private final TrackFileList _trackList;
	private final TimeZone _timezone;
	private int _numCurrentWorkers = 0;
	private final Deque<AbstractFileScanner> _waitingScanners = new LinkedList<>();
	private ScannerCreator _scannerCreator = FileScanner::new;

	private static final int MAX_PARALLEL_SCANS = 10;

	/** Constructor */
	public ScanController(TrackFileList inList, TimeZone inTimezone)
	{
		_trackList = inList;
		_trackList.addListener(this);
		_timezone = inTimezone;
	}

	@Override
	public void reactToTrackListChange(int inIndex)
	{
		List<TrackFile> tracks = _trackList.getCurrentContents();
		for (int i=inIndex; i<tracks.size(); i++)
		{
			TrackFile track = tracks.get(i);
			if (track.getStatus() == TrackFileStatus.FOUND
					&& _trackList.claimTrackFileForScanning(track))
			{
				_waitingScanners.add(_scannerCreator.createScanner(track, this, _timezone));
			}
		}
		startWaitingScanners();
	}

	public synchronized void cancel() {
		_waitingScanners.clear();
	}

	public synchronized void startWaitingScanners()
	{
		while (_numCurrentWorkers < MAX_PARALLEL_SCANS && !_waitingScanners.isEmpty())
		{
			_waitingScanners.pop().startScan();
			_numCurrentWorkers++;
		}
	}

	/** One worker has finished, so pass its results on. Also, we may be able to start another one */
	@Override
	public void informWorkComplete(TrackFile inFile, TrackContents inContents)
	{
		_trackList.scanComplete(inFile, inContents);
		synchronized (this) {
			_numCurrentWorkers--;
		}
		startWaitingScanners();
	}

	/** TEST_ONLY: replace the scanner creator to use a different type of worker for testing */
	void setScannerCreator(ScannerCreator inCreator) {
		_scannerCreator = inCreator;
	}

	interface ScannerCreator {
		AbstractFileScanner createScanner(TrackFile inTrack, WorkerCoordinator inCoordinator, TimeZone inTimezone);
	}
}
