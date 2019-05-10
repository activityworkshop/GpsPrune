package tim.prune.gui.map;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Class to manage coordinate conversions and other stuff for maps
 */
public abstract class MapUtils
{
	/**
	 * Transform a longitude into an x coordinate
	 * @param inLon longitude in degrees
	 * @return scaled X value from 0 to 1
	 */
	public static double getXFromLongitude(double inLon)
	{
		return (inLon + 180.0) / 360.0;
	}

	/**
	 * Transform a latitude into a y coordinate
	 * @param inLat latitude in degrees
	 * @return scaled Y value from 0 to 1
	 */
	public static double getYFromLatitude(double inLat)
	{
		return (1 - Math.log(Math.tan(inLat * Math.PI / 180) + 1 / Math.cos(inLat * Math.PI / 180)) / Math.PI) / 2;
	}

	/**
	 * Transform an x coordinate into a longitude
	 * @param inX scaled X value from 0(-180deg) to 1(+180deg)
	 * @return longitude in degrees
	 */
	public static double getLongitudeFromX(double inX)
	{
		// Ensure x is really between 0 and 1 (to wrap longitudes)
		double x = ((inX % 1.0) + 1.0) % 1.0;
		// Note: First %1.0 restricts range to (-1,1), then +1.0 shifts to (0,2)
		// Finally, %1.0 to give (0,1)
		return x * 360.0 - 180.0;
	}

	/**
	 * Transform a y coordinate into a latitude
	 * @param inY scaled Y value from 0 to 1
	 * @return latitude in degrees
	 */
	public static double getLatitudeFromY(double inY)
	{
		double n = Math.PI * (1 - 2 * inY);
		return 180 / Math.PI * Math.atan(0.5 * (Math.exp(n) - Math.exp(-n)));
	}

	/**
	 * Tests whether there are any dark pixels in the image within the specified x,y rectangle
	 * @param inImage image to test
	 * @param inX left X coordinate
	 * @param inY bottom Y coordinate
	 * @param inWidth width of rectangle
	 * @param inHeight height of rectangle
	 * @param inTextColour colour of text
	 * @return true if the rectangle overlaps stuff too close to the given colour
	 */
	public static boolean overlapsPoints(BufferedImage inImage, int inX, int inY,
		int inWidth, int inHeight, Color inTextColour)
	{
		// each of the colour channels must be further away than this to count as empty
		final int BRIGHTNESS_LIMIT = 80;
		final int textRGB = inTextColour.getRGB();
		final int textLow = textRGB & 255;
		final int textMid = (textRGB >> 8) & 255;
		final int textHigh = (textRGB >> 16) & 255;
		try
		{
			// loop over x coordinate of rectangle
			for (int x=0; x<inWidth; x++)
			{
				// loop over y coordinate of rectangle
				for (int y=0; y<inHeight; y++)
				{
					int pixelColor = inImage.getRGB(inX + x, inY - y);
					// split into four components rgba
					int pixLow = pixelColor & 255;
					int pixMid = (pixelColor >> 8) & 255;
					int pixHigh = (pixelColor >> 16) & 255;
					//int fourthBit = (pixelColor >> 24) & 255; // alpha ignored
					// If colours are too close in any channel then it's an overlap
					if (Math.abs(pixLow-textLow) < BRIGHTNESS_LIMIT ||
						Math.abs(pixMid-textMid) < BRIGHTNESS_LIMIT ||
						Math.abs(pixHigh-textHigh) < BRIGHTNESS_LIMIT) {return true;}
				}
			}
		}
		catch (NullPointerException e) {
			// ignore null pointers, just return false
		}
		return false;
	}
}
