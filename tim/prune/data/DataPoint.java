package tim.prune.data;

/**
 * Class to represent a single data point in the series
 * including all its fields
 * Can be either a track point or a waypoint
 */
public class DataPoint
{
	/** Array of Strings holding raw values */
	private String[] _fieldValues = null;
	/** list of field definitions */
	private FieldList _fieldList = null;
	/** Special fields for coordinates */
	private Coordinate _latitude = null, _longitude = null;
	private Altitude _altitude;
	private Timestamp _timestamp = null;
	private Photo _photo = null;
	private String _waypointName = null;
	private boolean _startOfSegment = false;


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
		// parse fields into objects
		parseFields(inAltFormat);
	}


	/**
	 * Parse the string values into objects eg Coordinates
	 * @param inAltFormat altitude format
	 */
	private void parseFields(int inAltFormat)
	{
		_latitude = new Latitude(getFieldValue(Field.LATITUDE));
		_longitude = new Longitude(getFieldValue(Field.LONGITUDE));
		_altitude = new Altitude(getFieldValue(Field.ALTITUDE), inAltFormat);
		_timestamp = new Timestamp(getFieldValue(Field.TIMESTAMP));
		_waypointName = getFieldValue(Field.WAYPT_NAME);
		String segmentStr = getFieldValue(Field.NEW_SEGMENT);
		if (segmentStr != null) {segmentStr = segmentStr.trim();}
		_startOfSegment = (segmentStr != null && (segmentStr.equals("1") || segmentStr.toUpperCase().equals("Y")));
	}


	/**
	 * Constructor for additional points (eg interpolated, photos)
	 * @param inLatitude latitude
	 * @param inLongitude longitude
	 * @param inAltitude altitude
	 */
	public DataPoint(Coordinate inLatitude, Coordinate inLongitude, Altitude inAltitude)
	{
		// Only these three fields are available
		_fieldValues = new String[3];
		Field[] fields = {Field.LATITUDE, Field.LONGITUDE, Field.ALTITUDE};
		_fieldList = new FieldList(fields);
		_latitude = inLatitude;
		_fieldValues[0] = inLatitude.output(Coordinate.FORMAT_DEG_MIN_SEC);
		_longitude = inLongitude;
		_fieldValues[1] = inLongitude.output(Coordinate.FORMAT_DEG_MIN_SEC);
		if (inAltitude == null) {
			_altitude = Altitude.NONE;
		}
		else {
			_altitude = inAltitude;
			_fieldValues[2] = "" + inAltitude.getValue(Altitude.FORMAT_METRES); // units are ignored
		}
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


	/**
	 * Set (or edit) the specified field value
	 * @param inField Field to set
	 * @param inValue value to set
	 */
	public void setFieldValue(Field inField, String inValue)
	{
		// See if this data point already has this field
		int fieldIndex = _fieldList.getFieldIndex(inField);
		// Add to field list if necessary
		if (fieldIndex < 0)
		{
			// If value is empty & field doesn't exist then do nothing
			if (inValue == null || inValue.equals(""))
			{
				return;
			}
			// value isn't empty so extend field list
			fieldIndex = _fieldList.extendList(inField);
		}
		// Extend array of field values if necessary
		if (fieldIndex >= _fieldValues.length)
		{
			resizeValueArray(fieldIndex);
		}
		// Set field value in array
		_fieldValues[fieldIndex] = inValue;
		// Change Coordinate, Altitude, Name or Timestamp fields after edit
		if (_altitude != null)
		{
			parseFields(_altitude.getFormat());
		}
		else
		{
			// use default altitude format of metres
			parseFields(Altitude.FORMAT_METRES);
		}
	}

	/** @param inFlag true for start of track segment */
	public void setSegmentStart(boolean inFlag)
	{
		setFieldValue(Field.NEW_SEGMENT, inFlag?"1":null);
	}

	/** @return latitude */
	public Coordinate getLatitude()
	{
		return _latitude;
	}
	/** @return longitude */
	public Coordinate getLongitude()
	{
		return _longitude;
	}
	/** @return true if point has altitude */
	public boolean hasAltitude()
	{
		return _altitude.isValid();
	}
	/** @return altitude */
	public Altitude getAltitude()
	{
		return _altitude;
	}
	/** @return true if point has timestamp */
	public boolean hasTimestamp()
	{
		return _timestamp.isValid();
	}
	/** @return timestamp */
	public Timestamp getTimestamp()
	{
		return _timestamp;
	}
	/** @return waypoint name, if any */
	public String getWaypointName()
	{
		return _waypointName;
	}

	/** @return true if start of new track segment */
	public boolean getSegmentStart()
	{
		return _startOfSegment;
	}

	/**
	 * @return true if point has a waypoint name
	 */
	public boolean isWaypoint()
	{
		return (_waypointName != null && !_waypointName.equals(""));
	}


	/**
	 * Compare two DataPoint objects to see if they are duplicates
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
		// Make sure photo points aren't specified as duplicates
		if (_photo != null) return false;
		// Compare latitude and longitude
		if (!_longitude.equals(inOther._longitude) || !_latitude.equals(inOther._latitude))
		{
			return false;
		}
		// Note that conversion from decimal to dms can make non-identical points into duplicates
		// Compare waypoint name (if any)
		if (!isWaypoint())
		{
			return !inOther.isWaypoint();
		}
		return (inOther._waypointName != null && inOther._waypointName.equals(_waypointName));
	}


	/**
	 * Set the photo for this data point
	 * @param inPhoto Photo object
	 */
	public void setPhoto(Photo inPhoto)
	{
		_photo = inPhoto;
	}


	/**
	 * @return associated Photo object
	 */
	public Photo getPhoto()
	{
		return _photo;
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
		// Loop over points
		for (int i=0; i<inNumPoints; i++)
		{
			Coordinate latitude = Coordinate.interpolate(_latitude, inEndPoint.getLatitude(), i, inNumPoints);
			Coordinate longitude = Coordinate.interpolate(_longitude, inEndPoint.getLongitude(), i, inNumPoints);
			Altitude altitude = Altitude.interpolate(_altitude, inEndPoint.getAltitude(), i, inNumPoints);
			range[i] = new DataPoint(latitude, longitude, altitude);
		}
		return range;
	}

	/**
	 * Interpolate between the two given points
	 * @param inStartPoint start point
	 * @param inEndPoint end point
	 * @param inFrac fractional distance from first point (0.0 to 1.0)
	 * @return new DataPoint object between two given ones
	 */
	public static DataPoint interpolate(DataPoint inStartPoint, DataPoint inEndPoint, double inFrac)
	{
		if (inStartPoint == null || inEndPoint == null) {return null;}
		return new DataPoint(
			Coordinate.interpolate(inStartPoint.getLatitude(), inEndPoint.getLatitude(), inFrac),
			Coordinate.interpolate(inStartPoint.getLongitude(), inEndPoint.getLongitude(), inFrac),
			Altitude.interpolate(inStartPoint.getAltitude(), inEndPoint.getAltitude(), inFrac)
		);
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


	/**
	 * Resize the value array
	 * @param inNewIndex new index to allow
	 */
	private void resizeValueArray(int inNewIndex)
	{
		int newSize = inNewIndex + 1;
		if (newSize > _fieldValues.length)
		{
			String[] newArray = new String[newSize];
			System.arraycopy(_fieldValues, 0, newArray, 0, _fieldValues.length);
			_fieldValues = newArray;
		}
	}


	/**
	 * @return a clone object with copied data
	 */
	public DataPoint clonePoint()
	{
		// Copy all values
		String[] valuesCopy = new String[_fieldValues.length];
		System.arraycopy(_fieldValues, 0, valuesCopy, 0, _fieldValues.length);
		// Make new object to hold cloned data
		DataPoint point = new DataPoint(valuesCopy, _fieldList, _altitude.getFormat());
		return point;
	}


	/**
	 * Restore the contents from another point
	 * @param inOther point containing contents to copy
	 * @return true if successful
	 */
	public boolean restoreContents(DataPoint inOther)
	{
		if (inOther != null)
		{
			// Copy string values across
			_fieldValues = inOther._fieldValues;
			if (_altitude != null)
			{
				parseFields(_altitude.getFormat());
			}
			else
			{
				// use default altitude format of metres
				parseFields(Altitude.FORMAT_METRES);
			}
			return true;
		}
		return false;
	}


	/**
	 * Get string for debug
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "[Lat=" + getLatitude().toString() + ", Lon=" + getLongitude().toString() + "]";
	}
}
