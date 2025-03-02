package tim.prune.load.xml;

import java.util.ArrayList;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import tim.prune.data.ExtensionInfo;
import tim.prune.data.Field;
import tim.prune.data.FieldGpx;
import tim.prune.data.FieldXml;
import tim.prune.data.FileType;


/**
 * Class for handling specifics of parsing Gpx files
 */
public class GpxHandler extends XmlHandler
{
	private boolean _insidePoint = false;
	private boolean _insideWaypoint = false;
	private boolean _insideExtensions = false;
	private boolean _startSegment = true;
	private int _trackNum = -1;
	private final GpxTag _fileTitle = new GpxTag(), _fileDescription = new GpxTag();
	private final GpxTag _pointName = new GpxTag(), _trackName = new GpxTag();
	private final GpxTag _elevation = new GpxTag(), _time = new GpxTag();
	private final GpxTag _type = new GpxTag(), _description = new GpxTag();
	private final GpxTag _link = new GpxTag(), _comment = new GpxTag();
	private final GpxTag _sym = new GpxTag();
	private GpxTag _currentTag = null;
	private final ExtensionInfo _extensionInfo = new ExtensionInfo();
	private final ArrayList<String[]> _pointList = new ArrayList<>();
	private final ArrayList<String> _linkList = new ArrayList<>();
	private Stack<String> _extensionTags = null;
	private FieldGpx _gpxField = null;


	/** Constructor, setting up the fields */
	public GpxHandler()
	{
		final Field[] fields = {Field.LATITUDE, Field.LONGITUDE, Field.ALTITUDE,
			Field.WAYPT_NAME, Field.TIMESTAMP, Field.NEW_SEGMENT,
			Field.WAYPT_TYPE, Field.DESCRIPTION, Field.COMMENT, Field.SYMBOL};
		for (Field field : fields) {
			addField(field);
		}
	}

