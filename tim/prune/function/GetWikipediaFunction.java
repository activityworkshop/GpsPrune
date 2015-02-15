package tim.prune.function;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.Latitude;
import tim.prune.data.Longitude;
import tim.prune.function.gpsies.GenericDownloaderFunction;
import tim.prune.function.gpsies.GpsiesTrack;

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
	 * Run method to call geonames in separate thread
	 */
	public void run()
	{
		_statusLabel.setText(I18nManager.getText("confirm.running"));
		// Get coordinates from current point (if any) or from centre of screen
		double lat = 0.0, lon = 0.0;
		DataPoint point = _app.getTrackInfo().getCurrentPoint();
		if (point == null)
		{
			double[] coords = _app.getViewport().getBounds();
			lat = (coords[0] + coords[2]) / 2.0;
			lon = (coords[1] + coords[3]) / 2.0;
		}
		else {
			lat = point.getLatitude().getDouble();
			lon = point.getLongitude().getDouble();
		}

		String descMessage = "";
		InputStream inStream = null;

		// Example http://api.geonames.org/findNearbyWikipedia?lat=47&lng=9
		String urlString = "http://api.geonames.org/findNearbyWikipedia?lat=" +
			lat + "&lng=" + lon + "&maxRows=" + MAX_RESULTS
			+ "&radius=" + MAX_DISTANCE + "&lang=" + I18nManager.getText("wikipedia.lang")
			+ "&username=" + GEONAMES_USERNAME;
		// Parse the returned XML with a special handler
		GetWikipediaXmlHandler xmlHandler = new GetWikipediaXmlHandler();
		try
		{
			URL url = new URL(urlString);
			SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
			inStream = url.openStream();
			saxParser.parse(inStream, xmlHandler);
		}
		catch (Exception e) {
			descMessage = e.getClass().getName() + " - " + e.getMessage();
		}
		// Close stream and ignore errors
		try {
			inStream.close();
		} catch (Exception e) {}
		// Add track list to model
		ArrayList<GpsiesTrack> trackList = xmlHandler.getTrackList();
		_trackListModel.addTracks(trackList);

		// Set status label according to error or "none found", leave blank if ok
		if (descMessage.equals("") && (trackList == null || trackList.size() == 0)) {
			descMessage = I18nManager.getText("dialog.gpsies.nonefound");
		}
		_statusLabel.setText(descMessage);
		// Show error message if any
		if (trackList == null || trackList.size() == 0) {
			String error = xmlHandler.getErrorMessage();
			if (error != null && !error.equals("")) {
				_app.showErrorMessageNoLookup(getNameKey(), error);
			}
		}
	}


	/**
	 * Load the selected track or point
	 */
	protected void loadSelected()
	{
		// Find the row selected in the table and get the corresponding track
		int rowNum = _trackTable.getSelectedRow();
		if (rowNum >= 0 && rowNum < _trackListModel.getRowCount())
		{
			String coords = _trackListModel.getTrack(rowNum).getDownloadLink();
			String[] latlon = coords.split(",");
			if (latlon.length == 2)
			{
				DataPoint point = new DataPoint(new Latitude(latlon[0]), new Longitude(latlon[1]), null);
				point.setFieldValue(Field.WAYPT_NAME, _trackListModel.getTrack(rowNum).getTrackName(), false);
				_app.createPoint(point);
			}
		}
		// Close the dialog
		_cancelled = true;
		_dialog.dispose();
	}
}
