package tim.prune.function.browser;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.DoubleRange;
import tim.prune.data.TrackInfo;

/**
 * Class to manage the generation of map urls
 * for display in an external browser
 */
public abstract class UrlGenerator
{
	/** Number formatter for five dp */
	private static final NumberFormat FIVE_DP = NumberFormat.getNumberInstance(Locale.UK);
	// Select the UK locale for this formatter so that decimal point is always used (not comma)
	static {
		if (FIVE_DP instanceof DecimalFormat) ((DecimalFormat) FIVE_DP).applyPattern("0.00000");
	}

	/** Constant for Google Maps */
	public static final int MAP_SOURCE_GOOGLE = 0;
	/** Constant for Open Street Maps */
	public static final int MAP_SOURCE_OSM    = 1;
	/** Constant for Mapquest */
	public static final int MAP_SOURCE_MAPQUEST = 2;
	/** Constant for Yahoo */
	public static final int MAP_SOURCE_YAHOO  = 3;
	/** Constant for Bing */
	public static final int MAP_SOURCE_BING = 4;

	/**
	 * Generate a URL for the given source and track info
	 * @param inSource source to use, either google or openstreetmap
	 * @param inTrackInfo track info
	 * @return url for map
	 */
	public static String generateUrl(int inSource, TrackInfo inTrackInfo)
	{
		if (inSource == MAP_SOURCE_GOOGLE) {
			return generateGoogleUrl(inTrackInfo);
		}
		else if (inSource == MAP_SOURCE_MAPQUEST) {
			return generateMapquestUrl(inTrackInfo);
		}
		else if (inSource == MAP_SOURCE_YAHOO) {
			return generateYahooUrl(inTrackInfo);
		}
		else if (inSource == MAP_SOURCE_BING) {
			return generateBingUrl(inTrackInfo);
		}
		return generateOpenStreetMapUrl(inTrackInfo);
	}

	/**
	 * Generate a url for Google maps
	 * @param inTrackInfo track information
	 * @return URL
	 */
	private static String generateGoogleUrl(TrackInfo inTrackInfo)
	{
		// Check if any data to display
		if (inTrackInfo == null || inTrackInfo.getTrack() == null || inTrackInfo.getTrack().getNumPoints() < 1)
		{
			return null;
		}
		double medianLat = getMedianValue(inTrackInfo.getTrack().getLatRange());
		double medianLon = getMedianValue(inTrackInfo.getTrack().getLonRange());
		double latSpan = getSpan(inTrackInfo.getTrack().getLatRange());
		double lonSpan = getSpan(inTrackInfo.getTrack().getLonRange());
		// Build basic url with centre position and span
		String url = "http://" + I18nManager.getText("url.googlemaps")
			+ "/?ll=" + FIVE_DP.format(medianLat) + "," + FIVE_DP.format(medianLon)
			+ "&spn=" + FIVE_DP.format(latSpan) + "," + FIVE_DP.format(lonSpan);
		DataPoint currPoint = inTrackInfo.getCurrentPoint();
		// Add selected point, if any
		if (currPoint != null) {
			url = url + "&q=" + FIVE_DP.format(currPoint.getLatitude().getDouble()) + ","
				+ FIVE_DP.format(currPoint.getLongitude().getDouble());
			if (currPoint.getWaypointName() != null) {
				url = url + "(" + currPoint.getWaypointName() + ")";
			}
		}
		return url;
	}

