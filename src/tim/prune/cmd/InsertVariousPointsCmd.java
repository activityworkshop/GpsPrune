package tim.prune.cmd;

import java.util.ArrayList;

import tim.prune.data.DataPoint;

/**
 * Command to insert a series of points at various different places in the track,
 * for example an interpolation, or the undo of a compression
 */
public class InsertVariousPointsCmd extends CompoundCommand
{
	private final int _numAdded;


	public InsertVariousPointsCmd(ArrayList<Integer> inIndexes, ArrayList<DataPoint> inPoints)
	{
		_numAdded = inPoints.size();
		addCommand(new AppendRangeCmd(inPoints));
		addCommand(new RearrangePointsCmd(inIndexes));
	}

	/**
	 * @return the total number of points inserted by this command
	 */
	public int getNumInserted() {
		return _numAdded;
	}
}
