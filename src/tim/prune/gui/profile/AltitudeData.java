package tim.prune.gui.profile;

import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.Track;
import tim.prune.data.UnitSet;

/**
 * Class to provide a source of altitude data for the profile chart
 */
public class AltitudeData extends ProfileData
{
	/**
	 * Constructor
	 * @param inTrack track object
	 */
	public AltitudeData(Track inTrack) {
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
		// multiplication factor for unit conversion
		final double multFactor = _unitSet.getAltitudeUnit().getMultFactorFromStd();
		if (_track != null)
		{
			try
			{
				for (int i=0; i<_track.getNumPoints(); i++)
				{
					DataPoint point = _track.getPoint(i);
					if (point != null && point.hasAltitude())
					{
						// Point has an altitude - store value and maintain max and min values
						double value = point.getAltitude().getMetricValue() * multFactor;
						_pointValues[i] = value;
						if (value < _minValue || !_hasData) {_minValue = value;}
						if (value > _maxValue || !_hasData) {_maxValue = value;}

						// if all values are zero then that's no data
						_hasData = _hasData || (point.getAltitude().getValue() != 0);
						_pointHasData[i] = true;
					}
					else _pointHasData[i] = false;
				}
			}
			catch (ArrayIndexOutOfBoundsException obe)
			{} // must be due to the track size changing during calculation
			   // assume that a redraw will be triggered
		}
	}

	/**
	 * @return text description including units
	 */
	public String getLabel()
	{
		return I18nManager.getText("fieldname.altitude") + " ("
			+ I18nManager.getText(_unitSet.getAltitudeUnit().getShortnameKey())
			+ ")";
	}

	/**
	 * @return key for message when no altitudes present
	 */
	public String getNoDataKey() {
		return "display.noaltitudes";
	}
}
