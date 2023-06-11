package tim.prune.cmd;

import tim.prune.data.DataPoint;
import tim.prune.data.Latitude;
import tim.prune.data.Longitude;
import tim.prune.data.Track;

/**
 * Class to help the tests compare the segment flags of their test tracks
 */
public class TrackHelper
{
	public static String describeSegments(Track inTrack)
	{
		StringBuilder result = new StringBuilder();
		for (int i=0; i<inTrack.getNumPoints(); i++)
		{
			DataPoint point = inTrack.getPoint(i);
			if (point.isWaypoint()) {
				result.append('w');
			}
			else {
				result.append(point.getSegmentStart() ? 'S' : '-');
			}
		}
		return result.toString();
	}

	public static Track makeTwelvePointTrack()
	{
		Track track = new Track();
		for (int i=0; i<12; i++) {
			track.appendPoint(new DataPoint(new Latitude("1.23"), new Longitude("2.34"), null));
		}
		return track;
	}
}
