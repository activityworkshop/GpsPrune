package tim.prune.gui.profile;

import tim.prune.I18nManager;
import tim.prune.data.SpeedCalculator;
import tim.prune.data.SpeedValue;
import tim.prune.data.Track;
import tim.prune.data.UnitSet;

/**
 * Class to provide a source of vertical speed data for the profile chart
 */
public class VerticalSpeedData extends ProfileData
{
	/**
	 * Constructor
	 * @param inTrack track object
	 */
	public VerticalSpeedData(Track inTrack) {
		super(inTrack);
	}

	/**
	 * Get the data and populate the instance arrays
	 */
	public void init(UnitSet inUnitSet)
	{
		setUnitSet(inUnitSet);
		initArrays();
		_hasData = false;
		_minValue = _maxValue = 0.0;
		SpeedValue speed = new SpeedValue();
		if (_track != null)
		{
			for (int i=0; i<_track.getNumPoints(); i++)
			{
				SpeedCalculator.calculateVerticalSpeed(_track, i, speed);
				// Check whether we got a value from either method
				if (speed.isValid())
				{
					// Store the value and maintain max and min values
					double speedValue = speed.getValue();
					_pointValues[i] = speedValue;
					if (speedValue < _minValue || !_hasData) {_minValue = speedValue;}
					if (speedValue > _maxValue || !_hasData) {_maxValue = speedValue;}
					_hasData = true;
				}
				_pointHasData[i] = speed.isValid();
			}
		}
	}

	/**
	 * @return text description including units
	 */
	public String getLabel()
	{
		return I18nManager.getText("fieldname.verticalspeed") + " ("
			+ I18nManager.getText(_unitSet.getVerticalSpeedUnit().getShortnameKey()) + ")";
	}

	/**
	 * @return key for message when no speeds present
	 */
	public String getNoDataKey()
	{
		if (!_track.hasAltitudeData()) {
			return "display.noaltitudes";
		}
		return "display.notimestamps";
	}
}
