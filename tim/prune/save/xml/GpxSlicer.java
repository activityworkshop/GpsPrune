package tim.prune.save.xml;

import java.io.IOException;
import java.io.InputStream;

/**
 * Class to slice up a gpx stream and report the found tags
 * back to a listener.
 * Used by Gpx caching to re-read and store the gpx source
 */
public class GpxSlicer
{
	/** listener to receive tags */
	private TagReceiver _receiver = null;
	/** string builder for copying source xml */
	private StringBuilder _builder = null;

	// character sequences for start and end of tags
	private static final char[] GPX_START = "<gpx".toCharArray();
	private static final char[] GPX_END = ">".toCharArray();
	private static final char[] TRKPT_START = "<trkpt".toCharArray();
	private static final char[] TRKPT_END = "/trkpt>".toCharArray();
	private static final char[] WPT_START = "<wpt".toCharArray();
	private static final char[] WPT_END = "/wpt>".toCharArray();
	private static final char[] RTEPT_START = "<rtept".toCharArray();
	private static final char[] RTEPT_END = "/rtept>".toCharArray();
	private static final char[] CDATA_START = "<![CDATA[".toCharArray();
	private static final char[] CDATA_END = "]]>".toCharArray();


	/**
	 * Constructor
	 * @param inReceiver listener for tags
	 */
	public GpxSlicer(TagReceiver inReceiver)
	{
		_receiver = inReceiver;
	}

	/**
	 * Begin the slicing and pass the found tags back to the listener
	 * @param inStream input stream for reading gpx source
	 */
	public void slice(InputStream inStream)
	{
		_builder = new StringBuilder(100);
		boolean insideTag = false;
		boolean insideCdata = false;
		char[] endTag = null;
		boolean foundHeader = false;
		int b = 0;
		try
		{
			while ((b = inStream.read()) >= 0)
			{
				if (!insideTag && !insideCdata) {
					if (b == '<') _builder.setLength(0);
				}
				// copy character
				_builder.append((char)b);

				if (insideCdata) {
					// Just look for end of cdata block
					if (foundSequence(CDATA_END)) {insideCdata = false;}
				}
				else
				{
					if (!insideTag)
					{
						// Look for start of one of the tags
						if (!foundHeader && foundSequence(GPX_START)) {
							insideTag = true;
							foundHeader = true;
							endTag = GPX_END;
						}
						else if (b == 't')
						{
							if (foundSequence(TRKPT_START)) {
								insideTag = true;
								endTag = TRKPT_END;
							}
							else if (foundSequence(WPT_START)) {
								insideTag = true;
								endTag = WPT_END;
							}
							else if (foundSequence(RTEPT_START)) {
								insideTag = true;
								endTag = RTEPT_END;
							}
						}
					}
					else
					{
						// Look for end of found tag
						if (foundSequence(endTag)) {
							_receiver.reportTag(_builder.toString());
							_builder.setLength(0);
							insideTag = false;
						}
					}
					// Look for start of cdata block
					if (foundSequence(CDATA_START)) {insideCdata = true;}
				}
			}
		}
		catch (IOException e) {} // ignore
	}

	/**
	 * Look for the given character sequence in the last characters read
	 * @param inChars sequence to look for
	 * @return true if sequence found
	 */
	private boolean foundSequence(char[] inChars)
	{
		final int numChars = inChars.length;
		final int bufflen = _builder.length();
		if (bufflen < numChars) {return false;}
		for (int i=0; i<numChars; i++)
		{
			char searchChar = inChars[numChars - 1 - i];
			char sourceChar = _builder.charAt(bufflen - 1 - i);
			if (searchChar != sourceChar) {return false;}
			//if (Character.toLowerCase(searchChar) != Character.toLowerCase(sourceChar)) {return false;}
		}
		return true;
	}
}
