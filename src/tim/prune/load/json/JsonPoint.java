package tim.prune.load.json;

/**
 * Structure for holding a single point extracted from the Json
 */
public class JsonPoint
{
	public String _latitude = null, _longitude = null, _altitude = null;
	public boolean _newSegment = false;

	/**
	 * Constructor
	 * @param inLat latitude string
	 * @param inLon longitude string
	 * @param inAlt altitude string
	 * @param inNewSegment true if this point starts a new segment
	 */
	public JsonPoint(String inLat, String inLon, String inAlt, boolean inNewSegment)
	{
		_latitude = inLat;
		_longitude = inLon;
		_altitude = inAlt;
		_newSegment = inNewSegment;
	}
}
