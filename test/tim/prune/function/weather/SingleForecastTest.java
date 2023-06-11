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
		testIconName("storm", "200", null);
		testIconName("storm", "204", "");
		testIconName("lightrain", "300", null);
		testIconName("lightrain", "301", null);
		testIconName(null, "400", null);
		testIconName("lightrain", "500", null);
		testIconName("rain", "501", null);
		testIconName("rain", "599", null);
		testIconName("hail", "511", null);
		testIconName("snow", "600", null);
		testIconName("fog", "700", null);
		testIconName("clear-day", "800", null);
		testIconName("clear-day", "800", "");
		testIconName("clear-day", "800", "01d");
		testIconName("clear-night", "800", "01n");
		testIconName("clouds-day", "802", "01d");
		testIconName("clouds-night", "802", "01n");
		testIconName("clouds", "804", "01n");
		testIconName("extreme", "900", "01d");
		testIconName("hail", "906", "01n");
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
