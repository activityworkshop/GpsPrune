package tim.prune.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

import javax.swing.ImageIcon;

/**
 * Class for providing generic image processing functions
 */
public abstract class ImageUtils
{
	private static final float SMOOTH_FACTOR = 0.008f;
	private static ConvolveOp CONVOLVER = null;

	/** Static block for initialization */
	static
	{
		float[] smoothMatrix = {
			0, SMOOTH_FACTOR, 0,
			SMOOTH_FACTOR, 1-(SMOOTH_FACTOR*4), SMOOTH_FACTOR,
			0, SMOOTH_FACTOR, 0
		};
		CONVOLVER = new ConvolveOp(new Kernel(3, 3, smoothMatrix));
	}


	/**
	 * Create a scaled and smoothed image according to the specified size
	 * @param inImage image to scale
	 * @param inWidth width to scale to
	 * @param inHeight height to scale to
	 * @return BufferedImage containing scaled result
	 */
	public static BufferedImage createScaledImage(Image inImage, int inWidth, int inHeight)
	{
		// create smaller image and force its loading
		Image smallerImage = inImage.getScaledInstance(inWidth, inHeight, Image.SCALE_SMOOTH);
		Image tempImage = new ImageIcon(smallerImage).getImage();
		tempImage.getWidth(null);

		// create buffered image to do transform
		BufferedImage buffer = new BufferedImage(inWidth, inHeight, BufferedImage.TYPE_INT_RGB);
		// copy scaled picture into buffer
		Graphics buffG = buffer.getGraphics();
		buffG.drawImage(smallerImage, 0, 0, inWidth, inHeight, null);
		buffG.dispose();

		// clear variables
		smallerImage = null; tempImage = null;
		// smooth scaled image using a normalized 3x3 matrix - taking next neighbour
		buffer = CONVOLVER.filter(buffer, null);

		return buffer;
	}


	/**
	 * Work out the max size of a thumbnail
	 * @param inOrigWidth width of original picture
	 * @param inOrigHeight height of original picture
	 * @param inMaxWidth max width of thumbnail
	 * @param inMaxHeight max height of thumbnail
	 * @return size of thumbnail as Dimension
	 */
	public static Dimension getThumbnailSize(int inOrigWidth, int inOrigHeight, int inMaxWidth, int inMaxHeight)
	{
		if (inMaxWidth <= 0 || inMaxHeight <= 0)
		{
			//System.out.println("Can't do it - maxwidth=" + inMaxWidth + ", maxheight=" + inMaxHeight);
			return new Dimension(0,0);
		}
		// work out maximum zoom ratio available so that thumbnail isn't too big
		double xZoom = inMaxWidth * 1.0 / inOrigWidth;
		double yZoom = inMaxHeight * 1.0 / inOrigHeight;
		double zoom = (xZoom > yZoom?yZoom:xZoom);
		// Don't make thumbnail bigger than picture
		if (zoom > 1.0) {return new Dimension(inOrigWidth, inOrigHeight);}
		// calculate new width and height
		return new Dimension ((int) (zoom * inOrigWidth), (int) (zoom * inOrigHeight));
	}
}
