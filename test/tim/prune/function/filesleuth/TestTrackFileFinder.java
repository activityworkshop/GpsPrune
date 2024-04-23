package tim.prune.function.filesleuth;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import tim.prune.function.filesleuth.data.TrackFile;
import tim.prune.function.filesleuth.data.TrackFileList;

import java.io.File;
import java.net.URL;
import java.util.List;

public class TestTrackFileFinder
{
	private File getDataDir() {
		URL url = TestTrackFileFinder.class.getResource("data");
		return new File(url.getPath());
	}

	@Test
	public void testFind()
	{
		TrackFileList tracks = new TrackFileList();
		new TrackFileFinder(tracks, getDataDir(), true).begin();
		// wait for asynchronous completion
		try {Thread.sleep(1000);} catch (InterruptedException ignored) {}
		// Should be at least two (more files may be added later)
		Assertions.assertTrue(tracks.getCurrentSize() >= 2);
		Assertions.assertTrue(contains(tracks.getCurrentContents(), "file1.txt"));
		Assertions.assertTrue(contains(tracks.getCurrentContents(), "file2.txt"));
	}

	/** Check if the given name is contained within the given track file list */
	private boolean contains(List<TrackFile> inTracks, String inName)
	{
		if (inTracks == null) {
			return false;
		}
		for (TrackFile track : inTracks)
		{
			if (track.getFile().getName().equals(inName)) {
				return true;
			}
		}
		return false;
	}

	static class ListenerCounter implements TrackListListener
	{
		public int numCalls = 0;
		@Override
		public void reactToTrackListChange(int inIndex) {
			numCalls++;
		}
	}

	@Test
	public void testCountFinds()
	{
		TrackFileList tracks = new TrackFileList();
		ListenerCounter listener = new ListenerCounter();
		tracks.addListener(listener);
		new TrackFileFinder(tracks, getDataDir(), true).begin();
		// wait for asynchronous completion
		try {Thread.sleep(1000);} catch (InterruptedException ignored) {}
		Assertions.assertTrue(listener.numCalls >= 2);
	}
}
