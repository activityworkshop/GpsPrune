package tim.prune.gui.profile;

import tim.prune.I18nManager;
import tim.prune.config.Config;
import tim.prune.data.DataPoint;
import tim.prune.data.Distance;
import tim.prune.data.Track;
import tim.prune.data.Distance.Units;

/**
 * Class to provide a source of speed data for the profile chart
 */
public class SpeedData extends ProfileData
{
	/** Flag for metric units */
	private boolean _metric = true;

	/**
	 * Constructor
	 * @param inTrack track object
	 */
	public SpeedData(Track inTrack) {
		super(inTrack);
	}

	/**
	 * Get the data and populate the instance arrays
	 */
	public void init()
	{
		initArrays();
		_metric = Config.getConfigBoolean(Config.KEY_METRIC_UNITS);
		_hasData = false;
		_minValue = _maxValue = 0.0;
		if (_track != null) {
			DataPoint prevPrevPoint = null, prevPoint = null, point = null;
			for (int i=0; i<_track.getNumPoints(); i++)
			{
				point = _track.getPoint(i);
				if (prevPrevPoint != null && prevPrevPoint.hasTimestamp()
					&& prevPoint != null && prevPoint.hasTimestamp()
					&& point != null && point.hasTimestamp())
				{
					// All three points have timestamps
					double seconds = point.getTimestamp().getSecondsSince(prevPrevPoint.getTimestamp());
					if (seconds > 0)
					{
						double distInRads = DataPoint.calculateRadiansBetween(prevPrevPoint, prevPoint)
							+ DataPoint.calculateRadiansBetween(prevPoint, point);
						double dist = Distance.convertRadiansToDistance(distInRads, _metric?Units.KILOMETRES:Units.MILES);
						// Store the value and maintain max and min values
						double value = dist / seconds * 60.0 * 60.0;
						_pointValues[i-1] = value;
						if (value < _minValue || _minValue == 0.0) {_minValue = value;}
						if (value > _maxValue) {_maxValue = value;}

						_hasData = true;
						_pointHasData[i-1] = true;
					}
				}
				// Exchange points
				prevPrevPoint = prevPoint;
				prevPoint = point;
			}
		}
	}

	/**
	 * @return text description including units
	 */
	public String getLabel()
	{
		return I18nManager.getText("fieldname.speed") + " ("
			+ I18nManager.getText(_metric?"units.kmh":"units.mph") + ")";
	}

	/**
	 * @return key for message when no speeds present
	 */
	public String getNoDataKey() {
		return "display.notimestamps";
	}
}
