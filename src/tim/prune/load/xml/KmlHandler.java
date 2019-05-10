package tim.prune.load.xml;

import java.util.ArrayList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import tim.prune.data.Field;


/**
 * Class for handling specifics of parsing Kml files
 */
public class KmlHandler extends XmlHandler
{
	private boolean _insideCoordinates = false;
	private boolean _insideGxTrack = false;
	private String _value = null;
	private String _name = null, _desc = null;
	private String _timestamp = null, _imgLink = null;
	private StringBuffer _coordinates = null;
	private ArrayList<String> _coordinateList = null;
	private ArrayList<String[]> _pointList = new ArrayList<String[]>();
	private ArrayList<String> _linkList = new ArrayList<String>();
	// variables for gx extensions
	private ArrayList<String> _whenList = new ArrayList<String>();
	private ArrayList<String> _whereList = new ArrayList<String>();


	/**
	 * Receive the start of a tag
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String localName, String qName,
		Attributes attributes) throws SAXException
	{
		String tagName = localName;
		if (tagName == null || tagName.equals("")) {tagName = qName;}
		tagName = tagName.toLowerCase();

		if (tagName.equals("placemark"))
		{
			_coordinateList = new ArrayList<String>();
		}
		else if (tagName.equals("coordinates"))
		{
			_insideCoordinates = true;
			_coordinates = null;
		}
		else if (tagName.equals("gx:track"))
		{
			_insideGxTrack = true;
		}
		_value = null;
		super.startElement(uri, localName, qName, attributes);
	}


	/**
	 * Process end tag
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String uri, String localName, String qName)
	throws SAXException
	{
		String tagName = localName;
		if (tagName == null || tagName.equals("")) {tagName = qName;}
		tagName = tagName.toLowerCase();

		if (tagName.equals("placemark"))
		{
			processPlacemark();
			_name = _desc = _imgLink = _timestamp = null;
		}
		else if (tagName.equals("coordinates"))
		{
			_insideCoordinates = false;
			if (_coordinates != null) _coordinateList.add(_coordinates.toString().trim());
		}
		else if (tagName.equals("name"))
		{
			_name = _value;
		}
		else if (tagName.equals("description"))
		{
			_desc = _value;
			_imgLink = getImgLink(_desc);
		}
		else if (tagName.equals("when"))
		{
			if (!_insideGxTrack)
				_timestamp = _value;
			else
				_whenList.add(_value);
		}
		else if (tagName.equals("gx:coord"))
		{
			if (_insideGxTrack) {
				_whereList.add(_value);
			}
		}
		else if (tagName.equals("gx:track"))
		{
			processGxTrack();
			_insideGxTrack = false;
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
		String val = new String(ch, start, length);
		if (_insideCoordinates)
		{
			if (_coordinates == null) {
				_coordinates = new StringBuffer();
			}
			_coordinates.append(val);
		}
		else
		{
			// Store string in _value
			if (_value == null) _value = val;
			else _value = _value + val;
		}
		super.characters(ch, start, length);
	}


	/**
	 * Process a placemark entry, either a single waypoint or a whole track
	 */
	private void processPlacemark()
	{
		if (_coordinateList == null || _coordinateList.isEmpty()) return;
		final boolean isSingleSelection = (_coordinateList.size() == 1);
		// Loop over coordinate sets in list (may have multiple <coordinates> tags within single placemark)
		for (String coords : _coordinateList)
		{
			String[] coordArray = coords.split("[ \n]");
			int numPoints = coordArray.length;
			if (numPoints == 1)
			{
				// Add single point to list
				final String name = (isSingleSelection ? _name : null);
				_pointList.add(makeStringArray(coords, name, _desc, _timestamp));
				_linkList.add(_imgLink);
			}
			else if (numPoints > 1)
			{
				// Add each of the unnamed track points to list
				boolean firstPoint = true;
				for (int p=0; p<numPoints; p++)
				{
					if (coordArray[p] != null && coordArray[p].trim().length()>3)
					{
						String[] pointArray = makeStringArray(coordArray[p], null, null, null);
						if (firstPoint) {pointArray[5] = "1";} // start of segment flag
						firstPoint = false;
						_pointList.add(pointArray);
					}
					_linkList.add(null);
				}
			}
		}
	}

	/**
	 * Process a Gx track including timestamps
	 */
	private void processGxTrack()
	{
		if (!_whereList.isEmpty())
		{
			// If the whens don't match, then throw them all away
			if (_whenList.size() != _whereList.size()) {System.out.println("clearing!"); _whenList.clear();}

			// Add each of the unnamed track points to list
			boolean firstPoint = true;
			final int numPoints = _whenList.size();
			for (int p=0; p < numPoints; p++)
			{
				String when  = (_whenList.isEmpty() ? null : _whenList.get(p));
				String where = _whereList.get(p);
				if (where != null)
				{
					String[] coords = where.split(" ");
					if (coords.length == 3)
					{
						String[] pointArray = new String[7];
						pointArray[0] = coords[0];
						pointArray[1] = coords[1];
						pointArray[2] = coords[2];
						// leave name and description empty
						if (firstPoint) {pointArray[5] = "1";} // start of segment flag
						firstPoint = false;
						pointArray[6] = when; // timestamp
						_pointList.add(pointArray);
					}
				}
				_linkList.add(null);
			}
		}
		_whenList.clear();
		_whereList.clear();
	}

	/**
	 * Extract an image link from the point description
	 * @param inDesc description tag contents
	 * @return image link if found or null
	 */
	private static String getImgLink(String inDesc)
	{
		if (inDesc == null || inDesc.equals("")) {return null;}
		// Pull out <img tag from description (if any)
		int spos = inDesc.indexOf("<img");
		int epos = inDesc.indexOf('>', spos + 10);
		if (spos < 0 || epos < 0) return null;
		String imgtag = inDesc.substring(spos + 4, epos);
		// Find the src attribute from img tag
		int quotepos = imgtag.toLowerCase().indexOf("src=");
		if (quotepos < 0) return null;
		// source may be quoted with single or double quotes
		char quotechar = imgtag.charAt(quotepos + 4);
		int equotepos = imgtag.indexOf(quotechar, quotepos + 7);
		if (equotepos < 0) return null;
		return imgtag.substring(quotepos + 5, equotepos);
	}

	/**
	 * Construct the String array for the given coordinates and name
	 * @param inCoordinates coordinate string in Kml format
	 * @param inName name of waypoint, or null if track point
	 * @param inDesc description of waypoint, if any
	 * @param inDesc timestamp of waypoint, if any
	 * @return String array for point
	 */
	private static String[] makeStringArray(String inCoordinates,
		String inName, String inDesc, String inTimestamp)
	{
		String[] result = new String[7];
		String[] values = inCoordinates.split(",");
		final int numValues = values.length;
		if (numValues == 3 || numValues == 2) {
			System.arraycopy(values, 0, result, 0, numValues);
		}
		result[3] = inName;
		result[4] = inDesc;
		result[6] = inTimestamp;
		return result;
	}


	/**
	 * @see tim.prune.load.xml.XmlHandler#getFieldArray()
	 */
	public Field[] getFieldArray()
	{
		final Field[] fields = {Field.LONGITUDE, Field.LATITUDE, Field.ALTITUDE,
			Field.WAYPT_NAME, Field.DESCRIPTION, Field.NEW_SEGMENT, Field.TIMESTAMP};
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
		for (int i=0; i<numPoints; i++) {
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
}
