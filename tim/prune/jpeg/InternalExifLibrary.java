package tim.prune.jpeg;

import java.io.File;

import tim.prune.jpeg.drew.ExifReader;
import tim.prune.jpeg.drew.ExifException;

/**
 * Class to act as an entry point to the internal exif library functions.
 * This should be the only class with dependence on the jpeg.drew package.
 */
public class InternalExifLibrary
{
	/**
	 * Use the _internal_ exif library to get the data from the given file
	 * @param inFile file to access
	 * @return Jpeg data if available, otherwise null
	 */
	public JpegData getJpegData(File inFile)
	{
		JpegData data = null;
		try {
			data = ExifReader.readMetadata(inFile);
		}
		catch (ExifException jpe) {} // data remains null
		return data;
	}
}
