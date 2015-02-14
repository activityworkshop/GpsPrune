package tim.prune.data;

/**
 * Class to represent a single data point in the series
 * including all its fields
 * Can be either a track point or a waypoint
 */
public class DataPoint
{
	// Hold these as Strings?  Or FieldValue objects?
	private String[] _fieldValues = null;
	// list of fields
	private FieldList _fieldList = null;
	// Special fields for coordinates
	private Coordinate _latitude = null, _longitude = null;
	private Altitude _altitude;
	private Timestamp _timestamp = null;
	private boolean _pointValid = false;


	// TODO: Make it possible to turn track point into waypoint - may need to alter FieldList

	/**
	 * Constructor
	 * @param inValueArray array of String values
	 * @param inFieldList list of fields
	 * @param inAltFormat altitude format
	 */
	public DataPoint(String[] inValueArray, FieldList inFieldList, int inAltFormat)
	{
		// save data
		_fieldValues = inValueArray;
		// save list of fields
		_fieldList = inFieldList;

		// parse fields
		_latitude = new Latitude(getFieldValue(Field.LATITUDE));
		_longitude = new Longitude(getFieldValue(Field.LONGITUDE));
		_altitude = new Altitude(getFieldValue(Field.ALTITUDE), inAltFormat);
		_timestamp = new Timestamp(getFieldValue(Field.TIMESTAMP));
	}


	/**
	 * Private constructor for artificially generated points (eg interpolated)
	 * @param inLatitude latitude
	 * @param inLongitude longitude
	 * @param inAltitude altitude
	 */
	private DataPoint(Coordinate inLatitude, Coordinate inLongitude, Altitude inAltitude)
	{
		// Only these three fields are available
		_fieldValues = new String[0];
		_fieldList = new FieldList();
		_latitude = inLatitude;
		_longitude = inLongitude;
		_altitude = inAltitude;
		_timestamp = new Timestamp(null);
	}


	/**
	 * Get the value for the given field
	 * @param inField field to interrogate
	 * @return value of field
	 */
	public String getFieldValue(Field inField)
	{
		return getFieldValue(_fieldList.getFieldIndex(inField));
	}


	/**
	 * Get the value at the given index
	 * @param inIndex index number starting at zero
	 * @return field value, or null if not found
	 */
	public String getFieldValue(int inIndex)
	{
		if (_fieldValues == null || inIndex < 0 || inIndex >= _fieldValues.length)
			return null;
		return _fieldValues[inIndex];
	}


	public Coordinate getLatitude()
	{
		return _latitude;
	}
	public Coordinate getLongitude()
	{
		return _longitude;
	}
	public boolean hasAltitude()
	{
		return _altitude.isValid();
	}
	public Altitude getAltitude()
	{
		return _altitude;
	}
	public boolean hasTimestamp()
	{
		return _timestamp.isValid();
	}
	public Timestamp getTimestamp()
	{
		return _timestamp;
	}

	/**
	 * @return true if point has a waypoint name
	 */
	public boolean isWaypoint()
	{
		String name = getFieldValue(Field.WAYPT_NAME);
		return (name != null && !name.equals(""));
	}

	/**
	 * Compare two DataPoint objects to see if they
	 * are duplicates
	 * @param inOther other object to compare
	 * @return true if the points are equivalent
	 */
	public boolean isDuplicate(DataPoint inOther)
	{
		if (inOther == null) return false;
		if (_longitude == null || _latitude == null
			|| inOther._longitude == null || inOther._latitude == null)
		{
			return false;
		}
		// Compare latitude and longitude
		if (!_longitude.equals(inOther._longitude) || !_latitude.equals(inOther._latitude))
		{
			return false;
		}
		// Note that conversion from decimal to dms can make non-identical points into duplicates
		// Compare description (if any)
		String name1 = getFieldValue(Field.WAYPT_NAME);
		String name2 = inOther.getFieldValue(Field.WAYPT_NAME);
		if (name1 == null || name1.equals(""))
		{
			return (name2 == null || name2.equals(""));
		}
		else
		{
			return (name2 != null && name2.equals(name1));
		}
	}


	/**
	 * @return true if the point is valid
	 */
	public boolean isValid()
	{
		return _latitude.isValid() && _longitude.isValid();
	}


	/**
	 * Interpolate a set of points between this one and the given one
	 * @param inEndPoint end point of interpolation
	 * @param inNumPoints number of points to generate
	 * @return the DataPoint array
	 */
	public DataPoint[] interpolate(DataPoint inEndPoint, int inNumPoints)
	{
		DataPoint[] range = new DataPoint[inNumPoints];
		Coordinate endLatitude = inEndPoint.getLatitude();
		Coordinate endLongitude = inEndPoint.getLongitude();
		Altitude endAltitude = inEndPoint.getAltitude();

		// Loop over points
		for (int i=0; i<inNumPoints; i++)
		{
			Coordinate latitude = Coordinate.interpolate(_latitude, endLatitude, i, inNumPoints);
			Coordinate longitude = Coordinate.interpolate(_longitude, endLongitude, i, inNumPoints);
			Altitude altitude = Altitude.interpolate(_altitude, endAltitude, i, inNumPoints);
			range[i] = new DataPoint(latitude, longitude, altitude);
		}
		return range;
	}


	/**
	 * Calculate the number of radians between two points (for distance calculation)
	 * @param inPoint1 first point
	 * @param inPoint2 second point
	 * @return angular distance between points in radians
	 */
	public static double calculateRadiansBetween(DataPoint inPoint1, DataPoint inPoint2)
	{
		if (inPoint1 == null || inPoint2 == null)
			return 0.0;
		final double TO_RADIANS = Math.PI / 180.0;
		// Get lat and long from points
		double lat1 = inPoint1.getLatitude().getDouble() * TO_RADIANS;
		double lat2 = inPoint2.getLatitude().getDouble() * TO_RADIANS;
		double lon1 = inPoint1.getLongitude().getDouble() * TO_RADIANS;
		double lon2 = inPoint2.getLongitude().getDouble() * TO_RADIANS;
		// Formula given by Wikipedia:Great-circle_distance as follows:
		// angle = 2 arcsin( sqrt( (sin ((lat2-lat1)/2))^^2 + cos(lat1)cos(lat2)(sin((lon2-lon1)/2))^^2))
		double firstSine = Math.sin((lat2-lat1) / 2.0);
		double secondSine = Math.sin((lon2-lon1) / 2.0);
		double term2 = Math.cos(lat1) * Math.cos(lat2) * secondSine * secondSine;
		double answer = 2 * Math.asin(Math.sqrt(firstSine*firstSine + term2));
		// phew
		return answer;
	}
}
