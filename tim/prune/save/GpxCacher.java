package tim.prune.save;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import tim.prune.data.DataPoint;
import tim.prune.data.SourceInfo;

/**
 * Class to read in a GPX file and cache all the point strings
 */
public class GpxCacher extends DefaultHandler
{
	private SourceInfo _sourceInfo = null;
	private String _headerString = null;
	private String[] _strings = null;
	private int _pointNum = 0;
	private boolean _insidePoint = false;
	private StringBuilder _builder = null;


	/**
	 * Constructor
	 * @param inSourceInfo source information
	 */
	public GpxCacher(SourceInfo inInfo)
	{
		_sourceInfo = inInfo;
		_strings = new String[inInfo.getNumPoints()];
		_pointNum = 0;
		// Should be a gpx file, but might be raw, zipped or gzipped
		File gpxFile = inInfo.getFile();
		String fileName = gpxFile.getName().toLowerCase();
		if (gpxFile.exists() && gpxFile.canRead())
		{
			try {
				SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
				if (fileName.endsWith(".gpx") || fileName.endsWith(".xml")) {
					saxParser.parse(inInfo.getFile(), this);
				}
				else if (fileName.endsWith(".zip")) {
					saxParser.parse(getZipInputStream(inInfo.getFile()), this);
				}
				else if (fileName.endsWith(".gz")) {
					saxParser.parse(new GZIPInputStream(new FileInputStream(inInfo.getFile())), this);
				}
				else {
					System.out.println("Unrecognised file type: " + inInfo.getFile().getName());
				}
			} catch (Exception e) {
				// TODO: Handle errors here with a list of warnings?
				e.printStackTrace();
			}
		}
		_builder = null;
	}


	/**
	 * Receive the start of a tag
	 */
	public void startElement(String inUri, String inLocalName, String inTagName,
		Attributes inAttributes) throws SAXException
	{
		if (inTagName.equalsIgnoreCase("gpx"))
		{
			// store initial gpx tag
			_builder = new StringBuilder(60);
			appendTag(_builder, inTagName, inAttributes);
			_headerString = _builder.toString();
		}
		else
		{
			if (inTagName.equalsIgnoreCase("wpt") || inTagName.equalsIgnoreCase("trkpt")
				|| inTagName.equalsIgnoreCase("rtept"))
			{
				_insidePoint = true;
				_builder = new StringBuilder(60);
			}
			if (_insidePoint) {
				appendTag(_builder, inTagName, inAttributes);
			}
		}
		super.startElement(inUri, inLocalName, inTagName, inAttributes);
	}

	/**
	 * Receive characters between tags (inside or outside)
	 */
	public void characters(char[] inChars, int inStart, int inLength)
		throws SAXException
	{
		if (_insidePoint) {
			_builder.append(new String(inChars, inStart, inLength));
		}
		super.characters(inChars, inStart, inLength);
	}

	/**
	 * Receive end of xml tag
	 */
	public void endElement(String inUri, String inLocalName, String inTagName)
		throws SAXException
	{
		if (_insidePoint) {
			_builder.append("</").append(inTagName).append('>');
		}
		if (inTagName.equalsIgnoreCase("wpt") || inTagName.equalsIgnoreCase("trkpt")
			|| inTagName.equalsIgnoreCase("rtept"))
		{
			_strings[_pointNum] = _builder.toString();
			_pointNum++;
			_insidePoint = false;
		}
		super.endElement(inUri, inLocalName, inTagName);
	}


	/**
	 * Append the current tag to the supplied StringBuilder
	 * @param inBuilder Stringbuilder object to append tag to
	 * @param inTagName name of tag
	 * @param inAttributes attributes of tag
	 */
	private static void appendTag(StringBuilder inBuilder, String inTagName, Attributes inAttributes)
	{
		inBuilder.append('<').append(inTagName);
		int numAtts = inAttributes.getLength();
		for (int i=0; i<numAtts; i++) {
			inBuilder.append(' ').append(inAttributes.getQName(i)).append("=\"")
				.append(inAttributes.getValue(i)).append('"');
		}
		inBuilder.append('>');
	}


	/**
	 * @return the header string from the GPX tag
	 */
	public String getHeaderString()
	{
		return _headerString;
	}

	/**
	 * Get the source string for the given point
	 * @param inPoint point to retrieve
	 * @return string if found, otherwise null
	 */
	public String getSourceString(DataPoint inPoint)
	{
		int index = _sourceInfo.getIndex(inPoint);
		if (index >= 0) {
			return _strings[index];
		}
		return null;
	}

	/**
	 * Get an inputstream of a GPX file inside a zip
	 * @param inFile File object describing zip file
	 * @return input stream for Xml parser
	 */
	private static InputStream getZipInputStream(File inFile)
	{
		try
		{
			ZipInputStream zis = new ZipInputStream(new FileInputStream(inFile));
			while (zis.available() > 0)
			{
				ZipEntry entry = zis.getNextEntry();
				String entryName = entry.toString();
				if (entryName != null && entryName.length() > 4)
				{
					String suffix = entryName.substring(entryName.length()-4).toLowerCase();
					if (suffix.equals(".gpx") || suffix.equals(".xml")) {
						// First matching file so must be gpx
						return zis;
					}
				}
			}
		}
		catch (Exception e) {} // ignore errors
		// not found - error!
		return null;
	}
}
