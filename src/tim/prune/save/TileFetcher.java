package tim.prune.save;

import java.awt.Image;
import javax.swing.ImageIcon;


public class TileFetcher
{
	private final Grouter _parent;
	private final Image _image;
	private final int _x;
	private final int _y;
	private boolean _done = false;


	/** Constructor */
	TileFetcher(Grouter inParent, Image inImage, int inX, int inY)
	{
		_parent = inParent;
		_image = inImage;
		_x = inX;
		_y = inY;
	}

	void go() {
		new Thread(this::loadImage).start();
	}

	private void loadImage()
	{
		new ImageIcon(_image);
		_parent.tileReady(_image, _x, _y);
		_done = true;
	}

	/** @return true if this fetcher is done */
	boolean isDone() {
		return _done;
	}
}
