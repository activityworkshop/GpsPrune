package tim.prune.function.filesleuth.data;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class TestFileTypeBlacklist
{
	@Test
	public void testGetExtension()
	{
		Assertions.assertEquals("txt", FileTypeBlacklist.getExtension(Paths.get("file1.txt")));
		Assertions.assertEquals("", FileTypeBlacklist.getExtension(Paths.get("file1.")));
		Assertions.assertEquals("", FileTypeBlacklist.getExtension(Paths.get("file1")));
		Assertions.assertEquals("csv", FileTypeBlacklist.getExtension(Paths.get("file1.something.csv")));
		Assertions.assertEquals("pruneconfig", FileTypeBlacklist.getExtension(Paths.get(".pruneconfig")));
	}

	@Test
	public void testAllowGoodTypes()
	{
		String[] gpsFileTypes = "gpx,kmz,kml,txt,csv,nmea,json,GPX,KMZ,KML,TXT,CSV,NMEA,JSON".split(",");
		for (String type : gpsFileTypes)
		{
			Path path = Paths.get("filename." + type);
			Assertions.assertTrue(FileTypeBlacklist.isAllowed(path), path.getFileName().toString());
		}
		Assertions.assertTrue(FileTypeBlacklist.isAllowed(Paths.get("filename")));
		Assertions.assertTrue(FileTypeBlacklist.isAllowed(Paths.get("filename.")));
	}

	@Test
	public void testBlockBadTypes()
	{
		String[] otherFileTypes = "PPT,PDF,Docx,xlsx,Gif,JPG,Jpeg,PDF,avi,bin,sh,bat".split(",");
		for (String type : otherFileTypes)
		{
			Path path = Paths.get("filename." + type);
			Assertions.assertFalse(FileTypeBlacklist.isAllowed(path), path.getFileName().toString());
		}
		Assertions.assertFalse(FileTypeBlacklist.isAllowed(Paths.get(".pruneconfig")));
	}
}
