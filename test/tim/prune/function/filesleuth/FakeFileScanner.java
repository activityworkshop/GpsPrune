package tim.prune.function.filesleuth;

import java.util.TimeZone;

import tim.prune.function.filesleuth.data.TrackContents;
import tim.prune.function.filesleuth.data.TrackFile;

/** Pretends to be a real file scanner but just fakes everything for testing */
public class FakeFileScanner extends AbstractFileScanner
{
	public FakeFileScanner(TrackFile inFile, WorkerCoordinator inCoordinator, TimeZone inTimezone) {
		super(inFile, inCoordinator, inTimezone);
	}

	@Override
	protected void run()
	{
		try {
			Thread.sleep(1000L);
		}
		catch (InterruptedException ignored) {}
		TrackContents contents = new TrackContents(_timezone);
		contents.addString("cabbage");
		finished(contents);
	}
}
