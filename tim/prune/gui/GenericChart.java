package tim.prune.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import tim.prune.I18nManager;
import tim.prune.config.ColourScheme;
import tim.prune.config.Config;
import tim.prune.data.TrackInfo;


/**
 * Generic chart component to form baseclass for map and profile charts
 */
public abstract class GenericChart extends GenericDisplay implements MouseListener
{
	protected Dimension MINIMUM_SIZE = new Dimension(200, 250);
	protected static final int BORDER_WIDTH = 8;

	// Colours
	private static final Color COLOR_NODATA_TEXT = Color.GRAY;


	/**
	 * Constructor
	 * @param inTrackInfo track info object
	 */
	protected GenericChart(TrackInfo inTrackInfo)
	{
		super(inTrackInfo);
	}

	/**
	 * Override minimum size method to restrict map
	 */
	public Dimension getMinimumSize()
	{
		return MINIMUM_SIZE;
	}

	/**
	 * Override paint method to draw map
	 * @param inG graphics object
	 */
	public void paint(Graphics inG)
	{
		super.paint(inG);
		int width = getWidth();
		int height = getHeight();
		// Get colours
		ColourScheme colourScheme = Config.getColourScheme();
		final Color borderColour = colourScheme.getColour(ColourScheme.IDX_BORDERS);
		final Color backgroundColour = colourScheme.getColour(ColourScheme.IDX_BACKGROUND);
		final Color insideColour = backgroundColour;
		// border background
		inG.setColor(backgroundColour);
		inG.fillRect(0, 0, width, height);
		if (width < 2*BORDER_WIDTH || height < 2*BORDER_WIDTH) return;
		// blank graph area, with line border
		inG.setColor(insideColour);
		inG.fillRect(BORDER_WIDTH, BORDER_WIDTH, width - 2*BORDER_WIDTH, height-2*BORDER_WIDTH);
		// Display message if no data to be displayed
		if (_track == null || _track.getNumPoints() <= 0)
		{
			inG.setColor(COLOR_NODATA_TEXT);
			inG.drawString(I18nManager.getText("display.nodata"), 50, height/2);
		}
		else {
			inG.setColor(borderColour);
			inG.drawRect(BORDER_WIDTH, BORDER_WIDTH, width - 2*BORDER_WIDTH, height-2*BORDER_WIDTH);
		}
	}


	/**
	 * Method to inform map that data has changed
	 */
	public void dataUpdated(byte inUpdateType)
	{
		repaint();
	}


	/**
	 * mouse enter events ignored
	 */
	public void mouseEntered(MouseEvent e)
	{}

	/**
	 * mouse exit events ignored
	 */
	public void mouseExited(MouseEvent e)
	{}

	/**
	 * ignore mouse pressed for now too
	 */
	public void mousePressed(MouseEvent e)
	{}

	/**
	 * and also ignore mouse released
	 */
	public void mouseReleased(MouseEvent e)
	{}
}
