package tim.prune.correlate;

import tim.prune.data.Distance;
import tim.prune.data.Unit;

/**
 * Class to hold the contents of a single row in the correlation preview table
 */
public class MediaPreviewTableRow extends MediaSelectionTableRow
{
	private final PointMediaPair _pointPair;
	private final double _distance;
	private boolean _correlate;


	/**
	 * Constructor
	 * @param inPointPair point pair object
	 */
	public MediaPreviewTableRow(PointMediaPair inPointPair)
	{
		super(inPointPair.getMedia(), inPointPair.getMinSeconds());
		_pointPair = inPointPair;
		_distance = inPointPair.getMinRadians();
		_correlate = (inPointPair.getMedia().getDataPoint() == null);
	}

	/**
	 * @param inUnits units to use
	 * @return distance in selected format
	 */
	public double getDistance(Unit inUnits)
	{
		return Distance.convertRadiansToDistance(_distance, inUnits);
	}

	/**
	 * @return point pair object
	 */
	public PointMediaPair getPointPair()
	{
		return _pointPair;
	}

	/**
	 * @return flag to set whether to correlate or not
	 */
	public boolean getCorrelateFlag() {
		return _correlate;
	}

	/**
	 * @param inFlag true to correlate, false to ignore
	 */
	public void setCorrelateFlag(boolean inFlag)
	{
		_correlate = inFlag;
	}
}
