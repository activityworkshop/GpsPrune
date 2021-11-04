package tim.prune.load.json;

import java.util.ArrayList;

/**
 * Structure to hold the contents of a Json block during parsing.
 * This will be held within the current [] or {} block
 */
public class JsonBlock
{
	private String _latitude = null;
	private String _longitude = null;
	private String _altitude = null;

	private Expectation _nextField = Expectation.NONE;
	private BlockType _type = BlockType.NONE;
	private boolean _hasNonNumbers = false;
	private ArrayList<String> _pointCoords = null;
	private ArrayList<ArrayList<String>> _coordList = null;


	private enum BlockType {
		NONE, FIELDS, POINTCOORDS, ISPOINTLIST, HASPOINTLIST
	}
	private enum Expectation {
		NONE, LATITUDE, LONGITUDE, ALTITUDE, COORDS
	}

	/** Internal method to remove quotes and NaNs from altitude strings */
	private String modifyAltitudeString(String inAlt)
	{
		if (inAlt == null || inAlt.equals("") || inAlt.equals("\"\"")) {
			return null;
		}
		String result = inAlt;
		if (inAlt.length() > 2 && inAlt.startsWith("\"") && inAlt.endsWith("\""))
		{
			result = inAlt.substring(1, inAlt.length()-1);
		}
		if (result.equals("NaN")) {return null;}
		return result;
	}

	/**
	 * Receive a token to this block
	 * @param inToken token from Json source
	 */
	public void addToken(String inToken)
	{
		if (inToken == null || inToken.isEmpty()) {return;}
		if (!_hasNonNumbers && !looksLikeNumber(inToken)) {
			_hasNonNumbers = true;
		}
		if (inToken.equals("\"latitude\"")) {
			_nextField = Expectation.LATITUDE;
		}
		else if (inToken.equals("\"longitude\"")) {
			_nextField = Expectation.LONGITUDE;
		}
		else if (inToken.equals("\"altitude\"")) {
			_nextField = Expectation.ALTITUDE;
		}
		else if (inToken.equals("\"coordinates\"")) {
			_nextField = Expectation.COORDS;
		}
		else
		{
			switch (_nextField)
			{
				case LATITUDE:
					_latitude = inToken;
					_type = BlockType.FIELDS;
					break;
				case LONGITUDE:
					_longitude = inToken;
					_type = BlockType.FIELDS;
					break;
				case ALTITUDE:
					_altitude = modifyAltitudeString(inToken);
					_type = BlockType.FIELDS;
					break;
				default:
					if (!_hasNonNumbers && looksLikeNumber(inToken))
					{
						if (_pointCoords == null) {
							_pointCoords = new ArrayList<String>();
						}
						_pointCoords.add(inToken);
						_type = BlockType.POINTCOORDS;
					}
					break;
			}
			_nextField = Expectation.NONE;
		}
	}

	/** @return true if String can be parsed as a double */
	private static boolean looksLikeNumber(String inToken)
	{
		double value = Double.NaN;
		try {
			value = Double.parseDouble(inToken);
		}
		catch (NumberFormatException nfe) {}
		return !Double.isNaN(value);
	}

	/** @return true if named fields are valid */
	public boolean areFieldsValid() {
		return _type == BlockType.FIELDS && _latitude != null && _longitude != null;
	}

	/** @return true if list of 2 or 3 doubles for a single point is valid */
	public boolean areSingleCoordsValid()
	{
		return !_hasNonNumbers && _pointCoords != null
			&& _pointCoords.size() >= 2 && _pointCoords.size() <= 3;
	}

	/**
	 * @param inNewSegment new segment flag
	 * @return created point
	 */
	public JsonPoint createSinglePoint(boolean inNewSegment)
	{
		return new JsonPoint(_latitude, _longitude, _altitude, inNewSegment);
	}

	/**
	 * Child block has finished processing a single set of point coordinates
	 * @param inChild child block
	 */
	public void addSingleCoordsFrom(JsonBlock inChild)
	{
		if (_type == BlockType.NONE && _nextField == Expectation.COORDS
			&& inChild._type == BlockType.POINTCOORDS)
		{
			// Named coordinates field
			_type = BlockType.FIELDS;
			_longitude = inChild.getSinglePointCoords(0);
			_latitude = inChild.getSinglePointCoords(1);
			_altitude = inChild.getSinglePointCoords(2);
		}
		else if ((_type == BlockType.NONE || _type == BlockType.ISPOINTLIST)
			&& !_hasNonNumbers && _nextField == Expectation.NONE
			&& inChild._type == BlockType.POINTCOORDS)
		{
			// add coordinates to list
			_type = BlockType.ISPOINTLIST;
			if (_coordList == null) {
				_coordList = new ArrayList<ArrayList<String>>();
			}
			_coordList.add(inChild._pointCoords);
		}
	}

	/** @return point coordinate for given index, or null if not present */
	private String getSinglePointCoords(int inIndex)
	{
		if (_pointCoords == null || inIndex < 0 || inIndex >= _pointCoords.size()) {
			return null;
		}
		return _pointCoords.get(inIndex);
	}

	/** @return true if list of point coords is valid */
	public boolean isCoordListValid()
	{
		return !_hasNonNumbers && _type == BlockType.ISPOINTLIST
			&& _coordList != null;
	}

	/** @return true if this block has a valid list of point coords */
	public boolean hasValidCoordList()
	{
		return _type == BlockType.HASPOINTLIST && _coordList != null;
	}

	/**
	 * Child block has finished processing a list of point coordinates
	 * @param inChild child block
	 */
	public void addCoordListFrom(JsonBlock inChild)
	{
		if (_type == BlockType.NONE && _nextField == Expectation.COORDS
			&& inChild._type == BlockType.ISPOINTLIST)
		{
			_coordList = inChild._coordList;
			_type = BlockType.HASPOINTLIST;
		}
		else if ((_type == BlockType.NONE || _type == BlockType.ISPOINTLIST)
			&& !_hasNonNumbers && inChild._type == BlockType.ISPOINTLIST)
		{
			if (_coordList == null) {
				_coordList = new ArrayList<ArrayList<String>>();
			}
			_coordList.addAll(inChild._coordList);
			_type = BlockType.ISPOINTLIST;
		}
	}

	/**
	 * @return number of points in the list, if this block has a list
	 */
	public int getNumPoints()
	{
		if (hasValidCoordList()) {
			return _coordList.size();
		}
		return 0;
	}

	/**
	 * @param inIndex point index within list
	 * @return created point for specified index
	 */
	public JsonPoint createPointFromList(int inIndex)
	{
		if (inIndex < 0 || inIndex >= getNumPoints()) {return null;}
		ArrayList<String> pointCoords = _coordList.get(inIndex);
		if (pointCoords.size() < 2 || pointCoords.size() > 3) {return null;}
		final String longitude = pointCoords.get(0);
		final String latitude = pointCoords.get(1);
		final String altitude = ((pointCoords.size() == 3) ? pointCoords.get(2) : null);
		return new JsonPoint(latitude, longitude, altitude, inIndex == 0);
	}
}
