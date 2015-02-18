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
		if (_previousInfo != null && _previousInfo.isSameDate(inDate))
		{
			// found it
			_previousInfo.incrementCount();
		}
		else
		{
			// loop through list, seeing if date already present
			boolean foundDate = false;
			for (DateInfo info : _infoList)
			{
				if (info.isSameDate(inDate))
				{
					info.incrementCount();
					_previousInfo = info;
					foundDate = true;
					break;
				}
			}
			// create new info if necessary
			if (!foundDate)
			{
				_previousInfo = new DateInfo(inDate);
				_previousInfo.incrementCount();
				_infoList.add(_previousInfo);
				_hasBeenSorted = false;
			}
		}
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
	 * not used, can be removed
	 * @return true if any points without dates were found
	 */
	public boolean hasDatelessPoints()
	{
		if (_infoList.isEmpty()) {return false;}
		sort();
		DateInfo firstInfo = _infoList.get(0);
		return (firstInfo != null && firstInfo.isDateless() && firstInfo.getPointCount() > 0);
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
