package tim.prune.function.gpsies;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * XML handler for dealing with XML returned from gpsies.com
 */
public class GpsiesXmlHandler extends DefaultHandler
{
	private boolean _inTracks = false;
	private boolean _inTrack = false;
	private boolean _inTrackName = false;
	private boolean _inDescription = false;
	private boolean _inFileId = false;
	private boolean _inTrackLength = false;
	private boolean _inLink = false;
	private String _value = null;
	private ArrayList<GpsiesTrack> _trackList = null;
	private GpsiesTrack _track = null;


	/**
	 * React to the start of an XML tag
	 */
	public void startElement(String inUri, String inLocalName, String inTagName,
		Attributes inAttributes) throws SAXException
	{
		if (inTagName.equals("tracks")) {
			_inTracks = true;
			_trackList = new ArrayList<GpsiesTrack>();
		}
		else if (_inTracks && inTagName.equals("track")) {
			_inTrack = true;
			_track = new GpsiesTrack();
		}
		else if (_inTrack && inTagName.equals("title")) {_inTrackName = true;}
		else if (_inTrack && inTagName.equals("description")) {_inDescription = true;}
		else if (_inTrack && inTagName.equals("fileId")) {_inFileId = true;}
		else if (_inTrack && inTagName.equals("trackLengthM")) {_inTrackLength = true;}
		else if (_inTrack && inTagName.equals("downloadLink")) {_inLink = true;}
		super.startElement(inUri, inLocalName, inTagName, inAttributes);
	}

	/**
	 * React to the end of an XML tag
	 */
	public void endElement(String inUri, String inLocalName, String inTagName)
	throws SAXException
	{
		if (inTagName.equals("tracks")) {_inTracks = false;}
		else if (_inTrack && inTagName.equals("track")) {
			_trackList.add(_track);
			_inTrack = false;
		}
		else if (_inTrackName && inTagName.equals("title")) {
			_track.setTrackName(_value);
			_inTrackName = false;
		}
		else if (_inDescription && inTagName.equals("description")) {
			_track.setDescription(_value);
			_inDescription = false;
		}
		else if (_inFileId && inTagName.equals("fileId")) {
			_track.setFileId(_value);
			_inFileId = false;
		}
		else if (_inTrackLength && inTagName.equals("trackLengthM")) {
			try {
				_track.setLength(Double.parseDouble(_value));
			}
			catch (NumberFormatException nfe) {}
			_inTrackLength = false;
		}
		else if (_inLink && inTagName.equals("downloadLink")) {
			_track.setDownloadLink(_value);
			_inLink = false;
		}
		super.endElement(inUri, inLocalName, inTagName);
	}

	/**
	 * React to characters received inside tags
	 */
	public void characters(char[] inCh, int inStart, int inLength)
	throws SAXException
	{
		_value = new String(inCh, inStart, inLength);
		// System.out.println("Value: '" + value + "'");
		// TODO: Note, this doesn't cope well with split characters for really long descriptions etc
		super.characters(inCh, inStart, inLength);
	}

	/**
	 * @return the list of tracks
	 */
	public ArrayList<GpsiesTrack> getTrackList()
	{
		return _trackList;
	}
}
