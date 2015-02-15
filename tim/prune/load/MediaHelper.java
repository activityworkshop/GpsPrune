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
	 * @return either Photo or AudioClip object as appropriate, or null
	 */
	public static MediaObject createMediaObject(File inZipFile, String inPath)
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
		else
			// If we haven't got a result by now, try to just load plain file
			return createMediaObject(inPath);
	}

	/**
	 * Construct a MediaObject for the given path
	 * @param inPath path to file
	 * @return either Photo or AudioClip object as appropriate, or null
	 */
	private static MediaObject createMediaObject(String inPath)
	{
		if (inPath == null) {return null;}
		File file = new File(inPath);
		if (!file.exists() || !file.canRead() || !file.isFile()) {return null;}
		initFilters();
		// Check if filename looks like a jpeg
		if (_jpegFilter.acceptFilename(file.getName())) {
			return JpegLoader.createPhoto(file);
		}
		// Check if filename looks like an audio clip
		if (_audioFilter.acceptFilename(file.getName())) {
			return new AudioClip(file);
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
