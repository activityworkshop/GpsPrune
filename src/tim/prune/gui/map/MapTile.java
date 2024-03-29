package tim.prune.gui.map;

import java.awt.Image;

/**
 * Simple wrapper to hold an image for a map tile
 * and a boolean flag to show whether it's expired or not
 */
public class MapTile
{
	/** Image or null if no image available */
	private final Image _image;
	/** True if image expired, false if still fresh */
	private final boolean _expired;

	/** Constructor */
	public MapTile(Image inImage, boolean inExpired)
	{
		_image = inImage;
		_expired = inExpired;
	}

	/** Get the image */
	public Image getImage() {
		return _image;
	}

	/** Return true if image is too old */
	public boolean isExpired() {
		return _expired;
	}
}
