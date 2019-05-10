package tim.prune.function.search;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.Distance;
import tim.prune.data.Field;
import tim.prune.data.Latitude;
import tim.prune.data.Longitude;
import tim.prune.data.SourceInfo;
import tim.prune.data.UnitSetLibrary;
import tim.prune.load.MediaLinkInfo;

/**
 * Function to search mapillary for photos
 */
public class SearchMapillaryFunction extends GenericDownloaderFunction
{
	/** Maximum number of results to get */
	private static final int MAX_RESULTS = 20;


	/**
	 * Constructor
	 * @param inApp app object
	 */
	public SearchMapillaryFunction(App inApp)
	{
		super(inApp);
	}

	@Override
	public String getNameKey() {
		return "function.mapillary";
	}

	@Override
	protected String getColumnKey(int inColNum)
	{
		if (inColNum == 0) return "dialog.wikipedia.column.name";
		return "dialog.wikipedia.column.distance";
	}

	/**
	 * Run method, for searching in a separate thread
	 */
	public void run()
	{
		_statusLabel.setText(I18nManager.getText("confirm.running"));
		// Get coordinates from current point (if any) or from centre of screen
		double lat = 0.0, lon = 0.0;
		DataPoint currentPoint = _app.getTrackInfo().getCurrentPoint();
		if (currentPoint == null)
		{
			double[] coords = _app.getViewport().getBounds();
			lat = (coords[0] + coords[2]) / 2.0;
			lon = (coords[1] + coords[3]) / 2.0;
		}
		else
		{
			lat = currentPoint.getLatitude().getDouble();
			lon = currentPoint.getLongitude().getDouble();
		}

		// Construct URL
		final String urlString = "http://api.mapillary.com/v1/im/close?lat="
			+ lat + "&lon=" + lon + "&distance=1000&limit=" + MAX_RESULTS;
		//System.out.println(urlString);
		InputStream inStream = null;
		try
		{
			inStream = new URL(urlString).openStream();
			StringBuilder sb = new StringBuilder();
			int ch = 0;
			while ((ch = inStream.read()) >= 0)
			{
				sb.append((char) ch);
			}
			//System.out.println("Got answer: '" + sb.toString() + "'");

			ArrayList<SearchResult> resultList = new ArrayList<SearchResult>();
			for (String result : sb.toString().split("\\},\\{"))
			{
				//System.out.println("Result: '" + result + "'");
				SearchResult sr = new SearchResult();
				for (String prop : result.split(","))
				{
					String key = getKey(prop);
					if (key == null) {continue;}
					if (key.equals("key"))
					{
						final String value = getValue(prop);
						sr.setDownloadLink("http://images.mapillary.com/" + value + "/thumb-1024.jpg");
						sr.setWebUrl("http://www.mapillary.com/map/im/" + value);
						sr.setTrackName(value);
					}
					else if (key.equals("lat")) {
						sr.setLatitude(getValue(prop));
					}
					else if (key.equals("lon")) {
						sr.setLongitude(getValue(prop));
					}
				}

				if (sr.getLatitude() != null && sr.getLongitude() != null && sr.getTrackName() != null)
				{
					// Calculate distance away from current point and set this in sr.setLength
					DataPoint resultPoint = new DataPoint(new Latitude(sr.getLatitude()), new Longitude(sr.getLongitude()), null);
					if (resultPoint.isValid() && currentPoint != null && currentPoint.isValid())
					{
						double radianDist = DataPoint.calculateRadiansBetween(currentPoint, resultPoint);
						double metresAway = Distance.convertRadiansToDistance(radianDist, UnitSetLibrary.UNITS_METRES);
						sr.setLength(metresAway);
					}

					// If there's a valid result, add it to the temporary list
					if (sr.getTrackName() != null) {
						resultList.add(sr);
					}
				}
			}
			// Add all the results to the table model in one go
			if (!resultList.isEmpty()) {
				_trackListModel.addTracks(resultList);
			}
		}
		catch (Exception e) {
			_errorMessage = e.getClass().getName() + " - " + e.getMessage();
		}
		// Close stream and ignore errors
		try {
			inStream.close();
		} catch (Exception e) {}

		// Set status label according to error or "none found", leave blank if ok
		if (_errorMessage == null && _trackListModel.isEmpty()) {
			_errorMessage = I18nManager.getText("dialog.mapillary.nonefound");
		}
		_statusLabel.setText(_errorMessage == null ? "" : _errorMessage);
	}

	/**
	 * From a JSON key:value string, return just the key
	 * @param inString string to parse
	 * @return just the key without the surrounding quotes, or null if not found
	 */
	private static String getKey(String inString)
	{
		if (inString == null || inString.equals("")) {return null;}
		final int colonPos = inString.indexOf(':');
		if (colonPos <= 0) {return null;}
		int startPos = 0;
		char c;
		while ((c = inString.charAt(startPos)) == '['
			|| c == '{' || c == '\"')
		{
			startPos++;
		}
		int endPos = colonPos;
		while ((c = inString.charAt(endPos-1)) == '\"')
		{
			endPos--;
		}
		return inString.substring(startPos, endPos);
	}

	/**
	 * From a JSON key:value string, return just the value
	 * @param inString string to parse
	 * @return just the value without the surrounding quotes
	 */
	private static String getValue(String inString)
	{
		final int colonPos = inString.indexOf(':');
		if (colonPos <= 0 || colonPos >= inString.length()) {return null;}
		int startPos = colonPos+1;
		char c;
		while ((c = inString.charAt(startPos)) == '\"')
		{
			startPos++;
		}
		int endPos = inString.length()-1;
		while ((c = inString.charAt(endPos-1)) == '\"'
			|| c == '}' || c == ']')
		{
			endPos--;
		}
		return inString.substring(startPos, endPos);
	}

	@Override
	protected void loadSelected()
	{
		// Find the row(s) selected in the table and get the corresponding track
		int numSelected = _trackTable.getSelectedRowCount();
		if (numSelected < 1) return;
		int[] rowNums = _trackTable.getSelectedRows();

		String[][] pointData = new String[numSelected][];
		String[]   linkArray = new String[numSelected];

		// Loop over each of the selected points
		for (int i=0; i<numSelected; i++)
		{
			pointData[i] = new String[3]; // lat, long, segment
			int rowNum = rowNums[i];
			if (rowNum >= 0 && rowNum < _trackListModel.getRowCount())
			{
				SearchResult result = _trackListModel.getTrack(rowNum);
				//String url = result.getDownloadLink();
				pointData[i][0] = result.getLatitude();
				pointData[i][1] = result.getLongitude();
				pointData[i][2] = "1"; // all points have a new segment
				linkArray[i]    = result.getDownloadLink();
			}
		}
		// Prepare the data for the app
		final Field[] fields = {Field.LATITUDE, Field.LONGITUDE, Field.NEW_SEGMENT};
		_app.autoAppendNextFile();
		_app.informDataLoaded(fields, pointData, null, new SourceInfo("mapillary", SourceInfo.FILE_TYPE.JSON),
			null, new MediaLinkInfo(linkArray));

		// Close the dialog
		_cancelled = true;
		_dialog.dispose();
	}
}
