package tim.prune.gui.map;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.MediaTracker;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import tim.prune.I18nManager;
import tim.prune.data.DoubleRange;
import tim.prune.data.Track;

/**
 * Class for the map canvas, to display a background map and draw on it
 */
public class MapCanvas extends JPanel
{

	private BufferedImage _mapImage = null;
	private Track _track = null;
	private DoubleRange _latRange = null, _lonRange = null;
	private DoubleRange _xRange = null, _yRange = null;
	private boolean _gettingTiles = false;
	/** Current zoom level */
	private int _currZoom = 0;
	/** Maximum zoom level (to avoid panning) */
	private int _maxZoom = 0;


	/**
	 * Constructor
	 * @param inTrack track object
	 */
	public MapCanvas(Track inTrack)
	{
		_track = inTrack;
		_latRange = inTrack.getLatRange();
		_lonRange = inTrack.getLonRange();
		_xRange = new DoubleRange(transformX(_lonRange.getMinimum()), transformX(_lonRange.getMaximum()));
		_yRange = new DoubleRange(transformY(_latRange.getMinimum()), transformY(_latRange.getMaximum()));
	}

	/**
	 * Paint method
	 * @see java.awt.Canvas#paint(java.awt.Graphics)
	 */
	public void paint(Graphics g)
	{
		super.paint(g);
		if (_mapImage == null && !_gettingTiles) {
			_gettingTiles = true;
			new Thread(new Runnable() {
				public void run()
				{
					getMapTiles();
				}
			}).start();
		}
		if (_mapImage != null) {
			g.drawImage(_mapImage, 0, 0, 512, 512, null);
		}
	}

	/**
	 * Get the map tiles for the specified track range
	 */
	private void getMapTiles()
	{
		_mapImage = new BufferedImage(512, 512, BufferedImage.TYPE_INT_RGB);
		// zoom out until mins and maxes all on same group of four tiles
		for (int zoom=15; zoom>1; zoom--)
		{
			int tx1 = (int) Math.floor(_xRange.getMinimum() * (1<<zoom));
			int tx2 = (int) Math.floor(_xRange.getMaximum() * (1<<zoom));
			int ty1 = (int) Math.floor(_yRange.getMinimum() * (1<<zoom));
			int ty2 = (int) Math.floor(_yRange.getMaximum() * (1<<zoom));

			// Stop if reached a block of four adjacent tiles
			if (tx2 <= (tx1+1) && ty1 >= (ty2-1))
			{
				_currZoom = zoom;
				_maxZoom = zoom;
				getMapTiles(tx1, ty1);
				break;
			}
		}
		_gettingTiles = false;
		repaint();
	}

	/**
	 * Get the map tiles for the current zoom level and given tile parameters
	 * @param inTileX x index of leftmost tile
	 * @param inTileY y index of lower tile
	 */
	private void getMapTiles(int inTileX, int inTileY)
	{
		// Check if tile parameters were given
		if (inTileX == -1 || inTileY == -1) {
			double tileX = _xRange.getMinimum() * (1<<_currZoom);
			double tileY = _yRange.getMinimum() * (1<<_currZoom);
			inTileX = (int) Math.floor(tileX);
			inTileY = (int) Math.floor(tileY);
			// see if should be shifted by 1 to make more central
			if (_currZoom != _maxZoom) {
				if ((tileX - inTileX) < 0.5) inTileX--; // don't squash to left
				if ((tileY - inTileY) < 0.5) inTileY--; // don't squash too high
			}
		}
		try
		{
			ImageIcon[] icons = new ImageIcon[4];
			boolean loadingFailed = false;
			// Clear map
			Graphics g = _mapImage.getGraphics();
			g.clearRect(0, 0, 512, 512);
			for (int i=0; i<4 && !loadingFailed; i++)
			{
				String url = "http://tile.openstreetmap.org/" + _currZoom + "/" + (inTileX + i%2) + "/" + (inTileY + i/2) + ".png";
				icons[i] = new ImageIcon(new URL(url));
				if (icons[i] == null || icons[i].getImage() == null || icons[i].getImageLoadStatus() == MediaTracker.ERRORED)
				{
					loadingFailed = true;
				}
				g.drawImage(icons[i].getImage(), 256*(i%2), 256*(i/2), 256, 256, null);
			}
			// show message if loading failed
			if (loadingFailed) {
				JOptionPane.showMessageDialog(this,
					I18nManager.getText("error.osmimage.failed"),
					I18nManager.getText("error.osmimage.dialogtitle"),
					JOptionPane.ERROR_MESSAGE);
			}
			// red rectangle
			int rectX1 = (int) (256 * ((_xRange.getMinimum() * (1<<_currZoom)) - inTileX));
			int rectX2 = (int) (256 * ((_xRange.getMaximum() * (1<<_currZoom)) - inTileX));
			int rectY1 = (int) (256 * ((_yRange.getMinimum() * (1<<_currZoom)) - inTileY));
			int rectY2 = (int) (256 * ((_yRange.getMaximum() * (1<<_currZoom)) - inTileY));
			g.setColor(Color.RED);
			g.drawRect(rectX1, rectY1, rectX2-rectX1, rectY2-rectY1);
			// draw points
			g.setColor(Color.BLUE);
			for (int i=0; i<_track.getNumPoints(); i++)
			{
				int px = (int) (256 * ((transformX(_track.getPoint(i).getLongitude().getDouble()) * (1<<_currZoom)) - inTileX));
				int py = (int) (256 * ((transformY(_track.getPoint(i).getLatitude().getDouble()) * (1<<_currZoom)) - inTileY));
				g.drawRect(px, py, 2, 2);
			}
		}
		catch (MalformedURLException urle) {
			_mapImage = null;
		}
	}

	/**
	 * Zoom out, if not already at minimum zoom
	 */
	public void zoomOut()
	{
		if (_currZoom >= 2)
		{
			_currZoom--;
			getMapTiles(-1, -1);
			repaint();
		}
	}

	/**
	 * Zoom in, if not already at maximum zoom
	 */
	public void zoomIn()
	{
		if (_currZoom < _maxZoom)
		{
			_currZoom++;
			getMapTiles(-1, -1);
			repaint();
		}
	}

	/**
	 * Transform a longitude into an x coordinate
	 * @param inLon longitude in degrees
	 * @return scaled X value from 0 to 1
	 */
	private static double transformX(double inLon)
	{
		return (inLon + 180.0) / 360.0;
	}

	/**
	 * Transform a latitude into a y coordinate
	 * @param inLat latitude in degrees
	 * @return scaled Y value from 0 to 1
	 */
	private static double transformY(double inLat)
	{
		return (1 - Math.log(Math.tan(inLat * Math.PI / 180) + 1 / Math.cos(inLat * Math.PI / 180)) / Math.PI) / 2;
	}

	/**
	 * @see javax.swing.JComponent#getMinimumSize()
	 */
	public Dimension getMinimumSize()
	{
		final Dimension minSize = new Dimension(512, 512);
		return minSize;
	}

	/**
	 * @see javax.swing.JComponent#getPreferredSize()
	 */
	public Dimension getPreferredSize()
	{
		return getMinimumSize();
	}
}
