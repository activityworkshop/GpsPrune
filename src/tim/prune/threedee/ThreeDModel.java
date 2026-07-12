package tim.prune.threedee;

import java.awt.Color;

import tim.prune.data.DataPoint;
import tim.prune.data.PointScaler;
import tim.prune.data.Track;
import tim.prune.gui.colour.PointColourer;

/**
 * Class to hold a 3d model of the track data,
 * including all points and scaling operations.
 * Used by java3d and also Pov export functions
 */
public class ThreeDModel
{
	private final Track _track;
	private Track _terrainTrack = null;
	private PointScaler _scaler = null;
	private PointColourer _pointColourer = null;
	private final double _scaleFactor = 1.0;
	private double _altFactor = 1.0;
	private double _externalScaleFactor = 1.0;
	// MAYBE: How to store rods (lifts) in data?
	private byte[] _pointTypes = null;
	private byte[] _pointColourCodes = null;

	// Constants for point types
	public static final byte POINT_TYPE_WAYPOINT      = 1;
	public static final byte POINT_TYPE_NORMAL_POINT  = 2;
	public static final byte POINT_TYPE_SEGMENT_START = 3;

	private static final int MAX_NUM_DISTINCT_COLOURS = 32;


	/**
	 * Constructor
	 * @param inTrack Track object
	 */
	public ThreeDModel(Track inTrack) {
		_track = inTrack;
	}

	/**
	 * @param inTrack terrain track to set
	 */
	public void setTerrain(Track inTrack) {
		_terrainTrack = inTrack;
	}

	/**
	 * @return the number of points in the model
	 */
	public int getNumPoints() {
		return (_track == null ? 0 : _track.getNumPoints());
	}

	/**
	 * @param inFactor altitude exaggeration factor (default 1.0)
	 */
	public void setAltitudeFactor(double inFactor) {
		_altFactor = inFactor;
	}

	public void setPointColourer(PointColourer inColourer) {
		_pointColourer = inColourer;
	}

	/**
	 * @param inSize size of model
	 */
	public void setModelSize(double inSize) {
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

	/** @return the number of colour used by the colourer */
	public int getNumColourCodes()
	{
		int numColourCodes = _pointColourer.getNumDistinctColours();
		if (numColourCodes <= 0) {
			numColourCodes = MAX_NUM_DISTINCT_COLOURS;
		}
		return numColourCodes;
	}

	/**
	 * Calculate the point types and colour codes
	 */
	private void calculatePointTypes()
	{
		int numPoints = getNumPoints();
		_pointTypes = new byte[numPoints];
		_pointColourCodes = new byte[numPoints];
		int numColourCodes = getNumColourCodes();
		// Loop over points in track
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = _track.getPoint(i);
			_pointTypes[i] = (point.isWaypoint() ? POINT_TYPE_WAYPOINT :
				(point.getSegmentStart() ? POINT_TYPE_SEGMENT_START : POINT_TYPE_NORMAL_POINT));
			_pointColourCodes[i] = (byte) (_pointColourer.getColourIndex(i) * (numColourCodes-1));
		}
	}

	/** @return the point colourer for track points */
	public PointColourer getPointColourer() {
		return _pointColourer;
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
	public byte getPointType(int inIndex) {
		return _pointTypes[inIndex];
	}

	/**
	 * @param inIndex index of point, starting at 0
	 * @return point colour code
	 */
	public byte getPointColourCode(int inIndex) {
		return _pointColourCodes[inIndex];
	}

	/**
	 * @param inIndex index of point, starting at 0
	 * @return point colour
	 */
	public Color getPointColour(int inIndex) {
		return _pointColourer.getColour(inIndex);
	}
}
