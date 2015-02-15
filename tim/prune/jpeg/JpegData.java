package tim.prune.jpeg;

import java.util.ArrayList;

/**
 * Class to hold the GPS data extracted from a Jpeg including position and time
 */
public class JpegData
{
	private boolean _exifDataPresent = false;
	private char _latitudeRef = '\0';
	private char _longitudeRef = '\0';
	private byte _altitudeRef = 0;
	private double[] _latitude = null;
	private double[] _longitude = null;
	private int _altitude = -1;
	private boolean _altitudePresent = false;
	private int[] _gpsTimestamp = null;
	private int[] _gpsDatestamp = null;
	private String _originalTimestamp = null;
	private String _digitizedTimestamp = null;
	private int _orientationCode = -1;
	private byte[] _thumbnail = null;
	private double _bearing = -1.0;
	private ArrayList<String> _errors = null;


	/**
	 * Set the exif data present flag
	 */
	public void setExifDataPresent()
	{
		_exifDataPresent = true;
	}
	/**
	 * @return true if exif data found
	 */
	public boolean getExifDataPresent()
	{
		return _exifDataPresent;
	}

	/**
	 * Set the latitude reference (N/S)
	 * @param inString string containing reference
	 */
	public void setLatitudeRef(String inString)
	{
		if (inString != null && inString.length() == 1)
			_latitudeRef = inString.charAt(0);
	}

	/**
	 * Set the latitude
	 * @param inValues array of three doubles for deg-min-sec
	 */
	public void setLatitude(double[] inValues)
	{
		if (inValues != null && inValues.length == 3)
			_latitude = inValues;
	}

	/**
	 * Set the longitude reference (E/W)
	 * @param inString string containing reference
	 */
	public void setLongitudeRef(String inString)
	{
		if (inString != null && inString.length() == 1)
			_longitudeRef = inString.charAt(0);
	}

	/**
	 * Set the longitude
	 * @param inValues array of three doubles for deg-min-sec
	 */
	public void setLongitude(double[] inValues)
	{
		if (inValues != null && inValues.length == 3)
			_longitude = inValues;
	}

	/**
	 * Set the altitude reference (sea level / not)
	 * @param inByte byte representing reference
	 */
	public void setAltitudeRef(byte inByte)
	{
		_altitudeRef = inByte;
	}

	/**
	 * Set the altitude
	 * @param inValue integer representing altitude
	 */
	public void setAltitude(int inValue)
	{
		_altitude = inValue;
		_altitudePresent = true;
	}

	/**
	 * Set the Gps timestamp
	 * @param inValues array of ints holding timestamp
	 */
	public void setGpsTimestamp(int[] inValues)
	{
		_gpsTimestamp = inValues;
	}

	/**
	 * Set the Gps datestamp
	 * @param inValues array of ints holding datestamp
	 */
	public void setGpsDatestamp(int[] inValues)
	{
		_gpsDatestamp = inValues;
	}

	/**
	 * Set the original timestamp
	 * @param inStamp original timestamp of photo
	 */
	public void setOriginalTimestamp(String inStamp)
	{
		_originalTimestamp = inStamp;
	}

	/**
	 * Set the digitized timestamp
	 * @param inStamp digitized (creation) timestamp of photo
	 */
	public void setDigitizedTimestamp(String inStamp)
	{
		_digitizedTimestamp = inStamp;
	}

	/**
	 * Set the orientation code
	 * @param inCode code from exif (1 to 8)
	 */
	public void setOrientationCode(int inCode)
	{
		if (inCode >= 1 && inCode <= 8) {
			_orientationCode = inCode;
		}
	}

	/**
	 * Set the bearing (0 - 360)
	 * @param inBearing bearing in degrees
	 */
	public void setBearing(double inBearing)
	{
		_bearing = inBearing;
	}

	/** @return latitude ref as char */
	public char getLatitudeRef() { return _latitudeRef; }
	/** @return latitude as array of 3 Rationals */
	public double[] getLatitude() { return _latitude; }
	/** @return longitude ref as char */
	public char getLongitudeRef() { return _longitudeRef; }
	/** @return longitude as array of 3 doubles */
	public double[] getLongitude() { return _longitude; }
	/** @return altitude ref as byte (should be 0) */
	public byte getAltitudeRef() { return _altitudeRef; }
	/** @return true if altitude present */
	public boolean hasAltitude() { return _altitudePresent; }
	/** @return altitude as int */
	public int getAltitude() { return _altitude; }
	/** @return Gps timestamp as array of 3 ints */
	public int[] getGpsTimestamp() { return _gpsTimestamp; }
	/** @return Gps datestamp as array of 3 ints */
	public int[] getGpsDatestamp() { return _gpsDatestamp; }
	/** @return orientation code (1 to 8) */
	public int getOrientationCode() { return _orientationCode; }
	/** @return original timestamp as string */
	public String getOriginalTimestamp() { return _originalTimestamp; }
	/** @return digitized timestamp as string */
	public String getDigitizedTimestamp() { return _digitizedTimestamp; }
	/** @return bearing in degrees or -1 */
	public double getBearing() { return _bearing; }

	/**
	 * Set the thumbnail
	 * @param inBytes byte array containing thumbnail
	 */
	public void setThumbnailImage(byte[] inBytes) {
		_thumbnail = inBytes;
	}
	/** @return thumbnail as byte array */
	public byte[] getThumbnailImage() {
		return _thumbnail;
	}

	/**
	 * @return rotation required to display photo properly (0 to 3)
	 */
	public int getRequiredRotation()
	{
		if (_orientationCode <= 2) { return 0; } // no rotation required
		if (_orientationCode <= 4) { return 2; } // 180 degrees
		if (_orientationCode <= 6) { return 1; } // 270 degrees, so need to rotate by 90
		return 3; // 90 degrees, so need to rotate by 270
	}

	/**
	 * @return true if data looks valid, ie has at least lat and long
	 *  (altitude and timestamp optional).
	 */
	public boolean isGpsValid()
	{
		return (_latitudeRef == 'N' || _latitudeRef == 'n' || _latitudeRef == 'S' || _latitudeRef == 's')
			&& _latitude != null
			&& (_longitudeRef == 'E' || _longitudeRef == 'e' || _longitudeRef == 'W' || _longitudeRef == 'w')
			&& _longitude != null;
	}

	/**
	 * Add the given error message to the list of errors
	 * @param inError String containing error message
	 */
	public void addError(String inError)
	{
		if (_errors == null) _errors = new ArrayList<String>();
		_errors.add(inError);
	}

	/**
	 * @return the number of errors, if any
	 */
	public int getNumErrors()
	{
		if (_errors == null) return 0;
		return _errors.size();
	}

	/**
	 * @return true if errors occurred
	 */
	public boolean hasErrors()
	{
		return getNumErrors() > 0;
	}

	/**
	 * @return all errors as a list
	 */
	public ArrayList<String> getErrors()
	{
		return _errors;
	}
}
