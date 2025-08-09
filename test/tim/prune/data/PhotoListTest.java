package tim.prune.data;

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PhotoListTest
{
	/** An empty list doesn't already contain a photo **/
	@Test
	public void testPhotoDuplicates_empty()
	{
		MediaList<Photo> photoList = new MediaList<>();
		Photo photo1 = new Photo(new File("doesnotexist.jpg"));
		// An empty list doesn't already contain a photo
		Assertions.assertFalse(photoList.hasAny());
		Assertions.assertFalse(photoList.contains(photo1));
		Assertions.assertFalse(photoList.hasDuplicate(photo1));
	}

	/** A list with one photo in it **/
	@Test
	public void testPhotoDuplicates_one()
	{
		MediaList<Photo> photoList = new MediaList<>();
		Photo photo1 = new Photo(new File("doesnotexist.jpg"));
		photoList.add(photo1);
		Assertions.assertTrue(photoList.hasAny());
		Assertions.assertTrue(photoList.contains(photo1));
		Assertions.assertTrue(photoList.hasDuplicate(photo1));

		// This photo is from a different jpg file, so no duplicates
		Photo photo2 = new Photo(new File("alsonot.jpg"));
		Assertions.assertFalse(photoList.contains(photo2));
		Assertions.assertFalse(photoList.hasDuplicate(photo2));

		// Photo 3 is from the same jpg file as photo1, so it has a duplicate
		// even though this new object isn't contained in the list
		Photo photo3 = new Photo(new File("doesnotexist.jpg"));
		Assertions.assertFalse(photoList.contains(photo3));
		Assertions.assertTrue(photoList.hasDuplicate(photo3));
	}
}
