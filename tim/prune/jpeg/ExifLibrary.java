package tim.prune.jpeg;

import java.io.File;

/**
 * Interface satisfied by both internal and external Exif implementations
 */
public interface ExifLibrary
{
	/** Get the Jpeg data from the given file */
	public JpegData getJpegData(File inFile);

	/** Check that dependencies are resolved */
	public boolean looksOK();
}
