package tim.prune.threedee;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.vecmath.Point3d;
import javax.vecmath.TexCoord2f;

import tim.prune.data.Altitude;
import tim.prune.data.Coordinate;
import tim.prune.data.DataPoint;
import tim.prune.data.DoubleRange;
import tim.prune.data.Field;
import tim.prune.data.FieldList;
import tim.prune.data.Latitude;
import tim.prune.data.Longitude;
import tim.prune.data.Track;
import tim.prune.data.TrackExtents;
import tim.prune.data.UnitSetLibrary;
import tim.prune.gui.map.MapUtils;

/**
 * Helper for generating the arrays needed for the 3d terrain
 */
public class TerrainHelper
{
	/** Number of nodes on each side of the square grid */
	private int _gridSize = 0;

	/**
	 * Constructor
	 * @param inGridSize grid size
	 */
	public TerrainHelper(int inGridSize) {
		_gridSize = inGridSize;
	}

	/**
	 * @return grid size
	 */
	public int getGridSize() {
		return _gridSize;
	}


	/**
	 * Convert the terrain coordinates from raw form to TriangleStripArray form
	 * (with repeated nodes)
	 * @param inRawPoints array of raw points as formed from the track
	 * @return point coordinates as array
	 */
	public Point3d[] getTerrainCoordinates(Point3d[] inRawPoints)
	{
		final int numNodes = _gridSize * _gridSize;
		if (_gridSize <= 1 || inRawPoints == null || inRawPoints.length != numNodes) {return null;}
		// Put these nodes into a new result array (repeating nodes as necessary)
		final int resultSize = _gridSize * (_gridSize * 2 - 2);
		Point3d[] result = new Point3d[resultSize];
		final int numStrips = _gridSize - 1;
		int resultIndex = 0;
		for (int strip=0; strip<numStrips; strip++)
		{
			for (int col=0; col<_gridSize; col++)
			{
				int bottomNodeIndex = strip * _gridSize + col;
				int topNodeIndex = bottomNodeIndex + _gridSize;
				result[resultIndex++] = inRawPoints[bottomNodeIndex];
				result[resultIndex++] = inRawPoints[topNodeIndex];
			}
		}
		return result;
	}


	/**
	 * Get the texture coordinates as an array
	 * @return texture coordinates as array
	 */
	public TexCoord2f[] getTextureCoordinates()
	{
		if (_gridSize <= 1) {return null;}
		final int numNodes = _gridSize * _gridSize;
		final float gridStep = 1.0f / (_gridSize - 1);
		// Build all the required nodes
		TexCoord2f[] nodes = new TexCoord2f[numNodes];
		for (int i=0; i<_gridSize; i++)
		{
			for (int j=0; j<_gridSize; j++)
			{
				nodes[j * _gridSize + i] = new TexCoord2f(gridStep * i, 1.0f - gridStep * j);
			}
		}
		// Now put these nodes into a new result array (repeating nodes as necessary)
		final int resultSize = _gridSize * (_gridSize * 2 - 2);
		TexCoord2f[] result = new TexCoord2f[resultSize];
		final int numStrips = _gridSize - 1;
		int resultIndex = 0;
		for (int strip=0; strip<numStrips; strip++)
		{
			for (int col=0; col<_gridSize; col++)
			{
				int bottomNodeIndex = strip * _gridSize + col;
				int topNodeIndex = bottomNodeIndex + _gridSize;
				result[resultIndex++] = nodes[bottomNodeIndex];
				result[resultIndex++] = nodes[topNodeIndex];
			}
		}
		return result;
	}

	/**
	 * @return strip lengths as array
	 */
	public int[] getStripLengths()
	{
		final int numStrips = _gridSize - 1;
		final int nodesPerStrip = _gridSize * 2;
		int[] result = new int[numStrips];
		for (int i=0; i<numStrips; i++) {
			result[i] = nodesPerStrip;
		}
		return result;
	}

