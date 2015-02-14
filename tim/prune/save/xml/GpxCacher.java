package tim.prune.save.xml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import tim.prune.data.DataPoint;
import tim.prune.data.SourceInfo;

/**
 * Class to read in a GPX file and cache all the point strings
 */
public class GpxCacher implements TagReceiver
{
	private SourceInfo _sourceInfo = null;
	private String _headerString = null;
	private String[] _strings = null;
	private int _pointNum = 0;


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
			GpxSlicer slicer = new GpxSlicer(this);
			InputStream istream = null;
			BufferedInputStream bstream = null;
			try {
				if (fileName.endsWith(".gpx") || fileName.endsWith(".xml")) {
					istream = new FileInputStream(inInfo.getFile());
				}
				else if (fileName.endsWith(".zip")) {
					istream = getZipInputStream(inInfo.getFile());
				}
				else if (fileName.endsWith(".gz")) {
					istream = new GZIPInputStream(new FileInputStream(inInfo.getFile()));
				}
				else {
					System.out.println("GpxCacher unrecognised file type: " + inInfo.getFile().getName());
				}
				if (istream != null) {
					bstream = new BufferedInputStream(istream);
					slicer.slice(bstream);
					bstream.close();
				}
			} catch (Exception e) {
				// TODO: Handle errors here with a list of warnings?
				e.printStackTrace();
			}
		}
	}

	/**
	 * Accept a tag from the slicer
	 */
	public void reportTag(String inTag)
	{
		if (_headerString == null) {
			_headerString = inTag;
		}
		else {
			_strings[_pointNum] = inTag;
			_pointNum++;
		}
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
