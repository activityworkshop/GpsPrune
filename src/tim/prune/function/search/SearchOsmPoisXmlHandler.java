package tim.prune.function.search;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * XML handler for dealing with XML returned from the OSM Overpass api,
 * specially for the OSM Poi service
 */
public class SearchOsmPoisXmlHandler extends DefaultHandler
{
	private ArrayList<SearchResult> _pointList = null;
	private SearchResult _currPoint = null;


	/**
	 * React to the start of an XML tag
	 */
	public void startElement(String inUri, String inLocalName, String inTagName,
		Attributes inAttributes) throws SAXException
	{
		if (inTagName.equals("osm")) {
			_pointList = new ArrayList<>();
		}
		else if (inTagName.equals("node"))
		{
			_currPoint = new SearchResult();
			_currPoint.setLatitude(inAttributes.getValue("lat"));
			_currPoint.setLongitude(inAttributes.getValue("lon"));
		}
		else if (inTagName.equals("tag") && _currPoint != null) {
			processTag(inAttributes);
		}
		super.startElement(inUri, inLocalName, inTagName, inAttributes);
	}

	/**
	 * @param inAttributes attributes to process
	 */
	private void processTag(Attributes inAttributes)
	{
		String key = inAttributes.getValue("k");
		if (key != null)
		{
			String value = inAttributes.getValue("v");
			if (key.equals("name")) {
				_currPoint.setTrackName(value);
			}
			else if (key.equals("amenity") || key.equals("highway") || key.equals("railway")) {
				_currPoint.setPointType(value);
			}
		}
	}

	/**
	 * React to the end of an XML tag
	 */
	public void endElement(String inUri, String inLocalName, String inTagName)
	throws SAXException
	{
		if (inTagName.equals("node"))
		{
			// end of the entry
			if (_currPoint.getTrackName() != null && !_currPoint.getTrackName().equals(""))
			_pointList.add(_currPoint);
		}
		super.endElement(inUri, inLocalName, inTagName);
	}

	/**
	 * @return the list of points
	 */
	public ArrayList<SearchResult> getPointList()
	{
		return _pointList;
	}
}
