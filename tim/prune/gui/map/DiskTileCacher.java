package tim.prune.gui.map;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Class to control the reading and saving of map tiles
 * to a cache on disk
 */
public class DiskTileCacher implements Runnable
{
	/** URL to get image from */
	private URL _url = null;
	/** File to save image to */
	private File _file = null;
	/** Observer to be notified */
	private ImageObserver _observer = null;
	/** Time limit to cache images for */
	private static final long CACHE_TIME_LIMIT = 20 * 24 * 60 * 60 * 1000; // 20 days in ms

	/**
	 * Private constructor
	 * @param inUrl URL to get
	 * @param inFile file to save to
	 */
	private DiskTileCacher(URL inUrl, File inFile, ImageObserver inObserver)
	{
		_url = inUrl;
		_file = inFile;
		_observer = inObserver;
		new Thread(this).start();
	}

	/**
	 * Get the specified tile from the disk cache
	 * @param inBasePath base path to whole disk cache
	 * @param inTilePath relative path to requested tile
	 * @param inCheckAge true to check age of file, false to ignore
	 * @return tile image if available, or null if not there
	 */
	public static Image getTile(String inBasePath, String inTilePath, boolean inCheckAge)
	{
		if (inBasePath == null) {return null;}
		File tileFile = new File(inBasePath, inTilePath);
		Image image = null;
		if (tileFile.exists() && tileFile.canRead() && tileFile.length() > 0) {
			long fileStamp = tileFile.lastModified();
			if (!inCheckAge || ((System.currentTimeMillis()-fileStamp) < CACHE_TIME_LIMIT))
			{
				try {
					image = Toolkit.getDefaultToolkit().createImage(tileFile.getAbsolutePath());
				}
				catch (Exception e) {}
			}
		}
		return image;
	}

	/**
	 * Save the specified image tile to disk
	 * @param inUrl url to get image from
	 * @param inBasePath base path to disk cache
	 * @param inTilePath relative path to this tile
	 * @param inObserver observer to inform when load complete
	 * @return true if successful, false for failure
	 */
	public static boolean saveTile(URL inUrl, String inBasePath, String inTilePath, ImageObserver inObserver)
	{
		if (inBasePath == null || inTilePath == null) {return false;}
		// save file if possible
		File basePath = new File(inBasePath);
		if (!basePath.exists() || !basePath.isDirectory() || !basePath.canWrite()) {
			// Can't write to base path
			return false;
		}
		File tileFile = new File(basePath, inTilePath);
		// Check if this file is already being loaded
		if (isBeingLoaded(tileFile)) {return true;}

		File dir = tileFile.getParentFile();
		// Start a new thread to load the image if necessary
		if ((dir.exists() || dir.mkdirs()) && dir.canWrite())
		{
			new DiskTileCacher(inUrl, tileFile, inObserver);
			return true;
		}
		return false; // couldn't write the file
	}

	/**
	 * Check whether the given tile is already being loaded
	 * @param inFile desired file
	 * @return true if temporary file with this name exists
	 */
	private static boolean isBeingLoaded(File inFile)
	{
		File tempFile = new File(inFile.getAbsolutePath() + ".temp");
		return tempFile.exists();
	}

	/**
	 * Run method for loading URL asynchronously and saving to file
	 */
	public void run()
	{
		boolean finished = false;
		InputStream in = null;
		FileOutputStream out = null;
		File tempFile = new File(_file.getAbsolutePath() + ".temp");
		// Use a synchronized block across all threads to make sure this url is only fetched once
		synchronized (DiskTileCacher.class) {
			if (tempFile.exists()) {return;}
			try {
				if (!tempFile.createNewFile()) {return;}
			}
			catch (Exception e) {return;}
		}
		try
		{
			// Open streams from URL and to file
			out = new FileOutputStream(tempFile);
			in = _url.openStream();
			int d = 0;
			// Loop over each byte in the stream (maybe buffering is more efficient?)
			while ((d = in.read()) >= 0) {
				out.write(d);
			}
			finished = true;
		} catch (IOException e) {}
		finally
		{
			// clean up files
			try {in.close();} catch (Exception e) {} // ignore
			try {out.close();} catch (Exception e) {} // ignore
			if (!finished) {
				tempFile.delete();
			}
		}
		// Move temp file to desired file location
		if (!tempFile.renameTo(_file))
		{
			// File couldn't be moved - delete both to be sure
			tempFile.delete();
			_file.delete();
		}
		// Tell parent that load is finished (parameters ignored)
		_observer.imageUpdate(null, ImageObserver.ALLBITS, 0, 0, 0, 0);
	}
}
