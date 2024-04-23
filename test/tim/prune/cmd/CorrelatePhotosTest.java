package tim.prune.cmd;

import org.junit.jupiter.api.Test;
import tim.prune.data.*;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the command to correlate photos
 */
class CorrelatePhotosTest
{
	@Test
	public void testOnePhotoNewPoint()
	{
		Track track = TrackHelper.makeTwelvePointTrack();
		assertEquals(12, track.getNumPoints());
		// Correlate by adding a new point and its photo
		DataPoint extraPoint = new DataPoint(Latitude.make("8.01"), Longitude.make("0.5"));
		Photo photo = new Photo(new File("p12345.jpg"));
		Command command = new CorrelateMediaCmd(MediaLinkType.LINK_PHOTOS, List.of(extraPoint),
			List.of(new PointAndMedia(extraPoint, photo, null)));
		TrackInfo info = new TrackInfo(track);
		assertTrue(command.execute(info));
		// check
		assertEquals(13, track.getNumPoints());
		assertTrue(track.getPoint(12).hasMedia());
		assertEquals("p12345.jpg", track.getPoint(12).getPhoto().getName());
		assertEquals(extraPoint, photo.getDataPoint());
		// undo and check again
		assertTrue(command.getInverse().execute(info));
		assertEquals(12, track.getNumPoints());
		assertNull(track.getPoint(12));
		assertNull(photo.getDataPoint());
	}

	@Test
	public void testOnePhotoExistingPoint()
	{
		Track track = TrackHelper.makeTwelvePointTrack();
		assertEquals(12, track.getNumPoints());
		// Correlate by attaching a photo to an existing point
		Photo photo = new Photo(new File("p12345.jpg"));
		DataPoint pointToLink = track.getPoint(3);
		Command command = new CorrelateMediaCmd(MediaLinkType.LINK_PHOTOS, List.of(),
			List.of(new PointAndMedia(pointToLink, photo, null)));
		TrackInfo info = new TrackInfo(track);
		assertTrue(command.execute(info));
		// check
		assertEquals(12, track.getNumPoints());
		assertTrue(pointToLink.hasMedia());
		assertEquals("p12345.jpg", pointToLink.getPhoto().getName());
		assertEquals(pointToLink, photo.getDataPoint());
		// undo and check again
		assertTrue(command.getInverse().execute(info));
		assertEquals(12, track.getNumPoints());
		assertFalse(pointToLink.hasMedia());
		assertNull(photo.getDataPoint());
	}

	@Test
	public void testOneAudioWithPhotoAlready()
	{
		Track track = TrackHelper.makeTwelvePointTrack();
		assertEquals(12, track.getNumPoints());
		DataPoint pointToLink = track.getPoint(1);
		Photo photo = new Photo(new File("p12345.jpg"));
		pointToLink.setPhoto(photo);
		photo.setDataPoint(pointToLink);
		AudioClip audio = new AudioClip(new File("audio1.mp3"));
		Command command = new CorrelateMediaCmd(MediaLinkType.LINK_AUDIOS, List.of(),
			List.of(new PointAndMedia(pointToLink, null, audio)));
		TrackInfo info = new TrackInfo(track);
		assertTrue(command.execute(info));
		// check
		assertEquals(12, track.getNumPoints());
		assertTrue(pointToLink.hasMedia());
		assertEquals("audio1.mp3", pointToLink.getAudio().getName());
		assertEquals(pointToLink, audio.getDataPoint());
		assertEquals("p12345.jpg", pointToLink.getPhoto().getName());
		assertEquals(pointToLink, photo.getDataPoint());
		// undo and check again
		assertTrue(command.getInverse().execute(info));
		assertEquals(12, track.getNumPoints());
		assertTrue(pointToLink.hasMedia());
		assertNull(pointToLink.getAudio());
		assertNull(audio.getDataPoint());
		// photo should be unaffected
		assertEquals("p12345.jpg", pointToLink.getPhoto().getName());
		assertEquals(pointToLink, photo.getDataPoint());
	}
}
