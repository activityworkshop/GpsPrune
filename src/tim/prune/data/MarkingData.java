package tim.prune.data;

public class MarkingData
{
	private final Track _track;
	private byte[] _flags = null;

	public MarkingData(Track inTrack) {
		_track = inTrack;
	}

	public boolean isPointMarkedForDeletion(int inIndex)
	{
		checkSize();
		if (_flags != null && inIndex >= 0 && inIndex < _flags.length) {
			return (_flags[inIndex] % 2) != 0;
		}
		return false;
	}

	public boolean isPointMarkedForSegmentBreak(int inIndex)
	{
		checkSize();
		if (_flags != null && inIndex >= 0 && inIndex < _flags.length)
		{
			int flag = _flags[inIndex] / 2;
			return (flag % 2) != 0;
		}
		return false;
	}

	private void checkSize()
	{
		if (_flags != null && _flags.length != _track.getNumPoints()) {
			_flags = null;
		}
	}

	public void clear() {
		_flags = null;
	}

	public void markPointsForDeletion(boolean[] inDeleteFlags) {
		markPointsForDeletion(inDeleteFlags, null);
	}

	public void markPointsForDeletion(boolean[] inDeleteFlags, boolean[] inSegmentFlags)
	{
		final int numFlags = inDeleteFlags == null ? 0 : inDeleteFlags.length;
		if (_flags == null || _flags.length != numFlags) {
			_flags = new byte[numFlags];
		}
		boolean[] segmentFlags = inSegmentFlags;
		if (inSegmentFlags != null && inSegmentFlags.length != numFlags) {
			segmentFlags = null;
		}
		for (int i=0; i<numFlags; i++)
		{
			boolean deleteFlag = inDeleteFlags[i];
			boolean segmentFlag = segmentFlags != null && segmentFlags[i];
			int flag = (deleteFlag ? 1 : 0) + (segmentFlag ? 2 : 0);
			_flags[i] = (byte) flag;
		}
	}

	public void markPointForDeletion(int inIndex) {
		markPointForDeletion(inIndex, true);
	}

	public void markPointForDeletion(int inIndex, boolean inDelete) {
		markPointForDeletion(inIndex, inDelete, false);
	}

	public void markPointForDeletion(int inIndex, boolean inDelete, boolean inSegmentBreak)
	{
		checkSize();
		if (_flags == null)
		{
			if (!inDelete) {
				return;
			}
			_flags = new byte[_track.getNumPoints()];
		}
		if (inIndex < 0 || inIndex >= _flags.length) {
			return;
		}
		_flags[inIndex] = combineFlags(inDelete, inSegmentBreak);
	}

	private byte combineFlags(boolean inDelete, boolean inSegmentBreak)
	{
		if (inDelete) {
			return (byte) ((inSegmentBreak ? 2 : 0) + 1);
		}
		return 0;
	}

	public boolean hasMarkedPoints()
	{
		checkSize();
		if (_flags != null)
		{
			for (byte flag : _flags)
			{
				if (flag != 0) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @return number of points which are marked to be deleted
	 */
	public int getNumDeleted()
	{
		int numDeleted = 0;
		checkSize();
		if (_flags != null)
		{
			for (byte flag : _flags)
			{
				if (flag != 0) {
					numDeleted++;
				}
			}
		}
		return numDeleted;
	}

}
