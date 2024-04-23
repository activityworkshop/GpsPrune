package tim.prune.function.filesleuth.data;

import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class TestFileTypeBlacklist
{
	@Test
	public void testGetExtension()
	{
		Assertions.assertEquals("txt", FileTypeBlacklist.getExtension(Path.of("file1.txt")));
		Assertions.assertEquals("", FileTypeBlacklist.getExtension(Path.of("file1.")));
		Assertions.assertEquals("", FileTypeBlacklist.getExtension(Path.of("file1")));
		Assertions.assertEquals("csv", FileTypeBlacklist.getExtension(Path.of("file1.something.csv")));
		Assertions.assertEquals("pruneconfig", FileTypeBlacklist.getExtension(Path.of(".pruneconfig")));
	}

	@Test
	public void testAllowGoodTypes()
	{
		String[] gpsFileTypes = "gpx,kmz,kml,txt,csv,nmea,json,GPX,KMZ,KML,TXT,CSV,NMEA,JSON".split(",");
		for (String type : gpsFileTypes)
		{
			Path path = Path.of("filename." + type);
			Assertions.assertTrue(FileTypeBlacklist.isAllowed(path), path.getFileName().toString());
		}
		Assertions.assertTrue(FileTypeBlacklist.isAllowed(Path.of("filename")));
		Assertions.assertTrue(FileTypeBlacklist.isAllowed(Path.of("filename.")));
	}

	@Test
	public void testBlockBadTypes()
	{
		String[] otherFileTypes = "PPT,PDF,Docx,xlsx,Gif,JPG,Jpeg,PDF,avi,bin,sh,bat".split(",");
		for (String type : otherFileTypes)
		{
			Path path = Path.of("filename." + type);
			Assertions.assertFalse(FileTypeBlacklist.isAllowed(path), path.getFileName().toString());
		}
		Assertions.assertFalse(FileTypeBlacklist.isAllowed(Path.of(".pruneconfig")));
	}
}
