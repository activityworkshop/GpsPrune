package tim.prune.function.browser;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.data.DataPoint;
import tim.prune.data.GeocacheCode;

/**
 * Function to open a geocache page for the current point
 */
public class OpenCachePageFunction extends GenericFunction
{
	/**
	 * Constructor
	 * @param inApp app object
	 */
	public OpenCachePageFunction(App inApp) {
		super(inApp);
	}

	@Override
	public String getNameKey() {
		return "function.opengeocachepage";
	}

	/**
	 * Do the function call
	 */
	@Override
	public void begin()
	{
		DataPoint currentPoint = _app.getTrackInfo().getCurrentPoint();
		if (currentPoint == null) {
			return;
		}
		String url = GeocacheCode.getUrl(currentPoint.getWaypointName());
		if (url != null) {
			BrowserLauncher.launchBrowser(url);
		}
	}
}
