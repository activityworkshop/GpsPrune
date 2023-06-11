package tim.prune.function.sew;

import java.util.ArrayList;
import java.util.List;

/**
 * A single segment in the sewing operation
 */
public class Segment
{
	private final int _startIndex;
	private final int _endIndex;
	private boolean _reversed;

	public Segment(int inStartIndex, int inEndIndex)
	{
		_startIndex = inStartIndex;
		_endIndex = inEndIndex;
		_reversed = false;
		// TODO: Set 'alive' status if either end point has photo/audio?
	}

	public boolean isSingle() {
		return _startIndex == _endIndex;
	}

	public void reverse() {
		_reversed = !_reversed;
	}

	public int getStartIndex() {
		return _reversed ? _endIndex : _startIndex;
	}

	public int getEndIndex() {
		return _reversed ? _startIndex : _endIndex;
	}

	public List<Integer> getPointIndexes(boolean withFirstPoint)
	{
		ArrayList<Integer> result = new ArrayList<>();
		boolean firstPoint = true;
		int numPoints = _endIndex - _startIndex + 1;
		for (int i = 0; i < numPoints; i++)
		{
			if (firstPoint) {
				firstPoint = false;
				if (!withFirstPoint) {
					continue;
				}
			}
			int indexToAdd = (_reversed ? _endIndex - i : _startIndex + i);
			result.add(indexToAdd);
		}
		return result;
	}
}
