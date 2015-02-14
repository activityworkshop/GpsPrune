package tim.prune.threedee;

import tim.prune.data.Altitude;
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
	private PointScaler _scaler = null;
	private double _modelSize;
	private int _altitudeCap = -1;
	private double _scaleFactor = 1.0;
	private double _altFactor = 1.0;
	// TODO: How to store rods (lifts) in data?
	private byte[] _pointTypes = null;
	private byte[] _pointHeights = null;

	private static final double DEFAULT_MODEL_SIZE = 10.0;
	public static final int MINIMUM_ALTITUDE_CAP = 100;

	// Constants for point types
	public static final byte POINT_TYPE_WAYPOINT = 1;
	public static final byte POINT_TYPE_NORMAL_POINT = 2;


	/**
	 * Constructor
	 * @param inTrack Track object
	 */
	public ThreeDModel(Track inTrack)
	{
		this(inTrack, DEFAULT_MODEL_SIZE);
	}


	/**
	 * Constructor
	 * @param inTrack Track object
	 * @param inSize model size
	 */
	public ThreeDModel(Track inTrack, double inSize)
	{
		_track = inTrack;
		_modelSize = inSize;
		if (_modelSize <= 0.0) _modelSize = DEFAULT_MODEL_SIZE;
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
	 * Set the altitude cap
	 * @param inAltitudeCap altitude range to cap to (ignored if less than data range)
	 */
	public void setAltitudeCap(int inAltitudeCap)
	{
		_altitudeCap = inAltitudeCap;
		if (_altitudeCap < MINIMUM_ALTITUDE_CAP)
		{
			_altitudeCap = MINIMUM_ALTITUDE_CAP;
		}
	}


	/**
	 * Scale all points and calculate factors
	 */
	public void scale()
	{
		// Use PointScaler to sort out x and y values
		_scaler = new PointScaler(_track);
		_scaler.scale();
		// Calculate scale factor to fit within box
		_scaleFactor = 1.0;
		if (_scaler.getMaximumHoriz() > 0.0 || _scaler.getMaximumVert() > 0.0)
		{
			if (_scaler.getMaximumHoriz() > _scaler.getMaximumVert())
			{
				// scale limited by longitude
				_scaleFactor = _modelSize / _scaler.getMaximumHoriz();
			}
			else
			{
				// scale limited by latitude
				_scaleFactor = _modelSize / _scaler.getMaximumVert();
			}
		}
		// calculate altitude scale factor
		_altFactor = 1.0;
		if (_scaler.getMaximumAlt() >= 0)
		{
			// limit by altitude cap or by data range?
			if (_scaler.getMaximumAlt() > _altitudeCap)
			{
				// data is bigger than cap
				_altFactor = _modelSize / _scaler.getMaximumAlt();
			}
			else
			{
				// capped
				_altFactor = _modelSize / _altitudeCap;
			}
		}
		// calculate lat/long lines
		_scaler.calculateLatLongLines();

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
			_pointTypes[i] = (point.isWaypoint()?POINT_TYPE_WAYPOINT:POINT_TYPE_NORMAL_POINT);
			_pointHeights[i] = (byte) (point.getAltitude().getValue(Altitude.FORMAT_METRES) / 500);
		}
	}


	/**
	 * Get the scaled horizontal value for the specified point
	 * @param inIndex index of point
	 * @return scaled horizontal value
	 */
	public double getScaledHorizValue(int inIndex)
	{
		return _scaler.getHorizValue(inIndex) * _scaleFactor;
	}

	/**
	 * Get the scaled vertical value for the specified point
	 * @param inIndex index of point
	 * @return scaled vertical value
	 */
	public double getScaledVertValue(int inIndex)
	{
		return _scaler.getVertValue(inIndex) * _scaleFactor;
	}

	/**
	 * Get the scaled altitude value for the specified point
	 * @param inIndex index of point
	 * @return scaled altitude value
	 */
	public double getScaledAltValue(int inIndex)
	{
		// if no altitude, just return 0
		int altVal = _scaler.getAltValue(inIndex);
		if (altVal < 0) return 0;
		// scale according to altitude cap
		return altVal * _altFactor;
	}


	/**
	 * @return number of latitude lines
	 */
	public int getNumLatitudeLines()
	{
		return _scaler.getLatitudeLines().length;
	}

	/**
	 * @param inIndex index of line, starting at 0
	 * @return scaled position of latitude line
	 */
	public double getScaledLatitudeLine(int inIndex)
	{
		return _scaler.getScaledLatitudeLines()[inIndex] * _scaleFactor;
	}

	/**
	 * @return number of longitude lines
	 */
	public int getNumLongitudeLines()
	{
		return _scaler.getLongitudeLines().length;
	}

	/**
	 * @param inIndex index of line, starting at 0
	 * @return scaled position of longitude line
	 */
	public double getScaledLongitudeLine(int inIndex)
	{
		return _scaler.getScaledLongitudeLines()[inIndex] * _scaleFactor;
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

	/**
	 * @return the current model size
	 */
	public double getModelSize()
	{
		return _modelSize;
	}
}
