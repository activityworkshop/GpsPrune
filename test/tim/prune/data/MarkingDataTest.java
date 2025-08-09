package tim.prune.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MarkingDataTest
{
	@Test
	public void testNoPointsNoFlags()
	{
		Track track = new Track();
		MarkingData marks = new MarkingData(track);
		Assertions.assertFalse(marks.isPointMarkedForDeletion(0));
		Assertions.assertFalse(marks.isPointMarkedForSegmentBreak(0));
	}

	@Test
	public void testOnePointNoFlags()
	{
		Track track = new Track();
		track.appendPoint(new DataPoint(1.81, 5.44));
		MarkingData marks = new MarkingData(track);
		Assertions.assertFalse(marks.isPointMarkedForDeletion(0));
		Assertions.assertFalse(marks.isPointMarkedForSegmentBreak(0));
	}

	@Test
	public void testTwoPointsOneMarked()
	{
		Track track = new Track();
		track.appendPoint(new DataPoint(1.81, 5.44));
		track.appendPoint(new DataPoint(1.91, 5.54));
		MarkingData marks = new MarkingData(track);
		marks.markPointsForDeletion(new boolean[] {false, true});
		Assertions.assertFalse(marks.isPointMarkedForDeletion(0));
		Assertions.assertTrue(marks.isPointMarkedForDeletion(1));
		Assertions.assertFalse(marks.isPointMarkedForSegmentBreak(0));
		Assertions.assertFalse(marks.isPointMarkedForSegmentBreak(1));
	}

	@Test
	public void testTwoPointsDoublyMarked()
	{
		Track track = new Track();
		track.appendPoint(new DataPoint(1.81, 5.44));
		track.appendPoint(new DataPoint(1.91, 5.54));
		MarkingData marks = new MarkingData(track);
		marks.markPointsForDeletion(new boolean[] {true, false}, new boolean[] {true, false});
		Assertions.assertTrue(marks.isPointMarkedForDeletion(0));
		Assertions.assertFalse(marks.isPointMarkedForDeletion(1));
		Assertions.assertTrue(marks.isPointMarkedForSegmentBreak(0));
		Assertions.assertFalse(marks.isPointMarkedForSegmentBreak(1));
	}

	@Test
	public void testTwoPointsOnlySetFalse()
	{
		Track track = new Track();
		track.appendPoint(new DataPoint(1.81, 5.44));
		track.appendPoint(new DataPoint(1.91, 5.54));
		MarkingData marks = new MarkingData(track);
		marks.markPointForDeletion(0, false);
		marks.markPointForDeletion(0, false, true);
		// Requesting a segment break is ignored if the delete flag is false
		Assertions.assertFalse(marks.isPointMarkedForDeletion(0));
		Assertions.assertFalse(marks.isPointMarkedForDeletion(1));
		Assertions.assertFalse(marks.isPointMarkedForSegmentBreak(0));
		Assertions.assertFalse(marks.isPointMarkedForSegmentBreak(1));
	}

	@Test
	public void testTwoPointsDeleteFirst()
	{
		Track track = new Track();
		track.appendPoint(new DataPoint(1.81, 5.44));
		track.appendPoint(new DataPoint(1.91, 5.54));
		MarkingData marks = new MarkingData(track);
		marks.markPointForDeletion(0, true);
		Assertions.assertTrue(marks.isPointMarkedForDeletion(0));
		Assertions.assertFalse(marks.isPointMarkedForDeletion(1));
		Assertions.assertFalse(marks.isPointMarkedForSegmentBreak(0));
		Assertions.assertFalse(marks.isPointMarkedForSegmentBreak(1));
	}
}
