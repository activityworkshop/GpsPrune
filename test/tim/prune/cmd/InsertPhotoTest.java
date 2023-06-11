package tim.prune.cmd;

import org.junit.jupiter.api.Test;
import tim.prune.data.Photo;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test the insertion of a photo, together with its undo
 */
class InsertPhotoTest
{
	@Test
	public void testAddPhoto()
	{
		Track track = new Track();
		TrackInfo info = new TrackInfo(track);
		Photo photo = new Photo(new File("abc.jpg"));
		assertEquals(0, info.getPhotoList().getCount());
		// add
		InsertPhotoCmd command = new InsertPhotoCmd(photo);
		assertTrue(command.execute(info));
		assertEquals(1, info.getPhotoList().getCount());
		assertEquals("abc.jpg", info.getPhotoList().get(0).getName());
		// undo
		assertTrue(command.getInverse().execute(info));
		assertEquals(0, info.getPhotoList().getCount());
	}
}
