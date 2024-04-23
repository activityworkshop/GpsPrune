package tim.prune.function.filesleuth.extract;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;

import tim.prune.function.filesleuth.data.LocationFilter;
import tim.prune.function.filesleuth.data.TrackContents;


/** Responsible for extracting text from gzipped xml files */
public class GzipFileExtractor implements ContentExtractor
{
	// TODO: Are we ignoring the possibility of gzipped text files here?
	private final File _file;

	public GzipFileExtractor(File inFile) {
		_file = inFile;
	}

	@Override
	public TrackContents getContents(TimeZone inTimezone)
	{
		if (_file == null || !_file.isFile() || !_file.canRead()) {
			return new TrackContents(inTimezone);
		}
		try (GZIPInputStream istream = new GZIPInputStream(new FileInputStream(_file)))
		{
			return new XmlFileExtractor(istream).getContents(inTimezone);
		} catch (IOException ignored) {}
		return new TrackContents(inTimezone);
	}

	@Override
	public boolean matchesFilter(LocationFilter inFilter)
	{
		if (_file == null || !_file.isFile() || !_file.canRead()) {
			return false;
		}
		try (GZIPInputStream istream = new GZIPInputStream(new FileInputStream(_file)))
		{
			return new XmlFileExtractor(istream).matchesFilter(inFilter);
		} catch (IOException ignored) {}
		return false;
	}
}
