package tim.prune.function.sew;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import tim.prune.App;
import tim.prune.cmd.SewSegmentsCmd;
import tim.prune.data.Coordinate;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.Latitude;
import tim.prune.data.Longitude;
import tim.prune.data.Track;
import tim.prune.gui.ProgressIndicator;

/**
 * Tests for the function to sew track segments together
 */
public class SewSegmentsTest
{
	/** Fake progress indicator */
	private static class FakeProgress implements ProgressIndicator {
		public void showProgress(int inCurrent, int inMax) {}
	}

	@Test
	public void testEmptyTrack()
	{
		App app = new App(null, null);
		Track track = app.getTrackInfo().getTrack();
		assertThrows(NothingDoneException.class, () -> new SewTrackSegmentsFunction(app).getSewSegmentsCommand(track, new FakeProgress()));
	}

	@Test
	public void testSingleSegment()
	{
		App app = new App(null, null);
		Track track = app.getTrackInfo().getTrack();
		for (int i=0; i<10; i++) {
			track.appendPoint(new DataPoint(Latitude.make("22.333"), Longitude.make("-11.44")));
		}
		track.getPoint(0).setSegmentStart(true);
		track.getPoint(7).setWaypointName("waypoint");
		try {
			SewSegmentsCmd command = new SewTrackSegmentsFunction(app).getSewSegmentsCommand(track, new FakeProgress());
			assertNull(command);
		}
		catch (NothingDoneException e) {
			assertEquals(1, e.numSegments);
		}
	}

	@Test
	public void testTwoSeparateSegments()
	{
		App app = new App(null, null);
		Track track = app.getTrackInfo().getTrack();
		for (int i=0; i<10; i++) {
			track.appendPoint(new DataPoint(Latitude.make("0." + i), Longitude.make("-4." + i)));
		}
		track.getPoint(0).setSegmentStart(true);
		track.getPoint(5).setSegmentStart(true);
		try {
			SewSegmentsCmd command = new SewTrackSegmentsFunction(app).getSewSegmentsCommand(track, new FakeProgress());
			assertNull(command);
		}
		catch (NothingDoneException e) {
			assertEquals(2, e.numSegments);
		}
	}

	@Test
	public void testTwoJoinableSegments() throws NothingDoneException
	{
		App app = new App(null, null);
		Track track = app.getTrackInfo().getTrack();
		for (int i=0; i<10; i++) {
			track.appendPoint(new DataPoint(Latitude.make("0." + i), Longitude.make("-4." + i)));
		}
		track.getPoint(0).setSegmentStart(true);
		// Make points 4 and 5 identical, with 5 being the next segment start
		for (int i=4; i<=5; i++) {
			track.getPoint(i).setFieldValue(Field.LATITUDE, "3.21", false);
			track.getPoint(i).setFieldValue(Field.LONGITUDE, "0.29", false);
		}
		track.getPoint(5).setSegmentStart(true);
		assertEquals(10, track.getNumPoints());
		String pointLats = describePointLatitudes(track);
		assertEquals("(0.0 0.1 0.2 0.3 3.2 (3.2 0.6 0.7 0.8 0.9", pointLats);

		// Now the sew should work
		SewSegmentsCmd command = new SewTrackSegmentsFunction(app).getSewSegmentsCommand(track, new FakeProgress());
		assertNotNull(command);
		assertTrue(command.execute(app.getTrackInfo()));
		assertEquals(9, track.getNumPoints());
		pointLats = describePointLatitudes(track);
		assertEquals("(0.0 0.1 0.2 0.3 3.2 0.6 0.7 0.8 0.9", pointLats);

		// and we should be able to undo
		assertTrue(command.getInverse().execute(app.getTrackInfo()));
		assertEquals(10, track.getNumPoints());
		pointLats = describePointLatitudes(track);
		assertEquals("(0.0 0.1 0.2 0.3 3.2 (3.2 0.6 0.7 0.8 0.9", pointLats);
	}

