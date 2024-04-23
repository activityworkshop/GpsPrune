package tim.prune.function.filesleuth;

import tim.prune.function.filesleuth.data.TrackContents;
import tim.prune.function.filesleuth.data.TrackFile;

public interface WorkerCoordinator {
	public void informWorkComplete(TrackFile inFile, TrackContents inContents);
}
