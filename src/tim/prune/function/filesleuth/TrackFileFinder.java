package tim.prune.function.filesleuth;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Paths;

import tim.prune.function.filesleuth.data.FileTypeBlacklist;
import tim.prune.function.filesleuth.data.TrackFileList;

/**
 * Responsible for searching the file tree and building up a TrackFileList
 */
public class TrackFileFinder
{
	private final TrackFileList _tracks;
	private final File _startDir;
	private final boolean _subdirectories;


	public TrackFileFinder(TrackFileList inList, File inStartDir, boolean inSubdirectories)
	{
		_tracks = inList;
		_startDir = inStartDir;
		_subdirectories = inSubdirectories;
	}

	public void begin() {
		new Thread(this::run).start();
	}

	/** Perform recursive file search in separate thread */
	private void run()
	{
		final int depth = _subdirectories ? Integer.MAX_VALUE : 1;
		try {
			Files.walk(Paths.get(_startDir.getAbsolutePath()), depth, FileVisitOption.FOLLOW_LINKS)
					.filter(Files::isRegularFile)
					.filter(FileTypeBlacklist::isAllowed)
					.forEach(_tracks::foundFile);
		}
		catch (IOException | java.io.UncheckedIOException ioe) {
			System.err.println("Error: " + ioe.getMessage());
		}
		_tracks.setListComplete();
	}
}
