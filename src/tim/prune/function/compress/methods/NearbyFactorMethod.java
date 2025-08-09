package tim.prune.function.compress.methods;

import tim.prune.data.NumberUtils;
import tim.prune.function.compress.CompressionMethodType;
import tim.prune.function.compress.TrackDetails;

/** Compression of nearby points using a span factor parameter */
public class NearbyFactorMethod extends NearbyPointsMethod
{
	private final int _factor;

	public NearbyFactorMethod(int factor) {
		_factor = factor;
	}

	public NearbyFactorMethod(String inString) {
		_factor = NumberUtils.getIntOrZero(recogniseString(inString) ? inString.substring(4) : inString);
	}

	public CompressionMethodType getType() {
		return CompressionMethodType.NEARBY_WITH_FACTOR;
	}

	public String getParam() {
		return "" + Math.abs(_factor);
	}

	public String getSettingsString() {
		return getType().getKey() + _factor;
	}

	static boolean recogniseString(String inString) {
		return recogniseString(inString, CompressionMethodType.NEARBY_WITH_FACTOR);
	}

	/** @return the radian threshold based on the track's extents */
	protected double getRadianThreshold(TrackDetails inDetails)
	{
		// Parse parameter
		double param = _factor <= 0 ? 1.0 : (1.0 / _factor);
		if (param <= 0.0 || param >= 1.0) {
			// Parameter isn't valid, don't delete any
			return 0.0;
		}
		return inDetails.getMaxRadians() * param;
	}
}
