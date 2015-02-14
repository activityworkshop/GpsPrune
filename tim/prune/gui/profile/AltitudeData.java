package tim.prune.gui.profile;

import tim.prune.I18nManager;
import tim.prune.data.Altitude;
import tim.prune.data.DataPoint;
import tim.prune.data.Track;

/**
 * Class to provide a source of altitude data for the profile chart
 */
public class AltitudeData extends ProfileData
{
	/** Altitude format for values */
	private Altitude.Format _altitudeFormat = Altitude.Format.NO_FORMAT;


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
	public void init()
	{
		initArrays();
		_hasData = false;
		_altitudeFormat = Altitude.Format.NO_FORMAT;
		if (_track != null) {
			for (int i=0; i<_track.getNumPoints(); i++)
			{
				DataPoint point = _track.getPoint(i);
				if (point != null && point.hasAltitude())
				{
					// Point has an altitude - if it's the first one, use its format
					if (_altitudeFormat == Altitude.Format.NO_FORMAT)
					{
						_altitudeFormat = point.getAltitude().getFormat();
						_minValue = _maxValue = point.getAltitude().getValue();
					}
					// Store the value and maintain max and min values
					double value = point.getAltitude().getValue(_altitudeFormat);
					_pointValues[i] = value;
					if (value < _minValue) {_minValue = value;}
					if (value > _maxValue) {_maxValue = value;}

					_hasData = true;
					_pointHasData[i] = true;
				}
				else _pointHasData[i] = false;
			}
		}
	}

	/**
	 * @return text description including units
	 */
	public String getLabel()
	{
		return I18nManager.getText("fieldname.altitude") + " ("
			+ I18nManager.getText(_altitudeFormat==Altitude.Format.FEET?"units.feet.short":"units.metres.short")
			+ ")";
	}

	/**
	 * @return key for message when no altitudes present
	 */
	public String getNoDataKey() {
		return "display.noaltitudes";
	}
}
