package tim.prune.correlate;

import tim.prune.data.MediaFile;
import tim.prune.data.TimeDifference;

/**
 * Class to hold the contents of a single row
 * in the selection table for correlation
 */
public class MediaSelectionTableRow
{
	private MediaFile _media = null;
	private TimeDifference _timeDiff = null;

	/**
	 * Constructor
	 * @param inMedia media item
	 * @param inNumSecs number of seconds time difference as long
	 */
	public MediaSelectionTableRow(MediaFile inMedia, long inNumSecs)
	{
		_media = inMedia;
		_timeDiff = new TimeDifference(inNumSecs);
	}

	/**
	 * @return Media object
	 */
	public MediaFile getMedia() {
		return _media;
	}

	/**
	 * @return time difference
	 */
	public TimeDifference getTimeDiff() {
		return _timeDiff;
	}
}
