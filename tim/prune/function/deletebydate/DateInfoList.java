package tim.prune.function.deletebydate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * List of date info objects for use by the table model
 */
public class DateInfoList
{
	/** list of info about points according to date */
	private List<DateInfo> _infoList = new ArrayList<DateInfo>();
	/** previously used dateinfo object to reduce list searching */
	private DateInfo _previousInfo = null;
	/** true if the list has been sorted, false otherwise */
	private boolean _hasBeenSorted = false;


	/**
	 * Add a point to the corresponding dateinfo
	 * @param inDate date of current point, or null if no timestamp
	 */
	public void addPoint(Date inDate)
	{
		DateInfo currentInfo = null;
		if (_previousInfo != null && _previousInfo.isSameDate(inDate))
		{
			// found it
			currentInfo = _previousInfo;
		}
		else
		{
			// loop through list, to see if date already present
			for (DateInfo info : _infoList)
			{
				if (info.isSameDate(inDate))
				{
					currentInfo = info;
					break;
				}
			}
			// create new info if necessary
			if (currentInfo == null)
			{
				currentInfo = new DateInfo(inDate);
				_infoList.add(currentInfo);
				_hasBeenSorted = false;
			}
			_previousInfo = currentInfo;
		}
		// Now we've identified the current info or created a new one
		currentInfo.incrementCount();
	}

	/**
	 * Clear the whole list
	 */
	public void clearAll()
	{
		_infoList.clear();
		_previousInfo = null;
		_hasBeenSorted = true;
	}

	/**
	 * @return number of entries in the list, including dateless points
	 */
	public int getNumEntries()
	{
		return _infoList.size();
	}

	/**
	 * @return the total number of points found, which should match the track size
	 */
	public int getTotalNumPoints()
	{
		int total = 0;
		for (DateInfo info : _infoList) {
			total += info.getPointCount();
		}
		return total;
	}

	/**
	 * Sort the info list by ascending timestamps
	 */
	private void sort()
	{
		if (!_hasBeenSorted)
		{
			Collections.sort(_infoList);
			_hasBeenSorted = true;
		}
	}

	/**
	 * Get the DateInfo object at the given index
	 * @param inIndex index in (sorted) list
	 * @return corresponding object (may throw exception if out of range)
	 */
	public DateInfo getDateInfo(int inIndex)
	{
		sort();
		return _infoList.get(inIndex);
	}
}