	@Test
	public void testTwoOppositeSegments() throws NothingDoneException
	{
		App app = new App(null, null);
		Track track = app.getTrackInfo().getTrack();
		int[] indexes = new int[] {5, 4, 3, 2, 1, 0, 8, 7, 6, 5};
		for (int i : indexes) {
			track.appendPoint(createPoint(i, track.getNumPoints() == 0 || track.getNumPoints() == 6));
		}
		assertEquals(10, track.getNumPoints());
		String originalLats = describePointLatitudes(track);
		assertEquals("(47.5 47.4 47.3 47.2 47.1 47.0 (47.8 47.7 47.6 47.5", originalLats);
		// Now the sew should work
		SewSegmentsCmd command = new SewTrackSegmentsFunction(app).getSewSegmentsCommand(track, new FakeProgress());
		assertNotNull(command);
		assertTrue(command.execute(app.getTrackInfo()));
		assertEquals(9, track.getNumPoints());
		String pointLats = describePointLatitudes(track);
		assertEquals("(47.8 47.7 47.6 47.5 47.4 47.3 47.2 47.1 47.0", pointLats);

		// and we should be able to undo
		assertTrue(command.getInverse().execute(app.getTrackInfo()));
		assertEquals(10, track.getNumPoints());
		pointLats = describePointLatitudes(track);
		assertEquals("(47.5 47.4 47.3 47.2 47.1 47.0 (47.8 47.7 47.6 47.5", pointLats);
	}

	@Test
	public void testThreeSegmentsWithReversal() throws NothingDoneException
	{
		// First segment can't be joined with second so it forms two chains which are then merged
		App app = new App(null, null);
		Track track = app.getTrackInfo().getTrack();
		int[] indexes = new int[] {5, 6, 7, 8, 14, 13, 12, 11, 11, 10, 9, 8};
		for (int i : indexes) {
			track.appendPoint(createPoint(i, (track.getNumPoints() % 4) == 0));
		}
		assertEquals(12, track.getNumPoints());
		String originalLats = describePointLatitudes(track);
		assertEquals("(47.5 47.6 47.7 47.8 (48.4 48.3 48.2 48.1 (48.1 48.0 47.9 47.8", originalLats);

		SewSegmentsCmd command = new SewTrackSegmentsFunction(app).getSewSegmentsCommand(track, new FakeProgress());
		assertNotNull(command);
		assertTrue(command.execute(app.getTrackInfo()));
		assertEquals(10, track.getNumPoints());
		String pointLats = describePointLatitudes(track);
		assertEquals("(47.5 47.6 47.7 47.8 47.9 48.0 48.1 48.2 48.3 48.4", pointLats);

		// and we should be able to undo
		assertTrue(command.getInverse().execute(app.getTrackInfo()));
		assertEquals(12, track.getNumPoints());
		pointLats = describePointLatitudes(track);
		assertEquals("(47.5 47.6 47.7 47.8 (48.4 48.3 48.2 48.1 (48.1 48.0 47.9 47.8", pointLats);
	}

	@Test
	public void testTwoSegmentsPlusSingleton() throws NothingDoneException
	{
		// First segment is a singleton which can't be merged
		App app = new App(null, null);
		Track track = app.getTrackInfo().getTrack();
		int[] indexes = new int[] {6, 4, 3, 2, 4, 5};
		List<Integer> breaks = List.of(0, 1, 4);
		for (int i : indexes) {
			track.appendPoint(createPoint(i, breaks.contains(track.getNumPoints())));
		}
		assertEquals(6, track.getNumPoints());
		String originalLats = describePointLatitudes(track);
		assertEquals("(47.6 (47.4 47.3 47.2 (47.4 47.5", originalLats);

		SewSegmentsCmd command = new SewTrackSegmentsFunction(app).getSewSegmentsCommand(track, new FakeProgress());
		assertNotNull(command);
		assertTrue(command.execute(app.getTrackInfo()));
		assertEquals(5, track.getNumPoints());
		String pointLats = describePointLatitudes(track);
		assertEquals("(47.6 (47.5 47.4 47.3 47.2", pointLats);

		// and we should be able to undo
		assertTrue(command.getInverse().execute(app.getTrackInfo()));
		assertEquals(6, track.getNumPoints());
		pointLats = describePointLatitudes(track);
		assertEquals("(47.6 (47.4 47.3 47.2 (47.4 47.5", pointLats);
	}

