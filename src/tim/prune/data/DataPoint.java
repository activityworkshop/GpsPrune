package tim.prune.data;

/**
 * Class to represent a single data point in the series
 * including all its fields
 * Can be either a track point or a waypoint
 */
public class DataPoint
{
	/** Array of Strings holding raw values */
	private String[] _fieldValues;
	/** list of field definitions */
	private final FieldList _fieldList;
	/** Special fields for coordinates */
	private Coordinate _latitude = null, _longitude = null;
	private Altitude _altitude = null;
	private Speed _hSpeed = null, _vSpeed = null;
	private Timestamp _timestamp = null;
	private SourceInfo _sourceInfo = null;
	private int _originalIndex = -1;
	/** Attached photo */
	private Photo _photo = null;
	/** Attached audio clip */
	private AudioClip _audio = null;
	private String _waypointName = null;
	private boolean _startOfSegment = false;
	private boolean _markedForDeletion = false;
	private int _modifyCount = 0;


	/**
	 * Constructor
	 * @param inValueArray array of String values
	 * @param inFieldList list of fields
	 * @param inOptions creation options such as units
	 */
	public DataPoint(String[] inValueArray, FieldList inFieldList, PointCreateOptions inOptions)
	{
		// save data
		_fieldValues = inValueArray;
		// save list of fields
		_fieldList = inFieldList;
		// Remove double quotes around values
		removeQuotes(_fieldValues);
		// parse fields into objects
		parseFields(null, inOptions);
	}


	/**
	 * Parse the string values into objects eg Coordinates
	 * @param inField field which has changed, or null for all
	 * @param inOptions creation options such as units
	 */
	private void parseFields(Field inField, PointCreateOptions inOptions)
	{
		if (inOptions == null) inOptions = new PointCreateOptions();
		if (inField == null || inField == Field.LATITUDE) {
			_latitude = Latitude.make(getFieldValue(Field.LATITUDE));
		}
		if (inField == null || inField == Field.LONGITUDE) {
			_longitude = Longitude.make(getFieldValue(Field.LONGITUDE));
		}
		if (inField == null || inField == Field.ALTITUDE)
		{
			final Unit altUnit;
			if (_altitude != null && _altitude.getUnit() != null) {
				altUnit = _altitude.getUnit();
			}
			else {
				altUnit = inOptions.getAltitudeUnits();
			}
			_altitude = new Altitude(getFieldValue(Field.ALTITUDE), altUnit);
		}
		if (inField == null || inField == Field.SPEED)
		{
			final Unit speedUnit;
			if (_hSpeed != null && _hSpeed.getUnit() != null) {
				speedUnit = _hSpeed.getUnit();
			}
			else {
				speedUnit = inOptions.getSpeedUnits();
			}
			_hSpeed = new Speed(getFieldValue(Field.SPEED), speedUnit);
		}
		if (inField == null || inField == Field.VERTICAL_SPEED)
		{
			final Unit vspeedUnit;
			final boolean isInverted;
			if (_vSpeed != null && _vSpeed.getUnit() != null)
			{
				vspeedUnit = _vSpeed.getUnit();
				isInverted = _vSpeed.isInverted();
			}
			else
			{
				vspeedUnit = inOptions.getVerticalSpeedUnits();
				isInverted = !inOptions.getVerticalSpeedsUpwards();
			}
			_vSpeed = new Speed(getFieldValue(Field.VERTICAL_SPEED), vspeedUnit, isInverted);
		}
		if (inField == null || inField == Field.TIMESTAMP) {
			_timestamp = new TimestampUtc(getFieldValue(Field.TIMESTAMP));
		}
		if (inField == null || inField == Field.WAYPT_NAME) {
			_waypointName = getFieldValue(Field.WAYPT_NAME);
		}
		if (inField == null || inField == Field.NEW_SEGMENT)
		{
			String segmentStr = getFieldValue(Field.NEW_SEGMENT);
			if (segmentStr != null) {segmentStr = segmentStr.trim();}
			_startOfSegment = (segmentStr != null && (segmentStr.equals("1") || segmentStr.equalsIgnoreCase("Y")));
		}
	}

