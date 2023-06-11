package tim.prune.load;

import java.io.File;

/**
 * Holds a lock on the given file and performs some action
 * when all locks are released
 */
public class FileToBeLoaded
{
	private final File _file;
	private final Runnable _afterwards;
	private int _ownerCounter;

	public FileToBeLoaded(File inFile, Runnable inAfterwards)
	{
		_file = inFile;
		_afterwards = inAfterwards;
		_ownerCounter = 1;
	}

	public File getFile() {
		return _file;
	}

	/** Accept ownership, perhaps for use in different thread */
	public synchronized void takeOwnership() {
		_ownerCounter++;
	}

	/** Release ownership */
	public synchronized void release()
	{
		_ownerCounter--;
		if (_ownerCounter == 0) {
			_afterwards.run();
		}
	}
}
