package tim.prune.function;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.data.Coordinate;
import tim.prune.data.DataPoint;
import tim.prune.data.Distance;
import tim.prune.data.Field;
import tim.prune.data.Latitude;
import tim.prune.data.Longitude;
import tim.prune.function.search.GenericDownloaderFunction;
import tim.prune.function.search.SearchResult;

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
			double[] coords  = _app.getViewport().getBounds();
			_searchLatitude  = (coords[0] + coords[2]) / 2.0;
			_searchLongitude = (coords[1] + coords[3]) / 2.0;
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

		// Calculate distances for each returned point
		DataPoint searchPoint = new DataPoint(new Latitude(_searchLatitude, Coordinate.FORMAT_DECIMAL_FORCE_POINT),
			new Longitude(_searchLongitude, Coordinate.FORMAT_DECIMAL_FORCE_POINT), null);
		for (SearchResult result : xmlHandler.getPointList())
		{
			Latitude pointLat = new Latitude(result.getLatitude());
			Longitude pointLon = new Longitude(result.getLongitude());
			DataPoint foundPoint = new DataPoint(pointLat, pointLon, null);
			double dist = DataPoint.calculateRadiansBetween(searchPoint, foundPoint);
			result.setLength(Distance.convertRadiansToDistance(dist));
		}

		// TODO: maybe limit number of results using MAX_RESULTS
		// Add track list to model
		ArrayList<SearchResult> pointList = xmlHandler.getPointList();
		_trackListModel.addTracks(pointList, true);
		_trackListModel.setShowPointTypes(true);

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
}
