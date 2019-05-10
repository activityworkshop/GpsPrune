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

	public enum WebService
	{
		MAP_SOURCE_GOOGLE,      /* Google maps */
		MAP_SOURCE_OSM,         /* OpenStreetMap */
		MAP_SOURCE_MAPQUEST,    /* Mapquest */
		MAP_SOURCE_YAHOO,       /* Yahoo */
		MAP_SOURCE_BING,        /* Bing */
		MAP_SOURCE_PEAKFINDER,  /* PeakFinder */
		MAP_SOURCE_GEOHACK,     /* Geohack */
		MAP_SOURCE_INLINESKATE, /* Inlinemap.net */
		MAP_SOURCE_GRAPHHOPPER  /* Routing with GraphHopper */
	}

	/**
	 * Generate a URL for the given source and track info
	 * @param inSource source to use, from the enum in UrlGenerator
	 * @param inTrackInfo track info
	 * @return url for map
	 */
	public static String generateUrl(WebService inSource, TrackInfo inTrackInfo)
	{
		switch (inSource)
		{
			case MAP_SOURCE_GOOGLE:
				return generateGoogleUrl(inTrackInfo);
			case MAP_SOURCE_MAPQUEST:
				return generateMapquestUrl(inTrackInfo);
			case MAP_SOURCE_YAHOO:
				return generateYahooUrl(inTrackInfo);
			case MAP_SOURCE_BING:
				return generateBingUrl(inTrackInfo);
			case MAP_SOURCE_PEAKFINDER:
			case MAP_SOURCE_GEOHACK:
			case MAP_SOURCE_INLINESKATE:
				return generateUrlForPoint(inSource, inTrackInfo);
			case MAP_SOURCE_GRAPHHOPPER:
				return generateGraphHopperUrl(inTrackInfo);
			case MAP_SOURCE_OSM:
			default:
				return generateOpenStreetMapUrl(inTrackInfo);
		}
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
	 * Generate a url for routing with GraphHopper
	 * @param inTrackInfo track information
	 * @return URL
	 */
	private static String generateGraphHopperUrl(TrackInfo inTrackInfo)
	{
		// Check if any data to display
		if (inTrackInfo != null && inTrackInfo.getTrack() != null && inTrackInfo.getTrack().getNumPoints() >= 2)
		{
			if (inTrackInfo.getTrack().getNumPoints() == 2)
			{
				// Use first and last point of track
				return generateGraphHopperUrl(inTrackInfo.getTrack().getPoint(0),
					inTrackInfo.getTrack().getPoint(1));
			}
			else if (inTrackInfo.getSelection().hasRangeSelected())
			{
				// Use first and last point of selection
				final int startIndex = inTrackInfo.getSelection().getStart();
				final int endIndex = inTrackInfo.getSelection().getEnd();
				return generateGraphHopperUrl(inTrackInfo.getTrack().getPoint(startIndex),
					inTrackInfo.getTrack().getPoint(endIndex));
			}
		}
		return null;
	}

	/**
	 * Generate a url for routing with GraphHopper
	 * @param inStartPoint start point of routing
	 * @param inEndPoint end point of routing
	 * @return URL
	 */
	private static String generateGraphHopperUrl(DataPoint inStartPoint, DataPoint inEndPoint)
	{
		final String url = "https://graphhopper.com/maps/"
			+ "?point=" + FIVE_DP.format(inStartPoint.getLatitude().getDouble())
			+ "%2C" + FIVE_DP.format(inStartPoint.getLongitude().getDouble())
			+ "&point=" + FIVE_DP.format(inEndPoint.getLatitude().getDouble())
			+ "%2C" + FIVE_DP.format(inEndPoint.getLongitude().getDouble())
			+ "&locale=" + I18nManager.getText("wikipedia.lang")
			+ "&elevation=true&weighting=fastest";
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
	 * Generate a URL which only needs the current point
	 * This is just a helper method to simplify the calls to the service-specific methods
	 * @param inSource service to call
	 * @param inTrackInfo track info
	 * @return URL if available, or null
	 */
	private static String generateUrlForPoint(WebService inService, TrackInfo inTrackInfo)
	{
		if (inTrackInfo == null || inTrackInfo.getTrack() == null || inTrackInfo.getTrack().getNumPoints() < 1)
		{
			return null;
		}
		// Need a current point
		DataPoint currPoint = inTrackInfo.getCurrentPoint();
		if (currPoint == null)
		{
			return null;
		}
		switch (inService)
		{
			case MAP_SOURCE_PEAKFINDER:
				return generatePeakfinderUrl(currPoint);
			case MAP_SOURCE_GEOHACK:
				return generateGeohackUrl(currPoint);
			case MAP_SOURCE_INLINESKATE:
				return generateInlinemapUrl(currPoint);
			default:
				return null;
		}
	}


	/**
	 * Generate a url for PeakFinder
	 * @param inPoint current point, not null
	 * @return URL
	 */
	private static String generatePeakfinderUrl(DataPoint inPoint)
	{
		return "https://www.peakfinder.org/?lat=" + FIVE_DP.format(inPoint.getLatitude().getDouble())
			+ "&lng=" + FIVE_DP.format(inPoint.getLongitude().getDouble());
	}

	/**
	 * Generate a url for Geohack
	 * @param inPoint current point, not null
	 * @return URL
	 */
	private static String generateGeohackUrl(DataPoint inPoint)
	{
		return "https://tools.wmflabs.org/geohack/geohack.php?params=" + FIVE_DP.format(inPoint.getLatitude().getDouble())
			+ "_N_" + FIVE_DP.format(inPoint.getLongitude().getDouble()) + "_E";
		// Note: Could use absolute values and S, W but this seems to work
	}

	/**
	 * Generate a url for Inlinemap.net
	 * @param inPoint current point, not null
	 * @return URL
	 */
	private static String generateInlinemapUrl(DataPoint inPoint)
	{
		return "http://www.inlinemap.net/en/?tab=new#/z14/" + FIVE_DP.format(inPoint.getLatitude().getDouble())
			+ "," + FIVE_DP.format(inPoint.getLongitude().getDouble()) + "/terrain";
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
