package tim.prune.cmd;

import org.junit.jupiter.api.Test;
import tim.prune.data.AudioClip;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test the insertion of an audio clip, together with its undo
 */
class InsertAudioTest
{
	@Test
	public void testAddAudio()
	{
		Track track = new Track();
		TrackInfo info = new TrackInfo(track);
		AudioClip audio = new AudioClip(new File("abc.wav"));
		assertEquals(0, info.getAudioList().getCount());
		// add
		InsertAudioCmd command = new InsertAudioCmd(audio);
		assertTrue(command.execute(info));
		assertEquals(1, info.getAudioList().getCount());
		assertEquals("abc.wav", info.getAudioList().get(0).getName());
		// undo
		assertTrue(command.getInverse().execute(info));
		assertEquals(0, info.getAudioList().getCount());
	}
}
