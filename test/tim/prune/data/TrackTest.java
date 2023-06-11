package tim.prune.data;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.Test;

import tim.prune.data.SourceInfo.FILE_TYPE;

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
		track.appendPoint(new DataPoint(new Latitude("1.81"), new Longitude("5.44"), null));
		assertEquals(1, track.getNumPoints());
		assertFalse(track.hasSingleSourceFile());
	}

	@Test
	void testSingleSourceWithFile()
	{
		Track track = new Track();
		DataPoint point = new DataPoint(new Latitude("1.81"), new Longitude("5.44"), null);
		SourceInfo source = new SourceInfo(new File("abc.gpx"), FILE_TYPE.GPX);
		point.setSourceInfo(source);
		track.appendPoint(point);
		assertEquals(1, track.getNumPoints());
		assertTrue(track.hasSingleSourceFile());
		// another point from same file
		DataPoint point2 = new DataPoint(new Latitude("1.82"), new Longitude("5.43"), null);
		point2.setSourceInfo(source);
		track.appendPoint(point2);
		assertEquals(2, track.getNumPoints());
		assertTrue(track.hasSingleSourceFile());

		// now add a sourceless point
		DataPoint point3 = new DataPoint(new Latitude("1.82"), new Longitude("5.43"), null);
		track.appendPoint(point3);
		assertEquals(3, track.getNumPoints());
		assertFalse(track.hasSingleSourceFile());
	}

	@Test
	void testSingleSourceWithMultipleFiles()
	{
		Track track = new Track();
		DataPoint point = new DataPoint(new Latitude("1.81"), new Longitude("5.44"), null);
		SourceInfo source = new SourceInfo(new File("abc.gpx"), FILE_TYPE.GPX);
		point.setSourceInfo(source);
		track.appendPoint(point);
		DataPoint point2 = new DataPoint(new Latitude("1.82"), new Longitude("5.43"), null);
		SourceInfo source2 = new SourceInfo(new File("abc2.gpx"), FILE_TYPE.GPX);
		point2.setSourceInfo(source2);
		track.appendPoint(point2);
		assertEquals(2, track.getNumPoints());
		assertFalse(track.hasSingleSourceFile());
	}
}
