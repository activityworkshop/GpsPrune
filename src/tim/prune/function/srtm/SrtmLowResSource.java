package tim.prune.function.srtm;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import tim.prune.GpsPrune;


/**
 * Low-resolution source of SRTM data
 */
public class SrtmLowResSource extends SrtmSource
{
	/** tile data loaded from file */
	private byte[] _tileData = null;

	/** URL prefix for all tiles */
	private static final String URL_PREFIX = "https://srtm.kurviger.de/SRTM3/";
	/** Directory names for each continent */
	private static final String[] CONTINENTS = {"", "Eurasia", "North_America", "Australia",
		"Islands", "South_America", "Africa"};

	@Override
	public int getTilePixels() {
		return 1201;
	}

	/**
	 * Get the Url for the given tile
	 * @param inTile Tile to get the data for
	 * @return single URL
	 */
	public URL getUrl(SrtmTile inTile)
	{
		if (inTile == null) {return null;}
		if (_tileData == null)
		{
			_tileData = readDatFile();
			if (_tileData == null)
			{
				System.err.println("Build error: resource srtmtiles.dat missing!");
				return null;
			}
		}

		URL url = null;
		// Get byte from lookup array
		int idx = (inTile.getLatitude() + 59)*360 + (inTile.getLongitude() + 180);
		try
		{
			int dir = _tileData[idx];
			if (dir > 0) {
				try {
					url = new URL(URL_PREFIX + CONTINENTS[dir] + "/"
						+ getFilename(inTile));
				} catch (MalformedURLException e) {} // ignore error, url stays null
			}
		} catch (ArrayIndexOutOfBoundsException e) {} // ignore error, url stays null

		return url;
	}

	/**
	 * @return filename with which this tile data will be cached
	 */
	public String getFilename(SrtmTile inTile)
	{
		return inTile.getTileName() + ".hgt.zip";
	}

	/**
	 * Read the dat file and get the contents
	 * @return byte array containing file contents
	 */
	private static byte[] readDatFile()
	{
		InputStream in = null;
		try
		{
			// Need absolute path to dat file
			in = SrtmLowResSource.class.getResourceAsStream("/tim/prune/function/srtm/srtmtiles.dat");
			if (in != null)
			{
				byte[] buffer = new byte[in.available()];
				in.read(buffer);
				in.close();
				return buffer;
			}
		}
		catch (java.io.IOException e) {
			System.err.println("Exception trying to read srtmtiles.dat : " + e.getMessage());
		}
		finally
		{
			try {
				in.close();
			}
			catch (Exception e) {} // ignore
		}
		return null;
	}

	/**
	 * Download a single tile of SRTM data
	 * @param inTile tile to get
	 */
	public Result downloadTile(SrtmTile inTile)
	{
		URL url = getUrl(inTile);
		if (url == null) {
			return Result.NOTHING_TO_DO;
		}

		// Check the cache is ok
		File srtmDir = getCacheDir();
		if (srtmDir != null)
		{
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

		InputStream inStream = null;
		Result result = Result.NOTHING_TO_DO;
		try
		{
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("User-Agent", "GpsPrune v" + GpsPrune.VERSION_NUMBER);
			inStream = conn.getInputStream();
			result = writeFileFromStream(url, inStream) ? Result.DOWNLOADED : Result.DOWNLOAD_FAILED;
		}
		catch (IOException ioe) {
			System.err.println(ioe.getClass().getName() + " - " + ioe.getMessage());
			result = Result.DOWNLOAD_FAILED;
		}
		// Make sure stream is closed
		try {inStream.close();} catch (Exception e) {}

		return result;
	}
}
