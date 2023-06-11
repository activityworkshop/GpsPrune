package tim.prune.data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * JUnit tests for calculation of moving time of a range
 * based on different timestamp availability
 * @author fperrin
 */
class RangeStatsTest
{
	@Test
	void movingTime()
	{
		Track track = new Track();
		List<DataPoint> points = List.of(
			createDataPoint("01-Jan-2020 00:00:00"),
			createDataPoint("01-Jan-2020 00:00:05"),
			createDataPoint("01-Jan-2020 00:00:07")
		);
		track.appendRange(points);

		RangeStats range = new RangeStats(track, 0, track.getNumPoints() - 1, 0);
		assertEquals(7, range.getMovingDurationInSeconds());
		assertEquals(7, range.getTotalDurationInSeconds());
		assertFalse(range.getTimestampsIncomplete());
		assertFalse(range.getTimestampsOutOfSequence());
	}

	@Test
	void movingTimeWithGap()
	{
		Track track = new Track();
		List<DataPoint> points = List.of(
			createDataPoint("01-Jan-2020 00:00:00"),
			createDataPoint(""),
			createDataPoint("01-Jan-2020 00:00:05"),
			createDataPoint("01-Jan-2020 00:00:07")
		);
		track.appendRange(points);

		RangeStats range = new RangeStats(track, 0, track.getNumPoints() - 1, 0);
		assertEquals(7, range.getMovingDurationInSeconds());
		assertEquals(7, range.getTotalDurationInSeconds());
		assertTrue(range.getTimestampsIncomplete());
		assertFalse(range.getTimestampsOutOfSequence());
	}

	@Test
	void movingTimeSeveralSegments()
	{
		Track track = new Track();
		List<DataPoint> points = List.of(
			createDataPoint("01-Jan-2020 00:01:00"),
			createDataPoint(""),
			createDataPoint("01-Jan-2020 00:01:05"),
			createDataPoint("01-Jan-2020 00:01:07"),
			// start a second segment
			createDataPoint("01-Jan-2020 00:00:20", true),
			createDataPoint("01-Jan-2020 00:00:27")
		);
		track.appendRange(points);

		RangeStats range = new RangeStats(track, 0, track.getNumPoints() - 1, 0);
		assertEquals(7 + 7, range.getMovingDurationInSeconds());
		assertEquals(47, range.getTotalDurationInSeconds());
		assertTrue(range.getEarliestTimestamp().isEqual(new TimestampUtc("01-Jan-2020 00:00:20")));
		assertTrue(range.getLatestTimestamp().isEqual(new TimestampUtc("01-Jan-2020 00:01:07")));
		assertTrue(range.getTimestampsIncomplete());

		// even though segment 2 is earlier than segment 1, timestamps
		// within each segment are normally ordered
		assertFalse(range.getTimestampsOutOfSequence());
	}

	@Test
	void movingTimeMissingFirstTimestamp()
	{
		Track track = new Track();
		List<DataPoint> points = List.of(
			createDataPoint(""),
			createDataPoint("01-Jan-2020 00:00:00"),
			createDataPoint("01-Jan-2020 00:00:05")
		);
		track.appendRange(points);

		RangeStats range = new RangeStats(track, 0, track.getNumPoints() - 1, 0);
		assertEquals(5, range.getMovingDurationInSeconds());
		assertEquals(5, range.getTotalDurationInSeconds());
		assertTrue(range.getTimestampsIncomplete());
		assertFalse(range.getTimestampsOutOfSequence());
	}

	private DataPoint createDataPoint(String timestamp) {
		return createDataPoint(timestamp, false);
	}

	private DataPoint createDataPoint(String timestamp, boolean newSegment) {
		return new DataPoint(
			new String[] {timestamp, newSegment ? "1" : "0"},
			new FieldList(Field.TIMESTAMP, Field.NEW_SEGMENT),
			null);
	}
}