	/**
	 * Constructor for additional points without altitude
	 * @param inLatitude latitude
	 * @param inLongitude longitude
	 */
	public DataPoint(Coordinate inLatitude, Coordinate inLongitude) {
		this(inLatitude, inLongitude, null);
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
		// TODO: Check if either latitude or longitude is null - in which case do what?
		_latitude = inLatitude;
		_fieldValues[0] = inLatitude.toString();
		_longitude = inLongitude;
		_fieldValues[1] = inLongitude.toString();
		if (inAltitude == null) {
			_altitude = Altitude.NONE;
		}
		else {
			_altitude = inAltitude;
			_fieldValues[2] = "" + inAltitude.getValue();
		}
		_timestamp = new TimestampUtc(null);
	}

	/** Simplified constructor just giving raw values for latitude and longitude */
	public DataPoint(double inLatitude, double inLongitude) {
		this(Latitude.make(inLatitude), Longitude.make(inLongitude), null);
	}

	/**
	 * Get the value for the given field
	 * @param inField field to interrogate
	 * @return value of field
	 */
	public String getFieldValue(Field inField) {
		return getFieldValue(_fieldList.getFieldIndex(inField));
	}

	/**
	 * Get the value at the given index
	 * @param inIndex index number starting at zero
	 * @return field value, or null if not found
	 */
	private String getFieldValue(int inIndex)
	{
		if (_fieldValues == null || inIndex < 0 || inIndex >= _fieldValues.length) {
			return null;
		}
		return _fieldValues[inIndex];
	}

	/**
	 * Set (or edit) the waypoint name field
	 * @param inValue name to set
	 */
	public void setWaypointName(String inValue)
	{
		setRawFieldValue(Field.WAYPT_NAME, inValue);
		setModified(false);
		_waypointName = inValue;
	}

	/**
	 * Set (or edit) the specified field value, without specifying a unit set
	 * @param inField Field to set
	 * @param inValue value to set
	 * @param inUndo true if undo operation, false otherwise
	 */
	public void setFieldValue(Field inField, String inValue, boolean inUndo) {
		setFieldValue(inField, inValue, null, inUndo);
	}

	/**
	 * Set (or edit) the specified field value
	 * @param inField Field to set
	 * @param inValue value to set
	 * @param inUnitSet unit set to use
	 * @param inUndo true if undo operation, false otherwise
	 */
	public void setFieldValue(Field inField, String inValue, UnitSet inUnitSet, boolean inUndo)
	{
		setRawFieldValue(inField, inValue);
		// Increment edit count on all field edits except segment
		if (inField != Field.NEW_SEGMENT) {
			setModified(inUndo);
		}

		final boolean needUnits;
		if (inField == Field.ALTITUDE) {
			needUnits = _altitude == null || _altitude.getUnit() == null;
		}
		else if (inField == Field.SPEED) {
			needUnits = _hSpeed == null || _hSpeed.getUnit() == null;
		}
		else if (inField == Field.VERTICAL_SPEED) {
			needUnits = _vSpeed == null || _vSpeed.getUnit() == null;
		}
		else {
			needUnits = false;
		}
		if (needUnits && inUnitSet == null && inValue != null && !inValue.equals("")) {
			throw new IllegalArgumentException("Units required to set field " + inField.getName());
		}

		parseFields(inField, inUnitSet == null ? null : inUnitSet.getDefaultOptions());
	}

