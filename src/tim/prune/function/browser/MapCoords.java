package tim.prune.function.browser;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import tim.prune.data.DataPoint;
import tim.prune.data.DoubleRange;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;

/**
 * Converts coordinates into strings for passing to URLs
 */
public class MapCoords
{
	private final String _latitude;
	private final String _longitude;
	private final String _latSpan;
	private final String _lonSpan;

	/** Number formatter for five dp */
	private static final NumberFormat FIVE_DP = NumberFormat.getNumberInstance(Locale.UK);
	// Select the UK locale for this formatter so that decimal point is always used (not comma)
	static {
		if (FIVE_DP instanceof DecimalFormat) ((DecimalFormat) FIVE_DP).applyPattern("0.00000");
	}

	/** Regular constructor */
	private MapCoords(double inLatitude, double inLongitude, DoubleRange inLatRange, DoubleRange inLonRange)
	{
		_latitude = FIVE_DP.format(inLatitude);
		_longitude = FIVE_DP.format(inLongitude);
		_latSpan = getSpan(inLatRange);
		_lonSpan = getSpan(inLonRange);
	}

	/**
	 * @param inTrackInfo track info containing one or more points
	 * @return MapCoords object representing the range
	 */
	public static MapCoords from(TrackInfo inTrackInfo)
	{
		// Check if any data to display
		if (inTrackInfo == null || inTrackInfo.getTrack() == null || inTrackInfo.getTrack().getNumPoints() < 1) {
			return null;
		}
		final Track track = inTrackInfo.getTrack();
		final double centreLat;
		final double centreLon;
		DataPoint selectedPoint = inTrackInfo.getCurrentPoint();
		if (selectedPoint == null) {
			centreLat = getMedianValue(track.getLatRange());
			centreLon = getMedianValue(track.getLonRange());
		}
		else {
			centreLat = selectedPoint.getLatitude().getDouble();
			centreLon = selectedPoint.getLongitude().getDouble();
		}
		return new MapCoords(centreLat, centreLon, track.getLatRange(), track.getLonRange());
	}

	/**
	 * @param inTrackInfo track info containing one or more points
	 * @return MapCoords object for the minimum latitude and longitude
	 */
	public static MapCoords minValues(TrackInfo inTrackInfo)
	{
		// Check if any data to display
		if (inTrackInfo == null || inTrackInfo.getTrack() == null || inTrackInfo.getTrack().getNumPoints() < 1) {
			return null;
		}
		double minLat = inTrackInfo.getTrack().getLatRange().getMinimum();
		double minLon = inTrackInfo.getTrack().getLonRange().getMinimum();
		return new MapCoords(minLat, minLon, null, null);
	}

	/**
	 * @param inTrackInfo track info containing one or more points
	 * @return MapCoords object for the maximum latitude and longitude
	 */
	public static MapCoords maxValues(TrackInfo inTrackInfo)
	{
		// Check if any data to display
		if (inTrackInfo == null || inTrackInfo.getTrack() == null || inTrackInfo.getTrack().getNumPoints() < 1) {
			return null;
		}
		double maxLat = inTrackInfo.getTrack().getLatRange().getMaximum();
		double maxLon = inTrackInfo.getTrack().getLonRange().getMaximum();
		return new MapCoords(maxLat, maxLon, null, null);
	}

	/**
	 * @param inPoint single data point
	 * @return MapCoords object
	 */
	public static MapCoords from(DataPoint inPoint)
	{
		if (inPoint == null) {
			return null;
		}
		return new MapCoords(inPoint.getLatitude().getDouble(), inPoint.getLongitude().getDouble(), null, null);
	}

	public String getLatitude() {
		return _latitude;
	}

	public String getLongitude() {
		return _longitude;
	}

	public String getLatSpan() {
		return _latSpan;
	}

	public String getLonSpan() {
		return _lonSpan;
	}

	/**
	 * Get the median value from the given lat/long range
	 * @param inRange range of values
	 * @return median value
	 */
	private static double getMedianValue(DoubleRange inRange) {
		return (inRange.getMaximum() + inRange.getMinimum()) / 2.0;
	}

	/**
	 * Get the span of the given lat/long range
	 * @param inRange range of values
	 * @return span as a string
	 */
	private static String getSpan(DoubleRange inRange) {
		return (inRange == null ? "" : FIVE_DP.format(inRange.getMaximum() - inRange.getMinimum()));
	}
}
