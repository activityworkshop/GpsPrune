package tim.prune.function.compress;

import tim.prune.function.compress.methods.CompressionMethod;

import java.util.ArrayList;

public class MethodList extends ArrayList<CompressionMethod>
{
	public String toConfigString()
	{
		StringBuilder sb = new StringBuilder();
		boolean firstMethod = true;
		for (CompressionMethod method : this)
		{
			if (method == null) {
				continue;
			}
			String methodString = method.getTotalSettingsString();
			if (!methodString.isEmpty())
			{
				if (!firstMethod) {
					sb.append(';');
				}
				sb.append(methodString);
				firstMethod = false;
			}
		}
		return sb.toString();
	}

	public static MethodList fromConfigString(String inString)
	{
		MethodList methods = new MethodList();
		if (inString != null && !inString.isEmpty())
		{
			for (String methodString : inString.split(";"))
			{
				CompressionMethod method = CompressionMethod.fromSettingsString(methodString);
				if (method != null) {
					methods.add(method);
				}
			}
		}
		return methods;
	}
}