	/**
	 * Generate a url for Open Street Map
	 * @param inTrackInfo track information
	 * @return URL
	 */
	private static String generateOpenStreetMapUrl(TrackInfo inTrackInfo)
	{
		// Check if any data to display
		if (inTrackInfo == null || inTrackInfo.getTrack() == null || inTrackInfo.getTrack().getNumPoints() < 1)
		{
			return null;
		}
		DoubleRange latRange = inTrackInfo.getTrack().getLatRange();
		DoubleRange lonRange = inTrackInfo.getTrack().getLonRange();
		// Build basic url using min and max lat and long
		String url = "http://openstreetmap.org/?minlat=" + FIVE_DP.format(latRange.getMinimum())
			+ "&maxlat=" + FIVE_DP.format(latRange.getMaximum())
			+ "&minlon=" + FIVE_DP.format(lonRange.getMinimum()) + "&maxlon=" + FIVE_DP.format(lonRange.getMaximum());
		DataPoint currPoint = inTrackInfo.getCurrentPoint();
		// Add selected point, if any (no way to add point name?)
		if (currPoint != null) {
			url = url + "&mlat=" + FIVE_DP.format(currPoint.getLatitude().getDouble())
				+ "&mlon=" + FIVE_DP.format(currPoint.getLongitude().getDouble());
		}
		return url;
	}

	/**
	 * Generate a url for Mapquest maps
	 * @param inTrackInfo track information
	 * @return URL
	 */
	private static String generateMapquestUrl(TrackInfo inTrackInfo)
	{
		// Check if any data to display
		if (inTrackInfo == null || inTrackInfo.getTrack() == null || inTrackInfo.getTrack().getNumPoints() < 1)
		{
			return null;
		}
		double medianLat = getMedianValue(inTrackInfo.getTrack().getLatRange());
		double medianLon = getMedianValue(inTrackInfo.getTrack().getLonRange());
		// Build basic url with centre position
		String url = "http://atlas.mapquest.com/maps/map.adp?latlongtype=decimal&latitude="
			+ FIVE_DP.format(medianLat) + "&longitude=" + FIVE_DP.format(medianLon);
		return url;
	}


	/**
	 * Generate a url for Yahoo maps
	 * @param inTrackInfo track information
	 * @return URL
	 */
	private static String generateYahooUrl(TrackInfo inTrackInfo)
	{
		// Check if any data to display
		if (inTrackInfo == null || inTrackInfo.getTrack() == null || inTrackInfo.getTrack().getNumPoints() < 1)
		{
			return null;
		}
		double medianLat = getMedianValue(inTrackInfo.getTrack().getLatRange());
		double medianLon = getMedianValue(inTrackInfo.getTrack().getLonRange());
		// Build basic url with centre position
		String url = "http://maps.yahoo.com/#lat=" + FIVE_DP.format(medianLat)
			+ "&lon=" + FIVE_DP.format(medianLon) + "&zoom=13";
		return url;
	}

	/**
	 * Generate a url for Bing maps
	 * @param inTrackInfo track information
	 * @return URL
	 */
	private static String generateBingUrl(TrackInfo inTrackInfo)
	{
		// Check if any data to display
		if (inTrackInfo == null || inTrackInfo.getTrack() == null || inTrackInfo.getTrack().getNumPoints() < 1)
		{
			return null;
		}
		double medianLat = getMedianValue(inTrackInfo.getTrack().getLatRange());
		double medianLon = getMedianValue(inTrackInfo.getTrack().getLonRange());
		// Build basic url with centre position
		String latStr = FIVE_DP.format(medianLat);
		String lonStr = FIVE_DP.format(medianLon);
		String url = "http://bing.com/maps/default.aspx?cp=" + latStr + "~" + lonStr
			+ "&where1=" + latStr + "%2C%20" + lonStr;
		return url;
	}


	/**
	 * Get the median value from the given lat/long range
	 * @param inRange range of values
	 * @return median value
	 */
	private static double getMedianValue(DoubleRange inRange)
	{
		return (inRange.getMaximum() + inRange.getMinimum()) / 2.0;
	}

	/**
	 * Get the span of the given lat/long range
	 * @param inRange range of values
	 * @return span
	 */
	private static double getSpan(DoubleRange inRange)
	{
		return inRange.getMaximum() - inRange.getMinimum();
	}
}
