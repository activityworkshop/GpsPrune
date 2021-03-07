package tim.prune;

import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.FieldList;
import tim.prune.data.RangeStats;
import tim.prune.data.TimestampUtc;
import tim.prune.data.Track;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class TestRangeStats {
	@Test
	void movingTime() {
		Track track = new Track();
		DataPoint[] points = {
			new DataPoint(
				new String[] {
					"01-Jan-2020 00:00:00",
				},
				new FieldList(new Field[] {
					Field.TIMESTAMP,
				}),
				null),
			new DataPoint(
				new String[] {
					"01-Jan-2020 00:00:05",
				},
				new FieldList(new Field[] {
					Field.TIMESTAMP,
				}),
				null),
			new DataPoint(
				new String[] {
					"01-Jan-2020 00:00:07",
				},
				new FieldList(new Field[] {
					Field.TIMESTAMP,
				}),
				null),
		};
		track.appendPoints(points);

		RangeStats range = new RangeStats(track, 0, track.getNumPoints() - 1);
		assertEquals(7, range.getMovingDurationInSeconds());
		assertEquals(7, range.getTotalDurationInSeconds());
		assertFalse(range.getTimestampsIncomplete());
		assertFalse(range.getTimestampsOutOfSequence());
	}

	@Test
	void movingTimeWithGap() {
		Track track = new Track();
		DataPoint[] points = {
			new DataPoint(
				new String[] {
					"01-Jan-2020 00:00:00",
				},
				new FieldList(new Field[] {
					Field.TIMESTAMP,
				}),
				null),
			new DataPoint(
				new String[] {},
				new FieldList(new Field[] {}),
				null),
			new DataPoint(
				new String[] {
					"01-Jan-2020 00:00:05",
				},
				new FieldList(new Field[] {
					Field.TIMESTAMP,
				}),
				null),
			new DataPoint(
				new String[] {
					"01-Jan-2020 00:00:07",
				},
				new FieldList(new Field[] {
					Field.TIMESTAMP,
				}),
				null),
		};
		track.appendPoints(points);

		RangeStats range = new RangeStats(track, 0, track.getNumPoints() - 1);
		assertEquals(7, range.getMovingDurationInSeconds());
		assertEquals(7, range.getTotalDurationInSeconds());
		assertTrue(range.getTimestampsIncomplete());
		assertFalse(range.getTimestampsOutOfSequence());
	}

	@Test
	void movingTimeSeveralSegments() {
		Track track = new Track();
		DataPoint[] points = {
			new DataPoint(
				new String[] {
					"01-Jan-2020 00:01:00",
				},
				new FieldList(new Field[] {
					Field.TIMESTAMP,
				}),
				null),
			new DataPoint(
				new String[] {},
				new FieldList(new Field[] {}),
				null),
			new DataPoint(
				new String[] {
					"01-Jan-2020 00:01:05",
				},
				new FieldList(new Field[] {
					Field.TIMESTAMP,
				}),
				null),
			new DataPoint(
				new String[] {
					"01-Jan-2020 00:01:07",
				},
				new FieldList(new Field[] {
					Field.TIMESTAMP,
				}),
				null),
			// start a second segment
			new DataPoint(
				new String[] {
					"01-Jan-2020 00:00:20",
					"1",
				},
				new FieldList(new Field[] {
					Field.TIMESTAMP,
					Field.NEW_SEGMENT,
				}),
				null),
			new DataPoint(
				new String[] {
					"01-Jan-2020 00:00:27",
				},
				new FieldList(new Field[] {
					Field.TIMESTAMP,
				}),
				null),
		};
		track.appendPoints(points);

		RangeStats range = new RangeStats(track, 0, track.getNumPoints() - 1);
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
	void movingTimeMissingFirstTimestamp() {
		Track track = new Track();
		DataPoint[] points = {
			new DataPoint(
				new String[] {},
				new FieldList(new Field[] {}),
				null),
			new DataPoint(
				new String[] {
					"01-Jan-2020 00:00:00",
				},
				new FieldList(new Field[] {
					Field.TIMESTAMP,
				}),
				null),
			new DataPoint(
				new String[] {
					"01-Jan-2020 00:00:05",
				},
				new FieldList(new Field[] {
					Field.TIMESTAMP,
				}),
				null),
		};
		track.appendPoints(points);

		RangeStats range = new RangeStats(track, 0, track.getNumPoints() - 1);
		assertEquals(5, range.getMovingDurationInSeconds());
		assertEquals(5, range.getTotalDurationInSeconds());
		assertTrue(range.getTimestampsIncomplete());
		assertFalse(range.getTimestampsOutOfSequence());
	}

}
