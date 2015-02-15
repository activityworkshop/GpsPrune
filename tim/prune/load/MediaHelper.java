package tim.prune.load;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import tim.prune.data.AudioClip;
import tim.prune.data.MediaObject;
import tim.prune.data.Photo;

/**
 * Class to provide helper functions for loading media
 */
public abstract class MediaHelper
{
	/** File filters */
	private static GenericFileFilter _jpegFilter = null, _audioFilter = null;


	/**
	 * Construct a MediaObject for the given path
	 * @param inZipFile path to archive file (if any)
	 * @param inPath path to media file
	 * @param inSourceFile file from which data was loaded
	 * @return either Photo or AudioClip object as appropriate, or null
	 */
	public static MediaObject createMediaObject(File inZipFile, String inPath, File inSourceFile)
	{
		if (inPath == null || inPath.length() < 5) return null;
		InputStream is = null;
		ZipFile zf     = null;
		byte[] data    = null;
		String url     = null;
		try
		{
			// Check if path is a URL, in which case get an input stream from it
			if (inPath.substring(0, 5).toLowerCase().equals("http:"))
			{
				url = inPath;
				is = new URL(inPath).openStream();
				data = ByteScooper.scoop(is);
			}
		}
		catch (IOException ioe) {
			System.err.println("Got ioe from url: " + ioe.getMessage());
		} // is stays null

		// Now see if file is in the zip file
		if (is == null && inZipFile != null && inZipFile.exists() && inZipFile.canRead())
		{
			try
			{
				zf = new ZipFile(inZipFile);
				ZipEntry entry = zf.getEntry(inPath);
				if (entry != null && entry.getSize() > 0)
				{
					data = ByteScooper.scoop(zf.getInputStream(entry));
					// System.out.println("Size of data " + (data.length == entry.getSize()?"matches":"DOESN'T match"));
				}
			}
			catch (IOException ioe) {
				System.err.println("Got ioe from zip file: " + ioe.getMessage());
			}
		}
		// Clean up input streams
		if (is != null) try {
			is.close();
		} catch (IOException ioe) {}
		if (zf != null) try {
			zf.close();
		} catch (IOException ioe) {}

		if (data != null)
		{
			// Create Photo or AudioClip using this entry
			String filename = new File(inPath).getName();
			initFilters();
			if (_jpegFilter.acceptFilename(inPath)) {
				return new Photo(data, filename, url);
			}
			else if (_audioFilter.acceptFilename(inPath)) {
				return new AudioClip(data, filename, url);
			}
			return null;
		}

		// If we haven't got a result by now, try to load plain file
		File file = new File(inPath);
		if (inSourceFile != null && !file.isAbsolute()) {
			file = new File(inSourceFile.getParent(), inPath);
		}
		// awkward construction because new File(startPath, absolutePath) doesn't work
		return createMediaObject(file);
	}

	/**
	 * Construct a MediaObject for the given file
	 * @param inFile file to load
	 * @return either Photo or AudioClip object as appropriate, or null
	 */
	private static MediaObject createMediaObject(File inFile)
	{
		if (inFile == null) {return null;}
		if (!inFile.exists() || !inFile.canRead() || !inFile.isFile()) {return null;}
		initFilters();
		// Check if filename looks like a jpeg
		if (_jpegFilter.acceptFilename(inFile.getName())) {
			return JpegLoader.createPhoto(inFile);
		}
		// Check if filename looks like an audio clip
		if (_audioFilter.acceptFilename(inFile.getName())) {
			return new AudioClip(inFile);
		}
		// Neither photo nor audio
		return null;
	}

	/**
	 * Initialise filters if necessary
	 */
	private static void initFilters()
	{
		if (_jpegFilter == null) {
			_jpegFilter = new JpegFileFilter();
			_audioFilter = new AudioFileFilter();
		}
	}
}
