package tim.prune.gui.map;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;

import tim.prune.I18nManager;
import tim.prune.config.ColourScheme;
import tim.prune.config.Config;
import tim.prune.data.Unit;

/**
 * Class to show a scale bar on the main map of GpsPrune
 */
public class ScaleBar extends JPanel
{
	/** zoom level */
	private int _zoomLevel = -1;
	/** y position */
	private double _yPos = 0.0;

	// Dimensions
	/** Offset from left side in pixels */
	private static final int LEFT_OFFSET = 20;
	/** Offset from top in pixels */
	private static final int Y_OFFSET = 10;
	/** Tick height in pixels */
	private static final int TICK_HEIGHT = 5;
	/** Margin between bar and end text in pixels */
	private static final int MARGIN_WIDTH = 8;

	/** scales for each zoom level */
	private static final int[] _scales = {10000, 5000, 2000, 2000, 1000, 500, 200, 100,
		50, 20, 10, 5, 2, 2, 1,
		-2, -5, -10, -20, -50, -100, -200};
	/** pixel counts for each zoom level (metric) */
	private static final double[] _metricPixels = {64, 64, 51, 102, 102, 102, 81, 81,
		81, 65, 65, 65, 52, 105, 105,
		105, 83, 83, 83, 67, 67, 67};


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
				final double distScaleFactor = Config.getUnitSet().getDistanceUnit().getMultFactorFromStd();
				double drightSide = LEFT_OFFSET + _metricPixels[_zoomLevel] / 1000.0 / distScaleFactor;
				int scale = _scales[_zoomLevel];

				// work out cos(latitude) from y position, and apply to scale
				final double angle = Math.PI * (1 - 2*_yPos);
				final double lat = Math.atan(Math.sinh(angle));
				final double cosLat = Math.cos(lat);
				int rightSide = (int) (drightSide / cosLat);
				// Adjust if scale is too large
				while (rightSide > 300)
				{
					rightSide /= 2;
					scale /= 2;
					// Abort if scale is now less than 1 unit (shouldn't ever be)
					if (scale < 1) {return;}
				}
				// Abort if scale is negative (around poles)
				if (rightSide < 1) {return;}

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
				String text = getScaleText(scale, Config.getUnitSet().getDistanceUnit());
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
	 * Get the scale text for the given scale
	 * @param inScale scale number
	 * @param inDistUnit distance unit
	 * @return scale text as string
	 */
	private static String getScaleText(int inScale, Unit inDistUnit)
	{
		if (inScale > 0) {
			// Positive scale means km or miles
			return "" + inScale	+ " " +
				I18nManager.getText(inDistUnit.getShortnameKey());
		}
		// negative scale means a fraction
		return "" + (-1.0 / inScale) + " " + I18nManager.getText(inDistUnit.getShortnameKey());
		// might be nice to say 100m instead of 0.1km, 275ft instead of 0.2miles, etc - need to be done by Unit itself?
	}

	/**
	 * Update the scale level
	 * @param inZoom new zoom level
	 * @param inYPos y position, where 0 is north pole, 1 is south pole
	 */
	public void updateScale(int inZoom, double inYPos)
	{
		_zoomLevel = inZoom;
		_yPos = inYPos;
	}
}