	/**
	 * Create a grid of points in a new Track
	 * @param inDataTrack track from which the extents should be obtained
	 * @return Track containing all the points in the grid
	 */
	public Track createGridTrack(Track inDataTrack)
	{
		// Work out the size of the current track
		TrackExtents extents = new TrackExtents(inDataTrack);
		extents.applySquareBorder();
		DoubleRange xRange = extents.getXRange();
		DoubleRange yRange = extents.getYRange();
		// Create the array of points
		final int numPoints = _gridSize * _gridSize;
		final double xStep = xRange.getRange() / (_gridSize - 1);
		final double yStep = yRange.getRange() / (_gridSize - 1);
		DataPoint[] points = new DataPoint[numPoints];
		for (int i=0; i<_gridSize; i++)
		{
			double pY = yRange.getMinimum() + i * yStep;
			for (int j=0; j<_gridSize; j++)
			{
				// Create a new point with the appropriate lat and long, with no altitude
				double pX = xRange.getMinimum() + j * xStep;
				DataPoint point = new DataPoint(
					new Latitude(MapUtils.getLatitudeFromY(pY), Coordinate.FORMAT_DECIMAL_FORCE_POINT),
					new Longitude(MapUtils.getLongitudeFromX(pX), Coordinate.FORMAT_DECIMAL_FORCE_POINT),
					null);
				//System.out.println("Created point at " + point.getLatitude().output(Coordinate.FORMAT_DEG_MIN_SEC)
				//	+ ", " + point.getLongitude().output(Coordinate.FORMAT_DEG_MIN_SEC));
				points[i * _gridSize + j] = point;
			}
		}
		// Put these into a new track
		Field[] fields = {Field.LATITUDE, Field.LONGITUDE, Field.ALTITUDE};
		Track grid = new Track(new FieldList(fields), points);
		return grid;
	}

	/**
	 * Write the given terrain track out to an indexed png file
	 * @param inModel three-d data model with terrain
	 * @param inPngFile file to write to
	 */
	public void writeHeightMap(ThreeDModel inModel, File inPngFile)
	{
		BufferedImage image = new BufferedImage(_gridSize, _gridSize, BufferedImage.TYPE_BYTE_INDEXED);
		for (int y=0; y<_gridSize; y++)
		{
			for (int x=0; x<_gridSize; x++)
			{
				double heightValue = inModel.getScaledTerrainValue(y * _gridSize + x) * 256;
				// Need to ask colour model what rgb to use for this index (a little round-the-houses)
				image.setRGB(x, y, image.getColorModel().getRGB((int) heightValue));
			}
		}
		try
		{
			ImageIO.write(image, "PNG", inPngFile);
		}
		catch (IOException ioe) {System.err.println(ioe.getClass().getName() + " - " + ioe.getMessage());}
	}


	/**
	 * Try to fix the voids in the given terrain track by averaging neighbour values where possible
	 * @param inTerrainTrack terrain track to fix
	 */
	public void fixVoids(Track inTerrainTrack)
	{
		int numVoids = countVoids(inTerrainTrack);
		if (numVoids == 0) {return;}
		//System.out.println("Starting to fix, num voids = " + numVoids);
		// Fix the holes which are surrounded on all four sides by non-holes
		fixSingleHoles(inTerrainTrack);
		//System.out.println("Fixed single holes, now num voids = " + countVoids(inTerrainTrack));
		// Maybe there is something to do in the corners?
		fixCornersAndEdges(inTerrainTrack);
		//System.out.println("Fixed corners, now num voids = " + countVoids(inTerrainTrack));
		// Now fix the bigger holes, which should fix everything left
		fixBiggerHoles(inTerrainTrack);
		final int numHolesLeft = countVoids(inTerrainTrack);
		if (numHolesLeft > 0) {
			System.out.println("Fixed bigger holes, now num voids = " + countVoids(inTerrainTrack));
		}
	}

