package tim.prune.gui.map;

public class MapLayer
{
	/** Base url */
	private final String _baseUrl;
	/** Site names */
	private final String _siteName;
	/** File extension (eg "png") */
	private final String _extension;
	/** Double resolution flag (512 instead of 256) */
	protected boolean _doubleRes = false;

	public MapLayer(String inBaseUrl, String inExtension)
	{
		_baseUrl = inBaseUrl;
		_siteName = SiteNameUtils.convertUrlToDirectory(inBaseUrl);
		_extension = inExtension;
	}

	public void setDoubleRes() {_doubleRes = true;}

	public String getBaseUrl() {return _baseUrl;}
	public String getSiteName() {return _siteName;}
	public String getExtension() {return _extension;}
	public boolean isDoubleRes() {return _doubleRes;}
}
