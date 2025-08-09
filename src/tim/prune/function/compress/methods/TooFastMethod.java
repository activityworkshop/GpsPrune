package tim.prune.function.compress.methods;

import tim.prune.data.NumberUtils;
import tim.prune.function.compress.CompressionMethodType;

public class TooFastMethod extends SpeedLimitMethod
{
	public TooFastMethod(String inString) {
		super(recogniseString(inString) ? NumberUtils.getDoubleOrZero(inString.substring(4)) : 0.0, true);
	}

	public TooFastMethod(double inValue) {
		super(Math.abs(inValue), true);
	}

	public CompressionMethodType getType() {
		return CompressionMethodType.TOO_FAST;
	}

	@Override
	public String getSettingsString() {
		return getType().getKey() + getSpeedLimit();
	}

	static boolean recogniseString(String inString) {
		return recogniseString(inString, CompressionMethodType.TOO_FAST);
	}
}
