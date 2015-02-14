package tim.prune.function.srtm;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


/**
 * Class to get the URLs of the SRTM tiles
 * using the srtmtiles.dat file
 */
public abstract class TileFinder
{
	/** URL prefix for all tiles */
	private static final String URL_PREFIX = "http://dds.cr.usgs.gov/srtm/version2_1/SRTM3/";
	/** Directory names for each continent */
	private static final String[] CONTINENTS = {"", "Eurasia", "North_America", "Australia",
		"Islands", "South_America", "Africa"};


	/**
	 * Get the Urls for the given list of tiles
	 * @param inTiles list of Tiles to get
	 * @return array of URLs
	 */
	public static URL[] getUrls(ArrayList<SrtmTile> inTiles)
	{
		if (inTiles == null || inTiles.size() < 1) {return null;}
		URL[] urls = new URL[inTiles.size()];
		// Read dat file into array
		byte[] lookup = readDatFile();
		for (int t=0; t<inTiles.size(); t++)
		{
			SrtmTile tile = inTiles.get(t);
			// Get byte from lookup array
			int idx = (tile.getLatitude() + 59)*360 + (tile.getLongitude() + 180);
			try
			{
				int dir = lookup[idx];
				if (dir > 0) {
					try {
						urls[t] = new URL(URL_PREFIX + CONTINENTS[dir] + "/" + tile.getTileName());
					} catch (MalformedURLException e) {} // ignore error, url stays null
				}
			} catch (ArrayIndexOutOfBoundsException e) {} // ignore error, url stays null
		}
		return urls;
	}

	/**
	 * Read the dat file and get the contents
	 * @return byte array containing file contents
	 */
	private static byte[] readDatFile()
	{
		try
		{
			// Need absolute path to dat file
			InputStream in = TileFinder.class.getResourceAsStream("/tim/prune/function/srtm/srtmtiles.dat");
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
		return null;
	}
}
