package tim.prune.drew.jpeg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class to hold a collection of Jpeg data segments
 * Each marker represents a list of multiple byte arrays
 * Based on Drew Noakes' Metadata extractor at http://drewnoakes.com
 */
public class JpegSegmentData
{
	/** A map of byte[], keyed by the segment marker */
	private final HashMap _segmentDataMap;


	/**
	 * Constructor for an empty collection
	 */
	public JpegSegmentData()
	{
		_segmentDataMap = new HashMap(10);
	}

	/**
	 * Add a segment to the collection
	 * @param inSegmentMarker marker byte
	 * @param inSegmentBytes data of segment
	 */
	public void addSegment(byte inSegmentMarker, byte[] inSegmentBytes)
	{
		// System.out.println("Adding segment: " + inSegmentMarker);
		List segmentList = getOrCreateSegmentList(inSegmentMarker);
		segmentList.add(inSegmentBytes);
	}


	/**
	 * Get the first segment with the given marker
	 * @param inSegmentMarker marker byte
	 * @return first segment with that marker
	 */
	public byte[] getSegment(byte inSegmentMarker)
	{
		return getSegment(inSegmentMarker, 0);
	}


	/**
	 * Get the nth segment with the given marker
	 * @param inSegmentMarker marker byte
	 * @param inOccurrence occurrence to get, starting at 0
	 * @return byte array from specified segment
	 */
	public byte[] getSegment(byte inSegmentMarker, int inOccurrence)
	{
		final List segmentList = getSegmentList(inSegmentMarker);

		if (segmentList==null || segmentList.size()<=inOccurrence)
			return null;
		else
			return (byte[]) segmentList.get(inOccurrence);
	}


	/**
	 * Get the number of segments with the given marker
	 * @param inSegmentMarker marker byte
	 * @return number of segments
	 */
	public int getSegmentCount(byte inSegmentMarker)
	{
		final List segmentList = getSegmentList(inSegmentMarker);
		if (segmentList == null)
			return 0;
		else
			return segmentList.size();
	}


	/**
	 * Get the list of segments with the given marker
	 * @param inSegmentMarker marker byte
	 * @return list of segments
	 */
	private List getSegmentList(byte inSegmentMarker)
	{
		return (List)_segmentDataMap.get(new Byte(inSegmentMarker));
	}


	/**
	 * Get the specified segment if it exists, otherwise create new one
	 * @param inSegmentMarker marker byte
	 * @return list of segments
	 */
	private List getOrCreateSegmentList(byte inSegmentMarker)
	{
		List segmentList = null;
		Byte key = new Byte(inSegmentMarker);
		if (_segmentDataMap.containsKey(key))
		{
			// list already exists
			segmentList = (List)_segmentDataMap.get(key);
		}
		else
		{
			// create new list and add it
			segmentList = new ArrayList();
			_segmentDataMap.put(key, segmentList);
		}
		return segmentList;
	}
}
