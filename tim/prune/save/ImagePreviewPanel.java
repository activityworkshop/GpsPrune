package tim.prune.save;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

import tim.prune.I18nManager;

/**
 * GUI component to display an image preview for the POV export
 * and for the image export
 */
public class ImagePreviewPanel extends JPanel
{
	/** Base image */
	private GroutedImage _baseImage = null;
	/** Loading flag */
	private boolean _loading = false;

	/** String to show before image is loaded */
	private static final String LOADING_STRING = I18nManager.getText("details.photo.loading") + " ...";
	/** Colour to use if there aren't any tiles at all (same as colour of POV export without image) */
	private static final Color EMPTY_IMAGE_COLOUR = new Color(0.5f, 0.75f, 0.8f);


	/** Set the base image */
	public void setImage(GroutedImage inImage)
	{
		_baseImage = inImage;
		_loading = false;
		repaint();
	}

	/** Inform that a load is starting */
	public void startLoading()
	{
		_baseImage = null;
		_loading = true;
		repaint();
	}

	/** Get minimum size */
	public Dimension getMinimumSize() {
		return new Dimension(200, 200);
	}
	/** Preferred size */
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

	/**
	 * Override paint method
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	public void paint(Graphics inG)
	{
		super.paint(inG);
		if (_loading)
		{
			inG.setColor(Color.BLACK);
			inG.drawString(LOADING_STRING, 10, 30);
		}
		else if (_baseImage != null)
		{
			final int width = getWidth();
			final int height = getHeight();
			final int previewSize = Math.min(width, height);
			if (previewSize > 1)
			{
				if (_baseImage.isValid())
				{
					inG.drawImage(_baseImage.getImage(),
						(width-previewSize)/2, (height-previewSize)/2, previewSize, previewSize, this);
				}
				else
				{
					// No content found at all
					inG.setColor(EMPTY_IMAGE_COLOUR);
					inG.fillRect((width-previewSize)/2, (height-previewSize)/2, previewSize, previewSize);
				}
				// draw frame around it to make it more obvious
				inG.setColor(Color.BLACK);
				inG.drawRect((width-previewSize)/2, (height-previewSize)/2, previewSize-1, previewSize-1);
			}
		}
	}

	/**
	 * @return true if there is an image to use and it contains map tiles
	 */
	public boolean getTilesFound()
	{
		return _baseImage != null && _baseImage.isValid() && _baseImage.getImageSize() > 1
			&& _baseImage.getNumTilesUsed() > 0;
	}
}
