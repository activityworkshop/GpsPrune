package tim.prune.data.sort;

import java.util.Comparator;

import tim.prune.data.DataPoint;


/**
 * Class for comparing photos to sort them by name or timestamp
 */
public class PhotoComparer implements Comparator<DataPoint>
{
	/** Sort mode */
	private SortMode _sortMode;


	/**
	 * Constructor
	 * @param inMode sort mode
	 */
	public PhotoComparer(SortMode inMode)
	{
		_sortMode = inMode;
	}

	/**
	 * Main compare method
	 */
	public int compare(DataPoint inP1, DataPoint inP2)
	{
		if (inP2 == null || inP2.getPhoto() == null) return -1; // all nulls at end
		if (inP1 == null || inP1.getPhoto() == null) return 1;
		// Sort by name
		int result = 0;
		if (_sortMode == SortMode.SORTBY_NAME) {
			result = compareNames(inP1, inP2);
		}
		if (result == 0) {
			result = compareTimes(inP1, inP2);
		}
		// check names if times didn't work
		if (result == 0 && _sortMode == SortMode.SORTBY_TIME) {
			result = compareNames(inP1, inP2);
		}
		// names and times equal, try width and height
		if (result == 0) {
			result = compareSizes(inP1, inP2);
		}
		return result;
	}

	/**
	 * Compare the names of the two photo points
	 * @param inP1 first point
	 * @param inP2 second point
	 * @return compare value (-1,0,1)
	 */
	private int compareNames(DataPoint inP1, DataPoint inP2)
	{
		// If the files can't be compared, use the photo names
		if (inP1.getPhoto().getFile() == null || inP2.getPhoto().getFile() == null) {
			return inP1.getPhoto().getName().compareTo(inP2.getPhoto().getName());
		}
		// both photos have files, so just compare the files
		return inP1.getPhoto().getFile().compareTo(inP2.getPhoto().getFile());
	}

	/**
	 * Compare the timestamps of the two photo points
	 * @param inP1 first point
	 * @param inP2 second point
	 * @return compare value (-1,0,1)
	 */
	private int compareTimes(DataPoint inP1, DataPoint inP2)
	{
		// Photos might not have timestamps
		if (!inP2.hasTimestamp()) return -1;
		if (!inP1.hasTimestamp()) return 1;
		// Compare the timestamps
		long secDiff = inP1.getPhoto().getTimestamp().getMillisecondsSince(inP2.getPhoto().getTimestamp());
		return (secDiff<0?-1:(secDiff==0?0:1));
	}

	/**
	 * Compare the sizes of the two photos
	 * @param inP1 first point
	 * @param inP2 second point
	 * @return compare value (-1,0,1)
	 */
	private int compareSizes(DataPoint inP1, DataPoint inP2)
	{
		// Try the widths
		int w1 = inP1.getPhoto().getWidth();
		int w2 = inP2.getPhoto().getWidth();
		if (w2 <= 0) return -1;
		if (w1 <= 0) return 1;
		if (w1 != w2) return (w2 > w1 ? 1 : -1);
		// Try the heights
		int h1 = inP1.getPhoto().getHeight();
		int h2 = inP2.getPhoto().getHeight();
		if (h2 <= 0) return -1;
		if (h1 <= 0) return 1;
		if (h1 != h2) return (h2 > h1 ? 1 : -1);
		// sizes same
		return 0;
	}
}
