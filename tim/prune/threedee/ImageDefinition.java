package tim.prune.threedee;

/**
 * Holds the definition of the image to use
 * (whether or not to use an image, and the source index and zoom)
 */
public class ImageDefinition
{
	private boolean _useImage    = false;
	private int     _sourceIndex = 0;
	private int     _zoom        = 0;


	/**
	 * Empty constructor specifying no image
	 */
	public ImageDefinition()
	{
		this(false, 0, 0);
	}

	/**
	 * Constructor
	 * @param inUse true to use an image
	 * @param inSourceIndex index of map source
	 * @param inZoom zoom level
	 */
	public ImageDefinition(boolean inUse, int inSourceIndex, int inZoom)
	{
		setUseImage(inUse, inSourceIndex, inZoom);
	}

	/**
	 * Set the parameters
	 * @param inUse true to use an image
	 * @param inSourceIndex index of map source
	 * @param inZoom zoom level
	 */
	public void setUseImage(boolean inUse, int inSourceIndex, int inZoom)
	{
		_useImage = inUse;
		_sourceIndex = inSourceIndex;
		_zoom = inZoom;
	}

	/**
	 * @return true if image should be used, false otherwise
	 */
	public boolean getUseImage() {
		return _useImage && _sourceIndex >= 0 && _zoom > 2;
	}

	/**
	 * @return source index
	 */
	public int getSourceIndex() {
		return _sourceIndex;
	}

	/**
	 * @return zoom level
	 */
	public int getZoom() {
		return _zoom;
	}
}
