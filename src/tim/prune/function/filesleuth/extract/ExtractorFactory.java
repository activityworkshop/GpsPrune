package tim.prune.function.filesleuth.extract;

import java.io.File;

public abstract class ExtractorFactory
{
	/** Use the filename extension to create an appropriate extractor */
	public static ContentExtractor createExtractor(File inFile)
	{
		String filename = (inFile == null ? null : inFile.getName().toLowerCase());
		if (filename == null || !filename.contains(".")) {
			return null;
		}
		if (filename.endsWith(".txt") || filename.endsWith(".csv")) {
			return new TextFileExtractor(inFile);
		}
		if (filename.endsWith(".gpx") || filename.endsWith(".kml")) {
			return new XmlFileExtractor(inFile);
		}
		if (filename.endsWith(".kmz") || filename.endsWith(".zip")) {
			return new ZipFileExtractor(inFile);
		}
		if (filename.endsWith(".gz")) {
			return new GzipFileExtractor(inFile);
		}
		// TODO: what about json / nmea?
		return null;
	}
}
