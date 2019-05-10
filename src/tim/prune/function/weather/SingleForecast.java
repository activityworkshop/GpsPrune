package tim.prune.function.weather;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Class to represent a weather forecast
 * for a single day or a 3-hour period
 */
public class SingleForecast
{
	private String _date = null;
	private String _dayDescKey = null;
	private String _timeFrom = null, _timeTo = null;
	private String _imageName = null;
	private String _desc = null;
	private String _tempString = null;
	private String _humidity = null;
	private String _windDesc = null;

	/** For getting today's and tomorrow's dates */
	private static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

	/** Set the time of the forecast */
	public void setTime(String inDate, String inTimeFrom, String inTimeTo)
	{
		_date = inDate;
		if (inTimeFrom != null && inTimeFrom.length() > 10
			&& inTimeTo != null && inTimeTo.length() > 10)
		{
			_date = inTimeFrom.substring(0, 10);
			_timeFrom = inTimeFrom.substring(11, 16);
			_timeTo   = inTimeTo.substring(11, 16);
		}
		_dayDescKey = getDayDescriptionKey(_date);
		// System.out.println(_date + " is " + _dayDescKey);
	}

	/**
	 * Set the symbol details
	 */
	public void setSymbol(String inNumber, String inImageCode, String inDesc)
	{
		_imageName = getIconName(inNumber, inImageCode);
		// System.out.println("For number " + inNumber + "(" + inDesc + ") and code " + inImageCode + ", the symbol is " + _imageName);
		_desc = inDesc;
	}

	/**
	 * Set the minimum and maximum temperatures (will be rounded to nearest int)
	 */
	public void setTemps(String inMin, String inMax)
	{
		String tempMin = null, tempMax = null;
		try {
			tempMin = "" + Math.round(Double.parseDouble(inMin));
		} catch (Exception e) {}; // tempMin stays null if temp can't be parsed
		try {
			tempMax = "" + Math.round(Double.parseDouble(inMax));
		} catch (Exception e) {}; // tempMax stays null if temp can't be parsed

		_tempString = tempMin;
		if (tempMin != null && tempMax != null) {
			if (!tempMin.equals(tempMax))
			{
				if (tempMin.charAt(0) == '-' && tempMax.charAt(0) != '-' && tempMax.charAt(0) != '0') {
					// min temp is negative, max is positive, so add a + to the max
					tempMax = "+" + tempMax;
				}
				_tempString = tempMin  + " &mdash; " + tempMax;
			}
		}
		else if (tempMax != null) {
			_tempString = tempMax;
		}
	}

	/** Set humidity */
	public void setHumidity(String inHumidity) {
		_humidity = inHumidity;
	}

	/** Set description of wind */
	public void setWindDesc(String inDesc) {
		_windDesc = inDesc;
	}

	/**
	 * Get the name of the image file for the given weather report
	 * @param inCode numeric three-digit code, as string
	 * @param inImage filename as given by openweather (just used for day/night)
	 * @return image file using GpsPrune's icons
	 */
	public static String getIconName(String inCode, String inImage)
	{
		final boolean daytime = inImage == null || inImage.length() != 3 || inImage.charAt(2) != 'n';
		final char leadDigit = (inCode == null || inCode.equals("")) ? '0' : inCode.charAt(0);
		String iconName = null;
		switch (leadDigit)
		{
			case '2':	return "storm.png";
			case '3':	return "lightrain.png";
			case '5':
				iconName = "rain.png";
				if (inCode.equals("500")) {iconName = "lightrain.png";}
				else if (inCode.equals("511")) {iconName = "hail.png";}
				break;
			case '6':	return "snow.png";
			case '7':	return "fog.png";
			case '8':
				iconName = daytime ? "clouds-day.png" : "clouds-night.png";
				if (inCode.equals("800")) {iconName = daytime ? "clear-day.png" : "clear-night.png";}
				else if (inCode.equals("804")) {iconName = "clouds.png";}
				break;
			case '9':
				iconName = "extreme.png";
				if (inCode.equals("906")) {iconName = "hail.png";}
				break;
		}
		return iconName;
	}

	/**
	 * MAYBE: Maybe split off into separate DateFunctions class?
	 * @param inDate date
	 * @return day description, such as "today" or "saturday"
	 */
	private static String getDayDescriptionKey(String inDate)
	{
		if (inDate == null || inDate.length() != 10) {return null;}
		Calendar cal = Calendar.getInstance();
		String todaysDate = DATE_FORMATTER.format(cal.getTime());
		if (inDate.equals(todaysDate)) {return "today";}
		cal.add(Calendar.DATE, 1);
		String tomorrowsDate = DATE_FORMATTER.format(cal.getTime());
		if (inDate.equals(tomorrowsDate)) {return "tomorrow";}
		// Construct a date with this string and find out its day
		try
		{
			cal.setTime(DATE_FORMATTER.parse(inDate));
			switch (cal.get(Calendar.DAY_OF_WEEK))
			{
				case Calendar.MONDAY   : return "monday";
				case Calendar.TUESDAY  : return "tuesday";
				case Calendar.WEDNESDAY : return "wednesday";
				case Calendar.THURSDAY : return "thursday";
				case Calendar.FRIDAY   : return "friday";
				case Calendar.SATURDAY : return "saturday";
				case Calendar.SUNDAY   : return "sunday";
			}
		}
		catch (ParseException pe) {}

		return "other";
	}

	/** @return true if there are times present, not just a date */
	public boolean hasTimes() {
		return _timeFrom != null && _timeTo != null;
	}
	/** @return temperature range */
	public String getTemps() {
		return _tempString;
	}

	/** @return date */
	public String getDate() {return _date;}
	/** @return time from */
	public String getTimeFrom() {return _timeFrom;}
	/** @return time to */
	public String getTimeTo() {return _timeTo;}
	/** @return day description */
	public String getDayDesc() {return _dayDescKey;}

	/** @return image name */
	public String getImageName() {return _imageName;}
	/** @return description */
	public String getDescription() {return _desc;}

	/** @return humidity */
	public String getHumidity() {return _humidity;}
	/** @return wind description */
	public String getWindDescription() {return _windDesc;}
}
