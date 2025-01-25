package tim.prune.data;

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FileInfoTest
{
	@Test
	public void testEmptyInfo()
	{
		FileInfo info = new FileInfo();
		Assertions.assertEquals(0, info.getNumFiles());
		Assertions.assertEquals("", info.getFilename());
		Assertions.assertTrue(info.getAllTitles().isEmpty());
		Assertions.assertTrue(info.getAllDescriptions().isEmpty());
	}

	@Test
	public void testInfoWithoutSources()
	{
		FileInfo info = new FileInfo();
		info.addSource(null);
		info.addSource(null);
		Assertions.assertEquals(0, info.getNumFiles());
		Assertions.assertEquals("", info.getFilename());
		Assertions.assertTrue(info.getAllTitles().isEmpty());
		Assertions.assertTrue(info.getAllDescriptions().isEmpty());
	}

	@Test
	public void testInfoWithSingleSource()
	{
		FileInfo info = new FileInfo();
		SourceInfo source = new SourceInfo(new File("example.gpx"), FileType.GPX);
		source.setFileTitle("my title");
		// Deliberately add the same source twice, second one is ignored
		info.addSource(source);
		info.addSource(source);
		Assertions.assertEquals(1, info.getNumFiles());
		Assertions.assertEquals("example.gpx", info.getFilename());
		Assertions.assertEquals(1, info.getAllTitles().size());
		Assertions.assertEquals("my title", info.getSource(0).getFileTitle());
		Assertions.assertTrue(info.getAllDescriptions().isEmpty());
		Assertions.assertNull(info.getSource(0).getFileDescription());
	}

	@Test
	public void testInfoWithTwoSources()
	{
		FileInfo info = new FileInfo();
		SourceInfo source1 = new SourceInfo(new File("example1.gpx"), FileType.GPX);
		source1.setFileTitle("first title");
		source1.setFileDescription("first longer description");
		SourceInfo source2 = new SourceInfo(new File("example2.kml"), FileType.KML);
		source2.setFileTitle("second title");
		// Add these sources multiple times
		SourceInfo[] sources = new SourceInfo[] {source1, source2, source1, null, source1, null, source2};
		for (SourceInfo source : sources) {
			info.addSource(source);
		}

		Assertions.assertEquals(2, info.getNumFiles());
		// No filename returned because there is more than one
		Assertions.assertEquals("", info.getFilename());
		Assertions.assertEquals(2, info.getAllTitles().size());
		Assertions.assertEquals("first title", info.getAllTitles().get(0));
		Assertions.assertEquals("second title", info.getAllTitles().get(1));
		Assertions.assertEquals(1, info.getAllDescriptions().size());
		Assertions.assertEquals("first longer description", info.getAllDescriptions().get(0));
	}
}
