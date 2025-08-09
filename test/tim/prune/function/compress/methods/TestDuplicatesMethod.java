package tim.prune.function.compress.methods;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import tim.prune.data.DataPoint;
import tim.prune.data.MarkingData;
import tim.prune.data.Track;
import tim.prune.function.compress.CompressionMethodType;
import tim.prune.function.compress.TrackDetails;

public class TestDuplicatesMethod
{
	@Test
	public void testSerialize_inactive()
	{
		DuplicatesMethod method = new DuplicatesMethod();
		Assertions.assertFalse(method.isActive());
		Assertions.assertEquals(CompressionMethodType.DUPLICATES, method.getType());
		Assertions.assertEquals("oDUP:", method.getTotalSettingsString());
		Assertions.assertEquals("", method.getParam());
	}

	@Test
	public void testDeserialize_inactive()
	{
		String fromConfig = "oDUP:";
		CompressionMethod method = CompressionMethod.fromSettingsString(fromConfig);
		Assertions.assertNotNull(method);
		Assertions.assertTrue(method instanceof DuplicatesMethod);
		Assertions.assertFalse(method.isActive());
	}

	@Test
	public void testSerialize_active()
	{
		DuplicatesMethod method = new DuplicatesMethod();
		method.setActive(true);
		Assertions.assertEquals("xDUP:", method.getTotalSettingsString());
	}

	@Test
	public void testDeserialize_active()
	{
		String fromConfig = "xDUP:";
		CompressionMethod method = CompressionMethod.fromSettingsString(fromConfig);
		Assertions.assertNotNull(method);
		Assertions.assertTrue(method instanceof DuplicatesMethod);
		Assertions.assertTrue(method.isActive());
	}

	@Test
	public void testDeserialize_tooLong()
	{
		CompressionMethod method = CompressionMethod.fromSettingsString("DDUP:PDUPDUP");
		Assertions.assertTrue(method instanceof DuplicatesMethod);
		Assertions.assertFalse(method.isActive());
	}

	@Test
	public void testDeserialize_fail()
	{
		Assertions.assertNull(CompressionMethod.fromSettingsString(null));
		Assertions.assertNull(CompressionMethod.fromSettingsString(""));
		Assertions.assertNull(CompressionMethod.fromSettingsString("DUP"));
	}

	@Test
	public void testDeleteOneOfTwoDuplicates()
	{
		Track track = new Track();
		for (int i=0; i<14; i++)
		{
			final double longitude = 1.0 + 0.01 * i;
			DataPoint point = new DataPoint(1.0, longitude);
			point.setSegmentStart(i == 0);
			track.appendPoint(point);
			if (i == 3) {
				track.appendPoint(new DataPoint(1.0, longitude));
			}
			else if (i == 6)
			{
				DataPoint extra = new DataPoint(1.0, longitude);
				extra.setSegmentStart(true);
				track.appendPoint(extra);
			}
		}
		// First duplicate should be deleted, second one shouldn't (because it's a segment start)
		TrackDetails details = new TrackDetails(track);
		MarkingData markings = new MarkingData(track);
		new DuplicatesMethod().compress(track, details, markings);
		Assertions.assertEquals(1, markings.getNumDeleted());
		for (int i=0; i<16; i++)
		{
			// Duplicate at index 4 should be deleted, but the duplicate at 8 not
			Assertions.assertEquals(i == 4, markings.isPointMarkedForDeletion(i));
			Assertions.assertFalse(markings.isPointMarkedForSegmentBreak(i));
		}
	}

	@Test
	public void testDeleteLastDuplicates()
	{
		Track track = new Track();
		double longitude = 0.0;
		for (int i=0; i<14; i++)
		{
			final int numToAdd = (i < 13 ? 1 : 4);
			longitude = 1.0 + 0.01 * i;
			for (int j=0; j<numToAdd; j++)
			{
				DataPoint point = new DataPoint(1.0, longitude);
				point.setSegmentStart(i == 0);
				track.appendPoint(point);
			}
		}
		// All the duplicates at the end should be deleted, even if they're segment end
		TrackDetails details = new TrackDetails(track);
		MarkingData markings = new MarkingData(track);
		new DuplicatesMethod().compress(track, details, markings);
		Assertions.assertEquals(3, markings.getNumDeleted());
		for (int i=0; i<17; i++)
		{
			Assertions.assertEquals(i > 13, markings.isPointMarkedForDeletion(i));
			Assertions.assertFalse(markings.isPointMarkedForSegmentBreak(i));
		}
	}
}
