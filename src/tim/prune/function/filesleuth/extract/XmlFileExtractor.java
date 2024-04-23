package tim.prune.function.filesleuth.extract;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.TimeZone;

import tim.prune.function.filesleuth.data.LocationFilter;
import tim.prune.function.filesleuth.data.TrackContents;


/**
 * Responsible for extracting text from gpx/kml files
 * by receiving the contents from a separate XmlParser
 */
public class XmlFileExtractor implements ContentExtractor, ContentReceiver
{
	private final File _file;
	private final InputStream _stream;
	private TrackContents _contents = null;
	private LocationFilter _locationFilter = null;
	private final Object _mutex = new Object();
	private boolean _filterMatched = false;
	private boolean _finishedParsing = false;


	public XmlFileExtractor(File inFile)
	{
		_file = inFile;
		_stream = null;
	}

	public XmlFileExtractor(InputStream inStream)
	{
		_file = null;
		_stream = inStream;
	}

	@Override
	public TrackContents getContents(TimeZone inTimezone)
	{
		_contents = new TrackContents(inTimezone);
		_locationFilter = null;
		readFile();
		return _contents;
	}

	/** Read the file and block until it has been completely read */
	private void readFile()
	{
		synchronized(_mutex)
		{
			_finishedParsing = false;
			XmlParser parser = new XmlParser(this);
			if (_file != null)
			{
				try (FileInputStream inStream = new FileInputStream(_file))
				{
					if (!parser.parseXmlStream(inStream)) {
						_finishedParsing = true;
					}
				}
				catch (IOException ioe) {
					_finishedParsing = true;
				}
			}
			else if (_stream == null
				|| !parser.parseXmlStream(_stream))
			{
				_finishedParsing = true;
			}
			while (!_finishedParsing)
			{
				// This is inside a synchronized block but the wait yields the lock
				// to allow the parsing thread to set the finished flag
				try {
					_mutex.wait();
				} catch (InterruptedException e) {}
			}
		}
	}

	@Override
	public void endDocument()
	{
		synchronized(_mutex)
		{
			_finishedParsing = true;
			_mutex.notifyAll();
		}
	}

	@Override
	public void addString(String inValue)
	{
		if (_contents != null) {
			_contents.addString(inValue);
		}
	}

	@Override
	public void setName(String inName)
	{
		if (_contents != null) {
			_contents.setName(inName);
		}
	}

	@Override
	public void setDescription(String inDesc)
	{
		if (_contents != null) {
			_contents.setDescription(inDesc);
		}
	}

	@Override
	public void addDateString(String inDate)
	{
		if (_contents != null) {
			_contents.addDateString(inDate);
		}
	}

	@Override
	public void addCoordinates(double inLatitude, double inLongitude)
	{
		if (_contents != null) {
			_contents.addCoordinates(inLatitude, inLongitude);
		}
		else if (_locationFilter != null && !_filterMatched) {
			_filterMatched = _locationFilter.doesLocationMatch(inLatitude, inLongitude);
		}
	}

	@Override
	public boolean matchesFilter(LocationFilter inFilter)
	{
		_locationFilter = inFilter;
		_contents = null;
		_filterMatched = false;
		readFile();
		return _filterMatched;
	}
}
