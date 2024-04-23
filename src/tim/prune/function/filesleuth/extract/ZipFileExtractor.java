package tim.prune.function.filesleuth.extract;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import tim.prune.function.filesleuth.data.LocationFilter;
import tim.prune.function.filesleuth.data.TrackContents;


/** Responsible for extracting text from zipped xml files */
public class ZipFileExtractor implements ContentExtractor
{
	private final File _file;

	public ZipFileExtractor(File inFile) {
		_file = inFile;
	}

	@Override
	public TrackContents getContents(TimeZone inTimezone)
	{
		ContentExtractor extractor = getExtractor();
		if (extractor == null) {
			return new TrackContents(inTimezone);
		}
		return extractor.getContents(inTimezone);
	}

	private ContentExtractor getExtractor()
	{
		if (_file == null || !_file.isFile() || !_file.canRead()) {
			return null;
		}
		try (ZipFile file = new ZipFile(_file))
		{
			Enumeration<?> entries = file.entries();
			while (entries.hasMoreElements())
			{
				ZipEntry entry = (ZipEntry) entries.nextElement();
				String entryName = entry.toString();
				if (entryName != null && entryName.length() > 4)
				{
					String suffix = entryName.substring(entryName.length()-4).toLowerCase();
					if (suffix.equals(".kml") || suffix.equals(".gpx") || suffix.equals(".xml"))
					{
						return new XmlFileExtractor(file.getInputStream(entry));
					}
					// TODO: Are we ignoring the possibility of zipped text/csv here?
				}
			}
		} catch (IOException ignored) {}
		return null;
	}

	@Override
	public boolean matchesFilter(LocationFilter inFilter)
	{
		ContentExtractor extractor = getExtractor();
		return extractor != null && extractor.matchesFilter(inFilter);
	}
}
