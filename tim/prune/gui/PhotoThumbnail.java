package tim.prune.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import tim.prune.I18nManager;
import tim.prune.data.Photo;

/**
 * GUI component for showing photo thumbnail
 */
public class PhotoThumbnail extends JPanel implements Runnable
{
	private Photo _photo = null;
	private Image _thumbnail = null;
	private boolean _loadingImage = false;
	private boolean _loadFailed = false;
	private boolean _inPanel = false;
	/** String to show before photo is loaded */
	private static final String LOADING_STRING = I18nManager.getText("details.photo.loading") + " ...";


	/**
	 * Constructor
	 */
	public PhotoThumbnail()
	{
		this(true);
	}

	/**
	 * Constructor
	 * @param inPanel true if thumbnail is inside panel
	 */
	public PhotoThumbnail(boolean inPanel)
	{
		setOpaque(true);
		_inPanel = inPanel;
	}

	/**
	 * Set the Photo
	 * @param inPhoto Photo object to show thumbnail for
	 */
	public void setPhoto(Photo inPhoto)
	{
		// Check whether the photo has changed
		if (_photo != inPhoto)
		{
			_photo = inPhoto;
			_thumbnail = null;
			_loadFailed = false;
		}
		repaint();
	}

	/**
	 * Force a refresh / reload
	 */
	public void refresh()
	{
		_thumbnail = null;
		_loadFailed = false;
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
			// read thumbnail in separate thread
			if (_thumbnail == null && !_loadingImage && !_loadFailed)
			{
				_loadingImage = true;
				new Thread(this).start();
			}
			// if loading, display message
			if (_loadingImage)
			{
				inG.setColor(Color.BLACK);
				inG.drawString(LOADING_STRING, 10, 30);
			}
			else if (_thumbnail != null && !_loadFailed)
			{
				// Copy scaled, smoothed (and rotated) image into scaled
				int usableWidth = getParent().getWidth()-10;
				int usableHeight = (_inPanel?usableWidth:getHeight()-10);
				Image scaled = ImageUtils.rotateImage(_thumbnail, usableWidth, usableHeight, _photo.getRotationDegrees());
				int scaleWidth = scaled.getWidth(null);
				int scaleHeight = scaled.getHeight(null);
				// Draw scaled / rotated image to component
				int horizOffset = (getWidth() - scaleWidth) / 2;
				int vertOffset = (getHeight() - scaleHeight) / 2;
				inG.drawImage(scaled, horizOffset, vertOffset, scaleWidth, scaleHeight, null);
				// Special resize behaviour when locked inside details panel
				if (_inPanel && (getHeight() < getWidth() || getHeight() > usableWidth))
				{
					Dimension newsize = new Dimension(usableWidth, usableWidth);
					setPreferredSize(newsize);
					setSize(newsize);
					invalidate();
					// Schedule a relayout because the size has changed
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							try {Thread.sleep(200);} catch (InterruptedException e) {}
							getParent().getParent().getParent().validate();
						}
					});
				}
			}
		}
	}


	/**
	 * Run method, for loading image in separate thread
	 * @see java.lang.Runnable#run()
	 */
	public void run()
	{
		if (_inPanel)
		{
			_thumbnail = null;
			// try to use exif thumbnail
			if (_photo.getExifThumbnail() != null) {
				// Use exif thumbnail
				Image image = new ImageIcon(_photo.getExifThumbnail()).getImage();
				_thumbnail = ImageUtils.createScaledImage(image, image.getWidth(null), image.getHeight(null));
				image = null;
			}
			// Maybe there's no thumbnail, maybe the load of the thumbnail failed
			if (_thumbnail == null)
			{
				// no exif thumbnail available, going to have to read whole thing
				int picWidth = _photo.getWidth();
				int picHeight = _photo.getHeight();
				if (picWidth > -1 && picHeight > -1)
				{
					// Just set a "reasonable" thumbnail size for now
					final int DEFAULT_THUMB_SIZE = 400;
					// calculate maximum thumbnail size
					Dimension thumbSize = ImageUtils.getThumbnailSize(picWidth, picHeight, DEFAULT_THUMB_SIZE, DEFAULT_THUMB_SIZE);
					// Make icon to load image into
					Image image = _photo.createImageIcon().getImage();
					// save scaled, smoothed thumbnail for reuse
					_thumbnail = ImageUtils.createScaledImage(image, thumbSize.width, thumbSize.height);
					image = null;
				}
				else _loadFailed = true;
			}
		}
		else {
			_thumbnail = _photo.createImageIcon().getImage();
		}
		_loadingImage = false;
		repaint();
	}
}
