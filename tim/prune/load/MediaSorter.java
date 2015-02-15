package tim.prune.load;

import java.io.File;
import java.util.Comparator;
import tim.prune.data.MediaObject;

/**
 * Class to sort photos, audios by name
 */
public class MediaSorter implements Comparator<MediaObject>
{
	/**
	 * Compare two media objects
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(MediaObject o1, MediaObject o2)
	{
		int nameComp = o1.getName().compareTo(o2.getName());
		if (nameComp != 0) {
			// names different
			return nameComp;
		}
		File file1 = o1.getFile();
		File file2 = o2.getFile();
		if (file1 != null && file2 != null)
		{
			// names same, maybe in different directories
			nameComp = file1.getAbsolutePath().compareTo(file2.getAbsolutePath());
		}
		else if (o1.getByteData() != null && o2.getByteData() != null) {
			// compare data lengths instead
			nameComp = o1.getByteData().length - o2.getByteData().length;
		}
		else {
			// one's a file, one's from data
			nameComp = 1;
		}
		return nameComp;
	}
}
