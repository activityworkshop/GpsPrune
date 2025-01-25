package tim.prune.save;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to combine sources with different version numbers and determine
 * which of those versions to use when exporting
 */
public class VersionCombiner
{
	private final List<String> _supportedVersions;
	private String _bestVersion = null;

	/** Constructor giving a list of supported version strings */
	public VersionCombiner(List<String> inSupportedVersions) {
		_supportedVersions = (inSupportedVersions == null ? new ArrayList<>() : inSupportedVersions);
	}

	/** add a version found in the loaded data */
	public void addVersion(String inVersion)
	{
		if (inVersion != null && (_bestVersion == null || inVersion.compareTo(_bestVersion) > 0))
		{
			for (String supported : _supportedVersions)
			{
				if (inVersion.equals(supported))
				{
					_bestVersion = inVersion;
					break;
				}
			}
		}
	}

	/** @return the highest of the added versions, or the highest of the supported versions */
	public String getBestVersion()
	{
		if (_bestVersion != null && !_bestVersion.equals("")) {
			return _bestVersion;
		}
		String lastVersion = null;
		for (String supported : _supportedVersions) {
			lastVersion = supported;
		}
		return lastVersion;
	}
}
