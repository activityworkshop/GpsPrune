package tim.prune.load.xml;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import tim.prune.data.ExtensionInfo;
import tim.prune.data.Field;
import tim.prune.data.FieldXml;
import tim.prune.data.FileType;


/**
 * Class for handling specifics of parsing Kml files
 */
public class KmlHandler extends XmlHandler
{
	private String _fileTitle = null;
	private boolean _insideCoordinates = false;
	private boolean _insideTrack = false;
	private boolean _insideExtendedData = false;
	private String _value = null;
	private String _name = null, _desc = null;
	private String _timestamp = null, _imgLink = null;
	private StringBuffer _coordinates = null;
	private ArrayList<String> _coordinateList = null;
	private ArrayList<String[]> _pointList = new ArrayList<>();
	private ArrayList<String> _linkList = new ArrayList<>();
	private final ExtensionInfo _extensionInfo = new ExtensionInfo();
	// variables for gx extensions / 2.3 timestamps
	private ArrayList<String> _whenList = new ArrayList<>();
	private ArrayList<String> _whereList = new ArrayList<>();
	private ArrayList<String> _valueList = null;
	private Field _extensionField = null;
	private ArrayList<KmlExtendedDataField> _extendedDataFields = null;


	/** Constructor, setting up the basic fields */
	public KmlHandler()
	{
		final Field[] fields = {Field.LONGITUDE, Field.LATITUDE, Field.ALTITUDE,
			Field.WAYPT_NAME, Field.DESCRIPTION, Field.NEW_SEGMENT, Field.TIMESTAMP};
		for (Field field : fields) {
			addField(field);
		}
	}

	/**
	 * Receive the start of a tag
	 */
	public void startElement(String uri, String localName, String qName,
		Attributes inAttributes) throws SAXException
	{
		String tagName = localName;
		if (tagName == null || tagName.equals("")) {tagName = qName;}
		tagName = tagName.toLowerCase();

		if (tagName.equals("placemark")) {
			_coordinateList = new ArrayList<String>();
		}
		else if (tagName.equals("coordinates"))
		{
			_insideCoordinates = true;
			_coordinates = null;
		}
		else if (tagName.equals("gx:track")) {
			_insideTrack = true;
		}
		else if (tagName.equals("track"))
		{
			_insideTrack = true;
			if ("2.2".equals(getFileVersion())) {
				setFileVersion("2.3");
			}
		}
		else if (tagName.equals("extendeddata"))
		{
			_insideExtendedData = true;
			if (_extendedDataFields == null) {
				_extendedDataFields = new ArrayList<>();
			}
			else {
				_extendedDataFields.clear();
			}
		}
		else if (_insideExtendedData
			&& (tagName.equals("simplearraydata") || tagName.equals("gx:simplearraydata")))
		{
			String fieldName = getAttribute(inAttributes, "name");
			_extensionField = new FieldXml(FileType.KML, fieldName, (String) null);
			if (!hasField(_extensionField)) {
				addField(_extensionField);
			}
			if (_valueList == null) {
				_valueList = new ArrayList<>();
			}
			else {
				_valueList.clear();
			}
		}
		else if (tagName.equals("kml"))
		{
			setFileType(FileType.KML);
			processKmlAttributes(inAttributes);
		}
		_value = null;
		super.startElement(uri, localName, qName, inAttributes);
	}


	/** Process the attributes of the main kml tag, including extensions */
	private void processKmlAttributes(Attributes attributes)
	{
		final int numAttributes = attributes.getLength();
		for (int i=0; i<numAttributes; i++)
		{
			String attributeName = attributes.getQName(i).toLowerCase();
			String attrValue = attributes.getValue(i);
			// System.out.println("   Attribute '" + attributeName + "' - '" + attributes.getValue(i) + "'");
			if (attributeName.equals("xmlns"))
			{
				String namespace = attrValue;
				_extensionInfo.setNamespace(namespace);
				if (namespace != null && namespace.length() > 3) {
					setFileVersion(namespace.substring(namespace.length() - 3));
				}
			}
			else if (attributeName.equals("xmlns:xsi")) {
				_extensionInfo.setXsi(attrValue);
			}
			else if (attributeName.equals("xsi:schemalocation"))
			{
				String[] schemas = attrValue.split(" ");
				for (int s=0; s<schemas.length; s++) {
					_extensionInfo.addXsiAttribute(schemas[s]);
				}
			}
			else if (attributeName.startsWith("xmlns:"))
			{
				String prefix= attributeName.substring(6);
				_extensionInfo.addNamespace(prefix, attrValue);
			}
		}
	}

