package tim.prune.function.media;

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import tim.prune.App;
import tim.prune.cmd.Command;
import tim.prune.data.AudioClip;
import tim.prune.data.DataPoint;
import tim.prune.data.MediaList;
import tim.prune.data.Photo;
import tim.prune.data.TrackInfo;

public class RemoveAllPhotosFunctionTest
{
	@Test
	public void testNoPhotos()
	{
		App app = new App(null, null);
		Assertions.assertEquals(0, app.getTrackInfo().getPhotoList().getCount());
		Assertions.assertEquals(0, app.getTrackInfo().getTrack().getNumPoints());

		// Execute function to remove all photos
		RemoveAllPhotosFunction removeFunc = new RemoveAllPhotosFunction(app);
		removeFunc.begin();
		Assertions.assertEquals(0, app.getTrackInfo().getPhotoList().getCount());
	}

	@Test
	public void testTwoPhotos()
	{
		// Add two photos
		App app = new App(null, null);
		Photo photo1 = new Photo(new File("photo1.jpg"));
		final MediaList<Photo> photoList = app.getTrackInfo().getPhotoList();
		photoList.add(photo1);
		Photo photo2 = new Photo(new File("photo2.jpg"));
		photoList.add(photo2);
		Assertions.assertEquals(2, photoList.getCount());
		Assertions.assertEquals(0, app.getTrackInfo().getTrack().getNumPoints());

		// Execute function to remove all photos
		RemoveAllPhotosFunction removeFunc = new RemoveAllPhotosFunction(app);
		removeFunc.begin();
		Assertions.assertEquals(0, photoList.getCount());

		// Undo
		app.undoActions(1);
		Assertions.assertEquals(2, photoList.getCount());
		Assertions.assertEquals("photo1.jpg", photoList.get(0).getName());
		Assertions.assertEquals("photo2.jpg", photoList.get(1).getName());
		Assertions.assertEquals(0, app.getTrackInfo().getTrack().getNumPoints());
	}

	@Test
	public void testTwoPointsOnePhoto_unlink()
	{
		// Add a photo and two points
		App app = new App(null, null);
		Photo photo1 = new Photo(new File("photo1.jpg"));
		app.getTrackInfo().getPhotoList().add(photo1);
		app.getTrackInfo().getTrack().appendPoint(new DataPoint(1.25, 2.5));
		app.getTrackInfo().getTrack().appendPoint(new DataPoint(2.25, -2.0));
		// Connect the photo to the first point
		photo1.setDataPoint(app.getTrackInfo().getTrack().getPoint(0));
		photo1.getDataPoint().setPhoto(photo1);
		Assertions.assertEquals(1, app.getTrackInfo().getPhotoList().getCount());
		Assertions.assertEquals(2, app.getTrackInfo().getTrack().getNumPoints());
		Assertions.assertTrue(app.getTrackInfo().getTrack().getPoint(0).hasMedia());

		// Execute function to remove all photos and unlink the connected one
		Command removeUnlinkCmd = RemoveAllPhotosFunction.makeCommand(app.getTrackInfo(), PopupResponse.UNLINK);
		app.execute(removeUnlinkCmd);
		Assertions.assertEquals(0, app.getTrackInfo().getPhotoList().getCount());
		Assertions.assertEquals(2, app.getTrackInfo().getTrack().getNumPoints());
		Assertions.assertFalse(app.getTrackInfo().getTrack().getPoint(0).hasMedia());

		// Undo
		app.undoActions(1);
		Assertions.assertEquals(1, app.getTrackInfo().getPhotoList().getCount());
		Assertions.assertEquals(2, app.getTrackInfo().getTrack().getNumPoints());
		Assertions.assertTrue(app.getTrackInfo().getTrack().getPoint(0).hasMedia());
		Assertions.assertEquals("photo1.jpg", app.getTrackInfo().getTrack().getPoint(0).getPhoto().getName());
	}

	@Test
	public void testTwoPointsOnePhoto_delete()
	{
		// Add a photo and two points
		App app = new App(null, null);
		Photo photo1 = new Photo(new File("photo1.jpg"));
		app.getTrackInfo().getPhotoList().add(photo1);
		app.getTrackInfo().getTrack().appendPoint(new DataPoint(1.25, 2.5));
		app.getTrackInfo().getTrack().appendPoint(new DataPoint(2.25, -2.0));
		// Connect the photo to the first point
		photo1.setDataPoint(app.getTrackInfo().getTrack().getPoint(0));
		photo1.getDataPoint().setPhoto(photo1);
		Assertions.assertEquals(1, app.getTrackInfo().getPhotoList().getCount());
		Assertions.assertEquals(2, app.getTrackInfo().getTrack().getNumPoints());
		Assertions.assertTrue(app.getTrackInfo().getTrack().getPoint(0).hasMedia());

		// Execute function to remove all photos and delete the connected point
		Command removeDeleteCmd = RemoveAllPhotosFunction.makeCommand(app.getTrackInfo(), PopupResponse.DELETE);
		app.execute(removeDeleteCmd);
		Assertions.assertEquals(0, app.getTrackInfo().getPhotoList().getCount());
		Assertions.assertEquals(1, app.getTrackInfo().getTrack().getNumPoints());
		Assertions.assertFalse(app.getTrackInfo().getTrack().getPoint(0).hasMedia());

		// Undo
		app.undoActions(1);
		Assertions.assertEquals(1, app.getTrackInfo().getPhotoList().getCount());
		Assertions.assertEquals(2, app.getTrackInfo().getTrack().getNumPoints());
		Assertions.assertTrue(app.getTrackInfo().getTrack().getPoint(0).hasMedia());
		Assertions.assertEquals("photo1.jpg", app.getTrackInfo().getTrack().getPoint(0).getPhoto().getName());
		Assertions.assertFalse(app.getTrackInfo().getTrack().getPoint(1).hasMedia());
	}

