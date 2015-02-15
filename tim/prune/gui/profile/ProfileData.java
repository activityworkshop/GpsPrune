package tim.prune.gui.profile;

import tim.prune.data.Track;
import tim.prune.data.UnitSet;

/**
 * Abstract class for all sources of profile data,
 * including altitudes and speeds
 */
public abstract class ProfileData
{
	/** Track object */
	protected final Track _track;
	/** Unit set to use */
	protected UnitSet _unitSet = null;
	/** Flag for availability of any data */
	protected boolean _hasData = false;
	/** Array of booleans for data per point */
	protected boolean[] _pointHasData = null;
	/** Array of values per point */
	protected double[] _pointValues = null;
	/** Minimum value for track */
	protected double _minValue = 0.0;
	/** Maximum value for track */
	protected double _maxValue = 0.0;

	/**
	 * Constructor giving track object
	 * @param inTrack track object
	 */
	public ProfileData(Track inTrack)
	{
		_track = inTrack;
	}

	/**
	 * @return true if this source has any data at all
	 */
	public boolean hasData() {
		return _hasData;
	}

	/**
	 * @param inPointNum index of point
	 * @return true if that point has data
	 */
	public boolean hasData(int inPointNum)
	{
		return (_hasData && _pointHasData != null && inPointNum >= 0
			&& inPointNum < _pointHasData.length && _pointHasData[inPointNum]);
	}

	/**
	 * @param inPointNum index of point
	 * @return value corresponding to that point
	 */
	public double getData(int inPointNum)
	{
		if (!hasData(inPointNum)) {return 0.0;}
		return _pointValues[inPointNum];
	}

	/**
	 * @return minimum value
	 */
	public double getMinValue() {
		return _minValue;
	}

	/**
	 * @return maximum value
	 */
	public double getMaxValue() {
		return _maxValue;
	}

	/**
	 * Get the data from the track and populate the value arrays
	 */
	public abstract void init(UnitSet inUnitSet);

	/**
	 * Set the UnitSet to use for the calculations
	 * @param inUnitSet unit set
	 */
	protected void setUnitSet(UnitSet inUnitSet) {
		_unitSet = inUnitSet;
	}

	/**
	 * @return text for label including units
	 */
	public abstract String getLabel();

	/**
	 * @return key for message when no data available
	 */
	public abstract String getNoDataKey();

	/**
	 * Initialise the data arrays to the correct size
	 */
	protected void initArrays()
	{
		int numTrackPoints = _track.getNumPoints();
		if (_pointHasData == null || _pointHasData.length != numTrackPoints)
		{
			_pointHasData = new boolean[numTrackPoints];
			_pointValues = new double[numTrackPoints];
		}
	}
}
