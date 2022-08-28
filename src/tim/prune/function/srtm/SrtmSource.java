package tim.prune.function.srtm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import tim.prune.config.Config;


/**
 * Superclass of each of the two available
 * sources of SRTM data
 */
public abstract class SrtmSource
{
	/** Possible results of the download */
	public enum Result {DOWNLOADED, NOTHING_TO_DO, DOWNLOAD_FAILED,
		CACHE_FAILED, NOT_ENABLED};

	/** Altitude below which is considered void */
	public static final int VOID_VAL = -32768;


	/**
	 * Get the Url for the given tile
	 * @param inTile Tile to get the data for
	 * @return single URL
	 */
	public abstract URL getUrl(SrtmTile inTile);

	/**
	 * Download a single tile of SRTM data
	 * @param inTile tile to get
	 */
	public abstract Result downloadTile(SrtmTile inTile) throws SrtmAuthException;

	/**
	 * @return filename with which this tile data will be cached
	 */
	public abstract String getFilename(SrtmTile inTile);

	/**
	 * @return the number of bytes in a complete hgt file (after unzipping)
	 */
	public long getTileSizeBytes() {return getTilePixels() * getTilePixels() * 2L;}

	/**
	 * @return number of pixels on each side of the square
	 */
	public abstract int getTilePixels();


	/**
	 * @return the directory in which all tiles are cached
	 */
	public File getCacheDir()
	{
		String diskCachePath = Config.getConfigString(Config.KEY_DISK_CACHE);
		if (diskCachePath == null) {return null;}

		return new File(diskCachePath, "srtm");
	}

	/**
	 * Get the path to write the tile to
	 * @param inUrl URL for online resource
	 * @return file object to write to, or null if already there
	 */
	protected File getFileToWrite(URL inUrl)
	{
		File srtmDir = getCacheDir();
		if (srtmDir != null && srtmDir.exists() && srtmDir.isDirectory() && srtmDir.canRead())
		{
			File srtmFile = new File(srtmDir, new File(inUrl.getFile()).getName());
			if (!srtmFile.exists() || !srtmFile.canRead() || srtmFile.length() <= 400) {
				return srtmFile;
			}
		}
		return null;
	}

	/**
	 * Write the contents of the stream to file
	 * @param inUrl url from which the stream came
	 * @param inStream stream containing data
	 * @return true if successful
	 */
	protected boolean writeFileFromStream(URL inUrl, InputStream inStream)
	{
		FileOutputStream outStream = null;
		boolean success = false;
		if (inStream != null)
		{
			try
			{
				int numBytesRead;
				File outputFile = getFileToWrite(inUrl);
				if (outputFile != null)
				{
					byte[] buffer = new byte[512];
					outStream = new FileOutputStream(outputFile);
					while ((numBytesRead = inStream.read(buffer)) != -1) {
						outStream.write(buffer, 0, numBytesRead);
					}
					success = true;
				}
			}
			catch (IOException ioe) {
				System.err.println(ioe.getClass().getName() + " - " + ioe.getMessage());
			}
			// Close output stream; input stream will be closed by creator
			try {outStream.close();} catch (Exception e) {}
		}

		return success;
	}
}
