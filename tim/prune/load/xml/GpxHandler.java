package tim.prune.load.xml;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import tim.prune.data.Field;
import tim.prune.load.TrackNameList;


/**
 * Class for handling specifics of parsing Gpx files
 */
public class GpxHandler extends XmlHandler
{
	private boolean _insidePoint = false;
	private boolean _insideWaypoint = false;
	private boolean _insideName = false;
	private boolean _insideElevation = false;
	private boolean _insideTime = false;
	private boolean _insideType = false;
	private boolean _startSegment = true;
	private boolean _isTrackPoint = false;
	private int _trackNum = -1;
	private String _trackName = null;
	private String _name = null, _latitude = null, _longitude = null;
	private String _elevation = null;
	private String _time = null;
	private String _type = null;
	private ArrayList<String[]> _pointList = new ArrayList<String[]>();
	private TrackNameList _trackNameList = new TrackNameList();


	/**
	 * Receive the start of a tag
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String localName, String qName,
		Attributes attributes) throws SAXException
	{
		// Read the parameters for waypoints and track points
		if (qName.equalsIgnoreCase("wpt") || qName.equalsIgnoreCase("trkpt") || qName.equalsIgnoreCase("rtept"))
		{
			_insidePoint = true;
			_insideWaypoint = qName.equalsIgnoreCase("wpt");
			_isTrackPoint = qName.equalsIgnoreCase("trkpt");
			int numAttributes = attributes.getLength();
			for (int i=0; i<numAttributes; i++)
			{
				String att = attributes.getQName(i);
				if (att.equals("lat")) {_latitude = attributes.getValue(i);}
				else if (att.equals("lon")) {_longitude = attributes.getValue(i);}
			}
			_elevation = null;
			_name = null;
			_time = null;
			_type = null;
		}
		else if (qName.equalsIgnoreCase("ele"))
		{
			_insideElevation = true;
		}
		else if (qName.equalsIgnoreCase("name"))
		{
			_name = null;
			_insideName = true;
		}
		else if (qName.equalsIgnoreCase("time"))
		{
			_insideTime = true;
		}
		else if (qName.equalsIgnoreCase("type"))
		{
			_insideType = true;
		}
		else if (qName.equalsIgnoreCase("trkseg"))
		{
			_startSegment = true;
		}
		else if (qName.equalsIgnoreCase("trk"))
		{
			_trackNum++;
			_trackName = null;
		}
		super.startElement(uri, localName, qName, attributes);
	}


	/**
	 * Process end tag
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String uri, String localName, String qName)
		throws SAXException
	{
		if (qName.equalsIgnoreCase("wpt") || qName.equalsIgnoreCase("trkpt") || qName.equalsIgnoreCase("rtept"))
		{
			processPoint();
			_insidePoint = false;
		}
		else if (qName.equalsIgnoreCase("ele"))
		{
			_insideElevation = false;
		}
		else if (qName.equalsIgnoreCase("name"))
		{
			_insideName = false;
		}
		else if (qName.equalsIgnoreCase("time"))
		{
			_insideTime = false;
		}
		else if (qName.equalsIgnoreCase("type"))
		{
			_insideType = false;
		}
		super.endElement(uri, localName, qName);
	}


	/**
	 * Process character text (inside tags or between them)
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length)
		throws SAXException
	{
		String value = new String(ch, start, length);
		if (_insideName && _insideWaypoint) {_name = checkCharacters(_name, value);}
		if (_insideName && !_insidePoint) {_trackName = checkCharacters(_trackName, value);}
		else if (_insideElevation) {_elevation = checkCharacters(_elevation, value);}
		else if (_insideTime) {_time = checkCharacters(_time, value);}
		else if (_insideType) {_type = checkCharacters(_type, value);}
		super.characters(ch, start, length);
	}


	/**
	 * Check to concatenate partially-received values, if necessary
	 * @param inVariable variable containing characters received until now
	 * @param inValue new value received
	 * @return concatenation
	 */
	private static String checkCharacters(String inVariable, String inValue)
	{
		if (inVariable == null) {return inValue;}
		return inVariable + inValue;
	}


	/**
	 * Process a point, either a waypoint or track point
	 */
	private void processPoint()
	{
		// Put the values into a String array matching the order in getFieldArray()
		String[] values = new String[7];
		values[0] = _latitude; values[1] = _longitude;
		values[2] = _elevation; values[3] = _name;
		values[4] = _time;
		if (_startSegment && !_insideWaypoint) {
			values[5] = "1";
			_startSegment = false;
		}
		values[6] = _type;
		_pointList.add(values);
		_trackNameList.addPoint(_trackNum, _trackName, _isTrackPoint);
	}


	/**
	 * @see tim.prune.load.xml.XmlHandler#getFieldArray()
	 */
	public Field[] getFieldArray()
	{
		final Field[] fields = {Field.LATITUDE, Field.LONGITUDE, Field.ALTITUDE,
			Field.WAYPT_NAME, Field.TIMESTAMP, Field.NEW_SEGMENT, Field.WAYPT_TYPE};
		return fields;
	}


	/**
	 * Return the parsed information as a 2d array
	 * @see tim.prune.load.xml.XmlHandler#getDataArray()
	 */
	public String[][] getDataArray()
	{
		int numPoints = _pointList.size();
		// construct data array
		String[][] result = new String[numPoints][];
		for (int i=0; i<numPoints; i++)
		{
			result[i] = _pointList.get(i);
		}
		return result;
	}


	/**
	 * @return track name list
	 */
	public TrackNameList getTrackNameList() {
		return _trackNameList;
	}
}
