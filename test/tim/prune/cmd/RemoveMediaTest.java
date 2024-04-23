package tim.prune.cmd;

import org.junit.jupiter.api.Test;
import tim.prune.data.*;

import java.io.File;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the command to remove one or more media
 */
class RemoveMediaTest
{
	@Test
	public void testRemoveOnlyPhoto()
	{
		Track track = new Track();
		Photo photo = new Photo(new File("abc.jpg"));
		TrackInfo info = new TrackInfo(track);
		info.getPhotoList().add(photo);
		assertEquals(1, info.getPhotoList().getCount());
		assertNull(photo.getDataPoint());

		// Remove this photo
		Command command = new RemoveMediaCmd(ListUtils.makeList(photo));
		assertTrue(command.execute(info));
		assertEquals(0, info.getPhotoList().getCount());
		assertNull(photo.getDataPoint());
		// undo the remove
		assertTrue(command.getInverse().executeCommand(info));
		assertNull(photo.getDataPoint());
		assertEquals(1, info.getPhotoList().getCount());
	}

	@Test
	public void testRemoveTwoAudios()
	{
		ArrayList<AudioClip> audios = new ArrayList<>();
		Track track = new Track();
		TrackInfo info = new TrackInfo(track);
		for (int i=0; i<4; i++)
		{
			AudioClip audio = new AudioClip(new File("audio" + i + ".mp3"));
			audios.add(audio);
			info.getAudioList().add(audio);
		}
		assertEquals(4, info.getAudioList().getCount());
		// remove 0 and 2
		RemoveMediaCmd command = new RemoveMediaCmd(ListUtils.makeList(audios.get(0), audios.get(2)));
		assertTrue(command.execute(info));
		assertEquals(2, info.getAudioList().getCount());
		assertEquals("audio1.mp3", info.getAudioList().get(0).getName());
		assertEquals("audio3.mp3", info.getAudioList().get(1).getName());
		// undo
		assertTrue(command.getInverse().execute(info));
		assertEquals(4, info.getAudioList().getCount());
		assertEquals("audio0.mp3", info.getAudioList().get(0).getName());
		assertEquals("audio1.mp3", info.getAudioList().get(1).getName());
		assertEquals("audio2.mp3", info.getAudioList().get(2).getName());
		assertEquals("audio3.mp3", info.getAudioList().get(3).getName());
	}
}
