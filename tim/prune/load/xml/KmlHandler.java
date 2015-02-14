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
	private boolean _insidePlacemark = false;
	private boolean _insideName = false;
	private boolean _insideCoordinates = false;
	private String _name = null;
	private StringBuffer _coordinates = null;
	private ArrayList<String[]> _pointList = new ArrayList<String[]>();


	/**
	 * Receive the start of a tag
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException
	{
		String tagName = localName;
		if (tagName == null || tagName.equals("")) {tagName = qName;}
		if (tagName.equalsIgnoreCase("Placemark")) _insidePlacemark = true;
		else if (tagName.equalsIgnoreCase("coordinates")) {_insideCoordinates = true; _coordinates = null;}
		else if (tagName.equalsIgnoreCase("name")) {_insideName = true; _name = null;}
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
		if (tagName.equalsIgnoreCase("Placemark"))
		{
			processPlacemark();
			_insidePlacemark = false;
		}
		else if (tagName.equalsIgnoreCase("coordinates")) _insideCoordinates = false;
		else if (tagName.equalsIgnoreCase("name")) _insideName = false;
		super.endElement(uri, localName, qName);
	}


	/**
	 * Process character text (inside tags or between them)
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length)
			throws SAXException
	{
		if (_insidePlacemark && (_insideName || _insideCoordinates))
		{
			String value = new String(ch, start, length);
			if (_insideName) {_name = value;}
			else if (_insideCoordinates)
			{
				if (_coordinates == null)
				{
					_coordinates = new StringBuffer();
				}
				_coordinates.append(value);
			}
		}
		super.characters(ch, start, length);
	}


	/**
	 * Process a placemark entry, either a single waypoint or a whole track
	 */
	private void processPlacemark()
	{
		if (_coordinates == null) return;
		String allCoords = _coordinates.toString();
		String[] coordArray = allCoords.split("[ \n]");
		int numPoints = coordArray.length;
		if (numPoints == 1)
		{
			// Add single waypoint to list
			_pointList.add(makeStringArray(allCoords, _name));
		}
		else if (numPoints > 1)
		{
			// Add each of the unnamed track points to list
			boolean firstPoint = true;
			for (int p=0; p<numPoints; p++)
			{
				if (coordArray[p] != null && coordArray[p].trim().length()>3)
				{
					String[] pointArray = makeStringArray(coordArray[p], null);
					if (firstPoint) {pointArray[4] = "1";}
					firstPoint = false;
					_pointList.add(pointArray);
				}
			}
		}
	}


	/**
	 * Construct the String array for the given coordinates and name
	 * @param inCoordinates coordinate string in Kml format
	 * @param inName name of waypoint, or null if track point
	 * @return String array for point
	 */
	private static String[] makeStringArray(String inCoordinates, String inName)
	{
		String[] result = new String[5];
		String[] values = inCoordinates.split(",");
		if (values.length == 3) {System.arraycopy(values, 0, result, 0, 3);}
		result[3] = inName;
		return result;
	}


	/**
	 * @see tim.prune.load.xml.XmlHandler#getFieldArray()
	 */
	public Field[] getFieldArray()
	{
		final Field[] fields = {Field.LONGITUDE, Field.LATITUDE, Field.ALTITUDE, Field.WAYPT_NAME, Field.NEW_SEGMENT};
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
