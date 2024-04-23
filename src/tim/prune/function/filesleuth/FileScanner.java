package tim.prune.function.filesleuth;

import java.util.TimeZone;

import tim.prune.function.filesleuth.data.TrackContents;
import tim.prune.function.filesleuth.data.TrackFile;
import tim.prune.function.filesleuth.extract.ContentExtractor;
import tim.prune.function.filesleuth.extract.ExtractorFactory;


/** Responsible for using a ContentExtractor to get the contents of a single file */
public class FileScanner extends AbstractFileScanner
{
	public FileScanner(TrackFile inFile, WorkerCoordinator inCoordinator, TimeZone inTimezone) {
		super(inFile, inCoordinator, inTimezone);
	}

	@Override
	protected void run()
	{
		ContentExtractor extractor = ExtractorFactory.createExtractor(getFile());
		TrackContents contents = (extractor == null ? new TrackContents(_timezone) : extractor.getContents(_timezone));
		// Wait a little to avoid overloading everything
		try {
			Thread.sleep(100L);
		}
		catch (InterruptedException ignored) {}
		finished(contents);
	}
}
