package tim.prune.load;

import java.util.ArrayList;

/**
 * Class to hold and manage a list of track names
 * from a gpx file, and report back which points
 * belong to which track
 */
public class TrackNameList
{
	/** Current point number */
	private int _pointNum = -1;
	/** Current track number */
	private int _trackNum = -1;
	/** List of unique track names */
	private ArrayList<String> _trackNames = new ArrayList<String>();
	/** List of point numbers at the start of each track */
	private ArrayList<Integer> _startIndices = new ArrayList<Integer>();


	/**
	 * Inform list of a new point
	 * @param inTrackNum number of track, starting at zero
	 * @param inTrackName name of track, if any
	 * @param inIsTrackpoint true if point is a trackpoint
	 */
	public void addPoint(int inTrackNum, String inTrackName, boolean inIsTrackpoint)
	{
		_pointNum++;
		if (inIsTrackpoint)
		{
			if (inTrackNum != _trackNum) {
				_trackNames.add(inTrackName);
				_startIndices.add(Integer.valueOf(_pointNum));
			}
		}
		_trackNum = inTrackNum;
	}

	/**
	 * @return number of tracks found in file
	 */
	public int getNumTracks()
	{
		return _trackNames.size();
	}

	/**
	 * @param inTrackNum index of track, starting from zero
	 * @return name of specified track (or null if none given)
	 */
	public String getTrackName(int inTrackNum)
	{
		if (inTrackNum < 0 || inTrackNum >= getNumTracks()) {return "";}
		String name = _trackNames.get(inTrackNum);
		return name;
	}

	/**
	 * @param inTrackNum index of track, starting from zero
	 * @return number of points in the specified track
	 */
	public int getNumPointsInTrack(int inTrackNum)
	{
		if (inTrackNum < 0 || inTrackNum >= getNumTracks()) {return 0;}
		if (inTrackNum == (getNumTracks()-1)) {
			// last track, use total points
			return _pointNum - _startIndices.get(inTrackNum) + 1;
		}
		return _startIndices.get(inTrackNum+1) - _startIndices.get(inTrackNum);
	}

	/**
	 * @param inTrackNum index of track, starting from zero
	 * @return start index of the specified track
	 */
	public int getStartIndex(int inTrackNum)
	{
		if (inTrackNum < 0 || inTrackNum >= getNumTracks()) {return 0;}
		return _startIndices.get(inTrackNum);
	}
}
