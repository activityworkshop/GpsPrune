package tim.prune.function.search;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.data.DataPoint;

/**
 * Function to load information about geocaches nearby to the current point
 * using the service at opencaching.de
 */
public class SearchOpenCachingDeFunction extends GenericDownloaderFunction
{
	/** Maximum distance from point in km */
	private static final int MAX_DISTANCE = 50;


	/**
	 * Constructor
	 * @param inApp App object
	 */
	public SearchOpenCachingDeFunction(App inApp) {
		super(inApp);
	}

	/**
	 * @return name key
	 */
	public String getNameKey() {
		return "function.searchopencachingde";
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
	 * Run method to call service in a separate thread
	 */
	public void run()
	{
		_statusLabel.setText(I18nManager.getText("confirm.running"));
		// Get coordinates from current point
		DataPoint point = _app.getTrackInfo().getCurrentPoint();
		if (point == null) {
			return;
		}

		final double lat = point.getLatitude().getDouble();
		final double lon = point.getLongitude().getDouble();
		submitSearch(lat, lon);

		// Set status label according to error or "none found", leave blank if ok
		if (_errorMessage == null && _trackListModel.isEmpty()) {
			_errorMessage = I18nManager.getText("dialog.geocaching.nonefound");
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
		// The only parameters are lat and long from the current point
		String urlString = "https://opencaching.de/search.php?searchto=searchbydistance&showresult=1"
			+ "&output=XML&sort=bydistance&lat=" + inLat
			+ "&lon=" + inLon + "&distance=" + MAX_DISTANCE + "&unit=km";
		// Parse the returned XML with a special handler
		OpenCachingDeXmlHandler xmlHandler = new OpenCachingDeXmlHandler();

		try
		{
			URL url = new URL(urlString);
			SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
			try (InputStream inStream = url.openStream()) {
				saxParser.parse(inStream, xmlHandler);
			}
		} catch (SAXException | IOException | ParserConfigurationException ignored) {
		}

		// Add track list to model
		ArrayList<SearchResult> trackList = xmlHandler.getTrackList();
		_trackListModel.addTracks(trackList);
	}
}
