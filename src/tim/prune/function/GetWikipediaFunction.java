package tim.prune.function;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.Distance;
import tim.prune.data.Field;
import tim.prune.data.Latitude;
import tim.prune.data.Longitude;
import tim.prune.data.UnitSetLibrary;
import tim.prune.function.search.GenericDownloaderFunction;
import tim.prune.function.search.SearchResult;

/**
 * Function to load nearby point information from Wikipedia (and Wikimedia)
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
			double[] coords = _app.getViewport().getBounds();
			lat = (coords[0] + coords[2]) / 2.0;
			lon = (coords[1] + coords[3]) / 2.0;
		}
		else
		{
			lat = point.getLatitude().getDouble();
			lon = point.getLongitude().getDouble();
		}

		// Before we ask geonames online, let's get wikimedia galleries first
		searchWikimediaGalleries(lat, lon);

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

	/**
	 * Load the selected point(s)
	 */
	protected void loadSelected()
	{
		// Find the rows selected in the table and get the corresponding coords
		int numSelected = _trackTable.getSelectedRowCount();
		if (numSelected < 1) return;
		int[] rowNums = _trackTable.getSelectedRows();
		for (int i=0; i<numSelected; i++)
		{
			int rowNum = rowNums[i];
			if (rowNum >= 0 && rowNum < _trackListModel.getRowCount())
			{
				String lat = _trackListModel.getTrack(rowNum).getLatitude();
				String lon = _trackListModel.getTrack(rowNum).getLongitude();
				if (lat != null && lon != null)
				{
					DataPoint point = new DataPoint(new Latitude(lat), new Longitude(lon), null);
					point.setFieldValue(Field.WAYPT_NAME, _trackListModel.getTrack(rowNum).getTrackName(), false);
					_app.createPoint(point);
				}
			}
		}
		// Close the dialog
		_cancelled = true;
		_dialog.dispose();
	}

	/**
	 * Search the local wikimedia index to see if there are any galleries nearby
	 * @param inLat latitude
	 * @param inLon longitude
	 */
	private void searchWikimediaGalleries(double inLat, double inLon)
	{
		BufferedReader reader = null;
		try
		{
			InputStream in = GetWikipediaFunction.class.getResourceAsStream("/tim/prune/function/search/wikimedia_galleries.txt");
			reader = new BufferedReader(new InputStreamReader(in));

			ArrayList<SearchResult> trackList = new ArrayList<SearchResult>();
			DataPoint herePoint = new DataPoint(new Latitude(inLat, Latitude.FORMAT_DEG), new Longitude(inLon, Longitude.FORMAT_DEG), null);
			// Loop through the file line by line, looking for nearby points
			String line = null;
			while ((line = reader.readLine()) != null)
			{
				String[] lineComps = line.split("\t");
				if (lineComps.length == 4)
				{
					DataPoint p = new DataPoint(new Latitude(lineComps[2]), new Longitude(lineComps[3]), null);
					double distFromHere = Distance.convertRadiansToDistance(
						DataPoint.calculateRadiansBetween(p, herePoint), UnitSetLibrary.UNITS_KILOMETRES);
					if (distFromHere < MAX_DISTANCE)
					{
						SearchResult gallery = new SearchResult();
						gallery.setTrackName(I18nManager.getText("dialog.wikipedia.gallery") + ": " + lineComps[0]);
						gallery.setDescription(lineComps[1]);
						gallery.setLatitude(lineComps[2]);
						gallery.setLongitude(lineComps[3]);
						gallery.setWebUrl("https://commons.wikimedia.org/wiki/" + lineComps[0]);
						gallery.setLength(distFromHere * 1000.0); // convert from km to m
						trackList.add(gallery);
					}
				}
			}
			_trackListModel.addTracks(trackList, true);
		}
		catch (java.io.IOException e) {
			System.err.println("Exception trying to read wikimedia file : " + e.getMessage());
		}
		catch (NullPointerException e) {
			System.err.println("Couldn't find wikimedia file : " + e.getMessage());
		}
		finally
		{
			try {
				reader.close();
			}
			catch (Exception e) {} // ignore
		}
	}
}