	/**
	 * Receive the start of a tag
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String localName, String qName, Attributes attributes)
		throws SAXException
	{
		// Read the parameters for waypoints and track points
		String tag = qName.toLowerCase();
		_gpxField = null;
		if (tag.equals("wpt") || tag.equals("trkpt") || tag.equals("rtept"))
		{
			_insidePoint = true;
			_insideWaypoint = tag.equals("wpt");
			resetCurrentValues();
			addCurrentValue(Field.LATITUDE, getAttribute(attributes, "lat"));
			addCurrentValue(Field.LONGITUDE, getAttribute(attributes, "lon"));
			_elevation.setValue(null);
			_pointName.setValue(null);
			_time.setValue(null);
			_type.setValue(null);
			_link.setValue(null);
			_description.setValue(null);
			_comment.setValue(null);
			_sym.setValue(null);
		}
		else if (tag.equals("ele")) {
			_currentTag = _elevation;
		}
		else if (tag.equals("name"))
		{
			if (_insidePoint) {
				_currentTag = _pointName;
			}
			else if (_trackNum < 0) {
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
		else if (tag.equals("description") || tag.equals("desc"))
		{
			if (_insidePoint) {
				_currentTag = _description;
			}
			else {
				_currentTag = _fileDescription;
			}
		}
		else if (tag.equals("cmt")) {
			_currentTag = _comment;
		}
		else if (tag.equals("sym")) {
			_currentTag = _sym;
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
		else if (tag.equals("extensions") && _insidePoint)
		{
			_insideExtensions = true;
			_currentTag = new GpxTag();
			_extensionTags = new Stack<>();
		}
		else if (_insideExtensions)
		{
			_extensionTags.add(qName);
			_currentTag.clear();
		}
		else if (tag.equals("gpx"))
		{
			setFileType(FileType.GPX);
			processGpxAttributes(attributes);
		}
		else
		{
			// Maybe it's a recognised gpx field like hdop
			_gpxField = FieldGpx.getField(tag);
			_currentTag = new GpxTag();
		}
		super.startElement(uri, localName, qName, attributes);
	}


	/** Process the attributes from the main gpx tag including extensions */
	private void processGpxAttributes(Attributes attributes)
	{
		// System.out.println("Start gpx element: " + qName);
		final int numAttributes = attributes.getLength();
		for (int i=0; i<numAttributes; i++)
		{
			String attributeName = attributes.getQName(i).toLowerCase();
			String attrValue = attributes.getValue(i);
			// System.out.println("   Attribute '" + attributeName + "' - '" + attributes.getValue(i) + "'");
			if (attributeName.equals("version")) {
				setFileVersion(attributes.getValue(i));
			}
			else if (attributeName.contentEquals("xmlns")) {
				_extensionInfo.setNamespace(attrValue);
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
				String prefix = attributeName.substring(6);
				_extensionInfo.addNamespace(prefix, attrValue);
			}
		}
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
		else if (tag.equals("extensions")) {
			_insideExtensions = false;
		}
		else if (_insideExtensions)
		{
			String value = _currentTag.getValue();
			_extensionTags.pop();
			if (!value.isEmpty())
			{
				FieldXml field = new FieldXml(FileType.GPX, tag, _extensionTags);
				if (!hasField(field)) {
					addField(field);
				}
				addCurrentValue(field, value);
			}
			_currentTag.clear();
		}
		else if (_gpxField != null)
		{
			String value = _currentTag.getValue();
			if (!value.isEmpty())
			{
				if (!hasField(_gpxField)) {
					addField(_gpxField);
				}
				addCurrentValue(_gpxField, value);
			}
			_currentTag.clear();
			_gpxField = null;
		}
		else if (_insidePoint && _currentTag != null && getFileVersion().equals("1.0"))
		{
			String value = _currentTag.getValue();
			String tagNamespace = getNamespace(tag);
			if (tagNamespace != null && !value.isEmpty())
			{
				String id = this.getExtensionInfo().getNamespaceName(tagNamespace);
				if (id != null)
				{
					FieldXml field = new FieldXml(FileType.GPX, tag, id);
					if (!hasField(field)) {
						addField(field);
					}
					addCurrentValue(field, value);
				}
			}
			_currentTag = null;
		}
		else {
			_currentTag = null;
		}
		super.endElement(uri, localName, qName);
	}


	private static String getNamespace(String inTagName)
	{
		int firstColonPos = inTagName.indexOf(':');
		if (firstColonPos > 0) {
			return inTagName.substring(0, firstColonPos);
		}
		return null;
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
		if (inVariable == null) {
			return inValue;
		}
		return inVariable + inValue;
	}


	/**
	 * Process a point, either a waypoint or track point
	 */
	private void processPoint()
	{
		// Values go into a String array matching the order in getFieldArray()
		addCurrentValue(Field.ALTITUDE, _elevation.getValue());
		if (_insideWaypoint) {
			addCurrentValue(Field.WAYPT_NAME, _pointName.getValue());
		}
		addCurrentValue(Field.TIMESTAMP, _time.getValue());
		if (_startSegment && !_insideWaypoint)
		{
			addCurrentValue(Field.NEW_SEGMENT, "1");
			_startSegment = false;
		}
		addCurrentValue(Field.WAYPT_TYPE, _type.getValue());
		addCurrentValue(Field.DESCRIPTION, _description.getValue());
		addCurrentValue(Field.COMMENT, _comment.getValue());
		addCurrentValue(Field.SYMBOL, _sym.getValue());
		_pointList.add(getCurrentValues());
		_linkList.add(_link.getValue());
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
		if (!hasLink) {
			result = null;
		}
		return result;
	}

	/**
	 * @return file title
	 */
	public String getFileTitle() {
		return _fileTitle.getValue();
	}

	/**
	 * @return file description
	 */
	public String getFileDescription() {
		return _fileDescription.getValue();
	}

	@Override
	public ExtensionInfo getExtensionInfo() {
		return _extensionInfo;
	}
}
