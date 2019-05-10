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
	private boolean _startSegment = true;
	private boolean _isTrackPoint = false;
	private int _trackNum = -1;
	private GpxTag _fileTitle = new GpxTag();
	private GpxTag _pointName = new GpxTag(), _trackName = new GpxTag();
	private String _latitude = null, _longitude = null;
	private GpxTag _elevation = new GpxTag(), _time = new GpxTag();
	private GpxTag _type = new GpxTag(), _description = new GpxTag();
	private GpxTag _link = new GpxTag();
	private GpxTag _currentTag = null;
	private ArrayList<String[]> _pointList = new ArrayList<String[]>();
	private ArrayList<String> _linkList = new ArrayList<String>();
	private TrackNameList _trackNameList = new TrackNameList();


	/**
	 * Receive the start of a tag
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String localName, String qName,
		Attributes attributes) throws SAXException
	{
		// Read the parameters for waypoints and track points
		String tag = qName.toLowerCase();
		if (tag.equals("wpt") || tag.equals("trkpt") || tag.equals("rtept"))
		{
			_insidePoint = true;
			_insideWaypoint = tag.equals("wpt");
			_isTrackPoint = tag.equals("trkpt");
			final int numAttributes = attributes.getLength();
			for (int i=0; i<numAttributes; i++)
			{
				String att = attributes.getQName(i).toLowerCase();
				if (att.equals("lat")) {_latitude = attributes.getValue(i);}
				else if (att.equals("lon")) {_longitude = attributes.getValue(i);}
			}
			_elevation.setValue(null);
			_pointName.setValue(null);
			_time.setValue(null);
			_type.setValue(null);
			_link.setValue(null);
			_description.setValue(null);
		}
		else if (tag.equals("ele")) {
			_currentTag = _elevation;
		}
		else if (tag.equals("name"))
		{
			if (_insidePoint) {
				_currentTag = _pointName;
			}
			else if (_trackNum < 0)
			{
				_currentTag = _fileTitle;
			}
			else {
				_currentTag = _trackName;
			}
		}
		else if (tag.equals("time")) {
			_currentTag = _time;
		}
		else if (tag.equals("type")) {
			_currentTag = _type;
		}
		else if (tag.equals("description") || tag.equals("desc")) {
			_currentTag = _description;
		}
		else if (tag.equals("link")) {
			_link.setValue(attributes.getValue("href"));
		}
		else if (tag.equals("trkseg")) {
			_startSegment = true;
		}
		else if (tag.equals("trk"))
		{
			_trackNum++;
			_trackName.setValue(null);
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
		String tag = qName.toLowerCase();
		if (tag.equals("wpt") || tag.equals("trkpt") || tag.equals("rtept"))
		{
			processPoint();
			_insidePoint = false;
		}
		else {
			_currentTag = null;
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
		if (_currentTag != null) {
			_currentTag.setValue(checkCharacters(_currentTag.getValue(), value));
		}
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
		String[] values = new String[8];
		values[0] = _latitude;
		values[1] = _longitude;
		values[2] = _elevation.getValue();
		if (_insideWaypoint) {values[3] = _pointName.getValue();}
		values[4] = _time.getValue();
		if (_startSegment && !_insideWaypoint)
		{
			values[5] = "1";
			_startSegment = false;
		}
		values[6] = _type.getValue();
		values[7] = _description.getValue();
		_pointList.add(values);
		_trackNameList.addPoint(_trackNum, _trackName.getValue(), _isTrackPoint);
		_linkList.add(_link.getValue());
	}


	/**
	 * @see tim.prune.load.xml.XmlHandler#getFieldArray()
	 */
	public Field[] getFieldArray()
	{
		final Field[] fields = {Field.LATITUDE, Field.LONGITUDE, Field.ALTITUDE,
			Field.WAYPT_NAME, Field.TIMESTAMP, Field.NEW_SEGMENT,
			Field.WAYPT_TYPE, Field.DESCRIPTION};
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
	 * @return array of links, or null if none
	 */
	public String[] getLinkArray()
	{
		int numPoints = _linkList.size();
		boolean hasLink = false;
		String[] result = new String[numPoints];
		for (int i=0; i<numPoints; i++)
		{
			result[i] = _linkList.get(i);
			if (result[i] != null) {hasLink = true;}
		}
		if (!hasLink) {result = null;}
		return result;
	}

	/**
	 * @return track name list
	 */
	public TrackNameList getTrackNameList() {
		return _trackNameList;
	}

	/**
	 * @return file title
	 */
	public String getFileTitle() {
		return _fileTitle.getValue();
	}
}
