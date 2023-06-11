package tim.prune.cmd;

import org.junit.jupiter.api.Test;
import tim.prune.data.AudioClip;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test the removal of an audio clip, together with its undo
 */
class RemoveAudioTest
{
	@Test
	public void testRemoveOnlyAudio()
	{
		Track track = new Track();
		TrackInfo info = new TrackInfo(track);
		AudioClip audio = new AudioClip(new File("abc.mp3"));
		info.getAudioList().add(audio);
		assertEquals(1, info.getAudioList().getCount());
		// delete
		RemoveAudioCmd command = new RemoveAudioCmd(0);
		assertTrue(command.execute(info));
		assertEquals(0, info.getPhotoList().getCount());
		assertEquals(0, info.getAudioList().getCount());
		// undo
		assertTrue(command.getInverse().execute(info));
		assertEquals(1, info.getAudioList().getCount());
		assertEquals(0, info.getPhotoList().getCount());
	}

	@Test
	public void testRemoveFromMiddle()
	{
		Track track = new Track();
		TrackInfo info = new TrackInfo(track);
		for (int i=0; i<4; i++) {
			info.getAudioList().add(new AudioClip(new File("audio" + i + ".mp3")));
		}
		assertEquals(4, info.getAudioList().getCount());
		// delete
		RemoveAudioCmd command = new RemoveAudioCmd(2);
		assertTrue(command.execute(info));
		assertEquals(3, info.getAudioList().getCount());
		assertEquals("audio0.mp3", info.getAudioList().get(0).getName());
		assertEquals("audio1.mp3", info.getAudioList().get(1).getName());
		assertEquals("audio3.mp3", info.getAudioList().get(2).getName());
		// undo
		assertTrue(command.getInverse().execute(info));
		assertEquals(4, info.getAudioList().getCount());
		assertEquals("audio0.mp3", info.getAudioList().get(0).getName());
		assertEquals("audio1.mp3", info.getAudioList().get(1).getName());
		assertEquals("audio2.mp3", info.getAudioList().get(2).getName());
		assertEquals("audio3.mp3", info.getAudioList().get(3).getName());
	}
}
