package tim.prune.drew.jpeg;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to hold the GPS data extracted from a Jpeg including position and time
 * All contents are in Rational format
 */
public class JpegData
{
	private boolean _exifDataPresent = false;
	private char _latitudeRef = '\0';
	private char _longitudeRef = '\0';
	private byte _altitudeRef = 0;
	private Rational[] _latitude = null;
	private Rational[] _longitude = null;
	private Rational   _altitude = null;
	private Rational[] _gpsTimestamp = null;
	private Rational[] _gpsDatestamp = null;
	private String _originalTimestamp = null;
	private byte[] _thumbnail = null;
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
	 * @param inChar character representing reference
	 */
	public void setLatitudeRef(char inChar)
	{
		_latitudeRef = inChar;
	}

	/**
	 * Set the latitude reference (N/S)
	 * @param inString string containing reference
	 */
	public void setLatitudeRef(String inString)
	{
		if (inString != null && inString.length() == 1)
			setLatitudeRef(inString.charAt(0));
	}

	/**
	 * Set the latitude
	 * @param inValues array of three Rationals for deg-min-sec
	 */
	public void setLatitude(Rational[] inValues)
	{
		if (inValues != null && inValues.length == 3)
			_latitude = inValues;
	}

	/**
	 * Set the longitude reference (E/W)
	 * @param inChar character representing reference
	 */
	public void setLongitudeRef(char inChar)
	{
		_longitudeRef = inChar;
	}

	/**
	 * Set the longitude reference (E/W)
	 * @param inString string containing reference
	 */
	public void setLongitudeRef(String inString)
	{
		if (inString != null && inString.length() == 1)
			setLongitudeRef(inString.charAt(0));
	}

	/**
	 * Set the longitude
	 * @param inValues array of three Rationals for deg-min-sec
	 */
	public void setLongitude(Rational[] inValues)
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
	 * @param inRational Rational number representing altitude
	 */
	public void setAltitude(Rational inRational)
	{
		_altitude = inRational;
	}

	/**
	 * Set the Gps timestamp
	 * @param inValues array of Rationals holding timestamp
	 */
	public void setGpsTimestamp(Rational[] inValues)
	{
		_gpsTimestamp = inValues;
	}

	/**
	 * Set the Gps datestamp
	 * @param inValues array of Rationals holding datestamp
	 */
	public void setGpsDatestamp(Rational[] inValues)
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

	/** @return latitude ref as char */
	public char getLatitudeRef() { return _latitudeRef; }
	/** @return latitude as array of 3 Rationals */
	public Rational[] getLatitude() { return _latitude; }
	/** @return longitude ref as char */
	public char getLongitudeRef() { return _longitudeRef; }
	/** @return longitude as array of 3 Rationals */
	public Rational[] getLongitude() { return _longitude; }
	/** @return altitude ref as byte (should be 0) */
	public byte getAltitudeRef() { return _altitudeRef; }
	/** @return altitude as Rational */
	public Rational getAltitude() { return _altitude; }
	/** @return Gps timestamp as array of 3 Rationals */
	public Rational[] getGpsTimestamp() { return _gpsTimestamp; }
	/** @return Gps datestamp as array of 3 Rationals */
	public Rational[] getGpsDatestamp() { return _gpsDatestamp; }
	/** @return original timestamp as string */
	public String getOriginalTimestamp() { return _originalTimestamp; }

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
	 * @return true if data looks valid, ie has at least lat and long
	 *  (altitude and timestamp optional).
	 */
	public boolean isValid()
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
	public List<String> getErrors()
	{
		return _errors;
	}
}
