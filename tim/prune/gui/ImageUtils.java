package tim.prune.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
		assert(inMaxWidth > 0 && inMaxHeight > 0);
		// work out maximum zoom ratio available so that thumbnail isn't too big
		double xZoom = inMaxWidth * 1.0 / inOrigWidth;
		double yZoom = inMaxHeight * 1.0 / inOrigHeight;
		double zoom = (xZoom > yZoom?yZoom:xZoom);
		// Don't make thumbnail bigger than picture
		if (zoom > 1.0) {return new Dimension(inOrigWidth, inOrigHeight);}
		// calculate new width and height
		return new Dimension ((int) (zoom * inOrigWidth), (int) (zoom * inOrigHeight));
	}


	/**
	 * Create a new image by rotating and scaling the given one
	 * @param inImage input image
	 * @param inMaxWidth maximum width of output image
	 * @param inMaxHeight maximum height of output image
	 * @param inRotationDegrees number of degrees to rotate clockwise (0, 90, 180 or 270)
	 * @return rotated, scaled image
	 */
	public static BufferedImage rotateImage(Image inImage, int inMaxWidth, int inMaxHeight, int inRotationDegrees)
	{
		// Create scaled image of suitable size
		boolean isRotated = (inRotationDegrees % 180 != 0);
		int origWidth = inImage.getWidth(null);
		int origHeight = inImage.getHeight(null);
		int thumbWidth = isRotated?origHeight:origWidth;
		int thumbHeight = isRotated?origWidth:origHeight;
		Dimension scaledSize = getThumbnailSize(thumbWidth, thumbHeight, inMaxWidth, inMaxHeight);
		BufferedImage result = new BufferedImage(scaledSize.width, scaledSize.height, BufferedImage.TYPE_INT_RGB);
		// Do different things according to rotation angle (a bit messy, sorry!)
		if (inRotationDegrees == 0)
		{
			// Not rotated, so just copy image directly
			result.getGraphics().drawImage(inImage, 0, 0, scaledSize.width, scaledSize.height, null);
		}
		else
		{
			// Need to use Graphics2D for rotation, not Graphics
			Graphics2D g2d = result.createGraphics();
			switch (inRotationDegrees)
			{
				case 90:
					g2d.rotate(Math.PI / 2, 0.0, 0.0);
					g2d.drawImage(inImage, 0, -scaledSize.width, scaledSize.height, scaledSize.width, null);
					break;
				case 180:
					g2d.rotate(Math.PI, scaledSize.width/2.0, scaledSize.height/2.0);
					g2d.drawImage(inImage, 0, 0, scaledSize.width, scaledSize.height, null);
					break;
				case 270:
					g2d.rotate(Math.PI * 3/2, 0.0, 0.0);
					g2d.drawImage(inImage, -scaledSize.height, 0, scaledSize.height, scaledSize.width, null);
			}
			// Clear up memory
			g2d.dispose();
		}
		return result;
	}
}
