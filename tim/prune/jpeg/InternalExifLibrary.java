package tim.prune.jpeg;

import java.io.File;
import tim.prune.jpeg.drew.ExifReader;
import tim.prune.jpeg.drew.JpegException;

/**
 * Class to act as a gateway into the internal exif library functions.
 * This should be the only class with dependence on the jpeg.drew package.
 * Should not be included if external library will be used (eg Debian).
 */
public class InternalExifLibrary implements ExifLibrary
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
			data = new ExifReader(inFile).extract();
		}
		catch (JpegException jpe) {} // data remains null
		return data;
	}

	/**
	 * Check whether the exifreader class can be correctly resolved
	 * @return true if it looks ok
	 */
	public boolean looksOK()
	{
		try {
			String test = ExifReader.class.getName();
			if (test != null) return true;
		}
		catch (LinkageError le) {}
		return false;
	}
}
