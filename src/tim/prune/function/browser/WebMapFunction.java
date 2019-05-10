package tim.prune.function.browser;

import tim.prune.App;
import tim.prune.GenericFunction;

/**
 * Function to show a webservice for the current area or point
 */
public class WebMapFunction extends GenericFunction
{
	/** Service to call */
	private UrlGenerator.WebService _service;
	/** Key for appearance in menu */
	private String _nameKey = null;

	/**
	 * Constructor
	 * @param inApp app object
	 * @param inService web service to call
	 * @param inNameKey name key for function
	 */
	public WebMapFunction(App inApp, UrlGenerator.WebService inService,
		String inNameKey)
	{
		super(inApp);
		_service = inService;
		_nameKey = inNameKey;
	}

	@Override
	public String getNameKey() {
		return _nameKey;
	}

	@Override
	/**
	 * Do the function call
	 */
	public void begin()
	{
		String url = UrlGenerator.generateUrl(_service, _app.getTrackInfo());
		if (url != null) {
			BrowserLauncher.launchBrowser(url);
		}
	}
}
