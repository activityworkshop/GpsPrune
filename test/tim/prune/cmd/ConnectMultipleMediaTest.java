package tim.prune.cmd;

import org.junit.jupiter.api.Test;
import tim.prune.data.*;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the command to connect multiple photos and/or audios
 */
class ConnectMultipleMediaTest
{
	@Test
	public void testConnectSinglePhoto()
	{
		Track track = makeTrackWithMedia(null, null);
		DataPoint point = track.getPoint(0);
		assertFalse(point.hasMedia());

		// Connect a photo
		List<PointAndMedia> connections = ListUtils.makeList(new PointAndMedia(point, new Photo(new File("abc.jpg")), null));
		Command command = new ConnectMultipleMediaCmd(MediaLinkType.LINK_PHOTOS, connections);
		TrackInfo info = new TrackInfo(track);
		assertTrue(command.execute(info));
		assertTrue(point.hasMedia());
		assertEquals("abc.jpg", point.getPhoto().getName());
		// undo
		assertTrue(command.getInverse().executeCommand(info));
		assertFalse(point.hasMedia());
	}

	@Test
	public void testConnectPhotoKeepAudio()
	{
		AudioClip audio = new AudioClip(new File("audio1.mp3"));
		Track track = makeTrackWithMedia(null, audio);
		DataPoint point = track.getPoint(0);
		assertTrue(point.hasMedia());
		assertNull(point.getPhoto());
		assertEquals("audio1.mp3", point.getAudio().getName());

		// Connect a photo
		List<PointAndMedia> connections = ListUtils.makeList(new PointAndMedia(point, new Photo(new File("abc.jpg")), null));
		Command command = new ConnectMultipleMediaCmd(MediaLinkType.LINK_PHOTOS, connections);
		TrackInfo info = new TrackInfo(track);
		assertTrue(command.execute(info));
		assertTrue(point.hasMedia());
		assertEquals("abc.jpg", point.getPhoto().getName());
		assertEquals("audio1.mp3", point.getAudio().getName());
		// undo
		assertTrue(command.getInverse().executeCommand(info));
		assertTrue(point.hasMedia());
		assertNull(point.getPhoto());
		assertEquals("audio1.mp3", point.getAudio().getName());
	}

	@Test
	public void testDisconnectPhotoKeepAudio()
	{
		Photo photo = new Photo(new File("abc.jpg"));
		AudioClip audio = new AudioClip(new File("audio1.mp3"));
		Track track = makeTrackWithMedia(photo, audio);
		DataPoint point = track.getPoint(0);
		assertTrue(point.hasMedia());
		assertEquals("abc.jpg", point.getPhoto().getName());
		assertNotNull(photo.getDataPoint());
		assertEquals("audio1.mp3", point.getAudio().getName());
		assertNotNull(audio.getDataPoint());

		// Disconnect the photo
		List<PointAndMedia> connections = ListUtils.makeList(new PointAndMedia(point, null, null));
		Command command = new ConnectMultipleMediaCmd(MediaLinkType.LINK_PHOTOS, connections);
		TrackInfo info = new TrackInfo(track);
		assertTrue(command.execute(info));
		assertTrue(point.hasMedia());
		assertNull(point.getPhoto());
		assertNull(photo.getDataPoint());
		assertEquals("audio1.mp3", point.getAudio().getName());
		// undo the disconnect
		assertTrue(command.getInverse().executeCommand(info));
		assertTrue(point.hasMedia());
		assertEquals("abc.jpg", point.getPhoto().getName());
		assertNotNull(photo.getDataPoint());
		assertEquals("audio1.mp3", point.getAudio().getName());
	}

	@Test
	public void testDisconnectBoth()
	{
		Photo photo = new Photo(new File("abc.jpg"));
		AudioClip audio = new AudioClip(new File("audio1.mp3"));
		Track track = makeTrackWithMedia(photo, audio);
		DataPoint point = track.getPoint(0);
		assertTrue(point.hasMedia());
		assertEquals("abc.jpg", point.getPhoto().getName());
		assertNotNull(photo.getDataPoint());
		assertEquals("audio1.mp3", point.getAudio().getName());
		assertNotNull(audio.getDataPoint());

		// Disconnect both media
		List<PointAndMedia> connections = ListUtils.makeList(new PointAndMedia(point, null, null));
		Command command = new ConnectMultipleMediaCmd(MediaLinkType.LINK_BOTH, connections);
		TrackInfo info = new TrackInfo(track);
		assertTrue(command.execute(info));
		assertFalse(point.hasMedia());
		assertNull(point.getPhoto());
		assertNull(photo.getDataPoint());
		assertNull(point.getAudio());
		// undo the disconnect
		assertTrue(command.getInverse().executeCommand(info));
		assertTrue(point.hasMedia());
		assertEquals("abc.jpg", point.getPhoto().getName());
		assertNotNull(photo.getDataPoint());
		assertEquals("audio1.mp3", point.getAudio().getName());
	}

	private Track makeTrackWithMedia(Photo inPhoto, AudioClip inAudio)
	{
		Track track = new Track();
		DataPoint point = new DataPoint(Latitude.make("1.23"), Longitude.make("2.34"),
				new Altitude("515", UnitSetLibrary.UNITS_METRES));
		track.appendPoint(point);
		if (inPhoto != null) {
			point.setPhoto(inPhoto);
			inPhoto.setDataPoint(point);
		}
		if (inAudio != null) {
			inAudio.setDataPoint(point);
			point.setAudio(inAudio);
		}
		return track;
	}
}
