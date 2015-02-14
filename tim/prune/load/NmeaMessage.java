package tim.prune.load;

import java.util.Calendar;

/**
 * Class to hold a single NMEA message
 */
public class NmeaMessage
{
	private String _latitude = null;
	private String _longitude = null;
	private String _altitude = null;
	private String _timestamp = null;
	private boolean _fix = false;
	private boolean _segment = false;

	/**
	 * Constructor
	 * @param inLatitude latitude
	 * @param inLongitude longitude
	 * @param inAltitude altitude
	 * @param inTimestamp timestamp
	 * @param inFix fix = 0, 1 or 2
	 */
	public NmeaMessage(String inLatitude, String inLongitude,
		String inAltitude, String inTimestamp, String inFix)
	{
		_latitude = inLatitude;
		_longitude = inLongitude;
		_altitude = inAltitude;
		_timestamp = inTimestamp;
		_fix = (inFix != null && !inFix.equals("0"));
	}

	/**
	 * @return true if message has a fix
	 */
	public boolean hasFix() {
		return _fix;
	}

	/**
	 * @param inSegment segment flag
	 */
	public void setSegment(boolean inSegment)
	{
		_segment = inSegment;
	}

	/**
	 * @return String array for loading
	 */
	public String[] getStrings()
	{
		String[] results = new String[] {modify(_latitude), modify(_longitude), _altitude,
			getTimestamp(), (_segment?"1":"")};
		return results;
	}

	/**
	 * Insert a separator between degrees and minutes
	 * @param inCoordinate NMEA coordinate string
	 * @return modified string or input string if format wasn't what was expected
	 */
	private static String modify(String inCoordinate)
	{
		if (inCoordinate != null && inCoordinate.length() > 6)
		{
			int dotPos = inCoordinate.indexOf('.');
			if (dotPos > 0) {
				return inCoordinate.substring(0, dotPos-2) + "d" + inCoordinate.substring(dotPos-2);
			}
		}
		return inCoordinate;
	}

	/**
	 * Use time from NMEA message, and today's date (as date isn't given in GPGGA messages)
	 * @return Timestamp in parseable format
	 */
	private String getTimestamp()
	{
		try
		{
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(_timestamp.substring(0, 2)));
			cal.set(Calendar.MINUTE, Integer.parseInt(_timestamp.substring(2, 4)));
			cal.set(Calendar.SECOND, Integer.parseInt(_timestamp.substring(4, 6)));
			cal.set(Calendar.MILLISECOND, 0);
			// Return time as number of milliseconds
			return "" + cal.getTimeInMillis();
		}
		catch (Exception e) {}  // ignore parsing errors, just have no timestamp
		return null;
	}
}
