package tim.prune.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import tim.prune.App;
import tim.prune.DataSubscriber;
import tim.prune.I18nManager;
import tim.prune.data.DataPoint;
import tim.prune.data.TrackInfo;


/**
 * Display component for the main map
 */
public class MapChart extends GenericChart implements MouseWheelListener, KeyListener, MouseMotionListener
{
	// Constants
	private static final int POINT_RADIUS = 4;
	private static final int CLICK_SENSITIVITY = 10;
	private static final double ZOOM_SCALE_FACTOR = 1.2;
	private static final int PAN_DISTANCE = 10;
	private static final int LIMIT_WAYPOINT_NAMES = 40;

	// Colours
	private static final Color COLOR_BG         = Color.WHITE;
	private static final Color COLOR_POINT      = Color.BLUE;
	private static final Color COLOR_CURR_RANGE = Color.GREEN;
	private static final Color COLOR_CROSSHAIRS = Color.RED;
	private static final Color COLOR_WAYPT_NAME = Color.BLACK;

	// Instance variables
	private App _app = null;
	private BufferedImage _image = null;
	private JPopupMenu _popup = null;
	private JCheckBoxMenuItem _autoPanMenuItem = null;
	private int _numPoints = -1;
	private double _scale;
	private double _offsetX, _offsetY, _zoomScale;
	private int _lastSelectedPoint = -1;
	private int _dragStartX = -1, _dragStartY = -1;
	private int _zoomDragFromX = -1, _zoomDragFromY = -1;
	private int _zoomDragToX = -1, _zoomDragToY = -1;
	private boolean _zoomDragging = false;


	/**
	 * Constructor
	 * @param inApp App object for callbacks
	 * @param inTrackInfo track info object
	 */
	public MapChart(App inApp, TrackInfo inTrackInfo)
	{
		super(inTrackInfo);
		_app = inApp;
		makePopup();
		addMouseListener(this);
		addMouseWheelListener(this);
		addMouseMotionListener(this);
		setFocusable(true);
		addKeyListener(this);
		MINIMUM_SIZE = new Dimension(200, 250);
		_zoomScale = 1.0;
	}


	/**
	 * Override track updating to refresh image
	 */
	public void dataUpdated(byte inUpdateType)
	{
		// Check if number of points has changed or data has been edited
		if (_track.getNumPoints() != _numPoints || (inUpdateType & DATA_EDITED) > 0)
		{
			_image = null;
			_lastSelectedPoint = -1;
			_numPoints = _track.getNumPoints();
		}
		super.dataUpdated(inUpdateType);
	}


