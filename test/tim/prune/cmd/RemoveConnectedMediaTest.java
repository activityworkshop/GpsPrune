package tim.prune.cmd;

import org.junit.jupiter.api.Test;
import tim.prune.data.*;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the command to remove the correlated media
 */
class RemoveConnectedMediaTest
{
	@Test
	public void testRemoveOnlyPhoto()
	{
		Track track = new Track();
		DataPoint point = new DataPoint(new Latitude("1.23"), new Longitude("2.34"),
				new Altitude("515", UnitSetLibrary.UNITS_METRES));
		Photo photo = new Photo(new File("abc.jpg"));
		TrackInfo info = new TrackInfo(track);
		info.getPhotoList().add(photo);
		point.setPhoto(photo);
		photo.setDataPoint(point);
		track.appendPoint(point);
		assertEquals(1, info.getPhotoList().getCount());
		assertTrue(point.hasMedia());
		assertEquals("abc.jpg", point.getPhoto().getName());
		assertNotNull(photo.getDataPoint());

		// Remove all connected media
		Command command = new RemoveCorrelatedMediaCmd();
		assertTrue(command.execute(info));
		assertEquals(0, info.getPhotoList().getCount());
		assertTrue(point.hasMedia()); // not disconnected!
		assertNotNull(point.getPhoto());
		assertNotNull(photo.getDataPoint());
		// undo the removal
		assertTrue(command.getInverse().executeCommand(info));
		assertTrue(point.hasMedia());
		assertEquals("abc.jpg", point.getPhoto().getName());
		assertNotNull(photo.getDataPoint());
		assertEquals(1, info.getPhotoList().getCount());
	}

	@Test
	public void testDontRemoveUnconnectedPhoto()
	{
		Track track = new Track();
		Photo photo = new Photo(new File("abc.jpg"));
		TrackInfo info = new TrackInfo(track);
		info.getPhotoList().add(photo);
		assertEquals(1, info.getPhotoList().getCount());
		assertNull(photo.getDataPoint());

		// Remove all connected media
		Command command = new RemoveCorrelatedMediaCmd();
		assertTrue(command.execute(info));
		assertEquals(1, info.getPhotoList().getCount());
		assertEquals("abc.jpg", info.getPhotoList().get(0).getName());
		// undo (also does nothing)
		assertTrue(command.getInverse().executeCommand(info));
		assertEquals(1, info.getPhotoList().getCount());
		assertEquals("abc.jpg", info.getPhotoList().get(0).getName());
	}

	@Test
	public void testDeleteFromMiddle()
	{
		Track track = new Track();
		TrackInfo info = new TrackInfo(track);
		for (int i=0; i<3; i++) {
			info.getPhotoList().add(new Photo(new File("photo" + i + ".jpg")));
		}
		assertEquals(3, info.getPhotoList().getCount());
		DataPoint point = new DataPoint(new Latitude("1.23"), new Longitude("2.34"),
			new Altitude("515", UnitSetLibrary.UNITS_METRES));
		point.setPhoto(info.getPhotoList().get(1));
		info.getPhotoList().get(1).setDataPoint(point);
		// delete
		Command command = new RemoveCorrelatedMediaCmd();
		assertTrue(command.execute(info));
		assertEquals(2, info.getPhotoList().getCount());
		assertEquals("photo0.jpg", info.getPhotoList().get(0).getName());
		assertEquals("photo2.jpg", info.getPhotoList().get(1).getName());
		// undo
		assertTrue(command.getInverse().execute(info));
		assertEquals(3, info.getPhotoList().getCount());
		assertEquals("photo0.jpg", info.getPhotoList().get(0).getName());
		assertEquals("photo1.jpg", info.getPhotoList().get(1).getName());
		assertEquals("photo2.jpg", info.getPhotoList().get(2).getName());
		assertEquals(point, info.getPhotoList().get(1).getDataPoint());
		assertEquals(info.getPhotoList().get(1), point.getPhoto());
	}
}
