package tim.prune.function.weather;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * JUnit tests for weather icons
 */
class SingleForecastTest
{

	@Test
	void testWeatherIcons()
	{
		testIconName(null, "100", "");
		testIconName("storm.png", "200", null);
		testIconName("storm.png", "204", "");
		testIconName("lightrain.png", "300", null);
		testIconName("lightrain.png", "301", null);
		testIconName(null, "400", null);
		testIconName("lightrain.png", "500", null);
		testIconName("rain.png", "501", null);
		testIconName("rain.png", "599", null);
		testIconName("hail.png", "511", null);
		testIconName("snow.png", "600", null);
		testIconName("fog.png", "700", null);
		testIconName("clear-day.png", "800", null);
		testIconName("clear-day.png", "800", "");
		testIconName("clear-day.png", "800", "01d");
		testIconName("clear-night.png", "800", "01n");
		testIconName("clouds-day.png", "802", "01d");
		testIconName("clouds-night.png", "802", "01n");
		testIconName("clouds.png", "804", "01n");
		testIconName("extreme.png", "900", "01d");
		testIconName("hail.png", "906", "01n");
	}

	/**
	 * Test getting an icon name according to code and image
	 */
	private static void testIconName(String inExpect, String inCode, String inImage)
	{
		String icon = SingleForecast.getIconName(inCode, inImage);
		assertEquals(inExpect, icon, showString(inCode) + ", " + showString(inImage));
	}

	private static String showString(String inString)
	{
		return inString == null ? "null" : inString;
	}
}
