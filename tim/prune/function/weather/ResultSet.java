package tim.prune.function.weather;

/**
 * Class to hold a set of (up to six) weather results,
 * so that they don't have to be downloaded again
 */
public class ResultSet
{
	/** Array of six results */
	private WeatherResults[] _results = new WeatherResults[6];
	/** Location id for which these results apply */
	private String _locationId = null;

	/**
	 * Clear the array, forget all results
	 */
	private void clear()
	{
		for (int i=0; i<6; i++) {
			_results[i] = null;
		}
	}

	/**
	 * Get the specified weather results, if available
	 * @param inLocationId location id
	 * @param inCurrent true to get the current weather
	 * @param inDaily true to get the daily forecast
	 * @param inHourly true to get the three-hourly forecast
	 * @param inCelsius true to get celsius
	 * @return weather results, or null if not available
	 */
	public WeatherResults getWeather(String inLocationId,
		boolean inCurrent, boolean inDaily, boolean inHourly, boolean inCelsius)
	{
		// Check location
		if (inLocationId == null || _locationId == null || !inLocationId.equals(_locationId)) {
			return null;
		}
		// check forecast type
		final int numTypesGiven = (inCurrent ? 1 : 0) + (inDaily ? 1 : 0) + (inHourly ? 1 : 0);
		if (numTypesGiven != 1) {
			System.err.println("getWeather, numtypesgiven = " + numTypesGiven);
			return null; // can't ask for more or less than one type
		}
		// Pull out from array
		final int index = (inCurrent ? 0 : (inDaily ? 2 : 4)) + (inCelsius ? 1 : 0);
		return _results[index];
	}

	/**
	 * Store the given weather results
	 * @param inResults results object
	 * @param inLocationId location id
	 * @param inCurrent true if this is the current weather
	 * @param inDaily true if this is the daily forecast
	 * @param inHourly true if this is the three-hourly forecast
	 * @param inCelsius true if numbers are celsius
	 */
	public void setWeather(WeatherResults inResults, String inLocationId,
		boolean inCurrent, boolean inDaily, boolean inHourly, boolean inCelsius)
	{
		// Check location
		if (inLocationId == null || inLocationId.equals("")) {
			return;
		}
		if (_locationId == null || !inLocationId.equals(_locationId))
		{
			// coordinates have changed
			clear();
			_locationId = inLocationId;
		}
		// check forecast type
		final int numTypesGiven = (inCurrent ? 1 : 0) + (inDaily ? 1 : 0) + (inHourly ? 1 : 0);
		if (numTypesGiven != 1) {
			System.err.println("setWeather, numtypesgiven = " + numTypesGiven);
			return; // can't set more or less than one type
		}
		// Store in array
		final int index = (inCurrent ? 0 : (inDaily ? 2 : 4)) + (inCelsius ? 1 : 0);
		_results[index] = inResults;
	}
}
