package tim.prune.data;

/** File type and version of source file */
public enum FileType
{
	TEXT("text"),
	GPX("gpx"),
	KML("kml"),
	NMEA("nmea"),
	GPSBABEL("gpsbabel"),
	JSON("json");

	private final String _token;

	private FileType(String inToken) {
		_token = inToken;
	}

	public String getTextKey() {
		return "filetype." + _token;
	}
}
