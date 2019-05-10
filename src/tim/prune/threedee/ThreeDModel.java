package tim.prune.threedee;

import tim.prune.data.DataPoint;
import tim.prune.data.PointScaler;
import tim.prune.data.Track;

/**
 * Class to hold a 3d model of the track data,
 * including all points and scaling operations.
 * Used by java3d and also Pov export functions
 */
public class ThreeDModel
{
	private Track _track = null;
	private Track _terrainTrack = null;
	private PointScaler _scaler = null;
	private double _scaleFactor = 1.0;
	private double _altFactor = 1.0;
	private double _externalScaleFactor = 1.0;
	// MAYBE: How to store rods (lifts) in data?
	private byte[] _pointTypes = null;
	private byte[] _pointHeights = null;

	// Constants for point types
	public static final byte POINT_TYPE_WAYPOINT      = 1;
	public static final byte POINT_TYPE_NORMAL_POINT  = 2;
	public static final byte POINT_TYPE_SEGMENT_START = 3;


	/**
	 * Constructor
	 * @param inTrack Track object
	 */
	public ThreeDModel(Track inTrack)
	{
		_track = inTrack;
	}


	/**
	 * @param inTrack terrain track to set
	 */
	public void setTerrain(Track inTrack)
	{
		_terrainTrack = inTrack;
	}

	/**
	 * @return the number of points in the model
	 */
	public int getNumPoints()
	{
		if (_track == null) return 0;
		return _track.getNumPoints();
	}

	/**
	 * @param inFactor altitude exaggeration factor (default 1.0)
	 */
	public void setAltitudeFactor(double inFactor)
	{
		_altFactor = inFactor;
	}

	/**
	 * @param inSize size of model
	 */
	public void setModelSize(double inSize)
	{
		_externalScaleFactor = inSize;
	}

	/**
	 * Scale all points and calculate factors
	 */
	public void scale()
	{
		// Use PointScaler to sort out x and y values
		_scaler = new PointScaler(_track);
		_scaler.addTerrain(_terrainTrack);
		_scaler.scale(); // Add 10% border

		// cap altitude scale factor if it's too big
		double maxAlt = _scaler.getAltitudeRange() * _altFactor;
		if (maxAlt > 0.5)
		{
			// capped
			// System.out.println("Capped alt factor from " + _altFactor + " to " + (_altFactor * 0.5 / maxAlt));
			_altFactor = _altFactor * 0.5 / maxAlt;
		}

		// calculate point types and height codes
		calculatePointTypes();
	}


	/**
	 * Calculate the point types and height codes
	 */
	private void calculatePointTypes()
	{
		int numPoints = getNumPoints();
		_pointTypes = new byte[numPoints];
		_pointHeights = new byte[numPoints];
		// Loop over points in track
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = _track.getPoint(i);
			_pointTypes[i] = (point.isWaypoint()?POINT_TYPE_WAYPOINT:
				(point.getSegmentStart()?POINT_TYPE_SEGMENT_START:POINT_TYPE_NORMAL_POINT));
			_pointHeights[i] = (byte) (point.getAltitude().getMetricValue() / 500);
		}
	}


	/**
	 * Get the scaled horizontal value for the specified point
	 * @param inIndex index of point
	 * @return scaled horizontal value
	 */
	public double getScaledHorizValue(int inIndex)
	{
		return _scaler.getHorizValue(inIndex) * _scaleFactor * _externalScaleFactor;
	}

	/**
	 * Get the scaled vertical value for the specified point
	 * @param inIndex index of point
	 * @return scaled vertical value
	 */
	public double getScaledVertValue(int inIndex)
	{
		return _scaler.getVertValue(inIndex) * _scaleFactor * _externalScaleFactor;
	}

	/**
	 * Get the scaled altitude value for the specified point
	 * @param inIndex index of point
	 * @return scaled altitude value
	 */
	public double getScaledAltValue(int inIndex)
	{
		// if no altitude, just return 0
		double altVal = _scaler.getAltValue(inIndex);
		if (altVal <= 0.0) return 0.0;
		// scale according to exaggeration factor
		return altVal * _altFactor * _externalScaleFactor;
	}


	/**
	 * Get the scaled horizontal value for the specified terrain point
	 * @param inIndex index of point
	 * @return scaled horizontal value
	 */
	public double getScaledTerrainHorizValue(int inIndex)
	{
		return _scaler.getTerrainHorizValue(inIndex) * _scaleFactor * _externalScaleFactor;
	}

	/**
	 * Get the scaled vertical value for the specified terrain point
	 * @param inIndex index of point
	 * @return scaled vertical value
	 */
	public double getScaledTerrainVertValue(int inIndex)
	{
		return _scaler.getTerrainVertValue(inIndex) * _scaleFactor * _externalScaleFactor;
	}

	/**
	 * Get the scaled altitude value for the specified terrain point
	 * @param inIndex index of point
	 * @return scaled altitude value
	 */
	public double getScaledTerrainValue(int inIndex)
	{
		// if no altitude, just return 0
		double altVal = _scaler.getTerrainAltValue(inIndex);
		if (altVal <= 0.0) return 0.0;
		// don't scale by scale factor, needs to be unscaled
		return altVal * _altFactor;
	}


	/**
	 * @param inIndex index of point, starting at 0
	 * @return point type, either POINT_TYPE_WAYPOINT or POINT_TYPE_NORMAL_POINT
	 */
	public byte getPointType(int inIndex)
	{
		return _pointTypes[inIndex];
	}

	/**
	 * @param inIndex index of point, starting at 0
	 * @return point height code
	 */
	public byte getPointHeightCode(int inIndex)
	{
		return _pointHeights[inIndex];
	}
}
