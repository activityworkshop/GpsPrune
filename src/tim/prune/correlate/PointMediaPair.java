package tim.prune.correlate;

import tim.prune.data.DataPoint;
import tim.prune.data.MediaObject;

/**
 * Class to hold a pair of points used to hold the result of correlation
 */
public class PointMediaPair
{
	private MediaObject _media = null;
	private DataPoint _pointBefore = null;
	private DataPoint _pointAfter = null;
	private long _secondsBefore = 1L;
	private long _secondsAfter = -1L;


	/**
	 * Constructor
	 * @param inMedia media object
	 */
	public PointMediaPair(MediaObject inMedia) {
		_media = inMedia;
	}

	/**
	 * Add a point to the pair
	 * @param inPoint data point
	 * @param inSeconds number of seconds time difference, positive means point later
	 */
	public void addPoint(DataPoint inPoint, long inSeconds)
	{
		// Check if point is closest point before
		if (inSeconds <= 0)
		{
			// point stamp is before media stamp
			if (inSeconds > _secondsBefore || _secondsBefore > 0L)
			{
				// point stamp is nearer to media
				_pointBefore = inPoint;
				_secondsBefore = inSeconds;
			}
		}
		// Check if point is closest point after
		if (inSeconds >= 0)
		{
			// point stamp is after media stamp
			if (inSeconds < _secondsAfter || _secondsAfter < 0L)
			{
				// point stamp is nearer to media
				_pointAfter = inPoint;
				_secondsAfter = inSeconds;
			}
		}
	}


	/**
	 * @return Media object
	 */
	public MediaObject getMedia() {
		return _media;
	}

	/**
	 * @return the closest point before the media
	 */
	public DataPoint getPointBefore() {
		return _pointBefore;
	}

	/**
	 * @return number of seconds between media and subsequent point
	 */
	public long getSecondsBefore() {
		return _secondsBefore;
	}

	/**
	 * @return the closest point after the media
	 */
	public DataPoint getPointAfter() {
		return _pointAfter;
	}

	/**
	 * @return number of seconds between previous point and media
	 */
	public long getSecondsAfter() {
		return _secondsAfter;
	}

	/**
	 * @return true if both points found
	 */
	public boolean isValid() {
		return getPointBefore() != null && getPointAfter() != null;
	}

	/**
	 * @return the fraction of the distance along the interpolated line
	 */
	public double getFraction()
	{
		if (_secondsAfter == 0L) return 0.0;
		return (-_secondsBefore * 1.0 / (-_secondsBefore + _secondsAfter));
	}

	/**
	 * @return the number of seconds to the nearest point
	 */
	public long getMinSeconds() {
		return Math.min(_secondsAfter, -_secondsBefore);
	}

	/**
	 * @return angle from media to nearest point in radians
	 */
	public double getMinRadians()
	{
		double totalRadians = DataPoint.calculateRadiansBetween(_pointBefore, _pointAfter);
		double frac = getFraction();
		return totalRadians * Math.min(frac, 1-frac);
	}
}
