package tim.prune.data;

import tim.prune.I18nManager;

/**
 * Class to represent a time difference, like the difference between two Timestamp objects,
 * and methods for representing and displaying them.
 */
public class TimeDifference
{
	private long _totalSeconds = 0L;
	private int _seconds = 0;
	private int _minutes = 0;
	private int _hours = 0;
	private String _description = null;


	/**
	 * Constructor using long
	 * @param inNumSeconds number of seconds time difference
	 */
	public TimeDifference(long inNumSeconds)
	{
		_totalSeconds = inNumSeconds;
		if (inNumSeconds < 0) {inNumSeconds = -inNumSeconds;}
		_hours = (int) (inNumSeconds / 60 / 60);
		_minutes = (int) (inNumSeconds / 60 - _hours * 60);
		_seconds = (int) (inNumSeconds % 60);
	}


	/**
	 * Constructor giving each field separately
	 * @param inHours number of hours
	 * @param inMinutes number of minutes
	 * @param inSeconds number of seconds
	 * @param inPositive true for positive time difference
	 */
	public TimeDifference(int inHours, int inMinutes, int inSeconds, boolean inPositive)
	{
		// Check for negative values?
		_hours = inHours;
		_minutes = inMinutes;
		_seconds = inSeconds;
		_totalSeconds = inHours * 3600L + inMinutes * 60L + inSeconds;
		if (!inPositive) {_totalSeconds = -_totalSeconds;}
	}


	/**
	 * @return total number of seconds time difference
	 */
	public long getTotalSeconds()
	{
		return _totalSeconds;
	}

	/**
	 * @return number of hours
	 */
	public int getNumHours()
	{
		return _hours;
	}

	/**
	 * @return number of minutes
	 */
	public int getNumMinutes()
	{
		return _minutes;
	}

	/**
	 * @return number of seconds
	 */
	public int getNumSeconds()
	{
		return _seconds;
	}

	/**
	 * @return true if time difference positive
	 */
	public boolean getIsPositive()
	{
		return _totalSeconds >= 0L;
	}


	/**
	 * Build a String to describe the time duration
	 * @return time as a string, days, hours, mins, secs as appropriate
	 */
	public String getDescription()
	{
		if (_description != null) {return _description;}
		StringBuffer buffer = new StringBuffer();
		boolean started = false;
		// hours
		if (_hours > 0)
		{
			buffer.append(_hours).append(' ').append(I18nManager.getText("display.range.time.hours"));
			started = true;
		}
		// minutes
		if (_minutes > 0)
		{
			if (started) {buffer.append(", ");}
			else {started = true;}
			buffer.append(_minutes).append(' ').append(I18nManager.getText("display.range.time.mins"));
		}
		// seconds
		if (_seconds > 0 || !started)
		{
			if (started) {buffer.append(", ");}
			buffer.append(_seconds).append(' ').append(I18nManager.getText("display.range.time.secs"));
		}
		_description = buffer.toString();
		return _description;
	}

}
