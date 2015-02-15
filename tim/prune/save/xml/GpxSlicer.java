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
		StringBuffer beginBuffer = new StringBuffer(200);
		ByteBuffer byteBuffer = new ByteBuffer();
		boolean insideTag = false;
		boolean insideCdata = false;
		char[] endTag = null;
		boolean foundHeader = false;
		int b = 0;
		try
		{
			while ((b = inStream.read()) >= 0)
			{
				// copy character
				byteBuffer.appendByte((byte) b);
				// clear buffer if necessary
				if (!insideTag && !insideCdata && (b == '>' || b == '\n'))
				{
					byteBuffer.clear();
					continue;
				}
				// if we're still at the beginning, copy to the begin buffer as well
				if (beginBuffer != null) {beginBuffer.append((char) b);}

				if (insideCdata) {
					// Just look for end of cdata block
					if (byteBuffer.foundSequence(CDATA_END)) {insideCdata = false;}
				}
				else
				{
					if (!insideTag)
					{
						// Look for start of one of the tags
						if (!foundHeader && byteBuffer.foundSequence(GPX_START))
						{
							insideTag = true;
							foundHeader = true;
							endTag = GPX_END;
							// Check begin buffer for utf8 encoding
							if (beginBuffer != null && beginBuffer.toString().toLowerCase().indexOf("encoding=\"utf-8\"") > 0)
							{
								byteBuffer.setEncodingUtf8();
							}
							beginBuffer = null; // don't need it any more
						}
						else if (b == 't')
						{
							if (byteBuffer.foundSequence(TRKPT_START)) {
								insideTag = true;
								endTag = TRKPT_END;
							}
							else if (byteBuffer.foundSequence(WPT_START)) {
								insideTag = true;
								endTag = WPT_END;
							}
							else if (byteBuffer.foundSequence(RTEPT_START)) {
								insideTag = true;
								endTag = RTEPT_END;
							}
						}
					}
					else
					{
						// Look for end of found tag
						if (byteBuffer.foundSequence(endTag))
						{
							String tag = byteBuffer.toString();
							_receiver.reportTag(tag);
							byteBuffer.clear();
							insideTag = false;
						}
					}
					// Look for start of cdata block
					if (byteBuffer.foundSequence(CDATA_START)) {
						insideCdata = true;
					}
				}
			}
		}
		catch (IOException e) {} // ignore
	}
}
