package tim.prune.function.weather;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * XML handler for dealing with the XML of weather forecasts
 * returned from openweathermap.org (current weather has different structure)
 */
public class OWMForecastHandler extends DefaultHandler
{
	private String _value = null;
	/** The location name */
	private String _locName = null;
	/** The forecast update time */
	private String _updateTime = null;
	/** The currently open forecast */
	private SingleForecast _forecast = null;
	/** List of all the forecasts found so far */
	private ArrayList<SingleForecast> _forecastList = new ArrayList<SingleForecast>();


	/**
	 * React to the start of an XML tag
	 */
	public void startElement(String inUri, String inLocalName, String inTagName,
		Attributes inAttributes) throws SAXException
	{
		if (inTagName.equals("time")) { // start of a new forecast
			_forecast = new SingleForecast();
			// date, timefrom, timeto
			_forecast.setTime(inAttributes.getValue("day"), inAttributes.getValue("from"), inAttributes.getValue("to"));
		}
		else if (inTagName.equals("symbol")) {
			// numeric code, owm image name, description
			_forecast.setSymbol(inAttributes.getValue("number"), inAttributes.getValue("var"), inAttributes.getValue("name"));
		}
		else if (inTagName.equals("windSpeed")) {
			_forecast.setWindDesc(inAttributes.getValue("name"));
		}
		else if (inTagName.equals("temperature")) {
			_forecast.setTemps(inAttributes.getValue("min"), inAttributes.getValue("max"));
		}
		else if (inTagName.equals("humidity")) {
			_forecast.setHumidity(inAttributes.getValue("value") + inAttributes.getValue("unit"));
		}
		_value = null;
		super.startElement(inUri, inLocalName, inTagName, inAttributes);
	}

	/**
	 * React to the end of an XML tag
	 */
	public void endElement(String inUri, String inLocalName, String inTagName)
	throws SAXException
	{
		if (inTagName.equals("name")) {
			_locName = _value;
		}
		else if (inTagName.equals("lastupdate")) {
			_updateTime = _value;
		}
		else if (inTagName.equals("time"))
		{
			// End of a time tag, add the current forecast to the list
			_forecastList.add(_forecast);
		}
		super.endElement(inUri, inLocalName, inTagName);
	}

	/**
	 * React to characters received inside tags
	 */
	public void characters(char[] inCh, int inStart, int inLength)
	throws SAXException
	{
		String value = new String(inCh, inStart, inLength);
		_value = (_value==null?value:_value+value);
		super.characters(inCh, inStart, inLength);
	}

	/** @return location name of forecast */
	public String getLocationName() {
		return _locName;
	}

	/** @return update time of forecast */
	public String getUpdateTime() {
		return _updateTime;
	}

	/**
	 * @return the list of forecasts
	 */
	public ArrayList<SingleForecast> getForecasts() {
		return _forecastList;
	}
}
