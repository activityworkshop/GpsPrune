package tim.prune.gui.map;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JPanel;

/**
 * Semi-transparent panel to go on top of the map
 * to contain a set of controls
 */
public class OverlayPanel extends JPanel
{
	// Previous dimensions to see if calculations necessary
	private int _prevWidth = -1, _prevHeight = -1;
	// Previously calculated border limits
	private int _minX, _minY, _width, _height;

	/**
	 * Constructor
	 */
	public OverlayPanel()
	{
		setOpaque(false);
	}

	/**
	 * Paint the contents
	 */
	public void paint(Graphics g)
	{
		int panelWidth = getWidth();
		int panelHeight = getHeight();
		if (panelWidth != _prevWidth || panelHeight != _prevHeight)
		{
			calculateBorder();
			_prevWidth = panelWidth;
			_prevHeight = panelHeight;
		}
		// Draw white background
		final Color BG = new Color(255, 255, 255, 200);
		g.setColor(BG);
		g.fillRect(_minX, _minY, _width, _height);
		// Draw black border
		g.setColor(Color.BLACK);
		g.drawRect(_minX, _minY, _width, _height);
		// Paint everything else
		super.paint(g);
	}

	/**
	 * Calculate the boundaries to paint over
	 */
	private void calculateBorder()
	{
		final int PADDING = 2;
		// Calculate where the border should be drawn
		final Component firstComp = getComponent(0);
		final Component lastComp = getComponent(getComponentCount()-1);
		_minX = Math.max(firstComp.getX() - PADDING, 0);
		final int maxX = Math.min(lastComp.getX() + lastComp.getWidth() + PADDING, getWidth()-1);
		_width = maxX - _minX;
		_minY = Math.max(Math.min(firstComp.getY(), lastComp.getY()) - PADDING, 0);
		final int maxY = Math.max(firstComp.getY()+firstComp.getHeight(), lastComp.getY()+lastComp.getHeight()) + PADDING;
		_height = maxY - _minY;
		//System.out.println("x from " + minx + " to " + maxx + ", y from " + miny + " to " + maxy);
	}
}
