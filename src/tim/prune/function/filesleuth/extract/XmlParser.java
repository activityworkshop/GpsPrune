package tim.prune.function.filesleuth.extract;

import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Responsible for parsing fields out of gpx/kml files
 * and passing them back to its receiver
 */
public class XmlParser extends DefaultHandler
{
	private final ContentReceiver _receiver;
	private String _value = null;


	public XmlParser(ContentReceiver inReceiver) {
		_receiver = inReceiver;
	}

	/** @return true if parsing succeeds */
	public boolean parseXmlStream(InputStream inStream)
	{
		// Firstly, try to use xerces to parse the xml (will throw an exception if not available)
		try
		{
			SAXParser saxParser = SAXParserFactory.newInstance("org.apache.xerces.parsers.SAXParser", null).newSAXParser();
			saxParser.parse(inStream, this);
			return true; // worked
		}
		catch (Throwable ignored) {} // don't care too much if it didn't work, there's a backup

		// If that didn't work, try the built-in classes (which work for xml1.0 but handling for 1.1 contains bugs)
		try
		{
			// Construct a SAXParser and use this as a default handler
			SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
			saxParser.parse(inStream, this);
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
		String tag = qName.toLowerCase();
		if (isTextTag(tag) || isTimeTag(tag)) {
			_value = "";
		}
		else if (isPointTag(tag))
		{
			String latitudeString = attributes.getValue("lat");
			String longitudeString = attributes.getValue("lon");
			addCoordinates(latitudeString, longitudeString);
		}
		super.startElement(uri, localName, qName, attributes);
	}

	private void addCoordinates(String inLatitude, String inLongitude)
	{
		try
		{
			double lat = Double.parseDouble(inLatitude);
			double lon = Double.parseDouble(inLongitude);
			_receiver.addCoordinates(lat, lon);
		}
		catch (Exception ignored) {}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException
	{
		if (_value != null) {
			_value += new String(ch, start, length);
		}
		super.characters(ch, start, length);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
		if (_value != null)
		{
			String tag = qName.toLowerCase();
			if (isTextTag(tag))
			{
				_receiver.addString(_value);
				if ("name".equals(tag)) {
					_receiver.setName(_value);
				}
				else if ("desc".equals(tag)) {
					_receiver.setDescription(_value);
				}
			}
			else if (isTimeTag(tag)) {
				_receiver.addDateString(_value);
			}
			_value = null;
		}
		super.endElement(uri, localName, qName);
	}

	private boolean isTextTag(String inTag) {
		return "name".equals(inTag) || "desc".equals(inTag);
	}

	private boolean isTimeTag(String inTag) {
		return "when".equals(inTag) || "time".equals(inTag);
	}

	private boolean isPointTag(String inTag) {
		return "wpt".equals(inTag) || "trkpt".equals(inTag) || "rtept".equals(inTag);
	}

	@Override
	public void endDocument() throws SAXException
	{
		super.endDocument();
		_receiver.endDocument();
	}
}
