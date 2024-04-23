package tim.prune.fileutils;

import java.io.File;

/** The method File.listFiles() can return null, so we want to avoid that */
public abstract class FileList
{
	/** Make sure the file list isn't null */
	public static File[] filesIn(File inDirectory)
	{
		File[] contents = (inDirectory == null ? null : inDirectory.listFiles());
		if (contents == null) {
			return new File[] {};
		}
		return contents;
	}
}
