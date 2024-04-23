package tim.prune.function.filesleuth;

import java.io.File;
import java.util.TimeZone;

import tim.prune.function.filesleuth.data.TrackContents;
import tim.prune.function.filesleuth.data.TrackFile;

/** Responsible for taking a file, reading it and summarising the contents */
public abstract class AbstractFileScanner
{
	private final TrackFile _trackFile;
	private final WorkerCoordinator _parent;
	protected final TimeZone _timezone;

	public AbstractFileScanner(TrackFile inFile, WorkerCoordinator inCoordinator, TimeZone inTimezone)
	{
		_trackFile = inFile;
		_parent = inCoordinator;
		_timezone = inTimezone;
	}

	protected File getFile() {
		return _trackFile == null ? null : _trackFile.getFile();
	}

	public void startScan() {
		new Thread(this::run).start();
	}

	protected abstract void run();

	protected void finished(TrackContents inContents) {
		_parent.informWorkComplete(_trackFile, inContents);
	}
}
