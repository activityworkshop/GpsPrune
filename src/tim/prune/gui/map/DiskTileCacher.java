package tim.prune.gui.map;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;

import tim.prune.GpsPrune;

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
	/** Hashset of all blocked / 404 tiles to avoid requesting them again */
	private static final HashSet<String> BLOCKED_URLS = new HashSet<String>();

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
	}

	/**
	 * Get the specified tile from the disk cache
	 * @param inBasePath base path to whole disk cache
	 * @param inTilePath relative path to requested tile
	 * @return tile image if available, or null if not there
	 */
	public static MapTile getTile(String inBasePath, String inTilePath)
	{
		if (inBasePath == null) {return null;}
		File tileFile = new File(inBasePath, inTilePath);
		Image image = null;
		if (tileFile.exists() && tileFile.canRead() && tileFile.length() > 0)
		{
			long fileStamp = tileFile.lastModified();
			boolean isExpired = ((System.currentTimeMillis()-fileStamp) > CACHE_TIME_LIMIT);
			try
			{
				image = Toolkit.getDefaultToolkit().createImage(tileFile.getAbsolutePath());
				return new MapTile(image, isExpired);
			}
			catch (Exception e) {
				System.err.println("createImage: " + e.getClass().getName() + " _ " + e.getMessage());
			}
		}
		return null;
	}

	/**
	 * Save the specified image tile to disk
	 * @param inUrl url to get image from
	 * @param inBasePath base path to disk cache
	 * @param inTilePath relative path to this tile
	 * @param inObserver observer to inform when load complete
	 */
	public static void saveTile(URL inUrl, String inBasePath, String inTilePath, ImageObserver inObserver)
	{
		if (inBasePath == null || inTilePath == null) {return;}
		// save file if possible
		File basePath = new File(inBasePath);
		if (!basePath.exists() || !basePath.isDirectory() || !basePath.canWrite()) {
			// Can't write to base path
			return;
		}
		File tileFile = new File(basePath, inTilePath);
		// Check if this file is already being loaded
		if (isBeingLoaded(tileFile)) {return;}
		// Check if it has already failed
		if (BLOCKED_URLS.contains(inUrl.toString())) {return;}

		File dir = tileFile.getParentFile();
		// Start a new thread to load the image if necessary
		if ((dir.exists() || dir.mkdirs()) && dir.canWrite())
		{
			new Thread(new DiskTileCacher(inUrl, tileFile, inObserver)).start();
		}
	}

	/**
	 * Check whether the given tile is already being loaded
	 * @param inFile desired file
	 * @return true if temporary file with this name exists
	 */
	private static boolean isBeingLoaded(File inFile)
	{
		File tempFile = new File(inFile.getAbsolutePath() + ".temp");
		if (!tempFile.exists()) {
			return false;
		}
		// File exists, so check if it was created recently
		final long fileAge = System.currentTimeMillis() - tempFile.lastModified();
		return fileAge < 1000000L; // overwrite if the temp file is still there after 1000s
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
		synchronized (DiskTileCacher.class)
		{
			if (tempFile.exists()) {tempFile.delete();}
			try {
				if (!tempFile.createNewFile()) {return;}
			}
			catch (Exception e) {return;}
		}
		try
		{
			// Open streams from URL and to file
			out = new FileOutputStream(tempFile);
			//System.out.println("Opening URL: " + _url.toString());
			// Set http user agent on connection
			URLConnection conn = _url.openConnection();
			conn.setRequestProperty("User-Agent", "GpsPrune v" + GpsPrune.VERSION_NUMBER);
			in = conn.getInputStream();
			int d = 0;
			// Loop over each byte in the stream (maybe buffering is more efficient?)
			while ((d = in.read()) >= 0) {
				out.write(d);
			}
			finished = true;
		} catch (IOException e) {
			System.err.println("ioe: " + e.getClass().getName() + " - " + e.getMessage());
			BLOCKED_URLS.add(_url.toString());
		}
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
		if (tempFile.exists() && !tempFile.renameTo(_file))
		{
			// File couldn't be moved - delete both to be sure
			tempFile.delete();
			_file.delete();
		}
		// Tell parent that load is finished (parameters ignored)
		_observer.imageUpdate(null, ImageObserver.ALLBITS, 0, 0, 0, 0);
	}
}
