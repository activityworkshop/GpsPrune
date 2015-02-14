package tim.prune.correlate;

import tim.prune.data.Photo;
import tim.prune.data.TimeDifference;

/**
 * Class to hold contents of a single row
 * in the photo selection table
 */
public class PhotoSelectionTableRow
{
	private Photo _photo = null;
	private TimeDifference _timeDiff = null;

	/**
	 * Constructor
	 * @param inPhoto Photo object
	 * @param inNumSecs number of seconds time difference as long
	 */
	public PhotoSelectionTableRow(Photo inPhoto, long inNumSecs)
	{
		_photo = inPhoto;
		_timeDiff = new TimeDifference(inNumSecs);
	}

	/**
	 * @return Photo object
	 */
	public Photo getPhoto()
	{
		return _photo;
	}

	/**
	 * @return time difference
	 */
	public TimeDifference getTimeDiff()
	{
		return _timeDiff;
	}
}
