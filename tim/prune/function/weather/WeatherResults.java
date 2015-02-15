package tim.prune.function.weather;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import tim.prune.gui.DisplayUtils;


/**
 * Model for results of weather forecast from openweathermap.org
 */
public class WeatherResults
{
	/** List of forecasts */
	private ArrayList<SingleForecast> _forecastList = new ArrayList<SingleForecast>();
	/** Flag whether the units are metric (Celsius) or not (Fahrenheit) */
	private boolean _tempsCelsius = true;
	/** Location name */
	private String _locationName = null;
	/** Last update timestamp */
	private String _updateTime = null;
	/** Sunrise and sunset times as HH:MM */
	private String _sunriseTime = null, _sunsetTime = null;


	/**
	 * Add a single forecast to this model (for the current weather)
	 * @param inResults current results
	 */
	public void setForecast(SingleForecast inResults)
	{
		_forecastList.clear();
		if (inResults != null) {
			_forecastList.add(inResults);
		}
	}

	/**
	 * Add a list of forecasts to this model
	 * @param inList list of forecasts to add
	 */
	public void setForecasts(ArrayList<SingleForecast> inList)
	{
		_forecastList.clear();
		if (inList != null && inList.size() > 0) {
			_forecastList.addAll(inList);
		}
	}

	/** @return the number of forecasts */
	public int getNumForecasts()
	{
		return _forecastList.size();
	}

	/**
	 * @param inIndex index of forecast starting from 0
	 * @return the specified forecast
	 */
	public SingleForecast getForecast(int inIndex)
	{
		if (inIndex < 0 || inIndex >= getNumForecasts()) {
			return null;
		}
		return _forecastList.get(inIndex);
	}

	/**
	 * Clear the list of forecasts
	 */
	public void clear()
	{
		_forecastList.clear();
		_sunriseTime = _sunsetTime = null;
		_updateTime = null;
	}

	/**
	 * @param inCelsius true for celsius, false for fahrenheit
	 */
	public void setTempsCelsius(boolean inCelsius)
	{
		_tempsCelsius = inCelsius;
	}

	/** @return true if this forecast uses Celsius */
	public boolean isCelsius() {
		return _tempsCelsius;
	}

	/**
	 * Set the sunrise and sunset times (only from current weather, not from forecast)
	 * @param inRiseTime sunrise time as YYYY-MM-DDThh:mm:ss
	 * @param inSetTime  sunset  time as YYYY-MM-DDThh:mm:ss
	 */
	public void setSunriseSunsetTimes(String inRiseTime, String inSetTime)
	{
		_sunriseTime = _sunsetTime = null;
		if (inRiseTime != null && inRiseTime.length() == 19
			&& inSetTime != null && inSetTime.length() == 19)
		{
			// Convert from UTC to system's time zone (not necessarily target's time zone!)
			_sunriseTime = convertToLocalTimezone(inRiseTime.substring(11, 16));
			_sunsetTime  = convertToLocalTimezone(inSetTime.substring(11, 16));
		}
	}

	/** @return sunrise time as HH:MM */
	public String getSunriseTime() {
		return _sunriseTime;
	}
	/** @return sunset time as HH:MM */
	public String getSunsetTime() {
		return _sunsetTime;
	}

	/** @param inName location name */
	public void setLocationName(String inName) {
		_locationName = inName;
	}

	/** @return location name */
	public String getLocationName() {
		return _locationName;
	}

	/** @param inTime timestamp of forecast */
	public void setUpdateTime(String inTime) {
		_updateTime = inTime;
	}

	/** @return timestamp of last update */
	public String getUpdateTime()
	{
		return _updateTime;
	}

	/**
	 * Convert the given UTC time (HH:MM) into current timezone of computer
	 * @param inTimeString sunrise/sunset time in UTC (HH:MM)
	 * @return time in this timezone (HH:MM)
	 */
	private static String convertToLocalTimezone(String inTimeString)
	{
		if (inTimeString != null && inTimeString.length() == 5)
		{
			try
			{
				final int hour = Integer.parseInt(inTimeString.substring(0, 2));
				final int min  = Integer.parseInt(inTimeString.substring(3));
				Calendar utcCal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
				utcCal.set(Calendar.HOUR_OF_DAY, hour);
				utcCal.set(Calendar.MINUTE, min);
				// Make a second calendar in the current time zone and apply values
				Calendar currCal = Calendar.getInstance();
				currCal.setTimeInMillis(utcCal.getTimeInMillis());
				return DisplayUtils.makeTimeString(currCal.get(Calendar.HOUR_OF_DAY),
					currCal.get(Calendar.MINUTE));
			}
			catch (NumberFormatException e) {} // ignore, just drop through
		}
		// Couldn't be parsed / converted
		return inTimeString;
	}
}
