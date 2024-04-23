package tim.prune.function.filesleuth;

import org.junit.jupiter.api.Test;

import tim.prune.function.filesleuth.data.TrackFile;
import tim.prune.function.filesleuth.data.TrackFileList;
import tim.prune.function.filesleuth.data.TrackFileStatus;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class TestScanController
{
	@Test
	public void testSingleFakeScanner()
	{
		TrackFileList tracks = new TrackFileList();
		ScanController controller = new ScanController(tracks, null);
		controller.setScannerCreator(FakeFileScanner::new);
		tracks.foundFile(Paths.get("fakefile.gpx"));
		assertEquals(1, tracks.getCurrentSize());
		// Worker should have already started, so initial status FOUND should already be SCANNING
		TrackFile track = tracks.getCurrentContents().get(0);
		assertEquals(TrackFileStatus.SCANNING, track.getStatus());
		assertFalse(track.hasContents());
		SearchResult result = new SearchResult(track);
		assertFalse(track.matchesStringFilter("bage", result));
		assertFalse(result.isMatch());

		// Wait for fake scanner to complete its work
		try {
			Thread.sleep(2000L);
		}
		catch (InterruptedException ignored) {}
		assertEquals(1, tracks.getCurrentSize());
		track = tracks.getCurrentContents().get(0);
		assertEquals(TrackFileStatus.COMPLETE, track.getStatus());
		assertTrue(track.hasContents());
		assertTrue(track.matchesStringFilter("bage", result));
		assertTrue(result.isMatch());
	}

	@Test
	public void testTwentyFakeScanners()
	{
		final int NUM_SCANNERS = 20;
		TrackFileList tracks = new TrackFileList();
		ScanController controller = new ScanController(tracks, null);
		controller.setScannerCreator(FakeFileScanner::new);
		for (int i=0; i<NUM_SCANNERS; i++) {
			tracks.foundFile(Paths.get("fakefile.gpx"));
		}
		assertEquals(NUM_SCANNERS, tracks.getCurrentContents().size());
		// all tracks should have been claimed (SCANNING) even though many are still waiting in the queue
		int numFound = countTracksWithStatus(tracks, TrackFileStatus.FOUND);
		assertEquals(0, numFound);
		int numScanning = countTracksWithStatus(tracks, TrackFileStatus.SCANNING);
		assertEquals(NUM_SCANNERS, numScanning);
		int numComplete = countTracksWithStatus(tracks, TrackFileStatus.COMPLETE);
		assertEquals(0, numComplete);

		// Wait for all fake scanners to complete their work
		try {
			Thread.sleep(3000L);
		}
		catch (InterruptedException ignored) {}
		numFound = countTracksWithStatus(tracks, TrackFileStatus.FOUND);
		assertEquals(0, numFound);
		numScanning = countTracksWithStatus(tracks, TrackFileStatus.SCANNING);
		assertEquals(0, numScanning);
		numComplete = countTracksWithStatus(tracks, TrackFileStatus.COMPLETE);
		assertEquals(NUM_SCANNERS, numComplete);
	}

	private int countTracksWithStatus(TrackFileList inTracks, TrackFileStatus inStatus)
	{
		int num = 0;
		for (TrackFile track : inTracks.getCurrentContents()) {
			if (track.getStatus() == inStatus) {
				num++;
			}
		}
		return num;
	}
}
