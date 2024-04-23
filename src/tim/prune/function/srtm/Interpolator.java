package tim.prune.function.srtm;

/**
 * Class responsible for doing the interpolation and void filling
 * once the data has been extracted from the SrtmTile
 */
public class Interpolator
{
	/**
	 * Fix a single void in the given array by replacing it with the average of the others
	 * @param inAltitudes array of altitudes containing one void
	 * @return fixed array without voids
	 */
	public static int[] fixVoid(int[] inAltitudes)
	{
		int[] fixed = new int[inAltitudes.length];
		for (int i = 0; i < inAltitudes.length; i++)
		{
			if (inAltitudes[i] == SrtmSource.VOID_VAL) {
				fixed[i] = (int) Math.round(averageNonVoid(inAltitudes));
			}
			else {
				fixed[i] = inAltitudes[i];
			}
		}
		return fixed;
	}

	/**
	 * Calculate the average of the non-void altitudes in the given array
	 * @param inAltitudes array of altitudes with one or more voids
	 * @return average of non-void altitudes
	 */
	static double averageNonVoid(int[] inAltitudes)
	{
		double totalAltitude = 0.0;
		int numAlts = 0;
		for (int altitude : inAltitudes)
		{
			if (altitude != SrtmSource.VOID_VAL)
			{
				totalAltitude += altitude;
				numAlts++;
			}
		}
		if (numAlts < 1) {return SrtmSource.VOID_VAL;}
		return totalAltitude / numAlts;
	}


	/**
	 * Perform a bilinear interpolation on the given altitude array
	 * @param inAltitudes array of four altitude values on corners of square (bl, br, tl, tr)
	 * @param inX x coordinate from 0 (left) to 1 (right)
	 * @param inY y coordinate from 0 (top) to 1 (bottom)
	 * @return interpolated altitude
	 */
	public static double bilinearInterpolate(int[] inAltitudes, double inX, double inY)
	{
		double alt = (1-inX)*inY*inAltitudes[0] + inX*inY*inAltitudes[1]
			+ (1-inX)*(1-inY)*inAltitudes[2] + inX*(1-inY)*inAltitudes[3];
		return alt;
	}

	/**
	 * Calculate the altitude for the given point
	 * @param inLongDegrees longitude of point to get the altitude for
	 * @param inLatDegrees latitude of point to get the altitude for
	 * @param inHeights height array from tile
	 * @param inIsNormalTrack true if track is a normal track, false for a terrain track
	 * @param inTilePixelsPerSide number of pixels per side of tile
	 * @return the altitude
	 */
	public static double calculateAltitude(double inLongDegrees, double inLatDegrees,
		int[] inHeights, boolean inIsNormalTrack, int inTilePixelsPerSide)
	{
		final double xFractionDegree = inLongDegrees - Math.floor(inLongDegrees);
		final double yFractionDegree = inLatDegrees - Math.floor(inLatDegrees);
		final double xPixels = xFractionDegree * (inTilePixelsPerSide-1);
		final double yPixelsUp = yFractionDegree * (inTilePixelsPerSide-1);
		final double xFracPixel = xPixels - Math.floor(xPixels);
		final int wholePixelsDown = inTilePixelsPerSide - 2 - (int) Math.floor(yPixelsUp);
		final double yFracPixelDown = 1.0 - (yPixelsUp - Math.floor(yPixelsUp));
		final int topLeftIndex = wholePixelsDown * inTilePixelsPerSide + (int) Math.floor(xPixels);

		int[] fouralts = {inHeights[topLeftIndex + inTilePixelsPerSide],
			inHeights[topLeftIndex + inTilePixelsPerSide + 1],
			inHeights[topLeftIndex], inHeights[topLeftIndex + 1]};
		final int numVoids = (fouralts[0]==SrtmSource.VOID_VAL?1:0) + (fouralts[1]==SrtmSource.VOID_VAL?1:0)
			+ (fouralts[2]==SrtmSource.VOID_VAL?1:0) + (fouralts[3]==SrtmSource.VOID_VAL?1:0);

		double altitude = 0.0;
		switch (numVoids)
		{
			case 0:	altitude = bilinearInterpolate(fouralts, xFracPixel, yFracPixelDown);
				break;
			case 1: altitude = bilinearInterpolate(Interpolator.fixVoid(fouralts), xFracPixel, yFracPixelDown);
				break;
			case 2:
			case 3: altitude = averageNonVoid(fouralts);
				break;
			default: altitude = SrtmSource.VOID_VAL;
				break;
		}
		// Special case for terrain tracks, don't interpolate voids yet
		if (!inIsNormalTrack && numVoids > 0) {
			altitude = SrtmSource.VOID_VAL;
		}
		return altitude;
	}
}
