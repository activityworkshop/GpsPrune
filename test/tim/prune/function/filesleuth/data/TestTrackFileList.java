package tim.prune.function.filesleuth.data;

import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestTrackFileList
{
	@Test
	public void testEmptyList()
	{
		TrackFileList list = new TrackFileList();
		assertTrue(list.getCurrentContents().isEmpty());
		list.clear();
		assertTrue(list.getCurrentContents().isEmpty());
	}

	static class BoolHolder {
		public boolean flag;
	}

	@Test
	public void testTriggerOnAdd()
	{
		BoolHolder bool = new BoolHolder();
		assertFalse(bool.flag);
		TrackFileList list = new TrackFileList();
		list.addListener((i) -> bool.flag = true);
		list.foundFile(Paths.get("blah.gpx"));
		assertTrue(bool.flag); // flag was set to true by triggering the lambda

		// Check status (partly also testing behaviour of TrackFile constructor)
		assertEquals(1, list.getCurrentSize());
		assertEquals(TrackFileStatus.FOUND, list.getCurrentContents().get(0).getStatus());
	}

	@Test
	public void testClaimFile()
	{
		TrackFileList list = new TrackFileList();
		list.foundFile(Paths.get("blah.gpx"));
		list.addListener((i) -> fail("Claim should not trigger listeners"));

		// Claim single file
		TrackFile file = list.getCurrentContents().get(0);
		list.claimTrackFileForScanning(file);

		// Check status
		assertEquals(1, list.getCurrentSize());
		assertEquals(TrackFileStatus.SCANNING, list.getCurrentContents().get(0).getStatus());
	}

	@Test
	public void testScanComplete()
	{
		TrackFileList list = new TrackFileList();
		list.foundFile(Paths.get("blah.gpx"));

		BoolHolder bool = new BoolHolder();
		list.addListener((i) -> bool.flag = true);
		assertFalse(bool.flag);

		// Scan single file
		TrackFile file = list.getCurrentContents().get(0);
		TrackContents contents = new TrackContents(null);
		list.scanComplete(file, contents);

		// Check status
		assertEquals(1, list.getCurrentSize());
		assertEquals(TrackFileStatus.COMPLETE, list.getCurrentContents().get(0).getStatus());
		assertTrue(bool.flag); // set by trigger
	}

	@Test
	public void testFixedList()
	{
		TrackFileList list = new TrackFileList();
		list.foundFile(Paths.get("blah.gpx"));

		List<TrackFile> tracks = list.getCurrentContents();
		assertEquals(1, tracks.size());

		// Meanwhile, another track is found
		list.foundFile(Paths.get("second.kmz"));
		assertEquals(1, tracks.size()); // list obtained earlier still has only 1 entry
		assertEquals(2, list.getCurrentContents().size()); // getting the list again finds 2
	}
}