	@Test
	public void testTwoPointsOnePhotoOneAudio_unlink()
	{
		// Add a photo and two points
		App app = new App(null, null);
		Photo photo1 = new Photo(new File("photo1.jpg"));
		TrackInfo trackInfo = app.getTrackInfo();
		trackInfo.getPhotoList().add(photo1);
		trackInfo.getTrack().appendPoint(new DataPoint(1.25, 2.5));
		trackInfo.getTrack().appendPoint(new DataPoint(2.25, -2.0));
		AudioClip audio = new AudioClip(new File("audio.mp3"));
		trackInfo.getAudioList().add(audio);
		// Connect the photo to the first point, and an audio as well
		DataPoint firstPoint = trackInfo.getTrack().getPoint(0);
		photo1.setDataPoint(firstPoint);
		photo1.getDataPoint().setPhoto(photo1);
		audio.setDataPoint(firstPoint);
		audio.getDataPoint().setAudio(audio);
		Assertions.assertEquals(1, trackInfo.getPhotoList().getCount());
		Assertions.assertEquals(1, trackInfo.getAudioList().getCount());
		Assertions.assertEquals(2, trackInfo.getTrack().getNumPoints());
		Assertions.assertTrue(trackInfo.getTrack().getPoint(0).hasMedia());

		// Execute function to remove all photos and unlink the connected one
		Command removeUnlinkCmd = RemoveAllPhotosFunction.makeCommand(trackInfo, PopupResponse.UNLINK);
		app.execute(removeUnlinkCmd);
		Assertions.assertEquals(0, trackInfo.getPhotoList().getCount());
		Assertions.assertEquals(2, trackInfo.getTrack().getNumPoints());
		// First point still has its audio though
		Assertions.assertTrue(trackInfo.getTrack().getPoint(0).hasMedia());
		Assertions.assertEquals(1, trackInfo.getAudioList().getCount());
		Assertions.assertTrue(trackInfo.getAudioList().get(0).isConnected());
		Assertions.assertEquals("audio.mp3", firstPoint.getAudio().getName());

		// Undo
		app.undoActions(1);
		Assertions.assertEquals(1, trackInfo.getPhotoList().getCount());
		Assertions.assertEquals(2, trackInfo.getTrack().getNumPoints());
		Assertions.assertTrue(trackInfo.getTrack().getPoint(0).hasMedia());
		Assertions.assertEquals("photo1.jpg", trackInfo.getTrack().getPoint(0).getPhoto().getName());
		Assertions.assertEquals("audio.mp3", firstPoint.getAudio().getName());
	}

	@Test
	public void testTwoPointsOnePhotoOneAudio_delete()
	{
		// Add a photo and two points
		App app = new App(null, null);
		Photo photo1 = new Photo(new File("photo1.jpg"));
		TrackInfo trackInfo = app.getTrackInfo();
		trackInfo.getPhotoList().add(photo1);
		trackInfo.getTrack().appendPoint(new DataPoint(1.25, 2.5));
		trackInfo.getTrack().appendPoint(new DataPoint(2.25, -2.0));
		AudioClip audio = new AudioClip(new File("audio.mp3"));
		trackInfo.getAudioList().add(audio);
		// Connect the photo to the first point, and an audio as well
		DataPoint firstPoint = trackInfo.getTrack().getPoint(0);
		photo1.setDataPoint(firstPoint);
		photo1.getDataPoint().setPhoto(photo1);
		audio.setDataPoint(firstPoint);
		audio.getDataPoint().setAudio(audio);
		Assertions.assertEquals(1, trackInfo.getPhotoList().getCount());
		Assertions.assertEquals(1, trackInfo.getAudioList().getCount());
		Assertions.assertEquals(2, trackInfo.getTrack().getNumPoints());
		Assertions.assertTrue(trackInfo.getTrack().getPoint(0).hasMedia());

		// Execute function to remove all photos and delete the connected one, also the audio
		Command removeUnlinkCmd = RemoveAllPhotosFunction.makeCommand(trackInfo, PopupResponse.DELETE);
		app.execute(removeUnlinkCmd);
		Assertions.assertEquals(0, trackInfo.getPhotoList().getCount());
		Assertions.assertEquals(1, trackInfo.getTrack().getNumPoints());
		Assertions.assertEquals(0, trackInfo.getAudioList().getCount());
		Assertions.assertFalse(trackInfo.getTrack().getPoint(0).hasMedia());

		// Undo
		app.undoActions(1);
		Assertions.assertEquals(1, trackInfo.getPhotoList().getCount());
		Assertions.assertEquals(2, trackInfo.getTrack().getNumPoints());
		Assertions.assertTrue(trackInfo.getTrack().getPoint(0).hasMedia());
		Assertions.assertEquals("photo1.jpg", trackInfo.getTrack().getPoint(0).getPhoto().getName());
		Assertions.assertEquals("audio.mp3", firstPoint.getAudio().getName());
	}
}
