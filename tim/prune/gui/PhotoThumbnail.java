package tim.prune.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import tim.prune.I18nManager;
import tim.prune.data.Photo;

/**
 * GUI component for showing photo thumbnail
 */
public class PhotoThumbnail extends JPanel implements Runnable
{
	private Photo _photo = null;
	private BufferedImage _thumbnail = null;
	private int _lastWidth = -1;
	private int _lastHeight = -1;
	private boolean _loadingImage = false;
	private static String _loadingString = null;


	/**
	 * Constructor
	 */
	public PhotoThumbnail()
	{
		// TODO: Make size of thumbnail dynamic, as big as it can be
		setOpaque(true);
		_loadingString = I18nManager.getText("details.photo.loading") + " ...";
	}


	/**
	 * Set the Photo
	 * @param inPhoto Photo object to show thumbnail for
	 */
	public void setPhoto(Photo inPhoto)
	{
		// Check whether the photo has changed
		if (_photo == inPhoto) {return;}
		_photo = inPhoto;
		_thumbnail = null;
	}


	/**
	 * Override paint method
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	public void paint(Graphics inG)
	{
		super.paint(inG);
		if (_photo != null)
		{
			// recalculate thumbnail if photo has changed
			if (_thumbnail == null || getWidth() != _lastWidth || getHeight() != _lastHeight)
			{
				// initiate load if not already started
				if (!_loadingImage)
				{
					_loadingImage = true;
					new Thread(this).start();
				}
			}
			// Set width and height
			_lastWidth = getWidth();
			_lastHeight = getHeight();
			// if loading, display image
			if (_loadingImage)
			{
				inG.setColor(Color.BLACK);
				inG.drawString(_loadingString, 10, 30);
			}
			else
			{
				// Copy scaled, smoothed image onto the screen
				inG.drawImage(_thumbnail, 0, 0, _thumbnail.getWidth(), _thumbnail.getHeight(), null);
			}
		}
	}


	/**
	 * Run method, for loading image in separate thread
	 * @see java.lang.Runnable#run()
	 */
	public void run()
	{
		int picWidth = _photo.getWidth();
		int picHeight = _photo.getHeight();
		if (picWidth > -1 && picHeight > -1)
		{
			int displayWidth = Math.min(getWidth(), getParent().getWidth());
			// System.out.println("width = " + getWidth() + ", " + getParent().getWidth() + " = " + displayWidth);
			int displayHeight = Math.min(getHeight(), getParent().getHeight());
			// System.out.println("height = " + getHeight() + ", " + getParent().getHeight() + " = " + displayHeight);

			// calculate maximum thumbnail size
			Dimension thumbSize = ImageUtils.getThumbnailSize(picWidth, picHeight, displayWidth, displayHeight);
			// Work out if need to remake image
			boolean needToRemake = (_thumbnail == null)
			 || _thumbnail.getWidth() != thumbSize.width || _thumbnail.getHeight() != thumbSize.height;
			if (thumbSize.width > 0 && thumbSize.height > 0 && needToRemake)
			{
				// Make icon to load image into
				Image image = new ImageIcon(_photo.getFile().getAbsolutePath()).getImage();
				// save scaled, smoothed thumbnail for reuse
				_thumbnail = ImageUtils.createScaledImage(image, thumbSize.width, thumbSize.height);
				image = null;
				// TODO: Calculate and set size of thumbnail here
				// setPreferredSize(new Dimension(200, 200));
			}
		}
		_loadingImage = false;
		repaint();
	}
}
