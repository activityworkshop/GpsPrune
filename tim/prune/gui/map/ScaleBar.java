package tim.prune.gui.map;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;

import tim.prune.I18nManager;
import tim.prune.config.ColourScheme;
import tim.prune.config.Config;

/**
 * Class to show a scale bar on the main map of Prune
 */
public class ScaleBar extends JPanel
{
	/** zoom level */
	private int _zoomLevel = -1;
	/** y tile number */
	private int _yTile = -1;

	// Dimensions
	/** Offset from left side in pixels */
	private static final int LEFT_OFFSET = 20;
	/** Offset from top in pixels */
	private static final int Y_OFFSET = 10;
	/** Tick height in pixels */
	private static final int TICK_HEIGHT = 5;
	/** Margin between bar and end text in pixels */
	private static final int MARGIN_WIDTH = 8;

	/** metric scales for each zoom level */
	private static final int[] _metricScales = {10000, 5000, 2000, 2000, 1000, 500, 200, 100,
		50, 20, 10, 5, 2, 2, 1, -2, -5, -10, -20, -50, -100, -200};
	/** pixel counts for each zoom level (metric) */
	private static final int[] _metricPixels = {64, 64, 51, 102, 102, 102, 81, 81,
		81, 65, 65, 65, 52, 105, 105, 105, 83, 83, 83, 67, 67, 67};
	/** imperial scales for each zoom level (num miles) */
	private static final int[] _mileScales = {10000, 10000, 5000, 2000, 2000, 1000, 500, 200,
		100, 50, 20, 10, 5, 2, 1, -2, -2, -5, -10, -20, -50, -100};
	/** pixel counts for each zoom level (miles) */
	private static final int[] _milePixels = {79, 79, 79, 64, 127, 127, 127, 102,
		102, 102, 81, 81, 81, 65, 65, 65, 130, 104, 104, 104, 104, 83, 83};


	/**
	 * Constructor
	 */
	public ScaleBar()
	{
		super();
		setOpaque(false);
		setPreferredSize(new Dimension(100, 20));
	}

	/**
	 * Paint method to override display
	 * @param inG graphics object
	 */
	public void paint(Graphics inG)
	{
		super.paint(inG);
		if (_zoomLevel > -1)
		{
			try {
				boolean useMetric = Config.getConfigBoolean(Config.KEY_METRIC_UNITS);
				int rightSide = LEFT_OFFSET + (useMetric?_metricPixels[_zoomLevel]:_milePixels[_zoomLevel]);
				int scale = (useMetric?_metricScales[_zoomLevel]:_mileScales[_zoomLevel]);

				// work out cos(latitude) from y tile and zoom, and apply to scale
				final double n = Math.pow(2, _zoomLevel);
				final double angle = Math.PI * (1 - 2.0*_yTile/n);
				final double lat = Math.atan(Math.sinh(angle));
				final double cosLat = Math.cos(lat);
				rightSide = (int) (rightSide / cosLat);
				// Adjust if scale is too large
				while (rightSide > 300)
				{
					rightSide /= 2;
					scale /= 2;
					// Abort if scale is now less than 1 unit (shouldn't ever be)
					if (scale < 1) {return;}
				}

				// Determine colours to use
				Color barColour = Config.getColourScheme().getColour(ColourScheme.IDX_TEXT);
				Color blankColour = new Color(255-barColour.getRed(), 255-barColour.getGreen(), 255-barColour.getBlue());
				// Should this blank colour be set to saturation zero?
				// Draw blank bars behind
				inG.setColor(blankColour);
				inG.drawLine(LEFT_OFFSET, Y_OFFSET-1, rightSide+2, Y_OFFSET-1);
				inG.drawLine(LEFT_OFFSET, Y_OFFSET+2, rightSide+2, Y_OFFSET+2);
				inG.drawLine(LEFT_OFFSET-1, Y_OFFSET+2, LEFT_OFFSET-1, Y_OFFSET-TICK_HEIGHT);
				inG.drawLine(LEFT_OFFSET+2, Y_OFFSET+2, LEFT_OFFSET+2, Y_OFFSET-TICK_HEIGHT);
				inG.drawLine(rightSide-1, Y_OFFSET+2, rightSide-1, Y_OFFSET-TICK_HEIGHT);
				inG.drawLine(rightSide+2, Y_OFFSET+2, rightSide+2, Y_OFFSET-TICK_HEIGHT);
				// horizontal
				inG.setColor(barColour);
				inG.drawLine(LEFT_OFFSET, Y_OFFSET, rightSide, Y_OFFSET);
				inG.drawLine(LEFT_OFFSET, Y_OFFSET+1, rightSide, Y_OFFSET+1);
				// 0 tick
				inG.drawLine(LEFT_OFFSET, Y_OFFSET, LEFT_OFFSET, Y_OFFSET-TICK_HEIGHT);
				inG.drawLine(LEFT_OFFSET+1, Y_OFFSET, LEFT_OFFSET+1, Y_OFFSET-TICK_HEIGHT);
				// end tick
				inG.drawLine(rightSide, Y_OFFSET+1, rightSide, Y_OFFSET-TICK_HEIGHT);
				inG.drawLine(rightSide+1, Y_OFFSET+1, rightSide+1, Y_OFFSET-TICK_HEIGHT);
				// text
				String text = (scale>0?(""+scale):("1/"+(-scale))) + " "
					+ I18nManager.getText(useMetric?"units.kilometres.short":"units.miles.short");
				inG.setColor(blankColour);
				inG.drawString(text, rightSide+MARGIN_WIDTH-1, Y_OFFSET);
				inG.drawString(text, rightSide+MARGIN_WIDTH+1, Y_OFFSET);
				inG.drawString(text, rightSide+MARGIN_WIDTH, Y_OFFSET-1);
				inG.drawString(text, rightSide+MARGIN_WIDTH, Y_OFFSET+1);
				inG.setColor(barColour);
				inG.drawString(text, rightSide+MARGIN_WIDTH, Y_OFFSET);
			}
			catch (ArrayIndexOutOfBoundsException ai) {}
		}
	}

	/**
	 * Update the scale level
	 * @param inZoom new zoom level
	 */
	public void updateScale(int inZoom, int inYtile)
	{
		_zoomLevel = inZoom;
		_yTile = inYtile;
	}
}
