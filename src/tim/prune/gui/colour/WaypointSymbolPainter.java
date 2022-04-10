package tim.prune.gui.colour;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

/**
 * Class to paint a given waypoint symbol with a given colour
 */
public abstract class WaypointSymbolPainter
{
	/**
	 * Paint the given icon with the given colour
	 * @param inIcon icon loaded from file (just black/white/transparent)
	 * @param inColor colour with which to paint
	 * @return painted image
	 */
	public static Image paintSymbol(ImageIcon inIcon, Color inColor)
	{
		if (inIcon == null || inIcon.getImage() == null) {
			return null;
		}
		Image srcImage = inIcon.getImage();
		if (inColor == null) {
			return srcImage;
		}
		final int width = srcImage.getWidth(null), height = srcImage.getHeight(null);
		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		result.getGraphics().drawImage(srcImage, 0, 0, width, height, null);
		for (int x=0; x<width; x++) {
			for (int y=0; y<height; y++) {
				result.setRGB(x, y, paintPixel(result.getRGB(x, y), inColor));
			}
		}
		return result;
	}

	/**
	 * @param pixelRgb current pixel colour
	 * @param inColor colour with which to paint
	 * @return modified pixel colour
	 */
	private static int paintPixel(int pixelRgb, Color inColor)
	{
		final int alpha = pixelRgb & 0xFF000000;
		final int red = (((pixelRgb & 0x00FF0000) * inColor.getRed() / 255) & 0x00FF0000);
		final int green = (((pixelRgb & 0x0000FF00) * inColor.getGreen() / 255) & 0x0000FF00);
		final int blue = (((pixelRgb & 0x000000FF) * inColor.getBlue() / 255) & 0x000000FF);
		return alpha | red | green | blue;
	}
}
