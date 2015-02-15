package tim.prune.load;

import java.io.File;
import java.util.Comparator;
import tim.prune.data.MediaFile;


/**
 * Class to sort photos by name
 */
public class MediaSorter implements Comparator<MediaFile>
{

	/**
	 * Compare two media files
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(MediaFile o1, MediaFile o2)
	{
		File file1 = o1.getFile();
		File file2 = o2.getFile();
		int nameComp = file1.getName().compareTo(file2.getName());
		if (nameComp == 0)
		{
			// names same, maybe in different directories
			return file1.getAbsolutePath().compareTo(file2.getAbsolutePath());
		}
		// names different
		return nameComp;
	}

}
