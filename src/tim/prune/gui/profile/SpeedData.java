package tim.prune.gui.profile;

import tim.prune.I18nManager;
import tim.prune.data.SpeedCalculator;
import tim.prune.data.SpeedValue;
import tim.prune.data.Track;
import tim.prune.data.UnitSet;

/**
 * Class to provide a source of speed data for the profile chart
 */
public class SpeedData extends ProfileData
{
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
				// Get the speed either from the speed value or from the distances and timestamps
				SpeedCalculator.calculateSpeed(_track, i, speed);
				if (speed.isValid())
				{
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
		return I18nManager.getText("fieldname.speed") + " ("
			+ I18nManager.getText(_unitSet.getSpeedUnit().getShortnameKey()) + ")";
	}

	/**
	 * @return key for message when no speeds present
	 */
	public String getNoDataKey() {
		return "display.notimestamps";
	}
}
