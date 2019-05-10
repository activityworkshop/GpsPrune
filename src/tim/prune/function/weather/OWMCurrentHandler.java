package tim.prune.function.weather;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * XML handler for dealing with the XML of current weather reports
 * returned from openweathermap.org (forecasts have different structure)
 */
public class OWMCurrentHandler extends DefaultHandler
{
	/** The location name */
	private String _locName = null;
	/** The location id */
	private String _locId = null;
	/** The last update time */
	private String _updateTime = null;
	/** Sunrise and sunset times */
	private String _sunriseTime = null, _sunsetTime = null;
	/** The currently open forecast */
	private SingleForecast _forecast = new SingleForecast();


	/**
	 * React to the start of an XML tag
	 */
	public void startElement(String inUri, String inLocalName, String inTagName,
		Attributes inAttributes) throws SAXException
	{
		if (inTagName.equals("city")) {
			_locName = inAttributes.getValue("name");
			_locId   = inAttributes.getValue("id");
		}
		else if (inTagName.equals("weather")) {
			// numeric code, owm image name, description
			_forecast.setSymbol(inAttributes.getValue("number"), inAttributes.getValue("icon"), inAttributes.getValue("value"));
		}
		else if (inTagName.equals("speed")) {
			_forecast.setWindDesc(inAttributes.getValue("name"));
		}
		else if (inTagName.equals("temperature"))
		{
			String currTemp = inAttributes.getValue("value");
			_forecast.setTemps(currTemp, currTemp);
			// We can ignore the min and max here
		}
		else if (inTagName.equals("humidity")) {
			_forecast.setHumidity(inAttributes.getValue("value") + inAttributes.getValue("unit"));
		}
		else if (inTagName.equals("lastupdate")) {
			_updateTime = inAttributes.getValue("value");
		}
		else if (inTagName.equals("sun"))
		{
			_sunriseTime = inAttributes.getValue("rise");
			_sunsetTime  = inAttributes.getValue("set");
		}

		super.startElement(inUri, inLocalName, inTagName, inAttributes);
	}

	/** @return location name of forecast */
	public String getLocationName() {
		return _locName;
	}

	/** @return location id of forecast */
	public String getLocationId() {
		return _locId;
	}

	/** @return update time of report */
	public String getUpdateTime() {
		return _updateTime;
	}

	/** @return current weather conditions */
	public SingleForecast getCurrentWeather() {
		return _forecast;
	}

	/** @return sunrise time as 2013-07-25T03:55:14 */
	public String getSunriseTime() {
		return _sunriseTime;
	}
	/** @return sunset time as 2013-07-25T19:07:25 */
	public String getSunsetTime() {
		return _sunsetTime;
	}
}
