package tim.prune.gui.profile;

import tim.prune.data.Field;
import tim.prune.data.Track;
import tim.prune.data.UnitSet;

/**
 * Class to provide a source of values for the profile chart
 * using any arbitary (non-built-in) field, units unknown
 */
public class ArbitraryData extends ProfileData
{
	/** Field to use */
	private Field _field = null;

	/**
	 * Constructor
	 * @param inTrack track object
	 * @param inField field to use
	 */
	public ArbitraryData(Track inTrack, Field inField)
	{
		super(inTrack);
		_field = inField;
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
		if (_track != null)
		{
			for (int i=0; i<_track.getNumPoints(); i++)
			{
				// Get the value of the given field
				boolean hasValue = false;
				String value = _track.getPoint(i).getFieldValue(_field);
				try
				{
					double dValue = Double.parseDouble(value);
					_pointValues[i] = dValue;
					if (dValue < _minValue || _minValue == 0.0) {_minValue = dValue;}
					if (dValue > _maxValue) {_maxValue = dValue;}
					hasValue = true;
					_hasData = true;
				}
				catch (Exception e) {} // ignore nulls and non-numbers
				_pointHasData[i] = hasValue;
			}
		}
	}

	/**
	 * @return name of field
	 */
	public String getLabel()
	{
		return _field.getName();
	}

	/**
	 * @return the field object
	 */
	public Field getField() {
		return _field;
	}

	/**
	 * @return key for message when no values present
	 */
	public String getNoDataKey() {
		return "display.novalues";
	}
}