	@Test
	public void testShouldMergeSingleton() throws NothingDoneException
	{
		App app = new App(null, null);
		Track track = app.getTrackInfo().getTrack();
		int[] indexes = new int[] {6, 3, 4, 5, 6};
		List<Integer> breaks = List.of(0, 1);
		for (int i : indexes) {
			track.appendPoint(createPoint(i, breaks.contains(track.getNumPoints())));
		}
		assertEquals(5, track.getNumPoints());
		String originalLats = describePointLatitudes(track);
		assertEquals("(47.6 (47.3 47.4 47.5 47.6", originalLats);

		SewSegmentsCmd command = new SewTrackSegmentsFunction(app).getSewSegmentsCommand(track, new FakeProgress());
		assertTrue(command.execute(app.getTrackInfo()));
		assertEquals(4, track.getNumPoints());
		String pointLats = describePointLatitudes(track);
		assertEquals("(47.3 47.4 47.5 47.6", pointLats);
		// and we should be able to undo
		assertTrue(command.getInverse().execute(app.getTrackInfo()));
		assertEquals(5, track.getNumPoints());
		pointLats = describePointLatitudes(track);
		assertEquals("(47.6 (47.3 47.4 47.5 47.6", pointLats);
	}

	@Test
	public void testMergeThreeWithSingleton() throws NothingDoneException
	{
		App app = new App(null, null);
		Track track = app.getTrackInfo().getTrack();
		int[] indexes = new int[] {6, 3, 4, 5, 6, 2, 3};
		List<Integer> breaks = List.of(0, 1, 5);
		for (int i : indexes) {
			track.appendPoint(createPoint(i, breaks.contains(track.getNumPoints())));
		}
		assertEquals(7, track.getNumPoints());
		String originalLats = describePointLatitudes(track);
		assertEquals("(47.6 (47.3 47.4 47.5 47.6 (47.2 47.3", originalLats);

		SewSegmentsCmd command = new SewTrackSegmentsFunction(app).getSewSegmentsCommand(track, new FakeProgress());
		assertTrue(command.execute(app.getTrackInfo()));
		assertEquals(5, track.getNumPoints());
		String pointLats = describePointLatitudes(track);
		assertEquals("(47.2 47.3 47.4 47.5 47.6", pointLats);
		// and we should be able to undo
		assertTrue(command.getInverse().execute(app.getTrackInfo()));
		assertEquals(7, track.getNumPoints());
		pointLats = describePointLatitudes(track);
		assertEquals("(47.6 (47.3 47.4 47.5 47.6 (47.2 47.3", pointLats);
	}

	/**
	 * Create a single point for adding to a test track
	 * @param inIndex numbered index to control coordinates
	 * @param inSegmentFlag true for a new segment, false for continuation
	 * @return created point
	 */
	private DataPoint createPoint(int inIndex, boolean inSegmentFlag)
	{
		double latitude = 47 + inIndex * 0.1;
		double longitude = 9 + inIndex * 0.15;
		DataPoint point = new DataPoint(Latitude.make(latitude), Longitude.make(longitude));
		point.setSegmentStart(inSegmentFlag);
		return point;
	}

	private String describePointLatitudes(Track track)
	{
		StringBuilder result = new StringBuilder();
		for (int i=0; i<track.getNumPoints(); i++) {
			if (track.getPoint(i).getSegmentStart()) {
				result.append('(');
			}
			result.append(track.getPoint(i).getLatitude().output(Coordinate.Format.DEG_WITHOUT_CARDINAL, 1)).append(' ');
		}
		return result.toString().trim();
	}
}