	/**
	 * @param inTerrainTrack terrain track
	 * @return number of voids (points without altitudes)
	 */
	private static int countVoids(Track inTerrainTrack)
	{
		// DEBUG: Show state of voids first
//		final int gridSize = (int) Math.sqrt(inTerrainTrack.getNumPoints());
//		StringBuilder sb = new StringBuilder();
//		for (int i=0; i<inTerrainTrack.getNumPoints(); i++)
//		{
//			if ((i%gridSize) == 0) sb.append('\n');
//			if (inTerrainTrack.getPoint(i).hasAltitude()) {
//				sb.append('A');
//			} else {
//				sb.append(' ');
//			}
//		}
//		System.out.println("Voids:" + sb.toString());
		// END DEBUG

		int numVoids = 0;
		if (inTerrainTrack != null)
		{
			for (int i=0; i<inTerrainTrack.getNumPoints(); i++) {
				if (!inTerrainTrack.getPoint(i).hasAltitude()) {
					numVoids++;
				}
			}
		}
		return numVoids;
	}

	/**
	 * Just deal with single holes surrounded by at least four direct neighbours
	 * @param inTerrainTrack terrain track to fix
	 */
	private void fixSingleHoles(Track inTerrainTrack)
	{
		// Holes with neighbours in all directions
		final int startIndex = 1, endIndex = _gridSize - 2;
		for (int x = startIndex; x <= endIndex; x++)
		{
			for (int y = startIndex; y <= endIndex; y++)
			{
				int pIndex = x * _gridSize + y;
				// Get the point and its neighbours
				final DataPoint p = inTerrainTrack.getPoint(pIndex);
				if (!p.hasAltitude())
				{
					final DataPoint pl = inTerrainTrack.getPoint(pIndex - 1);
					final DataPoint pr = inTerrainTrack.getPoint(pIndex + 1);
					final DataPoint pu = inTerrainTrack.getPoint(pIndex + _gridSize);
					final DataPoint pd = inTerrainTrack.getPoint(pIndex - _gridSize);
					// Check if the points are null??
					if (pl == null || pr == null || pu == null || pd == null)
					{
						System.err.println("Woah. Got a null point in fixSingleHoles. x=" + x + ", y=" + y + ", grid=" + _gridSize);
						System.err.println("index=" + pIndex);
						if (pl == null) System.err.println("pl is null");
						if (pr == null) System.err.println("pr is null");
						if (pu == null) System.err.println("pu is null");
						if (pd == null) System.err.println("pd is null");
						continue;
					}
					// Check that all the neighbours have altitudes
					if (pl.hasAltitude() && pr.hasAltitude() && pu.hasAltitude() && pd.hasAltitude())
					{
						// Now check the double-neighbours
						final DataPoint pll = inTerrainTrack.getPoint(pIndex - 2);
						final DataPoint prr = inTerrainTrack.getPoint(pIndex + 2);
						final DataPoint puu = inTerrainTrack.getPoint(pIndex + 2 * _gridSize);
						final DataPoint pdd = inTerrainTrack.getPoint(pIndex - 2 * _gridSize);

						double altitude = 0.0;
						if (pll != null && pll.hasAltitude() && prr != null && prr.hasAltitude()
							&& puu != null && puu.hasAltitude() && pdd != null && pdd.hasAltitude())
						{
							// Use the double-neighbours too to take into account the gradients
							altitude = (
								  pl.getAltitude().getMetricValue() * 1.5
								- pll.getAltitude().getMetricValue() * 0.5
								+ pr.getAltitude().getMetricValue() * 1.5
								- prr.getAltitude().getMetricValue() * 0.5
								+ pd.getAltitude().getMetricValue() * 1.5
								- pdd.getAltitude().getMetricValue() * 0.5
								+ pu.getAltitude().getMetricValue() * 1.5
								- puu.getAltitude().getMetricValue() * 0.5) / 4.0;
						}
						else
						{
							// no double-neighbours, just use neighbours
							altitude = (
								  pl.getAltitude().getMetricValue()
								+ pr.getAltitude().getMetricValue()
								+ pd.getAltitude().getMetricValue()
								+ pu.getAltitude().getMetricValue()) / 4.0;
						}
						// Set this altitude in the point
						p.setFieldValue(Field.ALTITUDE, "" + altitude, false);
						// force value to metres
						p.getAltitude().reset(new Altitude((int) altitude, UnitSetLibrary.UNITS_METRES));
					}
				}
			}
		}
	}

