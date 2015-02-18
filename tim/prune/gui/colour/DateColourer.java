package tim.prune.gui.colour;

import java.awt.Color;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import tim.prune.data.DataPoint;
import tim.prune.data.Timestamp;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;

/**
 * Point colourer giving a different colour to each date
 * Uses the system timezone so may give funny results for
 * data from other timezones (eg far-away holidays)
 */
public class DateColourer extends DiscretePointColourer
{
	// Doesn't really matter what format is used here, as long as dates are different
	private static final DateFormat DEFAULT_DATE_FORMAT = DateFormat.getDateInstance();

	/**
	 * Constructor
	 * @param inStartColour start colour of scale
	 * @param inEndColour end colour of scale
	 * @param inWrapLength number of unique colours before wrap
	 */
	public DateColourer(Color inStartColour, Color inEndColour, int inWrapLength)
	{
		super(inStartColour, inEndColour, inWrapLength);
	}

	/**
	 * Calculate the colours for each of the points in the given track
	 * @param inTrackInfo track info object
	 */
	@Override
	public void calculateColours(TrackInfo inTrackInfo)
	{
		// initialise the array to the right size
		Track track = inTrackInfo == null ? null : inTrackInfo.getTrack();
		final int numPoints = track == null ? 0 : track.getNumPoints();
		init(numPoints);
		// Make a hashmap of the already-used dates
		HashMap<String, Integer> usedDates = new HashMap<String, Integer>(20);
		// Also store the previous one, because they're probably consecutive
		String prevDate = null;
		int prevIndex = -1;

		// loop over track points
		int dayIndex = -1;
		for (int i=0; i<numPoints; i++)
		{
			DataPoint p = track.getPoint(i);
			if (p != null && !p.isWaypoint())
			{
				dayIndex = 0; // default index 0 will be used if no date found
				String date = getDate(p.getTimestamp());
				if (date != null)
				{
					// Check if it's the previous one
					if (prevDate != null && date.equals(prevDate)) {
						dayIndex = prevIndex;
					}
					else
					{
						// Look up in the hashmap to see if it's been used before
						Integer foundIndex = usedDates.get(date);
						if (foundIndex == null)
						{
							// not been used before, so add it
							dayIndex = usedDates.size() + 1;
							usedDates.put(date, dayIndex);
						}
						else
						{
							// found it
							dayIndex = foundIndex;
						}
						// Remember what we've got for the next point
						prevDate = date;
						prevIndex = dayIndex;
					}
				}
				// if date is null (no timestamp or invalid) then dayIndex remains 0
				setColour(i, dayIndex);
			}
		}
		// generate the colours needed
		generateDiscreteColours(usedDates.size() + 1);
	}


	/**
	 * Find which date (in the system timezone) the given timestamp falls on
	 * @param inTimestamp timestamp
	 * @return String containing description of date, or null
	 */
	private static String getDate(Timestamp inTimestamp)
	{
		if (inTimestamp == null || !inTimestamp.isValid()) {
			return null;
		}
		Calendar cal = inTimestamp.getCalendar();
		// use system time zone
		cal.setTimeZone(TimeZone.getDefault());
		return DEFAULT_DATE_FORMAT.format(cal.getTime());
	}
}
