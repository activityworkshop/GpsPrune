package tim.prune.function.compress;

import java.util.HashMap;

import tim.prune.config.ParamSet;
import tim.prune.function.compress.methods.CompressionMethod;

public class ParameterValues extends HashMap<CompressionMethodType, String>
{
	public ParameterValues()
	{
		put(CompressionMethodType.NEARBY_WITH_FACTOR, "200");
		put(CompressionMethodType.WACKY_POINTS, "2");
		put(CompressionMethodType.SINGLETONS, "2");
		put(CompressionMethodType.DOUGLAS_PEUCKER, "2000");
		put(CompressionMethodType.NEARBY_WITH_DISTANCE, "10");
		put(CompressionMethodType.TOO_SLOW, "2");
		put(CompressionMethodType.TOO_FAST, "40");
		put(CompressionMethodType.TIME_DIFFERENCE, "20");
	}

	public void applyNewStyleConfig(String inConfigString)
	{
		for (CompressionMethod method : MethodList.fromConfigString(inConfigString))
		{
			if (method != null && method.getType() != CompressionMethodType.DUPLICATES) {
				applyParameter(method.getType(), method.getParam());
			}
		}
	}

	public void applyOldStyleConfig(String inConfigString)
	{
		ParamSet params = new ParamSet(inConfigString);
		final CompressionMethodType[] allTypes = new CompressionMethodType[] {
			CompressionMethodType.DUPLICATES,
			CompressionMethodType.NEARBY_WITH_FACTOR, CompressionMethodType.WACKY_POINTS,
			CompressionMethodType.SINGLETONS, CompressionMethodType.DOUGLAS_PEUCKER
		};
		for (int i=1; i<allTypes.length; i++) {
			applyParameter(allTypes[i], params.getParam(i));
		}
	}

	/** Overwrite the default value with the value read from the config */
	private void applyParameter(CompressionMethodType inMethod, String inParam)
	{
		if (inParam != null && !inParam.isEmpty()) {
			put(inMethod, inParam);
		}
	}

	/** @return the value for the given method, or an empty string */
	public String getValue(CompressionMethodType inType) {
		return getOrDefault(inType, "");
	}
}