	/**
	 * Extend the field list if necessary and set the raw field value
	 * @param inField field to set
	 * @param inValue value to set
	 */
	private void setRawFieldValue(Field inField, String inValue)
	{
		if (inField == null) {
			return;
		}
		// See if this data point already has this field
		int fieldIndex = _fieldList.getFieldIndex(inField);
		// Add to field list if necessary
		if (fieldIndex < 0)
		{
			// If value is empty & field doesn't exist then do nothing
			if (inValue == null || inValue.equals("")) {
				return;
			}
			// value isn't empty so extend field list
			fieldIndex = _fieldList.addField(inField);
		}
		// Extend array of field values if necessary
		if (fieldIndex >= _fieldValues.length) {
			resizeValueArray(fieldIndex);
		}
		// Set field value in array
		_fieldValues[fieldIndex] = inValue;
	}

	/**
	 * Either increment or decrement the modify count, depending on whether it's an undo or not
	 * @param inUndo true for undo, false otherwise
	 */
	public void setModified(boolean inUndo)
	{
		if (!inUndo) {
			_modifyCount++;
		}
		else {
			_modifyCount--;
		}
	}

	/**
	 * @return field list for this point
	 */
	public FieldList getFieldList() {
		return _fieldList;
	}

	/** @param inFlag true for start of track segment */
	public void setSegmentStart(boolean inFlag) {
		setFieldValue(Field.NEW_SEGMENT, inFlag ? "1" : null, false);
	}

	/**
	 * Mark the point for deletion
	 * @param inFlag true to delete, false to keep
	 */
	public void setMarkedForDeletion(boolean inFlag) {
		_markedForDeletion = inFlag;
	}

	/** @return latitude */
	public Coordinate getLatitude() {
		return _latitude;
	}

	/** @return longitude */
	public Coordinate getLongitude() {
		return _longitude;
	}

	/** @return true if point has altitude */
	public boolean hasAltitude() {
		return _altitude != null && _altitude.isValid();
	}

	/** @return altitude */
	public Altitude getAltitude() {
		return _altitude;
	}

	/** @return true if point has horizontal speed (loaded as field) */
	public boolean hasHSpeed() {
		return _hSpeed != null && _hSpeed.isValid();
	}

	/** @return horizontal speed */
	public Speed getHSpeed() {
		return _hSpeed;
	}

	/** @return true if point has vertical speed (loaded as field) */
	public boolean hasVSpeed() {
		return _vSpeed != null && _vSpeed.isValid();
	}

	/** @return vertical speed */
	public Speed getVSpeed() {
		return _vSpeed;
	}

	/** @return true if point has timestamp */
	public boolean hasTimestamp() {
		return _timestamp != null && _timestamp.isValid();
	}

	/** @return timestamp */
	public Timestamp getTimestamp() {
		return _timestamp;
	}

	/** @return waypoint name, if any */
	public String getWaypointName() {
		return _waypointName;
	}

	/** @return true if start of new track segment */
	public boolean getSegmentStart() {
		return _startOfSegment;
	}

	/** @return true if point marked for deletion */
	public boolean getDeleteFlag() {
		return _markedForDeletion;
	}

	/**
	 * @return true if point has a waypoint name
	 */
	public boolean isWaypoint() {
		return _waypointName != null && !_waypointName.equals("");
	}

	/**
	 * @return true if point has been modified since loading
	 */
	public boolean isModified() {
		return _modifyCount > 0;
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
		if (!isWaypoint()) {
			return !inOther.isWaypoint();
		}
		return (inOther._waypointName != null && inOther._waypointName.equals(_waypointName));
	}

	/**
	 * Set the altitude including units
	 * @param inValue value as string
	 * @param inUnit units
	 * @param inIsUndo true if it's an undo operation
	 */
	public void setAltitude(String inValue, Unit inUnit, boolean inIsUndo)
	{
		_altitude = new Altitude(inValue, inUnit);
		setFieldValue(Field.ALTITUDE, inValue, inIsUndo);
		setModified(inIsUndo);
	}

	/**
	 * Set the photo for this data point
	 * @param inPhoto Photo object
	 */
	public void setPhoto(Photo inPhoto) {
		_photo = inPhoto;
		_modifyCount++;
	}

	/**
	 * @return associated Photo object
	 */
	public Photo getPhoto() {
		return _photo;
	}

