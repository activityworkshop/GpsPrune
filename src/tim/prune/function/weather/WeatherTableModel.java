package tim.prune.function.weather;

import javax.swing.table.AbstractTableModel;

import tim.prune.I18nManager;


/**
 * Table model for results of weather forecast
 */
public class WeatherTableModel extends AbstractTableModel
{
	/** Weather results */
	private WeatherResults _results;

	/** Row indices */
	public static final int ROW_DAY  = 0;
	public static final int ROW_DESC = 1;
	public static final int ROW_WIND = 2;
	public static final int ROW_ICON = 3;
	public static final int ROW_TEMP = 4;
	public static final int ROW_HUMID = 5;

	/** String for degrees Celsius */
	private static final String UNITS_DEGC = I18nManager.getText("units.degreescelsius.short");
	/** String for degrees Fahrenheit */
	private static final String UNITS_DEGF = I18nManager.getText("units.degreesfahrenheit.short");

	/**
	 * @return column count
	 */
	public int getColumnCount()
	{
		if (_results == null) {return 0;}
		return _results.getNumForecasts();
	}

	/**
	 * @param inColNum column number
	 * @return column label for given column
	 */
	public String getColumnName(int inColNum)
	{
		if (_results != null && inColNum >= 0 && inColNum < getColumnCount())
		{
			SingleForecast forecast = _results.getForecast(inColNum);
			if (!forecast.hasTimes() || forecast.getTimeFrom().startsWith("00")) {
				return forecast.getDate();
			}
			return forecast.getTimeFrom();
		}
		return "";
	}

	/**
	 * @return number of rows
	 */
	public int getRowCount()
	{
		return 6;
	}

	/** @return true if there are no columns */
	public boolean isEmpty()
	{
		return getColumnCount() == 0;
	}

	/**
	 * @param inRowNum row number
	 * @param inColNum column number
	 * @return cell entry at given row and column
	 */
	public Object getValueAt(int inRowNum, int inColNum)
	{
		if (inColNum < 0 || inColNum >= getColumnCount()) {return "";}
		SingleForecast forecast = _results.getForecast(inColNum);
		if (forecast != null)
		{
			switch (inRowNum)
			{
				case ROW_DAY:  {
					final String dayDesc = forecast.getDayDesc() == null ? "now" : forecast.getDayDesc();
					return buildDisplayString(null, I18nManager.getText("dialog.weather.day." + dayDesc));
				}
				case ROW_DESC: return buildDisplayString(null, forecast.getDescription());
				case ROW_WIND: return buildDisplayString(I18nManager.getText("dialog.weather.wind"), forecast.getWindDescription());
				case ROW_ICON: return forecast.getImageName();
				case ROW_TEMP: return buildDisplayString(I18nManager.getText("dialog.weather.temp"), forecast.getTemps()
					+ (_results.isCelsius() ? UNITS_DEGC : UNITS_DEGF));
				case ROW_HUMID: return buildDisplayString(I18nManager.getText("dialog.weather.humidity"), forecast.getHumidity());
			}
		}
		return "";
	}

	/**
	 * Build a html string from the given title and value
	 */
	private static final String buildDisplayString(String inTitle, String inValue)
	{
		if (inValue == null) {return null;}
		return "<html>" + (inTitle == null ? "" : (inTitle + ":&nbsp;"))
			+ "<big>" + inValue.replaceAll(" ", "&nbsp;") + "</big></html>";
	}

	/**
	 * Set the results
	 * @param inResults weather results including all forecasts
	 */
	public void setResults(WeatherResults inResults)
	{
		_results = inResults;
		fireTableStructureChanged();
	}

	/**
	 * Clear the list of forecasts
	 */
	public void clear()
	{
		setResults(null);
	}
}
