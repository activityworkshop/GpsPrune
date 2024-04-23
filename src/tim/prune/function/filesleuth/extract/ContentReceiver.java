package tim.prune.function.filesleuth.extract;

/** Interface to receive the contents from the parser */
public interface ContentReceiver
{
	void addString(String inValue);

	void setName(String inName);

	void setDescription(String inDesc);

	void addDateString(String inDate);

	void addCoordinates(double inLatitude, double inLongitude);

	void endDocument();
}