	/**
	 * Override paint method to draw map
	 */
	public void paint(Graphics g)
	{
		if (_track == null)
		{
			super.paint(g);
			return;
		}

		int width = getWidth();
		int height = getHeight();
		int x, y;

		// Find x and y ranges, and scale to fit
		double scaleX = (_track.getXRange().getMaximum() - _track.getXRange().getMinimum())
		  / (width - 2 * (BORDER_WIDTH + POINT_RADIUS));
		double scaleY = (_track.getYRange().getMaximum() - _track.getYRange().getMinimum())
		  / (height - 2 * (BORDER_WIDTH + POINT_RADIUS));
		_scale = scaleX;
		if (scaleY > _scale) _scale = scaleY;

		// Autopan if necessary
		int selectedPoint = _trackInfo.getSelection().getCurrentPointIndex();
		if (_autoPanMenuItem.isSelected() && selectedPoint >= 0 && selectedPoint != _lastSelectedPoint)
		{
			// Autopan is enabled and a point is selected - work out x and y to see if it's within range
			x = width/2 + (int) ((_track.getX(selectedPoint) - _offsetX) / _scale * _zoomScale);
			y = height/2 - (int) ((_track.getY(selectedPoint) - _offsetY) / _scale * _zoomScale);
			if (x <= BORDER_WIDTH)
			{
				// autopan left
				_offsetX -= (width / 4 - x) * _scale / _zoomScale;
				_image = null;
			}
			else if (x >= (width - BORDER_WIDTH))
			{
				// autopan right
				_offsetX += (x - width * 3/4) * _scale / _zoomScale;
				_image = null;
			}
			if (y <= BORDER_WIDTH)
			{
				// autopan up
				_offsetY += (height / 4 - y) * _scale / _zoomScale;
				_image = null;
			}
			else if (y >= (height - BORDER_WIDTH))
			{
				// autopan down
				_offsetY -= (y - height * 3/4) * _scale / _zoomScale;
				_image = null;
			}
		}
		_lastSelectedPoint = selectedPoint;

		// Create background if necessary
		if (_image == null || width != _image.getWidth() || height != _image.getHeight())
		{
			createBackgroundImage();
		}
		// return if image has been set to null by other thread
		if (_image == null) {return;}

		// draw buffered image onto g
		g.drawImage(_image, 0, 0, width, height, COLOR_BG, null);

		// draw selected range, if any
		if (_trackInfo.getSelection().hasRangeSelected() && !_zoomDragging)
		{
			int rangeStart = _trackInfo.getSelection().getStart();
			int rangeEnd = _trackInfo.getSelection().getEnd();
			g.setColor(COLOR_CURR_RANGE);
			for (int i=rangeStart; i<=rangeEnd; i++)
			{
				x = width/2 + (int) ((_track.getX(i) - _offsetX) / _scale * _zoomScale);
				y = height/2 - (int) ((_track.getY(i) - _offsetY) / _scale * _zoomScale);
				if (x > BORDER_WIDTH && x < (width - BORDER_WIDTH)
					&& y < (height - BORDER_WIDTH) && y > BORDER_WIDTH)
				{
					g.drawOval(x - 2, y - 2, 4, 4);
				}
			}
		}

		// Highlight selected point
		if (selectedPoint >= 0 && !_zoomDragging)
		{
			g.setColor(COLOR_CROSSHAIRS);
			x = width/2 + (int) ((_track.getX(selectedPoint) - _offsetX) / _scale * _zoomScale);
			y = height/2 - (int) ((_track.getY(selectedPoint) - _offsetY) / _scale * _zoomScale);
			if (x > BORDER_WIDTH && x < (width - BORDER_WIDTH)
				&& y < (height - BORDER_WIDTH) && y > BORDER_WIDTH)
			{
				// Draw cross-hairs for current point
				g.drawLine(x, BORDER_WIDTH, x, height - BORDER_WIDTH);
				g.drawLine(BORDER_WIDTH, y, width - BORDER_WIDTH, y);

				// Show selected point afterwards to make sure it's on top
				g.drawOval(x - 2, y - 2, 4, 4);
				g.drawOval(x - 3, y - 3, 6, 6);
			}
		}

		// Draw rectangle for dragging zoom area
		if (_zoomDragging)
		{
			g.setColor(COLOR_CROSSHAIRS);
			g.drawLine(_zoomDragFromX, _zoomDragFromY, _zoomDragFromX, _zoomDragToY);
			g.drawLine(_zoomDragFromX, _zoomDragFromY, _zoomDragToX, _zoomDragFromY);
			g.drawLine(_zoomDragToX, _zoomDragFromY, _zoomDragToX, _zoomDragToY);
			g.drawLine(_zoomDragFromX, _zoomDragToY, _zoomDragToX, _zoomDragToY);
		}

		// Attempt to grab keyboard focus if possible
		//this.requestFocus();
	}


