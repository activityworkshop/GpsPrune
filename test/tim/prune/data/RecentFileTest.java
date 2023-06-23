package tim.prune.data;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the RecentFile class
 */
public class RecentFileTest
{
	@Test
	public void testCreateConfigStrings() throws IOException
	{
		RecentFile noFile = new RecentFile(null, true);
		assertEquals("", noFile.getConfigString());

		File tempFile = FileUtils.makeValidFile();
		RecentFile someFile = new RecentFile(tempFile, true);
		assertEquals("r" + tempFile.getAbsolutePath(), someFile.getConfigString());
		assertTrue(someFile.isValid());
		assertTrue(someFile.isRegularLoad());
	}

	@Test
	public void testLoadConfigStrings() throws IOException
	{
		File tempFile = FileUtils.makeValidFile();
		String configString = "g" + tempFile.getAbsolutePath();
		RecentFile aFile = new RecentFile(configString);
		assertTrue(aFile.isValid());
		assertFalse(aFile.isRegularLoad());
	}
}