	/**
	 * Try to fix the corners and edges, if they're blank
	 * @param inTerrainTrack terrain track
	 */
	private void fixCornersAndEdges(Track inTerrainTrack)
	{
		fixCorner(inTerrainTrack, 0, 1, 1);
		fixCorner(inTerrainTrack, _gridSize-1, -1, 1);
		fixCorner(inTerrainTrack, (_gridSize-1)*_gridSize, 1, -1);
		fixCorner(inTerrainTrack, _gridSize*_gridSize-1, -1, -1);
		fixEdge(inTerrainTrack, 0, 1);
		fixEdge(inTerrainTrack, _gridSize-1, _gridSize);
		fixEdge(inTerrainTrack, (_gridSize-1)*_gridSize, -_gridSize);
		fixEdge(inTerrainTrack, _gridSize*_gridSize-1, -1);
	}

	/**
	 * Fix a single corner by searching along adjacent edges and averaging the nearest neighbours
	 * @param inTerrainTrack terrain track
	 * @param inCornerIndex index of corner to fill
	 * @param inXinc increment in x direction (+1 or -1)
	 * @param inYinc increment in y direction (+1 or -1)
	 */
	private void fixCorner(Track inTerrainTrack, int inCornerIndex, int inXinc, int inYinc)
	{
		DataPoint corner = inTerrainTrack.getPoint(inCornerIndex);
		if (corner == null || corner.hasAltitude()) {return;}
		// Corner hasn't got an altitude, we'll have to look for it
		int sIndex1 = inCornerIndex, sIndex2 = inCornerIndex;
		Altitude alt1 = null, alt2 = null;

		for (int i=1; i<_gridSize && !corner.hasAltitude(); i++)
		{
			sIndex1 += inXinc;
			sIndex2 += (inYinc * _gridSize);
			// System.out.println("To fill corner " + inCornerIndex + ", looking at indexes " + sIndex1 + " and " + sIndex2);
			if (alt1 == null)
			{
				DataPoint source1 = inTerrainTrack.getPoint(sIndex1);
				if (source1 != null && source1.hasAltitude()) {alt1 = source1.getAltitude();}
			}
			if (alt2 == null)
			{
				DataPoint source2 = inTerrainTrack.getPoint(sIndex2);
				if (source2 != null && source2.hasAltitude()) {alt2 = source2.getAltitude();}
			}
			// Can we average these?
			if (alt1 != null && alt2 != null)
			{
				// System.out.println("Averaging values " + alt1.getMetricValue() + " and " + alt2.getMetricValue());
				int newAltitude = (int) ((alt1.getMetricValue() + alt2.getMetricValue()) / 2.0);
				corner.setFieldValue(Field.ALTITUDE, "" + newAltitude, false);
				// TODO: Check forcing metres?  Is there a nicer way?
			}
		}
	}

