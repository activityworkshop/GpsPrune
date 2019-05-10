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
import tim.prune.function.search.GenericDownloaderFunction;
import tim.prune.function.search.SearchResult;

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
			_searchTerm = search.toString().toLowerCase();
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

		// Replace awkward characters with character equivalents
		final String searchTerm = encodeSearchTerm(_searchTerm);

		// Firstly try the local language
		String lang = I18nManager.getText("wikipedia.lang");
		submitSearch(searchTerm, lang);
		// If we didn't get anything, try a secondary language
		if (_trackListModel.isEmpty() && _errorMessage == null && lang.equals("als")) {
			submitSearch(searchTerm, "de");
		}
		// If still nothing then try english
		if (_trackListModel.isEmpty() && _errorMessage == null && !lang.equals("en")) {
			submitSearch(searchTerm, "en");
		}

		// Set status label according to error or "none found", leave blank if ok
		if (_errorMessage == null && _trackListModel.isEmpty()) {
			_errorMessage = I18nManager.getText("dialog.wikipedia.nonefound");
		}
		_statusLabel.setText(_errorMessage == null ? "" : _errorMessage);
	}

	/**
	 * Submit the given search to the server
	 * @param inSearchTerm search term
	 * @param inLang language code such as en, de
	 */
	private void submitSearch(String inSearchTerm, String inLang)
	{
		// System.out.println("Searching for '" + inSearchTerm + "' with lang: " + inLang);

		String urlString = "http://api.geonames.org/wikipediaSearch?title=" + inSearchTerm
			+ "&maxRows=" + MAX_RESULTS + "&lang=" + inLang + "&username=" + GEONAMES_USERNAME;
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
		// TODO: Do a better job of sorting replies by relevance - use three different lists
		_trackListModel.addTracks(trackList);
	}

	/**
	 * Replace tricky characters in the search term and encode the others
	 * @param inSearchTerm entered search term
	 * @return modified search term to give to geonames
	 */
	private static final String encodeSearchTerm(String inSearchTerm)
	{
		if (inSearchTerm != null && inSearchTerm.length() > 0)
		{
			// Replace umlauts oe, ue, ae, OE, UE, AE, szlig
			StringBuilder sb = new StringBuilder();
			final int numChars = inSearchTerm.length();
			for (int i=0; i<numChars; i++)
			{
				char c = inSearchTerm.charAt(i);
				switch (c)
				{
					// German umlauted vowels, add an "e"
					case '\u00fc' : sb.append("ue"); break;
					case '\u00e4' : sb.append("ae"); break;
					case '\u00f6' : sb.append("oe"); break;
					// German doppel s
					case '\u00df' : sb.append("ss"); break;
					// accented vowels
					case '\u00e8' : case '\u00e9' :
					case '\u00ea' : case '\u00eb' : sb.append('e'); break;
					case '\u00e0' : case '\u00e1' :
					case '\u00e2' : sb.append('a'); break;
					case '\u00f2' : case '\u00f3' :
					case '\u00f4' : sb.append('o'); break;
					case '\u00ec' : case '\u00ed' :
					case '\u00ee' : case '\u00ef' : sb.append('i'); break;
					// cedillas, ny, l bar
					case '\u00e7' : sb.append('c'); break;
					case '\u015f' : sb.append('s'); break;
					case '\u00f1' : sb.append('n'); break;
					case '\u0142' : sb.append('l'); break;
					// everything else
					default  : sb.append(c);
				}
			}
			String searchTerm = inSearchTerm;
			try {
				searchTerm = URLEncoder.encode(sb.toString(), "UTF-8");
			} catch (UnsupportedEncodingException e1) {}
			// System.out.println("Converted '" + inSearchTerm + "' to '" + searchTerm + "'");
			return searchTerm;
		}
		return "";
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
