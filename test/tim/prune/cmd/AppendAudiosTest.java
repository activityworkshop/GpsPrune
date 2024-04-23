package tim.prune.cmd;

import org.junit.jupiter.api.Test;

import tim.prune.data.AudioClip;
import tim.prune.data.ListUtils;
import tim.prune.data.MediaObject;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test the append of one or more audio clips
 */
class AppendAudiosTest
{
	@Test
	public void testAddAudio()
	{
		Track track = new Track();
		TrackInfo info = new TrackInfo(track);
		AudioClip audio = new AudioClip(new File("abc.wav"));
		assertEquals(0, info.getAudioList().getCount());
		// add
		AppendMediaCmd command = new AppendMediaCmd(ListUtils.makeList(audio));
		assertTrue(command.execute(info));
		assertEquals(1, info.getAudioList().getCount());
		assertEquals("abc.wav", info.getAudioList().get(0).getName());
		// undo
		assertTrue(command.getInverse().execute(info));
		assertEquals(0, info.getAudioList().getCount());
	}

	@Test
	public void testAddTwoAudios()
	{
		Track track = new Track();
		TrackInfo info = new TrackInfo(track);
		AudioClip audio = new AudioClip(new File("abc.wav"));
		assertTrue(new AppendMediaCmd(ListUtils.makeList(audio)).execute(info));
		assertEquals(1, info.getAudioList().getCount());
		// add two more
		List<MediaObject> audioPair = ListUtils.makeList(
			new AudioClip(new File("def.mp3")), new AudioClip(new File("omg.wav")));
		AppendMediaCmd command = new AppendMediaCmd(audioPair);
		assertTrue(command.execute(info));
		assertEquals(3, info.getAudioList().getCount());
		assertEquals("abc.wav", info.getAudioList().get(0).getName());
		assertEquals("def.mp3", info.getAudioList().get(1).getName());
		assertEquals("omg.wav", info.getAudioList().get(2).getName());
		// undo
		assertTrue(command.getInverse().execute(info));
		assertEquals(1, info.getAudioList().getCount());
	}
}
