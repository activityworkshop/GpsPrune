package tim.prune.save;

import java.awt.image.BufferedImage;

import tim.prune.data.DoubleRange;


/**
 * Class to represent the result of the MapGrouter's assembly of map tiles
 * into a single image.  Includes information about how complete the result is.
 */
public class GroutedImage
{
	private BufferedImage _image = null;
	private int   _numTilesFound = 0;
	private int   _numTilesMissing = 0;
	private DoubleRange _xRange = null;
	private DoubleRange _yRange = null;

	/**
	 * Constructor
	 * @param inImage image, or null if no image possible
	 * @param inTilesUsed number of tiles used
	 * @param inTilesMissing number of tiles which could not be found
	 */
	public GroutedImage(BufferedImage inImage, int inTilesUsed, int inTilesMissing)
	{
		_image = inImage;
		_numTilesFound = inTilesUsed;
		_numTilesMissing = inTilesMissing;
	}

	/**
	 * @return true if any content at all was found
	 */
	public boolean isValid() {
		return _image != null && _numTilesFound > 0;
	}

	/**
	 * @return the pixel dimensions of the result image
	 */
	public int getImageSize()
	{
		if (_image == null) {return -1;}
		return _image.getWidth();
	}

	/**
	 * @return the image object
	 */
	public BufferedImage getImage() {
		return _image;
	}

	/**
	 * @return the number of tiles used in the image
	 */
	public int getNumTilesUsed() {
		return _numTilesFound;
	}

	/**
	 * @return the number of tiles which could not be found, leaving gaps in the image
	 */
	public int getNumTilesMissing() {
		return _numTilesMissing;
	}

	/**
	 * @return the total number of tiles
	 */
	public int getNumTilesTotal() {
		return _numTilesFound + _numTilesMissing;
	}

	/**
	 * @param inRange x range of data
	 */
	public void setXRange(DoubleRange inRange) {
		_xRange = inRange;
	}

	/**
	 * @return x range of data
	 */
	public DoubleRange getXRange() {
		return _xRange;
	}

	/**
	 * @param inRange y range of data
	 */
	public void setYRange(DoubleRange inRange) {
		_yRange = inRange;
	}

	/**
	 * @return y range of data
	 */
	public DoubleRange getYRange() {
		return _yRange;
	}
}
