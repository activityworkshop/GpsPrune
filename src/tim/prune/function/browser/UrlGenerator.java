package tim.prune.function.browser;

import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.TrackInfo;

/**
 * Class to manage the generation of map urls
 * for display in an external browser
 */
public abstract class UrlGenerator
{
	public enum WebService
	{
		MAP_SOURCE_GOOGLE,      /* Google maps */
		MAP_SOURCE_OSM,         /* OpenStreetMap */
		MAP_SOURCE_MAPQUEST,    /* Mapquest */
		MAP_SOURCE_BING,        /* Bing */
		MAP_SOURCE_PEAKFINDER,  /* PeakFinder */
		MAP_SOURCE_GEOHACK,     /* Geohack */
		MAP_SOURCE_MAPILLARY,
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
			case MAP_SOURCE_BING:
				return generateBingUrl(inTrackInfo);
			case MAP_SOURCE_MAPILLARY:
				return generateMapillaryUrl(inTrackInfo);
			case MAP_SOURCE_PEAKFINDER:
				return generatePeakfinderUrl(inTrackInfo);
			case MAP_SOURCE_GEOHACK:
				return generateGeohackUrl(inTrackInfo);
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
		MapCoords coords = MapCoords.from(inTrackInfo);
		if (coords == null) {
			return null;
		}
		// Build basic url with centre position and span
		String url = "http://" + I18nManager.getText("url.googlemaps")
			+ "/?ll=" + coords.getLatitude() + "," + coords.getLongitude()
			+ "&spn=" + coords.getLatSpan() + "," + coords.getLonSpan();
		DataPoint currPoint = inTrackInfo.getCurrentPoint();
		// Add selected point, if any
		if (currPoint != null)
		{
			url = url + "&q=" + coords.getLatitude() + "," + coords.getLongitude();
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
		MapCoords coords = MapCoords.from(inTrackInfo);
		if (coords == null) {
			return null;
		}
		return "http://atlas.mapquest.com/maps/map.adp?latlongtype=decimal&latitude="
			+ coords.getLatitude() + "&longitude=" + coords.getLongitude();
	}

	/**
	 * Generate a url for Bing maps
	 * @param inTrackInfo track information
	 * @return URL
	 */
	private static String generateBingUrl(TrackInfo inTrackInfo)
	{
		MapCoords coords = MapCoords.from(inTrackInfo);
		if (coords == null) {
			return null;
		}
		return "http://bing.com/maps/default.aspx?cp=" + coords.getLatitude() + "~" + coords.getLongitude()
			+ "&where1=" + coords.getLatitude() + "%2C%20" + coords.getLongitude();
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
		MapCoords startCoords = MapCoords.from(inStartPoint);
		MapCoords endCoords = MapCoords.from(inEndPoint);
		if (startCoords == null || endCoords == null) {
			return null;
		}
		return "https://graphhopper.com/maps/"
			+ "?point=" + startCoords.getLatitude() + "%2C" + startCoords.getLongitude()
			+ "&point=" + endCoords.getLatitude() + "%2C" + endCoords.getLongitude()
			+ "&locale=" + I18nManager.getText("wikipedia.lang")
			+ "&elevation=true&weighting=fastest";
	}

	/**
	 * Generate a url for Open Street Map
	 * @param inTrackInfo track information
	 * @return URL
	 */
	private static String generateOpenStreetMapUrl(TrackInfo inTrackInfo)
	{
		MapCoords minCoords = MapCoords.minValues(inTrackInfo);
		MapCoords maxCoords = MapCoords.maxValues(inTrackInfo);
		if (minCoords == null || maxCoords == null) {
			return null;
		}
		String url = "http://openstreetmap.org/?minlat=" + minCoords.getLatitude()
			+ "&maxlat=" + maxCoords.getLatitude()
			+ "&minlon=" + minCoords.getLongitude() + "&maxlon=" + maxCoords.getLongitude();
		MapCoords pointCoords = MapCoords.from(inTrackInfo.getCurrentPoint());
		// Add selected point, if any (no way to add point name?)
		if (pointCoords != null) {
			url = url + "&mlat=" + pointCoords.getLatitude()
				+ "&mlon=" + pointCoords.getLongitude();
		}
		return url;
	}

	/**
	 * Generate a url for PeakFinder
	 * @param inTrackInfo track information
	 * @return URL
	 */
	private static String generatePeakfinderUrl(TrackInfo inTrackInfo)
	{
		MapCoords coords = MapCoords.from(inTrackInfo.getCurrentPoint());
		if (coords == null) {
			return null;
		}
		return "https://www.peakfinder.org/?lat=" + coords.getLatitude()
			+ "&lng=" + coords.getLongitude();
	}

	/**
	 * Generate a url for Geohack
	 * @param inTrackInfo track information
	 * @return URL
	 */
	private static String generateGeohackUrl(TrackInfo inTrackInfo)
	{
		MapCoords coords = MapCoords.from(inTrackInfo.getCurrentPoint());
		if (coords == null) {
			return null;
		}
		return "https://tools.wmflabs.org/geohack/geohack.php?params=" + coords.getLatitude()
			+ "_N_" + coords.getLongitude() + "_E";
		// Note: Could use absolute values and S, W but this seems to work
	}

	/**
	 * Generate a url for Mapillary
	 * @param inTrackInfo track information
	 * @return URL
	 */
	private static String generateMapillaryUrl(TrackInfo inTrackInfo)
	{
		MapCoords coords = MapCoords.from(inTrackInfo);
		if (coords == null) {
			return null;
		}
		return "https://www.mapillary.com/app/?lat=" + coords.getLatitude() + "&lng=" + coords.getLongitude() + "&z=16";
	}
}
