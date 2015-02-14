package tim.prune.function.srtm.gen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

/**
 * Class used to generate a lookup file to find the URLs of SRTM tiles.
 * The tiles are split into directories for each continent, so we need some way
 * of going from coordinates to directories.
 * This class reads the directory listings from the files tiles1.txt, tiles2.txt etc
 * and combines the result into a binary array
 */
public class GenerateTileLookup
{

	/**
	 * Main method for generating the array
	 * @param args ignored
	 */
	public static void main(String[] args)
	{
		System.out.println("Generate tile lookup");
		byte[] lookup = new byte[360 * 120]; // +/- 180 degrees longitude, +/- 60 degrees latitude
		for (int f=1; f<= 6; f++)
		{
			try
			{
				BufferedReader r = new BufferedReader(new FileReader(new File("tim/prune/function/srtm/gen/tiles" + f + ".txt")));
				String line = r.readLine();
				System.out.println("Read continent: '" + line + "'");
				while ((line = r.readLine()) != null) {
					if (line.length() == 7)
					{
						boolean north = (line.charAt(0) == 'N');
						int lat = Integer.parseInt(line.substring(1, 3));
						if (!north) {lat = -lat;}
						boolean east = (line.charAt(3) == 'E');
						int lon = Integer.parseInt(line.substring(4));
						if (!east) {lon = -lon;}
						// Store in lookup
						int arrindex = (lat+59)*360 + (lon+180);
						lookup[arrindex] = (byte) f;
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		// Now f should be populated
		StringBuilder b = new StringBuilder();
		for (int l=-180; l<180; l++) {
			int i = 59 * 360 + (l+180);
			b.append("" + lookup[i]);
		}
		System.out.println("equator: " + b.toString());

		// Write bytes to file
		try {
			FileOutputStream out = new FileOutputStream(new File("srtmtiles.dat"));
			out.write(lookup);
			out.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
