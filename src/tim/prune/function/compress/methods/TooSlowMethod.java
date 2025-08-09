package tim.prune.function.compress.methods;

import tim.prune.data.NumberUtils;
import tim.prune.function.compress.CompressionMethodType;

public class TooSlowMethod extends SpeedLimitMethod
{
	public TooSlowMethod(String inString) {
		super(recogniseString(inString) ? NumberUtils.getDoubleOrZero(inString.substring(4)) : 0.0, false);
	}

	public TooSlowMethod(double inValue) {
		super(Math.abs(inValue), false);
	}

	public CompressionMethodType getType() {
		return CompressionMethodType.TOO_SLOW;
	}

	@Override
	public String getSettingsString() {
		return getType().getKey() + getSpeedLimit();
	}

	static boolean recogniseString(String inString) {
		return recogniseString(inString, CompressionMethodType.TOO_SLOW);
	}
}
