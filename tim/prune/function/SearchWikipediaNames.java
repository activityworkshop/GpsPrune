package tim.prune.function;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.swing.JOptionPane;
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
 * Function to search Wikipedia for place names
 */
public class SearchWikipediaNames extends GenericDownloaderFunction
{
	/** search term */
	private String _searchTerm = null;
	/** Maximum number of results to get */
	private static final int MAX_RESULTS = 20;
	/** Username to use for geonames queries */
	private static final String GEONAMES_USERNAME = "gpsprune";

	/**
	 * Constructor
	 * @param inApp App object
	 */
	public SearchWikipediaNames(App inApp) {
		super(inApp);
	}

	/**
	 * @return name key
	 */
	public String getNameKey() {
		return "function.searchwikipedianames";
	}

	/**
	 * @param inColNum index of column, 0 or 1
	 * @return key for this column
	 */
	protected String getColumnKey(int inColNum)
	{
		if (inColNum == 0) return "dialog.wikipedia.column.name";
		return null;
	}

	/**
	 * Before dialog is shown, need to get search term
	 */
	public void begin()
	{
		Object search = JOptionPane.showInputDialog(_app.getFrame(),
			I18nManager.getText("dialog.searchwikipedianames.search"),
			I18nManager.getText(getNameKey()),
			JOptionPane.QUESTION_MESSAGE, null, null, "");
		if (search != null)
		{
			_searchTerm = search.toString();
			if (!_searchTerm.equals("")) {
				super.begin();
			}
		}
	}

	/**
	 * Run method to call geonames in separate thread
	 */
	public void run()
	{
		_statusLabel.setText(I18nManager.getText("confirm.running"));

		String descMessage = "";
		InputStream inStream = null;

		// language (only de and en available)
		String lang = I18nManager.getText("wikipedia.lang");
		if (lang.equals("de") || lang.equals("als")) {
			lang = "de";
		}
		else {
			lang = "en";
		}
		// Replace awkward characters with character equivalents
		String searchTerm;
		try {
			searchTerm = URLEncoder.encode(_searchTerm, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			searchTerm = _searchTerm;
		}
		// Example http://ws.geonames.org/wikipediaSearch?q=london&maxRows=10
		String urlString = "http://api.geonames.org/wikipediaSearch?title=" + searchTerm
			+ "&maxRows=" + MAX_RESULTS + "&lang=" + lang + "&username=" + GEONAMES_USERNAME;
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
		// TODO: Do a better job of sorting replies by relevance - use three different lists
		_trackListModel.addTracks(trackList);

		// Set status label according to error or "none found", leave blank if ok
		if (descMessage.equals("") && (trackList == null || trackList.size() == 0)) {
			descMessage = I18nManager.getText("dialog.gpsies.nonefound");
		}
		_statusLabel.setText(descMessage);
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
