package tim.prune.function.compress.methods;

import tim.prune.data.Distance;
import tim.prune.data.NumberUtils;
import tim.prune.data.UnitSetLibrary;
import tim.prune.function.compress.CompressionMethodType;
import tim.prune.function.compress.TrackDetails;

/** Compression of nearby points using an absolute distance parameter */
public class NearbyDistMethod extends NearbyPointsMethod
{
	private final double _metricDistance;

	public NearbyDistMethod(double inDistance) {
		_metricDistance = inDistance;
	}

	public NearbyDistMethod(String inString) {
		_metricDistance = recogniseString(inString) ? NumberUtils.getDoubleOrZero(inString.substring(4)) : 0.0;
	}

	public CompressionMethodType getType() {
		return CompressionMethodType.NEARBY_WITH_DISTANCE;
	}

	public String getParam() {
		return "" + Math.abs(_metricDistance);
	}

	public String getSettingsString() {
		return getType().getKey() + _metricDistance;
	}

	static boolean recogniseString(String inString) {
		return recogniseString(inString, CompressionMethodType.NEARBY_WITH_DISTANCE);
	}

	protected double getRadianThreshold(TrackDetails inDetails) {
		return Distance.convertDistanceToRadians(_metricDistance, UnitSetLibrary.UNITS_METRES);
	}
}
