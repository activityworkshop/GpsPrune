package tim.prune.data;

import java.io.File;
import java.io.IOException;

public class FileUtils
{
	public static File makeValidFile() throws IOException
	{
		File file = File.createTempFile("tempFile", ".gpx");
		file.deleteOnExit();
		return file;
	}
}
