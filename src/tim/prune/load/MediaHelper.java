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
	 * Construct a MediaObject for the given url
	 * @param inUrl url linked to point
	 * @return either Photo or AudioClip object as appropriate, or null
	 */
	public static MediaObject createMediaObjectFromUrl(URL inUrl)
	{
		if (inUrl == null) return null;
		byte[] data = null;
		try (InputStream is = inUrl.openStream()) {
			data = ByteScooper.scoop(is);
			return createMediaObject(data, inUrl.getFile(), inUrl.toString());
		}
		catch (IOException ioe) {
			System.err.println("Got ioe from url: " + ioe.getMessage());
		}
		return null;
	}


	/**
	 * Construct a MediaObject for the given path
	 * @param inZipFile path to archive file (if any)
	 * @param inPath path to media file
	 * @param inSourceFile file from which data was loaded
	 * @return either Photo or AudioClip object as appropriate, or null
	 */
	public static MediaObject createMediaObjectRelative(File inZipFile, String inPath, File inSourceFile)
	{
		if (inPath == null || inPath.length() < 5) return null;
		byte[] data = null;
		// See if file is in the zip file
		if (inZipFile != null && inZipFile.exists() && inZipFile.canRead())
		{
			try (ZipFile zf = new ZipFile(inZipFile))
			{
				ZipEntry entry = zf.getEntry(inPath);
				if (entry != null && entry.getSize() > 0) {
					data = ByteScooper.scoop(zf.getInputStream(entry));
				}
			}
			catch (IOException ioe) {
				System.err.println("Got ioe from zip file: " + ioe.getMessage());
			}
		}

		if (data != null)
		{
			final String filename = new File(inPath).getName();
			return createMediaObject(data, filename, null);
		}

		// If we haven't got a result by now, try to load plain file
		File file = new File(inPath);
		if (inSourceFile != null && !file.isAbsolute()) {
			file = new File(inSourceFile.getParent(), inPath);
		}
		return createMediaObject(file);
	}

	/**
	 * Create Photo or AudioClip using this entry
	 */
	private static MediaObject createMediaObject(byte[] inData, String inFilename, String inUrl)
	{
		initFilters();
		if (_jpegFilter.acceptFilename(inFilename)) {
			return new Photo(inData, inFilename, inUrl);
		}
		else if (_audioFilter.acceptFilename(inFilename)) {
			return new AudioClip(inData, inFilename, inUrl);
		}
		return null;
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
