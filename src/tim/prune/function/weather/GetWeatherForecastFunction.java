package tim.prune.function.weather;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.data.DataPoint;
import tim.prune.data.TrackInfo;
import tim.prune.function.browser.BrowserLauncher;

/**
 * Function to display a weather forecast for the current location
 * using the services of openweathermap.org
 */
public class GetWeatherForecastFunction extends GenericFunction
{
	/** Access to streams */
	private final OnlineStreamProvider _streamProvider;
	/** Flag to show that forecast is currently running, don't start another */
	private boolean _isRunning = false;


	/** Constructor */
	public GetWeatherForecastFunction(App inApp)
	{
		super(inApp);
		_streamProvider = new OnlineStreamProvider();
	}

	/** @return name key */
	public String getNameKey() {
		return "function.getweatherforecast";
	}

	/**
	 * Begin the function
	 */
	public void begin()	{
		// Start new thread to call service asynchronously
		new Thread(this::run).start();
	}

	/**
	 */
	public void run()
	{
		if (_isRunning) {return;} // don't run twice
		_isRunning = true;

		// Call the service to get the location
		try
		{
			final LatLongPair latLong = getCoordinatesFromTrack(_app.getTrackInfo());
			// TODO: Nls tokens for these four error messages, if this code isn't immediately deleted
			if (latLong == null) {
				_app.showErrorMessageNoLookup(getNameKey(), "Failed to get coordinates from the track");
				return;
			}
			final double lat = latLong.getLatitude();
			final double lon = latLong.getLongitude();
			LocationResult result = null;
			try (InputStream inStream = _streamProvider.getLocationStream(lat, lon))
			{
				byte[] resultBytes = inStream.readAllBytes();
				String strResult = new String(resultBytes, StandardCharsets.UTF_8);
				result = LocationResult.fromString(strResult);
			}
			catch (IOException e) {
				_app.showErrorMessageNoLookup(getNameKey(), "Error: " + e.getMessage());
				e.printStackTrace();
				return;
			}
			// Check for error
			if (result == null || result.isError()) {
				String message = (result == null ? null : result.getErrorMessage());
				if (message == null || message.isEmpty()) {
					_app.showErrorMessageNoLookup(getNameKey(), "Error: geo lookup didn't return a location");
				}
				else {
					_app.showErrorMessageNoLookup(getNameKey(), "Error: " + message);
				}
				return;
			}
			else
			{
				String url = makeUrl(result.getName());
				BrowserLauncher.launchBrowser(url);
			}
		}
		finally
		{
			// finished running
			_isRunning = false;
		}
	}

	private String makeUrl(String name)
	{
		return "https://openweathermap.org/find?q="
			+ URLEncoder.encode(name, StandardCharsets.UTF_8);
	}

	static LatLongPair getCoordinatesFromTrack(TrackInfo inTrackInfo)
	{
		if (inTrackInfo == null || inTrackInfo.getTrack().getNumPoints() < 1) {
			return null;
		}
		// See if a point is selected, if so use that
		DataPoint currPoint = inTrackInfo.getCurrentPoint();
		if (currPoint != null)
		{
			// Use selected point
			return new LatLongPair(currPoint.getLatitude().getDouble(), currPoint.getLongitude().getDouble());
		}
		else
		{
			return new LatLongPair(inTrackInfo.getTrack().getLatRange().getMidValue(),
					inTrackInfo.getTrack().getLonRange().getMidValue());
		}
	}
}
