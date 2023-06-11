package tim.prune.cmd;

import org.junit.jupiter.api.Test;
import tim.prune.data.*;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the removal of a photo, together with its undo
 */
class RemovePhotoTest
{
	@Test
	public void testRemoveOnlyPhoto()
	{
		Track track = new Track();
		TrackInfo info = new TrackInfo(track);
		Photo photo = new Photo(new File("abc.jpg"));
		info.getPhotoList().add(photo);
		assertEquals(1, info.getPhotoList().getCount());
		// delete
		RemovePhotoCmd command = new RemovePhotoCmd(0);
		assertTrue(command.execute(info));
		assertEquals(0, info.getPhotoList().getCount());
		// undo
		assertTrue(command.getInverse().execute(info));
		assertEquals(1, info.getPhotoList().getCount());
	}

	@Test
	public void testRemoveFromMiddle()
	{
		Track track = new Track();
		TrackInfo info = new TrackInfo(track);
		for (int i=0; i<4; i++) {
			info.getPhotoList().add(new Photo(new File("photo" + i + ".jpg")));
		}
		assertEquals(4, info.getPhotoList().getCount());
		// delete
		RemovePhotoCmd command = new RemovePhotoCmd(1);
		assertTrue(command.execute(info));
		assertEquals(3, info.getPhotoList().getCount());
		assertEquals("photo0.jpg", info.getPhotoList().get(0).getName());
		assertEquals("photo2.jpg", info.getPhotoList().get(1).getName());
		assertEquals("photo3.jpg", info.getPhotoList().get(2).getName());
		// undo
		assertTrue(command.getInverse().execute(info));
		assertEquals(4, info.getPhotoList().getCount());
		assertEquals("photo0.jpg", info.getPhotoList().get(0).getName());
		assertEquals("photo1.jpg", info.getPhotoList().get(1).getName());
		assertEquals("photo2.jpg", info.getPhotoList().get(2).getName());
		assertEquals("photo3.jpg", info.getPhotoList().get(3).getName());
	}
}
