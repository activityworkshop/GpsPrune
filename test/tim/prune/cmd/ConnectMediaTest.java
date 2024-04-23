package tim.prune.cmd;

import org.junit.jupiter.api.Test;
import tim.prune.data.*;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the command to connect a photo and/or audio
 */
class ConnectMediaTest
{
	@Test
	public void testConnectNothing()
	{
		Track track = new Track();
		DataPoint point = new DataPoint(Latitude.make("1.23"), Longitude.make("2.34"),
			new Altitude("515", UnitSetLibrary.UNITS_METRES));
		track.appendPoint(point);

		Command command = new ConnectMediaCmd(point, null, null);
		TrackInfo info = new TrackInfo(track);
		assertTrue(command.execute(info));
		assertFalse(point.hasMedia());
		// undo
		assertTrue(command.getInverse().executeCommand(info));
	}

	@Test
	public void testConnectPhoto()
	{
		Track track = new Track();
		DataPoint point = new DataPoint(Latitude.make("1.23"), Longitude.make("2.34"),
				new Altitude("515", UnitSetLibrary.UNITS_METRES));
		track.appendPoint(point);
		assertFalse(point.hasMedia());

		// Connect a photo
		Command command = new ConnectMediaCmd(point, new Photo(new File("abc.jpg")), null);
		TrackInfo info = new TrackInfo(track);
		assertTrue(command.execute(info));
		assertTrue(point.hasMedia());
		assertEquals("abc.jpg", point.getPhoto().getName());
		// undo
		assertTrue(command.getInverse().executeCommand(info));
		assertFalse(point.hasMedia());
	}

	@Test
	public void testDisconnectPhoto()
	{
		Track track = new Track();
		DataPoint point = new DataPoint(Latitude.make("1.23"), Longitude.make("2.34"),
				new Altitude("515", UnitSetLibrary.UNITS_METRES));
		Photo photo = new Photo(new File("abc.jpg"));
		point.setPhoto(photo);
		photo.setDataPoint(point);
		track.appendPoint(point);
		assertTrue(point.hasMedia());
		assertEquals("abc.jpg", point.getPhoto().getName());
		assertNotNull(photo.getDataPoint());

		// Disconnect the photo
		Command command = new ConnectMediaCmd(point, null, null);
		TrackInfo info = new TrackInfo(track);
		assertTrue(command.execute(info));
		assertFalse(point.hasMedia());
		assertNull(point.getPhoto());
		assertNull(photo.getDataPoint());
		// undo the disconnect
		assertTrue(command.getInverse().executeCommand(info));
		assertTrue(point.hasMedia());
		assertEquals("abc.jpg", point.getPhoto().getName());
		assertNotNull(photo.getDataPoint());
	}

	@Test
	public void testConnectAudio()
	{
		Track track = new Track();
		DataPoint point = new DataPoint(Latitude.make("1.23"), Longitude.make("2.34"),
				new Altitude("2515", UnitSetLibrary.UNITS_METRES));
		track.appendPoint(point);
		assertFalse(point.hasMedia());

		// Connect an audio clip
		Command command = new ConnectMediaCmd(point, null, new AudioClip(new File("filename.mp3")));
		TrackInfo info = new TrackInfo(track);
		assertTrue(command.execute(info));
		assertTrue(point.hasMedia());
		assertNull(point.getPhoto());
		assertNotNull(point.getAudio());
		assertEquals("filename.mp3", point.getAudio().getName());
		// undo
		assertTrue(command.getInverse().executeCommand(info));
		assertFalse(point.hasMedia());
		assertNull(point.getAudio());
	}

	@Test
	public void testDisconnectAudio()
	{
		Track track = new Track();
		TrackInfo info = new TrackInfo(track);
		DataPoint point = new DataPoint(Latitude.make("1.23"), Longitude.make("2.34"),
				new Altitude("2515", UnitSetLibrary.UNITS_METRES));
		track.appendPoint(point);
		AudioClip audio = new AudioClip(new File("audio1.mp3"));
		audio.setDataPoint(point);
		point.setAudio(audio);
		info.getAudioList().add(audio);
		assertTrue(point.hasMedia());

		// Disconnect the audio from the point
		Command command = new ConnectMediaCmd(point, null, null);
		assertTrue(command.execute(info));
		assertFalse(point.hasMedia());
		assertNull(point.getAudio());
		assertNull(audio.getDataPoint());
		// undo
		assertTrue(command.getInverse().executeCommand(info));
		assertTrue(point.hasMedia());
		assertNotNull(point.getAudio());
		assertEquals("audio1.mp3", point.getAudio().getName());
		assertEquals(point, audio.getDataPoint());
	}
}
