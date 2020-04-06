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
	/** True if cacher is active, false if blocked */
	private boolean _active = false;

	/** Time limit to cache images for */
	private static final long CACHE_TIME_LIMIT = 20 * 24 * 60 * 60 * 1000; // 20 days in ms
	/** Hashset of all blocked / 404 tiles to avoid requesting them again */
	private static final HashSet<String> BLOCKED_URLS = new HashSet<String>();
	/**Hashset of files which are currently being processed */
	private static final HashSet<String> DOWNLOADING_FILES = new HashSet<String>();
	/** Number of currently active threads */
	private static int NUMBER_ACTIVE_THREADS = 0;
	/** Flag to remember whether any server connection is possible */
	private static boolean CONNECTION_ACTIVE = true;


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
		_active = registerCacher(inFile.getAbsolutePath());
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
		if (!basePath.exists() || !basePath.isDirectory() || !basePath.canWrite())
		{
			// Can't write to base path
			return;
		}
		File tileFile = new File(basePath, inTilePath);

		// Check if it has already failed
		if (BLOCKED_URLS.contains(inUrl.toString())) {
			return;
		}

		File dir = tileFile.getParentFile();
		// Construct a cacher to load the image if necessary
		if ((dir.exists() || dir.mkdirs()) && dir.canWrite())
		{
			DiskTileCacher cacher = new DiskTileCacher(inUrl, tileFile, inObserver);
			cacher.startDownloading();
		}
	}

	/**
	 * Start downloading the configured tile
	 */
	private void startDownloading()
	{
		if (_active)
		{
			new Thread(this).start();
		}
	}

	/**
	 * Run method for loading URL asynchronously and saving to file
	 */
	public void run()
	{
		waitUntilAllowedToRun();
		if (doDownload())
		{
			if (!CONNECTION_ACTIVE)
			{
				// wasn't active before but this download worked - we've come back online
				BLOCKED_URLS.clear();
				CONNECTION_ACTIVE = true;
			}
		}
		// Release file and thread
		unregisterCacher(_file.getAbsolutePath());
		threadFinished();
	}

	/**
	 * Blocks (in separate thread) until allowed by concurrent thread limit
	 */
	private void waitUntilAllowedToRun()
	{
		while (!canStartNewThread())
		{
			try {
				Thread.sleep(400);
			}
			catch (InterruptedException e) {}
		}
	}

	/**
	 * @return true if download was successful
	 */
	private boolean doDownload()
	{
		boolean finished = false;
		InputStream in = null;
		FileOutputStream out = null;
		File tempFile = new File(_file.getAbsolutePath() + ".temp");

		if (tempFile.exists())
		{
			tempFile.delete();
		}
		try
		{
			if (!tempFile.createNewFile()) {return false;}
		}
		catch (Exception e) {return false;}

		try
		{
			// Open streams from URL and to file
			out = new FileOutputStream(tempFile);
			//System.out.println("DiskTileCacher opening URL: " + _url.toString());
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
		}
		catch (IOException e)
		{
			System.err.println("ioe: " + e.getClass().getName() + " - " + e.getMessage());
			BLOCKED_URLS.add(_url.toString());
			CONNECTION_ACTIVE = false;
		}
		finally
		{
			// clean up files
			try {in.close();} catch (Exception e) {} // ignore
			try {out.close();} catch (Exception e) {} // ignore
			if (!finished)
			{
				tempFile.delete();
			}
		}
		boolean success = false;
		// Move temp file to desired file location
		if (tempFile.exists() && tempFile.length() > 0L)
		{
			if (tempFile.renameTo(_file))
			{
				success = true;
			}
			else
			{
				// File couldn't be moved - delete both to be sure
				System.out.println("Failed to rename temp file: " + tempFile.getAbsolutePath());
				tempFile.delete();
				_file.delete();
			}
		}

		// Tell parent that load is finished (parameters ignored)
		_observer.imageUpdate(null, ImageObserver.ALLBITS, 0, 0, 0, 0);
		return success;
	}

	// Blocking of cachers working on same file

	/**
	 * Register a cacher writing to the specified file path
	 * @param inFilePath destination path to tile file
	 * @return true if nobody else has claimed this file yet
	 */
	private synchronized static boolean registerCacher(String inFilePath)
	{
		if (DOWNLOADING_FILES.contains(inFilePath))
		{
			return false;
		}
		// Nobody has claimed this file yet
		DOWNLOADING_FILES.add(inFilePath);
		return true;
	}

	/**
	 * Cacher has finished dealing with the specified file
	 * @param inFilePath destination path to tile file
	 */
	private synchronized static void unregisterCacher(String inFilePath)
	{
		DOWNLOADING_FILES.remove(inFilePath);
	}

	// Limiting of active threads

	/**
	 * @return true if another thread is allowed to become active
	 */
	private synchronized static boolean canStartNewThread()
	{
		final int MAXIMUM_NUM_THREADS = 8;
		if (NUMBER_ACTIVE_THREADS < MAXIMUM_NUM_THREADS)
		{
			NUMBER_ACTIVE_THREADS++;
			return true;
		}
		// Already too many threads active
		return false;
	}

	/**
	 * Inform that one of the previously active threads has now completed
	 */
	private synchronized static void threadFinished()
	{
		if (NUMBER_ACTIVE_THREADS > 0)
		{
			NUMBER_ACTIVE_THREADS--;
		}
	}
}
