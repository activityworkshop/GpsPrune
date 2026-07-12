package tim.prune.function.media;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import tim.prune.App;
import tim.prune.cmd.AppendRangeCmd;
import tim.prune.cmd.Command;
import tim.prune.cmd.ConnectMediaCmd;
import tim.prune.data.DataPoint;
import tim.prune.data.Latitude;
import tim.prune.data.Longitude;
import tim.prune.data.Photo;
import tim.prune.data.TrackInfo;

public class PhotoSequenceTest
{
	@Test
	public void testNoPhotos()
	{
		App app = new App(null, null);
		PhotoSequence sequence = new PhotoSequence(app);
		sequence.recalculate();
		Assertions.assertFalse(sequence.hasPrevious());
		Assertions.assertFalse(sequence.hasNext());
	}

	@Test
	public void testRecalculations()
	{
		App app = new App(null, null);
		PhotoSequence sequence = new PhotoSequence(app);
		Assertions.assertEquals(0, sequence._numRecalculations);
		sequence.recalculate();
		Assertions.assertEquals(1, sequence._numRecalculations);

		// Track is unaltered so no recalculation is done, count stays 1
		sequence.recalculate();
		Assertions.assertEquals(1, sequence._numRecalculations);

		// Track is altered by appending a point, so recalculation is necessary, count incremented to 2
		DataPoint point = new DataPoint(Latitude.make("1.23"), Longitude.make("2.34"));
		Command command = new AppendRangeCmd(List.of(point));
		app.execute(command);
		sequence.recalculate();
		Assertions.assertEquals(2, sequence._numRecalculations);
	}

	@Test
	public void testOnePhotoNoneSelected()
	{
		App app = new App(null, null);
		PhotoSequence sequence = new PhotoSequence(app);
		app.getTrackInfo().getPhotoList().add(new Photo(new File("example.jpg")));
		sequence.recalculate();
		Assertions.assertFalse(sequence.hasPrevious());
		Assertions.assertFalse(sequence.hasNext());
	}

	@Test
	public void testTwoUnlinkedPhotos()
	{
		App app = new App(null, null);
		PhotoSequence sequence = new PhotoSequence(app);
		app.getTrackInfo().getPhotoList().add(new Photo(new File("example1.jpg")));
		app.getTrackInfo().getPhotoList().add(new Photo(new File("example2.jpg")));

		// If first photo is selected, then there's a next but no previous
		app.getTrackInfo().selectPhoto(0);
		sequence.recalculate();
		Assertions.assertFalse(sequence.hasPrevious());
		Assertions.assertNull(sequence.getPreviousName());
		Assertions.assertTrue(sequence.hasNext());
		Assertions.assertEquals("example2.jpg", sequence.getNextName());

		// If second photo is selected, then there's a previous but no next
		app.getTrackInfo().selectPhoto(1);
		sequence.recalculate();
		Assertions.assertTrue(sequence.hasPrevious());
		Assertions.assertEquals("example1.jpg", sequence.getPreviousName());
		Assertions.assertFalse(sequence.hasNext());
		Assertions.assertNull(sequence.getNextName());
	}

	@Test
	public void testLinkedAndUnlinkedPhotos()
	{
		App app = new App(null, null);
		PhotoSequence sequence = new PhotoSequence(app);
		final Photo photo1 = new Photo(new File("example1.jpg"));
		final TrackInfo trackInfo = app.getTrackInfo();
		trackInfo.getPhotoList().add(photo1);
		final Photo photo2 = new Photo(new File("example2.jpg"));
		trackInfo.getPhotoList().add(photo2);
		final DataPoint point = new DataPoint(1.25, 2.75);
		trackInfo.getTrack().appendPoint(point);
		// Connect single point with photo2, so the second photo will be listed first
		Command command = new ConnectMediaCmd(point, photo2, null);
		assertTrue(command.execute(trackInfo));

		// If first photo is selected, then there's a previous but no next
		trackInfo.selectPhoto(0);
		sequence.recalculate();
		Assertions.assertTrue(sequence.hasPrevious());
		Assertions.assertEquals("example2.jpg", sequence.getPreviousName());
		Assertions.assertFalse(sequence.hasNext());
		Assertions.assertNull(sequence.getNextName());

		// If second photo is selected, then there's a previous but no next
		trackInfo.selectPhoto(1);
		sequence.recalculate();
		Assertions.assertFalse(sequence.hasPrevious());
		Assertions.assertNull(sequence.getPreviousName());
		Assertions.assertTrue(sequence.hasNext());
		Assertions.assertEquals("example1.jpg", sequence.getNextName());
	}
}