	/**
	 * Process end tag
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
			if (_coordinateList == null) {
				_fileTitle = _value;
			}
			_name = _value;
		}
		else if (tagName.equals("description"))
		{
			_desc = _value;
			_imgLink = getImgLink(_desc);
		}
		else if (tagName.equals("when"))
		{
			if (!_insideTrack) {
				_timestamp = _value;
			}
			else {
				_whenList.add(_value);
			}
		}
		else if (tagName.equals("gx:coord") || tagName.equals("coord"))
		{
			if (_insideTrack) {
				_whereList.add(_value);
			}
		}
		else if (tagName.equals("gx:track") || tagName.equals("track"))
		{
			processTrack();
			_insideTrack = false;
		}
		else if (_insideExtendedData
			&& (tagName.equals("gx:value") || tagName.equals("value"))
			&& _valueList != null)
		{
			_valueList.add(_value);
		}
		else if (_insideExtendedData
			&& (tagName.equals("gx:simplearraydata") || tagName.equals("simplearraydata")))
		{
			_extendedDataFields.add(new KmlExtendedDataField(_extensionField, _valueList));
			_extensionField = null;
			_valueList.clear();
		}
		else if (tagName.equals("extendeddata"))
		{
			_insideExtendedData = false;
			_extensionField = null;
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
		else if (_value == null) {
			// Store string in _value
			_value = val;
		}
		else {
			_value = _value + val;
		}
		super.characters(ch, start, length);
	}


	/**
	 * Process a placemark entry, either a single waypoint or a whole track
	 */
	private void processPlacemark()
	{
		if (_coordinateList == null || _coordinateList.isEmpty()) {
			return;
		}
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
				_pointList.add(makeStringArray(true, coords, name, _desc, _timestamp));
				_linkList.add(_imgLink);
			}
			else if (numPoints > 1)
			{
				// Add each of the unnamed track points to list
				boolean firstPoint = true;
				for (String coord : coordArray)
				{
					if (coord != null && coord.trim().length()>3)
					{
						String[] pointArray = makeStringArray(firstPoint, coord, null, null, null);
						firstPoint = false;
						_pointList.add(pointArray);
					}
					_linkList.add(null);
				}
			}
		}
	}

	/**
	 * Process a track or gx track including timestamps and extended data
	 */
	private void processTrack()
	{
		if (!_whereList.isEmpty())
		{
			// If the whens don't match, then throw them all away
			if (_whenList.size() != _whereList.size()) {
				// TODO: Show warning about mismatch?
				_whenList.clear();
			}

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
						resetCurrentValues();
						addCurrentValue(Field.LONGITUDE, coords[0]);
						addCurrentValue(Field.LATITUDE, coords[1]);
						addCurrentValue(Field.ALTITUDE, coords[2]);
						// leave name and description empty
						addCurrentValue(Field.NEW_SEGMENT, firstPoint ? "1" : null);
						firstPoint = false;
						addCurrentValue(Field.TIMESTAMP, when);
						// Loop over extended data fields too and add values for these if available
						if (_extendedDataFields != null)
						{
							for (KmlExtendedDataField exField : _extendedDataFields) {
								addCurrentValue(exField.getField(), getFieldValue(exField, p));
							}
						}
						_pointList.add(getCurrentValues());
					}
				}
				_linkList.add(null);
			}
		}
		_whenList.clear();
		_whereList.clear();
	}

	/** Get the value for the specified point out of the extended data */
	private String getFieldValue(KmlExtendedDataField inField, int inPointIndex)
	{
		if (inField == null) {
			return null;
		}
		List<String> values = inField.getValues();
		if (values == null || values.isEmpty() || inPointIndex < 0 || inPointIndex >= values.size()) {
			return null;
		}
		return values.get(inPointIndex);
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
	 * @param inFirstPoint true if this is the first point in a segment
	 * @param inCoordinates coordinate string in Kml format
	 * @param inName name of waypoint, or null if track point
	 * @param inDesc description of waypoint, if any
	 * @param inTimestamp timestamp of waypoint, if any
	 * @return String array for point
	 */
	private String[] makeStringArray(boolean inFirstPoint, String inCoordinates,
		String inName, String inDesc, String inTimestamp)
	{
		resetCurrentValues();
		String[] values = inCoordinates.split(",");
		final int numValues = values.length;
		if (numValues == 3 || numValues == 2)
		{
			addCurrentValue(Field.LONGITUDE, values[0]);
			addCurrentValue(Field.LATITUDE, values[1]);
			if (numValues == 3) {
				addCurrentValue(Field.ALTITUDE, values[2]);
			}
		}
		addCurrentValue(Field.WAYPT_NAME, inName);
		addCurrentValue(Field.DESCRIPTION, inDesc);
		addCurrentValue(Field.TIMESTAMP, inTimestamp);
		addCurrentValue(Field.NEW_SEGMENT, inFirstPoint ? "1" : null);
		return getCurrentValues();
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

	/**
	 * @return file title
	 */
	public String getFileTitle() {
		return _fileTitle;
	}

	public ExtensionInfo getExtensionInfo() {
		return _extensionInfo;
	}
}
