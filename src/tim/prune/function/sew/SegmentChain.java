package tim.prune.function.sew;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tim.prune.data.DataPoint;
import tim.prune.data.Track;

/**
 * A chain of segments
 */
public class SegmentChain
{
	private final ArrayList<Segment> _segments = new ArrayList<>();
	private final Track _track;

	/**
	 * Constructor
	 * @param inFirstSegment first segment of chain
	 */
	public SegmentChain(Track inTrack, Segment inFirstSegment) {
		_segments.add(inFirstSegment);
		_track = inTrack;
	}

	private DataPoint getFirstPoint() {
		return _track.getPoint(_segments.get(0).getStartIndex());
	}

	private DataPoint getLastPoint() {
		return _track.getPoint(_segments.get(_segments.size()-1).getEndIndex());
	}

	private boolean arePointsSame(DataPoint inFirst, DataPoint inSecond)
	{
		return inFirst.getLatitude().equals(inSecond.getLatitude())
			&& inFirst.getLongitude().equals(inSecond.getLongitude());
	}

	public boolean append(Segment inNextSegment)
	{
		DataPoint chainStart = getFirstPoint();
		DataPoint chainEnd = getLastPoint();
		for (int i=0; i<2; i++)
		{
			DataPoint segmentStart = _track.getPoint(inNextSegment.getStartIndex());
			if (arePointsSame(chainEnd, segmentStart))
			{
				_segments.add(inNextSegment);
				return true;
			}
			DataPoint segmentEnd = _track.getPoint(inNextSegment.getEndIndex());
			if (arePointsSame(chainStart, segmentEnd))
			{
				_segments.add(0, inNextSegment);
				return true;
			}
			inNextSegment.reverse();
		}
		return false;
	}

	/**
	 * Reverse the whole chain
	 */
	private void reverse()
	{
		for (Segment segment : _segments) {
			segment.reverse();
		}
		Collections.reverse(_segments);
	}

	/**
	 * Try to add another chain to this one
	 * @param inOther other chain
	 * @return true if successful, other one is now redundant
	 */
	public boolean append(SegmentChain inOther)
	{
		DataPoint chainStart = getFirstPoint();
		DataPoint chainEnd = getLastPoint();
		for (int i=0; i<2; i++)
		{
			DataPoint segmentStart = inOther.getFirstPoint();
			if (arePointsSame(chainEnd, segmentStart))
			{
				_segments.addAll(inOther._segments);
				return true;
			}
			DataPoint segmentEnd = inOther.getLastPoint();
			if (arePointsSame(chainStart, segmentEnd))
			{
				_segments.addAll(0, inOther._segments);
				return true;
			}
			inOther.reverse();
		}
		return false;
	}

	/**
	 * @return the point indices of this chain in order
	 */
	public List<Integer> getPointIndexes()
	{
		ArrayList<Integer> result = new ArrayList<>();
		for (Segment segment : _segments) {
			result.addAll(segment.getPointIndexes(result.isEmpty()));
		}
		return result;
	}
}
