package tim.prune.function.gpsies;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import tim.prune.App;
import tim.prune.GpsPrune;
import tim.prune.I18nManager;
import tim.prune.function.search.GenericDownloaderFunction;
import tim.prune.function.search.SearchResult;
import tim.prune.load.xml.XmlFileLoader;
import tim.prune.load.xml.ZipFileLoader;

/**
 * Function to load track information from Gpsies.com
 * according to the currently viewed area
 */
public class GetGpsiesFunction extends GenericDownloaderFunction
{
	/** Number of results per page */
	private static final int RESULTS_PER_PAGE = 20;
	/** Maximum number of results to get */
	private static final int MAX_RESULTS = 60;
	/** New API key (specific to this program) */
	private static final String GPSIES_API_KEY = "oumgvvbckiwpvsnb";


	/**
	 * Constructor
	 * @param inApp App object
	 */
	public GetGpsiesFunction(App inApp) {
		super(inApp);
	}

	/**
	 * @return name key
	 */
	public String getNameKey() {
		return "function.getgpsies";
	}

	/**
	 * @param inColNum index of column, 0 or 1
	 * @return key for this column
	 */
	protected String getColumnKey(int inColNum)
	{
		if (inColNum == 0) return "dialog.gpsies.column.name";
		return "dialog.gpsies.column.length";
	}


	/**
	 * Run method to call gpsies.com in separate thread
	 */
	public void run()
	{
		_statusLabel.setText(I18nManager.getText("confirm.running"));
		// Act on callback to update list and send another request if necessary
		double[] coords = _app.getViewport().getBounds();
		int currPage = 1;

		ArrayList<SearchResult> trackList = null;
		URL url = null;
		String descMessage = "";
		InputStream inStream = null;
		// Loop for each page of the results
		do
		{
			// Example http://ws.gpsies.com/api.do?BBOX=10,51,12,53&limit=20&resultPage=1&key=oumgvvbckiwpvsnb
			String urlString = "http://ws.gpsies.com/api.do?BBOX=" +
				coords[1] + "," + coords[0] + "," + coords[3] + "," + coords[2] +
				"&limit=" + RESULTS_PER_PAGE + "&resultPage=" + currPage +
				"&key=" + GPSIES_API_KEY;
			// Parse the returned XML with a special handler
			GpsiesXmlHandler xmlHandler = new GpsiesXmlHandler();
			try
			{
				url = new URL(urlString);
				SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
				URLConnection conn = url.openConnection();
				conn.setRequestProperty("User-Agent", "GpsPrune v" + GpsPrune.VERSION_NUMBER);
				inStream = conn.getInputStream();
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
			trackList = xmlHandler.getTrackList();
			_trackListModel.addTracks(trackList);

			// Compare number of results with results per page and call again if necessary
			currPage++;
		}
		while (trackList != null && trackList.size() == RESULTS_PER_PAGE
			&& _trackListModel.getRowCount() < MAX_RESULTS && !_cancelled);
		// Set status label according to error or "none found", leave blank if ok
		if (descMessage.equals("") && (trackList == null || trackList.size() == 0)) {
			descMessage = I18nManager.getText("dialog.gpsies.nonefound");
		}
		_statusLabel.setText(descMessage);
	}

	/**
	 * Load the selected track(s)
	 */
	protected void loadSelected()
	{
		// Find the row(s) selected in the table and get the corresponding track
		int numSelected = _trackTable.getSelectedRowCount();
		if (numSelected < 1) return;
		int[] rowNums = _trackTable.getSelectedRows();
		for (int i=0; i<numSelected; i++)
		{
			int rowNum = rowNums[i];
			if (rowNum >= 0 && rowNum < _trackListModel.getRowCount())
			{
				String url = _trackListModel.getTrack(rowNum).getDownloadLink();
				XmlFileLoader xmlLoader = new XmlFileLoader(_app);
				ZipFileLoader loader = new ZipFileLoader(_app, xmlLoader);
				if (i>0) _app.autoAppendNextFile();
				try
				{
					loader.openStream(new URL(url).openStream());
				}
				catch (IOException ioe) {
					System.err.println("IO Exception : " + ioe.getMessage());
				}
			}
		}
		// Close the dialog
		_cancelled = true;
		_dialog.dispose();
	}
}
