package tim.prune.data;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for some functions of the Track class
 */
class TrackTest
{
	@Test
	void testSourcelessWithSingleSource()
	{
		Track track = new Track();
		assertEquals(0, track.getNumPoints());
		assertTrue(track.hasSingleSourceFile());

		// add point without source, now no longer single source
		track.appendPoint(new DataPoint(Latitude.make("1.81"), Longitude.make("5.44")));
		assertEquals(1, track.getNumPoints());
		assertFalse(track.hasSingleSourceFile());
	}

	@Test
	void testSingleSourceWithFile()
	{
		Track track = new Track();
		DataPoint point = new DataPoint(Latitude.make("1.81"), Longitude.make("5.44"));
		SourceInfo source = new SourceInfo(new File("abc.gpx"), FileType.GPX);
		point.setSourceInfo(source);
		track.appendPoint(point);
		assertEquals(1, track.getNumPoints());
		assertTrue(track.hasSingleSourceFile());
		// another point from same file
		DataPoint point2 = new DataPoint(Latitude.make("1.82"), Longitude.make("5.43"));
		point2.setSourceInfo(source);
		track.appendPoint(point2);
		assertEquals(2, track.getNumPoints());
		assertTrue(track.hasSingleSourceFile());

		// now add a sourceless point
		DataPoint point3 = new DataPoint(Latitude.make("1.82"), Longitude.make("5.43"));
		track.appendPoint(point3);
		assertEquals(3, track.getNumPoints());
		assertFalse(track.hasSingleSourceFile());
	}

	@Test
	void testSingleSourceWithMultipleFiles()
	{
		Track track = new Track();
		DataPoint point = new DataPoint(Latitude.make("1.81"), Longitude.make("5.44"));
		SourceInfo source = new SourceInfo(new File("abc.gpx"), FileType.GPX);
		point.setSourceInfo(source);
		track.appendPoint(point);
		DataPoint point2 = new DataPoint(Latitude.make("1.82"), Longitude.make("5.43"));
		SourceInfo source2 = new SourceInfo(new File("abc2.gpx"), FileType.GPX);
		point2.setSourceInfo(source2);
		track.appendPoint(point2);
		assertEquals(2, track.getNumPoints());
		assertFalse(track.hasSingleSourceFile());
	}
}
