package tim.prune.gui.map;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;

import tim.prune.I18nManager;
import tim.prune.config.ColourScheme;
import tim.prune.config.Config;
import tim.prune.data.Distance;
import tim.prune.data.Unit;

/**
 * Class to show a scale bar on the main map of GpsPrune
 */
public class ScaleBar extends JPanel
{
	/** Config object, for units and colours */
	private final Config _config;
	/** zoom level */
	private int _zoomLevel = -1;
	/** y position */
	private double _yPos = 0.0;
	/** Scaling factor on screen (usually 1.0) */
	private double _displayScaling = 1.0;

	/** Invalid scale, do not draw */
	private static final int INVALID_SCALE = 0;


	/**
	 * Constructor
	 * @param inConfig config object
	 */
	public ScaleBar(Config inConfig)
	{
		_config = inConfig;
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
		if (_zoomLevel < 2) {return;}

		Unit distUnit = _config.getUnitSet().getDistanceUnit();
		final double distScaleFactor = getPixelsPerDist(distUnit);
		final int scale = getScaleToUse(distScaleFactor);
		if (scale != INVALID_SCALE)
		{
			final int barWidth = getBarWidth(distScaleFactor, scale);
			paintScaleBar(inG, scale, barWidth);
		}
	}

	/**
	 * @param inUnit distance unit
	 * @return scale factor in pixels per unit distance
	 */
	private double getPixelsPerDist(Unit inUnit)
	{
		// work out cos(latitude) from y position
		final double angle = Math.PI * (1 - 2*_yPos);
		final double lat = Math.atan(Math.sinh(angle));
		final double distAroundEarth = Distance.convertRadiansToDistance(2 * Math.PI, inUnit) * Math.cos(lat);
		// pixels at this zoom level
		return (256 << _zoomLevel) / distAroundEarth / _displayScaling;
	}

	/**
	 * @param inScaleFactor distance factor depending on current units
	 * @return scale to use, or INVALID_SCALE if not possible
	 */
	private static int getScaleToUse(double inScaleFactor)
	{
		// possible scales to use
		final int[] scales = {10000, 5000, 2000, 1000, 500, 200, 100,
			50, 20, 10, 5, 2, 1, -2, -5, -10, -20, -50, -100, -200};
		final int MAX_BAR_WIDTH = 280;
		for (int scale : scales)
		{
			int width = getBarWidth(inScaleFactor, scale);
			if (width <= MAX_BAR_WIDTH) {
				return scale;
			}
		}
		return INVALID_SCALE;
	}

	/**
	 * @param inScaleFactor distance factor depending on current units
	 * @param inScale selected scale
	 * @return bar width in pixels
	 */
	private static int getBarWidth(double inScaleFactor, int inScale)
	{
		double dDist = (inScale > 0 ? inScale : (-1.0/inScale)) * inScaleFactor;
		return (int) dDist;
	}

	/**
	 * Draw the components of the scale bar
	 * @param inG graphics object
	 * @param inScale scale level related to selected units
	 * @param inWidth width of scale bar, in pixels
	 */
	private void paintScaleBar(Graphics inG, int inScale, int inWidth)
	{
		// Offset from left side in pixels
		final int LEFT_OFFSET = 20;
		// Offset from top in pixels
		final int Y_OFFSET = 10;
		// Tick height in pixels
		final int TICK_HEIGHT = 5;
		// Margin between bar and end text in pixels
		final int MARGIN_WIDTH = 8;

		// Determine colours to use
		Color barColour = _config.getColourScheme().getColour(ColourScheme.IDX_TEXT);
		Color blankColour = new Color(255-barColour.getRed(), 255-barColour.getGreen(), 255-barColour.getBlue());
		final int rightSide = LEFT_OFFSET + inWidth;

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
		final String text = getScaleText(inScale, _config.getUnitSet().getDistanceUnit());
		inG.setColor(blankColour);
		inG.drawString(text, rightSide+MARGIN_WIDTH-1, Y_OFFSET);
		inG.drawString(text, rightSide+MARGIN_WIDTH+1, Y_OFFSET);
		inG.drawString(text, rightSide+MARGIN_WIDTH, Y_OFFSET-1);
		inG.drawString(text, rightSide+MARGIN_WIDTH, Y_OFFSET+1);
		inG.setColor(barColour);
		inG.drawString(text, rightSide+MARGIN_WIDTH, Y_OFFSET);
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

	/**
	 * @param inScaling current display scaling factor from OS
	 */
	public void setDisplayScaling(double inScaling) {
		_displayScaling = inScaling;
	}
}
