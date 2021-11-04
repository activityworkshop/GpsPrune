package tim.prune.function.srtm;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import tim.prune.config.Config;

/**
 * High-resolution Srtm source, using 1-arcsecond data
 * instead of the low-resolution 3-arcsecond data
 * @author fperrin, activityworkshop
 */
public class SrtmHighResSource extends SrtmSource
{
	/** Flag set to false if auth fails */
	private boolean _enabled = true;

	/** URL prefix for all tiles */
	private static final String URL_PREFIX = "https://e4ftl01.cr.usgs.gov/MEASURES/SRTMGL1.003/2000.02.11/";
	/** Auth URL */
	private static final String AUTH_HOST = "urs.earthdata.nasa.gov";

	private static final int HTTP_CODE_OK = 200;
	private static final int HTTP_CODE_REDIRECT = 302;


	@Override
	public int getTilePixels() {
		return 3601;
	}

	/**
	 * @return the Url for the given tile
	 */
	@Override
	public URL getUrl(SrtmTile inTile)
	{
		URL url = null;
		try {
			url = new URL(URL_PREFIX + getFilename(inTile));
		} catch (MalformedURLException e) {} // ignore error, url stays null
		return url;
	}

	@Override
	public Result downloadTile(SrtmTile inTile)
	{
		final String authString = getAuthString();
		if (authString == null || !_enabled) {return Result.NOT_ENABLED;}
		try
		{
			// Retrieve a stream for the resource
			URL url = getUrl(inTile);
			InputStream inStream = getResource(url, authString);
			if (writeFileFromStream(url, inStream)) {
				return Result.DOWNLOADED;
			}
		}
		catch (SrtmAuthException authExc)
		{
			System.err.println("Auth exception: " + authExc.getMessage());
			_enabled = false;
			return Result.AUTH_FAILED;
		}
		catch (Exception e) {}
		return Result.DOWNLOAD_FAILED;
	}

	@Override
	public String getFilename(SrtmTile inTile)
	{
		return inTile.getTileName() + ".SRTMGL1.hgt.zip";
	}

	/**
	 * @return auth string, if possible
	 */
	private static String getAuthString()
	{
		String authString = Config.getConfigString(Config.KEY_EARTHDATA_AUTH);
		if (authString == null)
		{
			return null;
		}
		return "Basic " + authString;
	}

	/**
	 * Returns an input stream for a designated resource on a URS
	 * authenticated remote server.
	 * @param resourceUrl url to get
	 * @param inAuthString username/password from config
	 */
	private static InputStream getResource(URL resourceUrl, String inAuthString)
		throws Exception
	{
		// Method provided by:
		//   https://wiki.earthdata.nasa.gov/display/EL/How+To+Access+Data+With+Java
		int redirects = 0;
		URL url = resourceUrl;
		// Place an upper limit on the number of redirects we will follow
		while( redirects < 10 )
		{
			++redirects;

			// Configure a connection to the resource server and submit the
			// request for our resource.
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setInstanceFollowRedirects(false);
			connection.setUseCaches(false);
			connection.setDoInput(true);

			// If this is the URS server, add in the authentication header.
			if (url.getHost().startsWith(AUTH_HOST))
			{
				connection.setDoOutput(true);
				connection.setRequestProperty("Authorization", inAuthString);
			}

			// Execute the request and get the response status code.
			int status = connection.getResponseCode();
			if (status == HTTP_CODE_OK)
			{
				// OK means that we have got our resource
				return connection.getInputStream();
			}

			if (status == HTTP_CODE_REDIRECT)
			{
				// 302 is a redirect, so we go round the loop again.
				url = new URL(connection.getHeaderField("Location"));
			}
			else {
				throw new SrtmAuthException(status);
			}
		}

		// If we get here, we exceeded our redirect limit
		throw new Exception("Redirection limit exceeded");
	}
}
