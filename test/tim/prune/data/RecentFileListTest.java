package tim.prune.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * Tests for the RecentFileList class
 */
public class RecentFileListTest
{
	@Test
	public void testEmptyList()
	{
		RecentFileList files = new RecentFileList();
		assertEquals(6, files.getCapacity());
		assertEquals(0, files.getNumEntries());
		assertNull(files.getFile(0));
		assertEquals("6;;;;;;", files.getConfigString());
	}

	@Test
	public void testSingleEntry() throws IOException
	{
		RecentFileList files = new RecentFileList();
		File tempFile = FileUtils.makeValidFile();
		files.addFile(new RecentFile(tempFile, true));
		assertEquals(1, files.getNumEntries());
		assertNotNull(files.getFile(0));
		String expectedConfig = "6;r" + tempFile.getAbsolutePath() + ";;;;;";
		assertEquals(expectedConfig, files.getConfigString());
		// add the same file again
		files.addFile(new RecentFile(tempFile, true));
		assertEquals(1, files.getNumEntries());
		assertNotNull(files.getFile(0));
		assertEquals(expectedConfig, files.getConfigString());
	}

	@Test
	public void testRemoveInvalidFile() throws IOException
	{
		RecentFileList files = new RecentFileList();
		File tempFile = FileUtils.makeValidFile();
		files.addFile(new RecentFile(tempFile, true));
		assertEquals(1, files.getNumEntries());
		assertNotNull(files.getFile(0));
		// Delete file and review
		assertTrue(tempFile.delete());
		files.verifyAll();
		assertEquals(0, files.getNumEntries());
		assertNull(files.getFile(0));
	}
}
