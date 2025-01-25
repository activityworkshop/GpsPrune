package tim.prune.save.xml;

/** Represents a version of Gpx */
public enum GpxVersion
{
	GPX_1_0("1/0", "10.xsd"), GPX_1_1("1/1", "11.xsd");

	private final String _idString;
	private final String _altString;

	GpxVersion(String inId, String inAlternative) {
		_idString = inId;
		_altString = "/" + inAlternative;
	}

	/** @return true if the given string matches this version */
	boolean matches(String inString)
	{
		return inString != null
				&& (inString.endsWith(_idString) || inString.contains(_idString + "/")
						|| inString.endsWith(_altString));
	}
}
