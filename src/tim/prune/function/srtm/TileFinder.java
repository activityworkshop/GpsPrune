package tim.prune.function.srtm;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Class to get the URLs of the SRTM tiles
 * using the srtmtiles.dat file
 */
public class TileFinder
{
	/** tile data loaded from file */
	private byte[] _tileData = null;

	/** URL prefix for all tiles */
	private static final String URL_PREFIX = "https://dds.cr.usgs.gov/srtm/version2_1/SRTM3/";
	/** Directory names for each continent */
	private static final String[] CONTINENTS = {"", "Eurasia", "North_America", "Australia",
		"Islands", "South_America", "Africa"};


	/**
	 * Get the Urls for the given list of tiles
	 * @param inTiles list of Tiles to get
	 * @return array of URLs
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
					url = new URL(URL_PREFIX + CONTINENTS[dir] + "/" + inTile.getTileName());
				} catch (MalformedURLException e) {} // ignore error, url stays null
			}
		} catch (ArrayIndexOutOfBoundsException e) {} // ignore error, url stays null

		return url;
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
			in = TileFinder.class.getResourceAsStream("/tim/prune/function/srtm/srtmtiles.dat");
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
}
