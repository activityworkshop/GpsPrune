package tim.prune.load.xml;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import tim.prune.data.Field;


/**
 * Class for handling specifics of parsing Gpx files
 */
public class GpxHandler extends XmlHandler
{
	private boolean _insideWaypoint = false;
	private boolean _insideName = false;
	private boolean _insideElevation = false;
	private boolean _insideTime = false;
	private boolean _startSegment = true;
	private String _name = null, _latitude = null, _longitude = null;
	private String _elevation = null;
	private String _time = null;
	private ArrayList<String[]> _pointList = new ArrayList<String[]>();


	/**
	 * Receive the start of a tag
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String localName, String qName,
		Attributes attributes) throws SAXException
	{
		// Read the parameters for waypoints and track points
		if (qName.equalsIgnoreCase("wpt") || qName.equalsIgnoreCase("trkpt"))
		{
			_insideWaypoint = qName.equalsIgnoreCase("wpt");
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
		}
		else if (qName.equalsIgnoreCase("ele"))
		{
			_insideElevation = true;
		}
		else if (qName.equalsIgnoreCase("name"))
		{
			_insideName = true;
		}
		else if (qName.equalsIgnoreCase("time"))
		{
			_insideTime = true;
		}
		else if (qName.equalsIgnoreCase("trkseg"))
		{
			_startSegment = true;
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
		if (qName.equalsIgnoreCase("wpt") || qName.equalsIgnoreCase("trkpt"))
		{
			processPoint();
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
		else if (_insideElevation) {_elevation = checkCharacters(_elevation, value);}
		else if (_insideTime) {_time = checkCharacters(_time, value);}
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
		String[] values = new String[6];
		values[0] = _latitude; values[1] = _longitude;
		values[2] = _elevation; values[3] = _name;
		values[4] = _time;
		if (_startSegment && !_insideWaypoint) {
			values[5] = "1";
			_startSegment = false;
		}
		_pointList.add(values);
	}


	/**
	 * @see tim.prune.load.xml.XmlHandler#getFieldArray()
	 */
	public Field[] getFieldArray()
	{
		final Field[] fields = {Field.LATITUDE, Field.LONGITUDE, Field.ALTITUDE,
			Field.WAYPT_NAME, Field.TIMESTAMP, Field.NEW_SEGMENT};
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
}