	/**
	 * Set the audio clip for this point
	 * @param inAudio audio object
	 */
	public void setAudio(AudioClip inAudio) {
		_audio = inAudio;
		_modifyCount++;
	}

	/**
	 * @return associated audio object
	 */
	public AudioClip getAudio() {
		return _audio;
	}

	/**
	 * @return true if the point is valid
	 */
	public boolean isValid() {
		return _latitude != null && _longitude != null;
	}

	/**
	 * @return true if the point has either a photo or audio attached
	 */
	public boolean hasMedia() {
		return _photo != null || _audio != null;
	}

	/**
	 * @return name of attached photo and/or audio
	 */
	public String getMediaName()
	{
		String mediaName = null;
		if (_photo != null) {
			mediaName = _photo.getName();
		}
		if (_audio != null)
		{
			if (mediaName == null) {
				mediaName = _audio.getName();
			}
			else {
				mediaName = mediaName + ", " + _audio.getName();
			}
		}
		return mediaName;
	}

	/**
	 * Calculate the number of radians between two points (for distance calculation)
	 * @param inPoint1 first point
	 * @param inPoint2 second point
	 * @return angular distance between points in radians
	 */
	public static double calculateRadiansBetween(DataPoint inPoint1, DataPoint inPoint2)
	{
		if (inPoint1 == null || inPoint2 == null) {
			return 0.0;
		}
		// Get lat and long from points, in degrees
		double lat1 = inPoint1.getLatitude().getDouble();
		double lat2 = inPoint2.getLatitude().getDouble();
		double lon1 = inPoint1.getLongitude().getDouble();
		double lon2 = inPoint2.getLongitude().getDouble();
		return Distance.calculateRadiansBetween(lat1, lon1, lat2, lon2);
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
		// Copy all values (note that photo is not copied)
		String[] valuesCopy = new String[_fieldValues.length];
		System.arraycopy(_fieldValues, 0, valuesCopy, 0, _fieldValues.length);

		PointCreateOptions options = new PointCreateOptions();
		if (_altitude != null) {
			options.setAltitudeUnits(_altitude.getUnit());
		}
		// Make new object to hold cloned data
		DataPoint point = new DataPoint(valuesCopy, _fieldList, options);
		// Copy the speed information
		if (hasHSpeed()) {
			point.getHSpeed().copyFrom(_hSpeed);
		}
		if (hasVSpeed()) {
			point.getVSpeed().copyFrom(_vSpeed);
		}
		return point;
	}

	public void setSourceInfo(SourceInfo inInfo) {
		_sourceInfo = inInfo;
	}

	public SourceInfo getSourceInfo() {
		return _sourceInfo;
	}

	public void setOriginalIndex(int inIndex) {
		_originalIndex = inIndex;
	}

	public int getOriginalIndex() {
		return _originalIndex;
	}

	/**
	 * Remove all single and double quotes surrounding each value
	 * @param inValues array of values
	 */
	private static void removeQuotes(String[] inValues)
	{
		if (inValues != null)
		{
			for (int i=0; i<inValues.length; i++) {
				inValues[i] = removeQuotes(inValues[i]);
			}
		}
	}

	/**
	 * Remove any single or double quotes surrounding a value
	 * @param inValue value to modify
	 * @return modified String
	 */
	private static String removeQuotes(String inValue)
	{
		if (inValue == null) {
			return null;
		}
		final int len = inValue.length();
		if (len <= 1) {return inValue;}
		// get the first and last characters
		final char firstChar = inValue.charAt(0);
		final char lastChar  = inValue.charAt(len-1);
		if (firstChar == lastChar)
		{
			if (firstChar == '\"' || firstChar == '\'') {
				return inValue.substring(1, len-1);
			}
		}
		return inValue;
	}

	/**
	 * Get string for debug
	 */
	public String toString() {
		return "[Lat=" + getLatitude().toString() + ", Lon=" + getLongitude().toString() + "]";
	}
}