	/**
	 * Fix any holes found in the specified edge
	 * @param inTerrainTrack terrain track
	 * @param inCornerIndex index of corner to start from
	 * @param inInc increment along edge
	 */
	private void fixEdge(Track inTerrainTrack, int inCornerIndex, int inInc)
	{
		int prevIndexWithAlt = -1;
		int sIndex = inCornerIndex;
		if (inTerrainTrack.getPoint(sIndex).hasAltitude()) {prevIndexWithAlt = 0;}
		for (int i=1; i<_gridSize; i++)
		{
			sIndex += inInc;
			if (inTerrainTrack.getPoint(sIndex).hasAltitude())
			{
				if (prevIndexWithAlt >= 0 && prevIndexWithAlt < (i-1))
				{
					final int gapLen = i - prevIndexWithAlt;
					final int cellIndex1 = inCornerIndex + prevIndexWithAlt * inInc;
					final double alt1 = inTerrainTrack.getPoint(cellIndex1).getAltitude().getMetricValue();
					final int cellIndex2 = inCornerIndex + i * inInc;
					final double alt2 = inTerrainTrack.getPoint(cellIndex2).getAltitude().getMetricValue();
					//System.out.println("Altitude along edge goes from " + alt1 + " (at " + prevIndexWithAlt + ") to " +
					//		alt2 + " (at " + i + ")");
					for (int j = 1; j < gapLen; j++)
					{
						final double alt = alt1 + (alt2-alt1) * j / gapLen;
						//System.out.println("Fill in " + (prevIndexWithAlt + j) + "(" + (inCornerIndex + (prevIndexWithAlt + j) * inInc) + ")  with alt " + (int) alt);
						final DataPoint p = inTerrainTrack.getPoint(inCornerIndex + (prevIndexWithAlt + j) * inInc);
						p.setFieldValue(Field.ALTITUDE, "" + (int) alt, false);
						// TODO: Check forcing metres?
					}
				}
				prevIndexWithAlt = i;
			}
		}
	}

	/**
	 * Try to fix bigger holes by interpolating between neighbours
	 * @param inTerrainTrack terrain track
	 */
	private void fixBiggerHoles(Track inTerrainTrack)
	{
		TerrainPatch patch = new TerrainPatch(_gridSize);
		for (int i=0; i<_gridSize; i++)
		{
			int prevHoriz = -1, prevVert = -1;
			for (int j=0; j<_gridSize; j++)
			{
				if (inTerrainTrack.getPoint(i * _gridSize + j).hasAltitude())
				{
					if (prevHoriz > -1 && prevHoriz != (j-1))
					{
						//System.out.println("Found a gap for y=" + i +" between x=" + prevHoriz + " and " + j + " (" + (j-prevHoriz-1) + ")");
						double startVal = inTerrainTrack.getPoint(i * _gridSize + prevHoriz).getAltitude().getMetricValue();
						double endVal   = inTerrainTrack.getPoint(i * _gridSize + j).getAltitude().getMetricValue();
						for (int k=prevHoriz + 1; k< j; k++)
						{
							double val = startVal + (k-prevHoriz) * (endVal-startVal) / (j-prevHoriz);
							patch.addAltitude(i * _gridSize + k, val, k-prevHoriz, j-prevHoriz);
						}
					}
					prevHoriz = j;
				}
				if (inTerrainTrack.getPoint(j * _gridSize + i).hasAltitude())
				{
					if (prevVert > -1 && prevVert != (j-1))
					{
						//System.out.println("Found a gap for x=" + i +" between y=" + prevVert + " and " + j + " (" + (j-prevVert-1) + ")");
						double startVal = inTerrainTrack.getPoint(prevVert * _gridSize + i).getAltitude().getMetricValue();
						double endVal   = inTerrainTrack.getPoint(j * _gridSize + i).getAltitude().getMetricValue();
						for (int k=prevVert + 1; k< j; k++)
						{
							double val = startVal + (k-prevVert) * (endVal-startVal) / (j-prevVert);
							patch.addAltitude(k * _gridSize + i, val, k-prevVert, j-prevVert);
						}
					}
					prevVert = j;
				}
			}
		}
		// Smooth the patch to reduce the blocky effect from the voids
		patch.smooth();

		// Now the doubles have been set and averaged, we can set the values in the points
		for (int i=0; i<inTerrainTrack.getNumPoints(); i++)
		{
			DataPoint p = inTerrainTrack.getPoint(i);
			if (!p.hasAltitude())
			{
				final double altitude = patch.getAltitude(i);
				p.setFieldValue(Field.ALTITUDE, "" + altitude, false);
				p.getAltitude().reset(new Altitude((int) altitude, UnitSetLibrary.UNITS_METRES));
			}
		}
	}
}
