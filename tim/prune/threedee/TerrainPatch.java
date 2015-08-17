package tim.prune.threedee;

public class TerrainPatch
{
	private int      _gridSize = 0;
	private double[] _altitudes = null;
	private int[]    _tempDists = null;

	/**
	 * Constructor
	 * @param inGridSize size of grid edge
	 */
	public TerrainPatch(int inGridSize)
	{
		_gridSize = inGridSize;
		int numNodes = inGridSize * inGridSize;
		_altitudes = new double[numNodes];
		_tempDists = new int[numNodes];
	}

	/**
	 * Add an altitude interpolation to the mix
	 * @param inPointIndex point index to array
	 * @param inValue altitude value in metres
	 * @param inGapIndex index of point within gap, from 1 to gapLength-1
	 * @param inGapLength length of gap, minimum 2
	 */
	public void addAltitude(int inPointIndex, double inValue, int inGapIndex, int inGapLength)
	{
		final int dist = Math.min(inGapIndex, inGapLength-inGapIndex);
		if (_tempDists[inPointIndex] == 0)
		{
			if (_altitudes[inPointIndex] > 0.0) System.err.println("Altitude shouldn't be 0 if dist is 0!");
			// first point
			_altitudes[inPointIndex] = inValue;
			_tempDists[inPointIndex] = dist;
		}
		else
		{
			// second point
			final double firstValue = _altitudes[inPointIndex];
			final int firstDist     = _tempDists[inPointIndex];
			final double firstWeight = dist * 1.0 / (dist + firstDist);
			final double secondWeight= firstDist * 1.0 / (dist + firstDist);
			_altitudes[inPointIndex] = firstWeight * firstValue + secondWeight * inValue;
			_tempDists[inPointIndex] = 0;
		}
	}

	/**
	 * Smooth the patch to reduce blockiness
	 */
	public void smooth()
	{
		double[] altCopy = new double[_altitudes.length];
		for (int i=0; i<_gridSize; i++)
		{
			for (int j=0; j<_gridSize; j++)
			{
				if (hasAltitude(i, j) && hasAltitude(i-1, j) && hasAltitude(i+1, j)
					&& hasAltitude(i, j+1) && hasAltitude(i-1, j+1) && hasAltitude(i+1, j+1)
					&& hasAltitude(i, j-1) && hasAltitude(i-1, j-1) && hasAltitude(i+1, j-1))
				{
					// got a 3x3 square, can do a blur
					double alt = (getAltitude(i, j) + getAltitude(i-1, j) + getAltitude(i+1, j)
						+ getAltitude(i, j+1) + getAltitude(i-1, j+1) + getAltitude(i+1, j+1)
						+ getAltitude(i, j-1) + getAltitude(i-1, j-1) + getAltitude(i+1, j-1)) / 9.0;
					altCopy[i * _gridSize + j] = alt;
				}
			}
		}
		// Copy results back
		for (int k=0; k<altCopy.length; k++)
		{
			if (altCopy[k] > 0.0)
			{
				_altitudes[k] = altCopy[k];
			}
		}
	}

	/**
	 * @param inI first index
	 * @param inJ second index
	 * @return true if there is an altitude in the patch in this position
	 */
	private boolean hasAltitude(int inI, int inJ)
	{
		return inI >= 0 && inI < _gridSize && inJ >= 0 && inJ < _gridSize
			&& _altitudes[inI * _gridSize + inJ] > 0.0;
	}

	/**
	 * @param inI first index
	 * @param inJ second index
	 * @return true if there is an altitude in the patch in this position
	 */
	private double getAltitude(int inI, int inJ)
	{
		if (inI >= 0 && inI < _gridSize && inJ >= 0 && inJ < _gridSize)
		{
			return _altitudes[inI * _gridSize + inJ];
		}
		return 0.0;
	}

	/**
	 * @param inPointIndex point index
	 * @return altitude value
	 */
	public double getAltitude(int inPointIndex)
	{
		if (_tempDists[inPointIndex] != 0) System.err.println("Dists should be 0 if we're retrieving!");
		return _altitudes[inPointIndex];
	}
}
