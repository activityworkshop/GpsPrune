package tim.prune.function;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import tim.prune.function.search.SearchResult;

/**
 * XML handler for dealing with XML returned from the opencaching.de api
 */
public class OpenCachingDeXmlHandler extends DefaultHandler
{
	private String _value = null;
	private ArrayList<SearchResult> _trackList = null;
	private SearchResult _track = null;
	private String _errorMessage = null;


	/**
	 * React to the start of an XML tag
	 */
	public void startElement(String inUri, String inLocalName, String inTagName,
		Attributes inAttributes) throws SAXException
	{
		if (inTagName.equals("result")) {
			_trackList = new ArrayList<SearchResult>();
		}
		else if (inTagName.equals("cache"))
		{
			_track = new SearchResult();
		}
//		else if (inTagName.equals("status")) {
//			_errorMessage = inAttributes.getValue("message");
//		}
		else _value = null;
		super.startElement(inUri, inLocalName, inTagName, inAttributes);
	}

	/**
	 * React to the end of an XML tag
	 */
	public void endElement(String inUri, String inLocalName, String inTagName)
	throws SAXException
	{
		if (inTagName.equals("cache"))
		{
			// end of the entry
			_trackList.add(_track);
		}
		else if (inTagName.equals("name")) {
			_track.setTrackName(_value);
		}
		else if (inTagName.equals("desc")) {
			_track.setDescription(_value);
		}
		else if (inTagName.equals("lat")) {
			_track.setLatitude(_value);
		}
		else if (inTagName.equals("lon")) {
			_track.setLongitude(_value);
		}
		else if (inTagName.equals("distance")) {
			try {
				_track.setLength(Double.parseDouble(_value) * 1000.0); // convert from km to m
			}
			catch (NumberFormatException nfe) {}
		}
		else if (inTagName.equals("link")) {
			_track.setWebUrl(_value);
		}
		super.endElement(inUri, inLocalName, inTagName);
	}

	/**
	 * React to characters received inside tags
	 */
	public void characters(char[] inCh, int inStart, int inLength)
	throws SAXException
	{
		String value = new String(inCh, inStart, inLength);
		_value = (_value==null?value:_value+value);
		super.characters(inCh, inStart, inLength);
	}

	/**
	 * @return the list of tracks
	 */
	public ArrayList<SearchResult> getTrackList()
	{
		return _trackList;
	}

	/**
	 * @return error message, if any
	 */
	public String getErrorMessage() {
		return _errorMessage;
	}
}
