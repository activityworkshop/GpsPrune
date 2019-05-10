package tim.prune.function.charts;

/**
 * Class to hold a data series for the charts
 */
public class ChartSeries
{
	/** Array of booleans, true for data existing, false otherwise */
	private boolean[] _hasData = null;
	/** Array of data */
	private double[] _data = null;

	/**
	 * Constructor
	 * @param inNumPoints number of points
	 */
	public ChartSeries(int inNumPoints)
	{
		_hasData = new boolean[inNumPoints];
		_data = new double[inNumPoints];
	}

	/**
	 * @param inIndex index of point
	 * @return true if series has data for this point
	 */
	public boolean hasData(int inIndex)
	{
		return _hasData[inIndex];
	}

	/**
	 * @param inIndex index of point
	 * @return data value for this point
	 */
	public double getData(int inIndex)
	{
		return _data[inIndex];
	}

	/**
	 * Set the data at the given index
	 * @param inIndex index of point
	 * @param inData data value
	 */
	public void setData(int inIndex, double inData)
	{
		_hasData[inIndex] = true;
		_data[inIndex] = inData;
	}
}
