package tim.prune.function.compress.methods;

import tim.prune.data.DataPoint;
import tim.prune.data.MarkingData;
import tim.prune.data.Track;
import tim.prune.function.compress.CompressionMethodType;
import tim.prune.function.compress.TrackDetails;

/** Parent class of all the compression methods */
public abstract class CompressionMethod
{
	private boolean _active = false;

	/**
	 * Preview the algorithm by counting the number of points deleted
	 * @param inTrack track object
	 * @param inDetails track details including span
	 * @param inMarkings information about deletion flags from previous algorithms
	 * @return number of points to be deleted by this algorithm
	 */
	public int preview(Track inTrack, TrackDetails inDetails, MarkingData inMarkings)
	{
		if (!_active) {
			return 0;
		}
		TrackDetails modifiedDetails = inDetails.modifyUsingMarkings(inMarkings);
		return compress(inTrack, modifiedDetails, inMarkings);
	}

	public void setActive(boolean inActive) {
		_active = inActive;
	}

	public boolean isActive() {
		return _active;
	}

	/**
	 * Perform the compression and set the results in the given array
	 * @param inTrack track object
	 * @param inDetails track details including span
	 * @param inMarkings information about deletion flags from previous algorithms
	 * @return number of points deleted by this algorithm
	 */
	public abstract int compress(Track inTrack, TrackDetails inDetails, MarkingData inMarkings);

	/**
	 * @return String to save in Settings
	 */
	public String getTotalSettingsString() {
		return (_active ? "x" : "o") + getSettingsString();
	}

	protected abstract String getSettingsString();

	public abstract CompressionMethodType getType();

	public abstract String getParam();

	/** Construct a specific CompressionMethod from a string fragment */
	public static CompressionMethod fromSettingsString(String inString)
	{
		if (inString == null || inString.isEmpty()) {
			return null;
		}
		CompressionMethod method = fromSettingsSubstring(inString.substring(1));
		if (method != null) {
			method.setActive(inString.charAt(0) == 'x');
		}
		return method;
	}

	private static CompressionMethod fromSettingsSubstring(String inString)
	{
		if (inString == null || inString.length() < 3) {
			return null;
		}
		if (DuplicatesMethod.recogniseString(inString)) {
			return new DuplicatesMethod();
		}
		if (NearbyFactorMethod.recogniseString(inString)) {
			return new NearbyFactorMethod(inString);
		}
		if (WackyPointsMethod.recogniseString(inString)) {
			return new WackyPointsMethod(inString);
		}
		if (SingletonsMethod.recogniseString(inString)) {
			return new SingletonsMethod(inString);
		}
		if (DouglasPeuckerMethod.recogniseString(inString)) {
			return new DouglasPeuckerMethod(inString);
		}
		if (NearbyDistMethod.recogniseString(inString)) {
			return new NearbyDistMethod(inString);
		}
		if (TooSlowMethod.recogniseString(inString)) {
			return new TooSlowMethod(inString);
		}
		if (TooFastMethod.recogniseString(inString)) {
			return new TooFastMethod(inString);
		}
		if (TooSoonMethod.recogniseString(inString)) {
			return new TooSoonMethod(inString);
		}
		if (SkiLiftsMethod.recogniseString(inString)) {
			return new SkiLiftsMethod();
		}
		return null;
	}

	protected static boolean recogniseString(String inString, CompressionMethodType inType) {
		return inString != null && inString.startsWith(inType.getKey());
	}

	public static boolean isPointDeleted(int inIndex, MarkingData inMarkings) {
		return inMarkings != null && inMarkings.isPointMarkedForDeletion(inIndex);
	}

	public static boolean isPointAtSegmentBoundary(int inIndex, TrackDetails inDetails, MarkingData inMarkings)
	{
		if (inDetails != null && (inDetails.isSegmentStart(inIndex) || inDetails.isSegmentEnd(inIndex))) {
			return true;
		}
		// Maybe it wasn't a segment boundary before but due to the deletion flags it now is
		for (int i=inIndex - 1; i >= 0; i--)
		{
			if (isPointDeleted(i, inMarkings))
			{
				if (inMarkings.isPointMarkedForSegmentBreak(i)) {
					return true;
				}
			}
			else if (!inDetails.isWaypoint(i))
			{
				// Found a track point before our point, so our point isn't the start
				return false;
			}
		}
		// Couldn't find any points
		return true;
	}

	protected static DataPoint getNextTrackPoint(Track inTrack, int inIndex, MarkingData inMarkings)
	{
		int index = inIndex + 1;
		while (index < inTrack.getNumPoints())
		{
			DataPoint point = inTrack.getPoint(index);
			if (!point.isWaypoint() && !inMarkings.isPointMarkedForDeletion(index)) {
				return point;
			}
			index++;
		}
		return null;
	}
}