	/**
	 * Draw the map onto an offscreen image
	 */
	private void createBackgroundImage()
	{
		int width = getWidth();
		int height = getHeight();
		int x, y;
		// Make a new image and initialise it
		_image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics bufferedG = _image.getGraphics();
		super.paint(bufferedG);

		// Loop and show all points
		int numPoints = _track.getNumPoints();
		bufferedG.setColor(COLOR_POINT);
		int halfWidth = width/2;
		int halfHeight = height/2;
		for (int i=0; i<numPoints; i++)
		{
			x = halfWidth + (int) ((_track.getX(i) - _offsetX) / _scale * _zoomScale);
			y = halfHeight - (int) ((_track.getY(i) - _offsetY) / _scale * _zoomScale);
			if (x > BORDER_WIDTH && x < (width - BORDER_WIDTH)
				&& y < (height - BORDER_WIDTH) && y > BORDER_WIDTH)
			{
				bufferedG.drawOval(x - 2, y - 2, 4, 4);
			}
		}

		// Loop again and show waypoints with names
		bufferedG.setColor(COLOR_WAYPT_NAME);
		FontMetrics fm = bufferedG.getFontMetrics();
		int nameHeight = fm.getHeight();
		int numWaypointNamesShown = 0;
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = _track.getPoint(i);
			String waypointName = point.getWaypointName();
			if (waypointName != null && !waypointName.equals(""))
			{
				// escape if nothing more to do
				if (numWaypointNamesShown >= LIMIT_WAYPOINT_NAMES || _image == null) {break;}
				// calculate coordinates of point
				x = halfWidth + (int) ((_track.getX(i) - _offsetX) / _scale * _zoomScale);
				y = halfHeight - (int) ((_track.getY(i) - _offsetY) / _scale * _zoomScale);
				if (x > BORDER_WIDTH && x < (width - BORDER_WIDTH)
						&& y < (height - BORDER_WIDTH) && y > BORDER_WIDTH)
				{
					bufferedG.fillOval(x - 3, y - 3, 6, 6);
					// Figure out where to draw name so it doesn't obscure track
					int nameWidth = fm.stringWidth(waypointName);
					if (nameWidth < (width - 2 * BORDER_WIDTH))
					{
						double nameAngle = 0.3;
						double nameRadius = 1.0;
						boolean drawnName = false;
						while (!drawnName)
						{
							int nameX = x + (int) (nameRadius * Math.cos(nameAngle)) - (nameWidth/2);
							int nameY = y + (int) (nameRadius * Math.sin(nameAngle)) + (nameHeight/2);
							if (nameX > BORDER_WIDTH && (nameX + nameWidth) < (width - BORDER_WIDTH)
								&& nameY < (height - BORDER_WIDTH) && (nameY - nameHeight) > BORDER_WIDTH)
							{
								// name can fit in grid - does it overlap data points?
								if (!overlapsPoints(nameX, nameY, nameWidth, nameHeight) || nameRadius > 50.0)
								{
									bufferedG.drawString(waypointName, nameX, nameY);
									drawnName = true;
									numWaypointNamesShown++;
								}
							}
							nameAngle += 0.08;
							nameRadius += 0.2;
							// wasn't room within the radius, so don't print name
							if (nameRadius > 50.0)
							{
								drawnName = true;
							}
						}
					}
				}
			}
		}
	}


	/**
	 * Tests whether there are any data points within the specified x,y rectangle
	 * @param inX left X coordinate
	 * @param inY bottom Y coordinate
	 * @param inWidth width of rectangle
	 * @param inHeight height of rectangle
	 * @return true if there's at least one data point in the rectangle
	 */
	private boolean overlapsPoints(int inX, int inY, int inWidth, int inHeight)
	{
		try
		{
			// loop over x coordinate of rectangle
			for (int x=0; x<inWidth; x++)
			{
				// loop over y coordinate of rectangle
				for (int y=0; y<inHeight; y++)
				{
					int pixelColor = _image.getRGB(inX + x, inY - y);
					if (pixelColor != -1) return true;
				}
			}
		}
		catch (NullPointerException e) {
			// ignore null pointers, just return false
		}
		return false;
	}


	/**
	 * Make the popup menu for right-clicking the map
	 */
	private void makePopup()
	{
		_popup = new JPopupMenu();
		JMenuItem zoomIn = new JMenuItem(I18nManager.getText("menu.map.zoomin"));
		zoomIn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				zoomMap(true);
			}});
		zoomIn.setEnabled(true);
		_popup.add(zoomIn);
		JMenuItem zoomOut = new JMenuItem(I18nManager.getText("menu.map.zoomout"));
		zoomOut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				zoomMap(false);
			}});
		zoomOut.setEnabled(true);
		_popup.add(zoomOut);
		JMenuItem zoomFull = new JMenuItem(I18nManager.getText("menu.map.zoomfull"));
		zoomFull.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				zoomToFullScale();
			}});
		zoomFull.setEnabled(true);
		_popup.add(zoomFull);
		_autoPanMenuItem = new JCheckBoxMenuItem(I18nManager.getText("menu.map.autopan"));
		_autoPanMenuItem.setSelected(true);
		_popup.add(_autoPanMenuItem);
	}


	/**
	 * Zoom map to full scale
	 */
	private void zoomToFullScale()
	{
		_zoomScale = 1.0;
		_offsetX = 0.0;
		_offsetY = 0.0;
		_numPoints = 0;
		dataUpdated(DataSubscriber.ALL);
	}


	/**
	 * Zoom map either in or out by one step
	 * @param inZoomIn true to zoom in, false for out
	 */
	private void zoomMap(boolean inZoomIn)
	{
		if (inZoomIn)
		{
			// Zoom in
			_zoomScale *= ZOOM_SCALE_FACTOR;
		}
		else
		{
			// Zoom out
			_zoomScale /= ZOOM_SCALE_FACTOR;
			if (_zoomScale < 0.5) _zoomScale = 0.5;
		}
		_numPoints = 0;
		dataUpdated(DataSubscriber.ALL);
	}


	/**
	 * Pan the map by the specified amounts
	 * @param inUp upwards pan
	 * @param inRight rightwards pan
	 */
	private void panMap(int inUp, int inRight)
	{
		double panFactor = _scale / _zoomScale;
		_offsetY = _offsetY + (inUp * panFactor);
		_offsetX = _offsetX - (inRight * panFactor);
		// Limit pan to sensible range??
		_numPoints = 0;
		_image = null;
		repaint();
	}


	/**
	 * React to click on map display
	 */
	public void mouseClicked(MouseEvent e)
	{
		this.requestFocus();
		if (_track != null)
		{
			int xClick = e.getX();
			int yClick = e.getY();
			// Check click is within main area (not in border)
			if (xClick > BORDER_WIDTH && yClick > BORDER_WIDTH && xClick < (getWidth() - BORDER_WIDTH)
				&& yClick < (getHeight() - BORDER_WIDTH))
			{
				// Check left click or right click
				if (e.isMetaDown())
				{
					// Only show popup if track has data
					if (_track != null && _track.getNumPoints() > 0)
						_popup.show(this, e.getX(), e.getY());
				}
				else
				{
					// Find point within range of click point
					double pointX = (xClick - getWidth()/2) * _scale / _zoomScale + _offsetX;
					double pointY = (getHeight()/2 - yClick) * _scale / _zoomScale + _offsetY;
					int selectedPointIndex = _track.getNearestPointIndex(
						pointX, pointY, CLICK_SENSITIVITY * _scale, false);
					// Select the given point (or deselect if no point was found)
					_trackInfo.getSelection().selectPoint(selectedPointIndex);
				}
			}
		}
	}


	/**
	 * Respond to mouse released to reset dragging
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e)
	{
		_dragStartX = _dragStartY = -1;
		if (e.isMetaDown())
		{
			if (_zoomDragFromX >= 0 || _zoomDragFromY >= 0)
			{
				// zoom area marked out - calculate offset and zoom
				int xPan = (getWidth() - _zoomDragFromX - e.getX()) / 2;
				int yPan = (getHeight() - _zoomDragFromY - e.getY()) / 2;
				double xZoom = Math.abs(getWidth() * 1.0 / (e.getX() - _zoomDragFromX));
				double yZoom = Math.abs(getHeight() * 1.0 / (e.getY() - _zoomDragFromY));
				double extraZoom = (xZoom>yZoom?yZoom:xZoom);
				// deselect point if selected (to stop autopan)
				_trackInfo.getSelection().selectPoint(-1);
				// Pan first to ensure pan occurs with correct scale
				panMap(yPan, xPan);
				// Then zoom in and request repaint
				_zoomScale = _zoomScale * extraZoom;
				_image = null;
				repaint();
			}
			_zoomDragFromX = _zoomDragFromY = -1;
			_zoomDragging = false;
		}
	}


	/**
	 * Respond to mouse wheel events to zoom the map
	 * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
	 */
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		zoomMap(e.getWheelRotation() < 0);
	}


	/**
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent e)
	{
		int code = e.getKeyCode();
		// Check for meta key
		if (e.isControlDown())
		{
			// Check for arrow keys to zoom in and out
			if (code == KeyEvent.VK_UP)
				zoomMap(true);
			else if (code == KeyEvent.VK_DOWN)
				zoomMap(false);
			// Key nav for next/prev point
			else if (code == KeyEvent.VK_LEFT)
				_trackInfo.getSelection().selectPreviousPoint();
			else if (code == KeyEvent.VK_RIGHT)
				_trackInfo.getSelection().selectNextPoint();
		}
		else
		{
			// Check for arrow keys to pan
			int upwardsPan = 0;
			if (code == KeyEvent.VK_UP)
				upwardsPan = PAN_DISTANCE;
			else if (code == KeyEvent.VK_DOWN)
				upwardsPan = -PAN_DISTANCE;
			int rightwardsPan = 0;
			if (code == KeyEvent.VK_RIGHT)
				rightwardsPan = -PAN_DISTANCE;
			else if (code == KeyEvent.VK_LEFT)
				rightwardsPan = PAN_DISTANCE;
			panMap(upwardsPan, rightwardsPan);
			// Check for delete key to delete current point
			if (code == KeyEvent.VK_DELETE && _trackInfo.getSelection().getCurrentPointIndex() >= 0)
			{
				_app.deleteCurrentPoint();
				// reset last selected point to trigger autopan
				_lastSelectedPoint = -1;
			}
		}
	}


	/**
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent e)
	{
		// ignore
	}


	/**
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(KeyEvent e)
	{
		// ignore
	}


	/**
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	public void mouseDragged(MouseEvent e)
	{
		if (!e.isMetaDown())
		{
			if (_dragStartX > 0)
			{
				int xShift = e.getX() - _dragStartX;
				int yShift = e.getY() - _dragStartY;
				panMap(yShift, xShift);
			}
			_dragStartX = e.getX();
			_dragStartY = e.getY();
		}
		else
		{
			// Right click-and-drag for zoom
			if (_zoomDragFromX < 0 || _zoomDragFromY < 0)
			{
				_zoomDragFromX = e.getX();
				_zoomDragFromY = e.getY();
			}
			else
			{
				_zoomDragToX = e.getX();
				_zoomDragToY = e.getY();
				_zoomDragging = true;
			}
			repaint();
		}
	}


	/**
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent e)
	{
		// ignore
	}
}
