package tim.prune.function.search;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.data.DataPoint;

/**
 * Function to load nearby point information from Wikipedia
 * according to the currently viewed area
 */
public class GetWikipediaFunction extends GenericDownloaderFunction
{
	/** Maximum number of results to get */
	private static final int MAX_RESULTS = 20;
	/** Maximum distance from point in km */
	private static final int MAX_DISTANCE = 15;
	/** Username to use for geonames queries */
	private static final String GEONAMES_USERNAME = "gpsprune";


	/**
	 * Constructor
	 * @param inApp App object
	 */
	public GetWikipediaFunction(App inApp) {
		super(inApp);
	}

	/**
	 * @return name key
	 */
	public String getNameKey() {
		return "function.getwikipedia";
	}

	/**
	 * @param inColNum index of column, 0 or 1
	 * @return key for this column
	 */
	protected String getColumnKey(int inColNum)
	{
		if (inColNum == 0) return "dialog.wikipedia.column.name";
		return "dialog.wikipedia.column.distance";
	}


	/**
	 * Run method to get the nearby points in a separate thread
	 */
	public void run()
	{
		_statusLabel.setText(I18nManager.getText("confirm.running"));
		// Get coordinates from current point (if any) or from centre of screen
		double lat = 0.0, lon = 0.0;
		DataPoint point = _app.getTrackInfo().getCurrentPoint();
		if (point == null)
		{
			lat = _app.getViewport().getCentreLatitude();
			lon = _app.getViewport().getCentreLongitude();
		}
		else
		{
			lat = point.getLatitude().getDouble();
			lon = point.getLongitude().getDouble();
		}

		// For geonames, firstly try the local language
		String lang = I18nManager.getText("wikipedia.lang");
		submitSearch(lat, lon, lang);
		// If we didn't get anything, try a secondary language
		if (_trackListModel.isEmpty() && _errorMessage == null && lang.equals("als")) {
			submitSearch(lat, lon, "de");
		}
		// If still nothing then try english
		if (_trackListModel.isEmpty() && _errorMessage == null && !lang.equals("en")) {
			submitSearch(lat, lon, "en");
		}

		// Set status label according to error or "none found", leave blank if ok
		if (_errorMessage == null && _trackListModel.isEmpty()) {
			_errorMessage = I18nManager.getText("dialog.wikipedia.nonefound");
		}
		_statusLabel.setText(_errorMessage == null ? "" : _errorMessage);
	}

	/**
	 * Submit the search for the given parameters
	 * @param inLat latitude
	 * @param inLon longitude
	 * @param inLang language code to use, such as en or de
	 */
	private void submitSearch(double inLat, double inLon, String inLang)
	{
		// Example http://api.geonames.org/findNearbyWikipedia?lat=47&lng=9
		String urlString = "http://api.geonames.org/findNearbyWikipedia?lat=" +
			inLat + "&lng=" + inLon + "&maxRows=" + MAX_RESULTS
			+ "&radius=" + MAX_DISTANCE + "&lang=" + inLang
			+ "&username=" + GEONAMES_USERNAME;
		// Parse the returned XML with a special handler
		GetWikipediaXmlHandler xmlHandler = new GetWikipediaXmlHandler();
		InputStream inStream = null;

		try
		{
			URL url = new URL(urlString);
			SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
			inStream = url.openStream();
			saxParser.parse(inStream, xmlHandler);
		}
		catch (Exception e) {
			_errorMessage = e.getClass().getName() + " - " + e.getMessage();
		}
		// Close stream and ignore errors
		try {
			inStream.close();
		} catch (Exception e) {}
		// Add track list to model
		ArrayList<SearchResult> trackList = xmlHandler.getTrackList();
		_trackListModel.addTracks(trackList, true);

		// Show error message if any
		if (_trackListModel.isEmpty())
		{
			String error = xmlHandler.getErrorMessage();
			if (error != null && !error.equals(""))
			{
				_app.showErrorMessageNoLookup(getNameKey(), error);
				_errorMessage = error;
			}
		}
	}
}
