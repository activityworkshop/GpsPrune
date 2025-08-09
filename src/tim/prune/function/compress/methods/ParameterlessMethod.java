package tim.prune.function.compress.methods;


public abstract class ParameterlessMethod extends CompressionMethod
{
	public String getParam() {
		return "";
	}

	public String getSettingsString() {
		return getType().getKey();
	}
}
