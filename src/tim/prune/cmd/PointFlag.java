package tim.prune.cmd;

import tim.prune.data.DataPoint;

/**
 * Holds a point together with a boolean flag
 */
public class PointFlag
{
	private final DataPoint _point;
	private final boolean _flag;

	/**
	 * Constructor
	 */
	public PointFlag(DataPoint inPoint, boolean inFlag)
	{
		_point = inPoint;
		_flag = inFlag;
	}

	public DataPoint getPoint() {
		return _point;
	}

	public boolean getFlag() {
		return _flag;
	}
}
