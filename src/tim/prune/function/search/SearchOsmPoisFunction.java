package tim.prune.function.search;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.Distance;
import tim.prune.data.Latitude;
import tim.prune.data.Longitude;
import tim.prune.data.Unit;

/**
 * Function to load nearby point information from OSM
 */
public class SearchOsmPoisFunction extends GenericDownloaderFunction
{
	/** Maximum distance from point in m */
	private static final int MAX_DISTANCE = 250;
	/** Coordinates to search for */
	private double _searchLatitude = 0.0, _searchLongitude = 0.0;


	/**
	 * Constructor
	 * @param inApp App object
	 */
	public SearchOsmPoisFunction(App inApp) {
		super(inApp);
	}

	/**
	 * @return name key
	 */
	public String getNameKey() {
		return "function.searchosmpois";
	}

	/**
	 * @param inColNum index of column, 0 or 1
	 * @return key for this column
	 */
	protected String getColumnKey(int inColNum)
	{
		if (inColNum == 0) return "dialog.osmpois.column.name";
		return "dialog.osmpois.column.type";
	}


	/**
	 * Run method to get the nearby points in a separate thread
	 */
	public void run()
	{
		_statusLabel.setText(I18nManager.getText("confirm.running"));
		// Get coordinates from current point (if any) or from centre of screen
		DataPoint point = _app.getTrackInfo().getCurrentPoint();
		if (point == null)
		{
			_searchLatitude = _app.getViewport().getCentreLatitude();
			_searchLongitude = _app.getViewport().getCentreLongitude();
		}
		else
		{
			_searchLatitude  = point.getLatitude().getDouble();
			_searchLongitude = point.getLongitude().getDouble();
		}

		// Submit search (language not an issue here)
		submitSearch(_searchLatitude, _searchLongitude);

		// Set status label according to error or "none found", leave blank if ok
		if (_errorMessage == null && _trackListModel.isEmpty()) {
			_errorMessage = I18nManager.getText("dialog.osmpois.nonefound");
		}
		_statusLabel.setText(_errorMessage == null ? "" : _errorMessage);
	}

	/**
	 * Submit the search for the given parameters
	 * @param inLat latitude
	 * @param inLon longitude
	 */
	private void submitSearch(double inLat, double inLon)
	{
		String coords = "around:" + MAX_DISTANCE + "," + inLat + "," + inLon;
		String urlString = "http://overpass-api.de/api/interpreter?data=("
			+ "node(" + coords + ")[\"amenity\"][\"name\"];"
			+ "node(" + coords + ")[\"railway\"][\"name\"];"
			+ "node(" + coords + ")[\"highway\"][\"name\"];"
			+ ");out%20qt;";
		//System.out.println(urlString);
		// Parse the returned XML with a special handler
		SearchOsmPoisXmlHandler xmlHandler = new SearchOsmPoisXmlHandler();

		try
		{
			URL url = new URL(urlString);
			SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
			try (InputStream inStream = url.openStream()) {
				saxParser.parse(inStream, xmlHandler);
			} catch (Exception e) {
				_errorMessage = e.getClass().getName() + " - " + e.getMessage();
			}
		} catch (MalformedURLException | SAXException | ParserConfigurationException ignored) {
		}

		// Calculate distances for each returned point
		DataPoint searchPoint = new DataPoint(Latitude.make(_searchLatitude),
			Longitude.make(_searchLongitude));
		Unit distUnit = getConfig().getUnitSet().getDistanceUnit();
		for (SearchResult result : xmlHandler.getPointList())
		{
			DataPoint foundPoint = new DataPoint(Latitude.make(result.getLatitude()),
				Longitude.make(result.getLongitude()));
			double dist = DataPoint.calculateRadiansBetween(searchPoint, foundPoint);
			result.setLength(Distance.convertRadiansToDistance(dist, distUnit));
		}

		// TODO: maybe limit number of results using MAX_RESULTS
		// Add track list to model
		ArrayList<SearchResult> pointList = xmlHandler.getPointList();
		_trackListModel.addTracks(pointList, true);
		_trackListModel.setShowPointTypes(true);
	}
}
