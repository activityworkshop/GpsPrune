package tim.prune.function.srtm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import tim.prune.GpsPrune;
import tim.prune.config.Config;

/**
 * Class to provide a download function for the Space Shuttle's SRTM data files.
 * HGT files are downloaded into memory via HTTP and stored in the map cache.
 */
public class TileDownloader
{
	/** Possible results of the download */
	public enum Result {DOWNLOADED, NOTHING_TO_DO, DOWNLOAD_FAILED, CACHE_FAILED};

	/**
	 * Download a single tile of SRTM data
	 * @param inUrl remote URL to get
	 */
	public Result downloadTile(URL inUrl)
	{
		if (inUrl == null) {
			return Result.NOTHING_TO_DO;
		}

		// Check the cache is ok
		final String diskCachePath = Config.getConfigString(Config.KEY_DISK_CACHE);
		if (diskCachePath != null)
		{
			File srtmDir = new File(diskCachePath, "srtm");
			if (srtmDir.exists() && !srtmDir.isDirectory()) {
				// exists but isn't a directory - can't be used
				return Result.CACHE_FAILED;
			}
			if (!srtmDir.exists() && !srtmDir.mkdir()) {
				// can't create the srtm directory
				return Result.CACHE_FAILED;
			}
		}
		else {
			// no cache set up
			return Result.CACHE_FAILED;
		}

		// Define streams
		FileOutputStream outStream = null;
		InputStream inStream = null;
		Result result = Result.NOTHING_TO_DO;
		try
		{
			// See if we've already got this tile or not
			File outputFile = getFileToWrite(inUrl);
			if (outputFile != null)
			{
				System.out.println("Download: Need to download: " + inUrl);
				outStream = new FileOutputStream(outputFile);
				URLConnection conn = inUrl.openConnection();
				conn.setRequestProperty("User-Agent", "GpsPrune v" + GpsPrune.VERSION_NUMBER);
				inStream = conn.getInputStream();
				// Copy all the bytes to the file
				int c;
				while ((c = inStream.read()) != -1)
				{
					outStream.write(c);
				}
				result = Result.DOWNLOADED;
			}
		}
		catch (IOException ioe) {
			System.err.println(ioe.getClass().getName() + " - " + ioe.getMessage());
			result = Result.DOWNLOAD_FAILED;
		}
		// Make sure streams are closed
		try {inStream.close();} catch (Exception e) {}
		try {outStream.close();} catch (Exception e) {}

		return result;
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
}
