package tim.prune.correlate;

import tim.prune.data.Distance;

/**
 * Class to hold contents of a single row
 * in the photo preview table
 */
public class PhotoPreviewTableRow extends PhotoSelectionTableRow
{
	private PointPair _pointPair = null;
	private double _distance = 0.0;
	private int _status = 0;
	private boolean _correlate = false;


	/**
	 * Constructor
	 * @param inPointPair point pair object
	 */
	public PhotoPreviewTableRow(PointPair inPointPair)
	{
		super(inPointPair.getPhoto(), inPointPair.getMinSeconds());
		_pointPair = inPointPair;
		_distance = inPointPair.getMinRadians();
		_status = 0;
		_correlate = (inPointPair.getPhoto().getDataPoint() == null);
	}

	/**
	 * @param inUnits units to use
	 * @return distance in selected format
	 */
	public double getDistance(int inUnits)
	{
		return Distance.convertRadiansToDistance(_distance, inUnits);
	}

	/**
	 * @return point status
	 */
	public int getStatus()
	{
		return _status;
	}

	/**
	 * @return point pair object
	 */
	public PointPair getPointPair()
	{
		return _pointPair;
	}

	/**
	 * @return flag to set whether to correlate or not
	 */
	public Boolean getCorrelateFlag()
	{
		return Boolean.valueOf(_correlate);
	}

	/**
	 * @param inFlag true to correlate, false to ignore
	 */
	public void setCorrelateFlag(boolean inFlag)
	{
		_correlate = inFlag;
	}
}
