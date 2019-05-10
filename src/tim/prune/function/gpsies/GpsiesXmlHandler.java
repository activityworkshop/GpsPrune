package tim.prune.function.gpsies;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import tim.prune.function.search.SearchResult;

/**
 * XML handler for dealing with XML returned from gpsies.com
 */
public class GpsiesXmlHandler extends DefaultHandler
{
	private String _value = null;
	private ArrayList<SearchResult> _trackList = null;
	private SearchResult _track = null;


	/**
	 * React to the start of an XML tag
	 */
	public void startElement(String inUri, String inLocalName, String inTagName,
		Attributes inAttributes) throws SAXException
	{
		if (inTagName.equals("tracks")) {
			_trackList = new ArrayList<SearchResult>();
		}
		else if (inTagName.equals("track")) {
			_track = new SearchResult();
		}
		_value = null;
		super.startElement(inUri, inLocalName, inTagName, inAttributes);
	}

	/**
	 * React to the end of an XML tag
	 */
	public void endElement(String inUri, String inLocalName, String inTagName)
	throws SAXException
	{
		if (inTagName.equals("track")) {
			_trackList.add(_track);
		}
		else if (inTagName.equals("title")) {
			_track.setTrackName(_value);
		}
		else if (inTagName.equals("description")) {
			_track.setDescription(_value);
		}
		else if (inTagName.equals("fileId")) {
			_track.setWebUrl("https://gpsies.com/map.do?fileId=" + _value);
		}
		else if (inTagName.equals("trackLengthM")) {
			try {
				_track.setLength(Double.parseDouble(_value));
			}
			catch (NumberFormatException nfe) {}
		}
		else if (inTagName.equals("downloadLink")) {
			_track.setDownloadLink(_value);
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
}
