package tim.prune.function.compress.methods;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import tim.prune.data.DataPoint;
import tim.prune.data.Distance;
import tim.prune.data.Field;
import tim.prune.data.MarkingData;
import tim.prune.data.Track;
import tim.prune.data.UnitSetLibrary;
import tim.prune.function.PointUtils;
import tim.prune.function.compress.TrackDetails;

public class TestMultipleMethods
{
	@Test
	public void testTwoMethods()
	{
		Track track = new Track();
		final double angleRadians = 25.0 / 180.0 * Math.PI;
		DataPoint point = new DataPoint(47.1, 8.0);
		double speed = 10.0;
		long seconds = 123456L;
		final int numPoints = 33;
		for (int i=0; i<numPoints; i++)
		{
			final double distanceRadians = Distance.convertDistanceToRadians(speed, UnitSetLibrary.UNITS_METRES);
			point = PointUtils.projectPoint(point, angleRadians, distanceRadians);
			point.setSegmentStart(i == 0);
			point.setFieldValue(Field.TIMESTAMP, "" + seconds, false);
			track.appendPoint(point);
			speed += i - i*i/21;
			seconds += 10;
		}

		TrackDetails details = new TrackDetails(track);
		MarkingData markings = new MarkingData(track);
		// Firstly, do a speed limit compression to remove 3 points going too fast in the middle
		new TooFastMethod(32.5).compress(track, details, markings);
		Assertions.assertEquals(3, markings.getNumDeleted());
		for (int i=0; i<numPoints; i++)
		{
			boolean shouldDelete = i >= 20 && i <= 22;
			Assertions.assertEquals(shouldDelete, markings.isPointMarkedForDeletion(i), "point " + i);
			Assertions.assertEquals(shouldDelete, markings.isPointMarkedForSegmentBreak(i));
		}

		// Then, without resetting the markings, do a time difference method
		// which should respect both the deletion flags _and_ the inserted segment break
		TrackDetails modifiedDetails = details.modifyUsingMarkings(markings);
		new TooSoonMethod(45).compress(track, modifiedDetails, markings);
		Assertions.assertEquals(25, markings.getNumDeleted());
		for (int i=0; i<numPoints; i++)
		{
			final List<Integer> shouldDeletes = List.of(/* Keep 0 */ 1, 2, 3, 4, /* Keep 5 */
					6, 7, 8, 9, /* Keep 10 */ 11, 12, 13, 14, /* Keep 15 */
					16, 17, 18, /* Keep 19 */ 20, 21, 22, /* Keep 23 */ 24, 25, 26, 27, /* Keep 28 */ 29, 30, 31 /* Keep last */);
			boolean shouldDelete = shouldDeletes.contains(i);
			Assertions.assertEquals(shouldDelete, markings.isPointMarkedForDeletion(i), "point " + i);
			Assertions.assertEquals(i >= 20 && i <= 22, markings.isPointMarkedForSegmentBreak(i));
		}
	}
}
