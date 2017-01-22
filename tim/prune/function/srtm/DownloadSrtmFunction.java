package tim.prune.function.srtm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.GpsPrune;
import tim.prune.I18nManager;
import tim.prune.config.Config;
import tim.prune.data.DoubleRange;
import tim.prune.gui.ProgressDialog;

/**
 * Class to provide a download function for the Space Shuttle's SRTM data files.
 * HGT files are downloaded into memory via HTTP and stored in the map cache.
 */
public class DownloadSrtmFunction extends GenericFunction implements Runnable
{
	/** Progress dialog */
	private ProgressDialog _progress = null;
	/** Flag to check whether this function is currently running or not */
	private boolean _running = false;


	/**
	 * Constructor
	 * @param inApp  App object
	 */
	public DownloadSrtmFunction(App inApp) {
		super(inApp);
	}

	/** @return name key */
	public String getNameKey() {
		return "function.downloadsrtm";
	}

	/**
	 * Begin the download
	 */
	public void begin()
	{
		_running = true;
		if (_progress == null) {
			_progress = new ProgressDialog(_parentFrame, getNameKey());
		}
		_progress.show();
		// start new thread for time-consuming part
		new Thread(this).start();
	}

	/**
	 * Run method using separate thread
	 */
	public void run()
	{
		// Compile list of tiles to get
		ArrayList<SrtmTile> tileList = new ArrayList<SrtmTile>();

		// First, loop to see which tiles are needed
		DoubleRange lonRange = _app.getTrackInfo().getTrack().getLonRange();
		DoubleRange latRange = _app.getTrackInfo().getTrack().getLatRange();
		final int minLon = (int) Math.floor(lonRange.getMinimum());
		final int maxLon = (int) Math.floor(lonRange.getMaximum());
		final int minLat = (int) Math.floor(latRange.getMinimum());
		final int maxLat = (int) Math.floor(latRange.getMaximum());

		for (int lon=minLon; lon<= maxLon; lon++)
		{
			for (int lat=minLat; lat <= maxLat; lat++)
			{
				SrtmTile tile = new SrtmTile(lat, lon);
				boolean alreadyGot = false;
				for (int t = 0; t < tileList.size(); t++)
				{
					if (tileList.get(t).equals(tile)) {
						alreadyGot = true;
					}
				}
				if (!alreadyGot) {tileList.add(tile);}
			}
		}

		downloadTiles(tileList);
		// Finished
		_running = false;
	}


	/**
	 * Download the tiles of SRTM data
	 * @param inTileList list of tiles to get
	 */
	private void downloadTiles(ArrayList<SrtmTile> inTileList)
	{
		// Update progress bar
		if (_progress != null)
		{
			_progress.setMaximum(inTileList.size());
			_progress.setValue(0);
		}

		String errorMessage = null;

		// Check the cache is ok
		final String diskCachePath = Config.getConfigString(Config.KEY_DISK_CACHE);
		if (diskCachePath != null)
		{
			File srtmDir = new File(diskCachePath, "srtm");
			if (!srtmDir.exists() && !srtmDir.mkdir()) {
				// can't create the srtm directory
				errorMessage = I18nManager.getText("error.downloadsrtm.nocache");
			}
		}
		else {
			// no cache set up
			errorMessage = I18nManager.getText("error.downloadsrtm.nocache");
		}

		// Get urls for each tile
		URL[] urls = TileFinder.getUrls(inTileList);
		int numDownloaded = 0;
		for (int t=0; t<inTileList.size() && !_progress.isCancelled(); t++)
		{
			if (urls[t] != null)
			{
				// Define streams
				FileOutputStream outStream = null;
				InputStream inStream = null;
				try
				{
					// Set progress
					_progress.setValue(t);
					// See if we've already got this tile or not
					File outputFile = getFileToWrite(urls[t]);
					if (outputFile != null)
					{
						// System.out.println("Download: Need to download: " + urls[t]);
						outStream = new FileOutputStream(outputFile);
						URLConnection conn = urls[t].openConnection();
						conn.setRequestProperty("User-Agent", "GpsPrune v" + GpsPrune.VERSION_NUMBER);
						inStream = conn.getInputStream();
						// Copy all the bytes to the file
						int c;
						while ((c = inStream.read()) != -1)
						{
							outStream.write(c);
						}

						numDownloaded++;
					}
					// else System.out.println("Don't need to download: " + urls[t].getFile());
				}
				catch (IOException ioe) {errorMessage = ioe.getClass().getName() + " - " + ioe.getMessage();
				}
				// Make sure streams are closed
				try {inStream.close();} catch (Exception e) {}
				try {outStream.close();} catch (Exception e) {}
			}
		}

		_progress.dispose();
		if (_progress.isCancelled()) {
			return;
		}

		if (errorMessage != null) {
			_app.showErrorMessageNoLookup(getNameKey(), errorMessage);
		}
		else if (numDownloaded == 1)
		{
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getTextWithNumber("confirm.downloadsrtm.1", numDownloaded),
				I18nManager.getText(getNameKey()), JOptionPane.INFORMATION_MESSAGE);
		}
		else if (numDownloaded > 1)
		{
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getTextWithNumber("confirm.downloadsrtm", numDownloaded),
				I18nManager.getText(getNameKey()), JOptionPane.INFORMATION_MESSAGE);
		}
		else if (inTileList.size() > 0) {
			_app.showErrorMessage(getNameKey(), "confirm.downloadsrtm.none");
		}
	}

	/**
	 * See whether the SRTM file is already available locally
	 * @param inUrl URL for online resource
	 * @return file object to write to, or null if already there
	 */
	private static File getFileToWrite(URL inUrl)
	{
		String diskCachePath = Config.getConfigString(Config.KEY_DISK_CACHE);
		if (diskCachePath != null)
		{
			File srtmDir = new File(diskCachePath, "srtm");
			if (srtmDir.exists() && srtmDir.isDirectory() && srtmDir.canRead())
			{
				File srtmFile = new File(srtmDir, new File(inUrl.getFile()).getName());
				if (!srtmFile.exists() || !srtmFile.canRead() || srtmFile.length() <= 400) {
					return srtmFile;
				}
			}
		}
		return null;
	}

	/**
	 * @return true if a thread is currently running
	 */
	public boolean isRunning()
	{
		return _running;
	}
}
