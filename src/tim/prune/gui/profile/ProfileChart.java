package tim.prune.gui.profile;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import tim.prune.I18nManager;
import tim.prune.config.ColourScheme;
import tim.prune.config.Config;
import tim.prune.data.Field;
import tim.prune.data.FieldList;
import tim.prune.data.TrackInfo;
import tim.prune.gui.GenericDisplay;

/**
 * Chart component for the profile display
 */
public class ProfileChart extends GenericDisplay implements MouseListener
{
	/** Inner class to handle popup menu clicks */
	class MenuClicker implements ActionListener
	{
		private Field _field = null;
		MenuClicker(Field inField) {_field = inField;}
		/** React to menu click by changing the field */
		public void actionPerformed(ActionEvent arg0) {
			changeView(_field);
		}
	}

	/** Inner class to remember a single index */
	class PointIndex
	{
		public int index = -1;
		public boolean hasValue = false;
		public PointIndex()
		{
			index = -1;
			hasValue = false;
		}
		/** Set a single value */
		public void set(int inValue)
		{
			index = inValue;
			hasValue = (inValue != -1);
		}
		/** Add an index to the minimum calculation */
		public void setMin(PointIndex other)
		{
			if (!other.hasValue) {return;}
			if (!hasValue) {
				index = other.index;
				hasValue = true;
			}
			else {
				index = Math.min(index, other.index);
			}
		}
		/** Add an index to the maximum calculation */
		public void setMax(PointIndex other)
		{
			if (!other.hasValue) {return;}
			if (!hasValue) {
				index = other.index;
				hasValue = true;
			}
			else {
				index = Math.max(index, other.index);
			}
		}
		/** @return true if two Indexes are equal */
		public boolean equals(PointIndex other)
		{
			if (!hasValue || !other.hasValue) {
				return hasValue == other.hasValue;
			}
			return index == other.index;
		}
	}

	/** Inner class to remember previous chart parameters */
	class ChartParameters
	{
		public PointIndex selectedPoint = new PointIndex();
		public PointIndex rangeStart = new PointIndex(), rangeEnd = new PointIndex();
		public void clear()
		{
			selectedPoint.hasValue = false;
			rangeStart.hasValue = false;
			rangeEnd.hasValue = false;
		}
		/** Get the minimum index which has changed between two sets of parameters */
		public int getMinChangedIndex(ChartParameters other)
		{
			PointIndex minIndex = new PointIndex();
			if (!selectedPoint.equals(other.selectedPoint)) {
				minIndex.setMin(selectedPoint);
				minIndex.setMin(other.selectedPoint);
			}
			if (!rangeStart.equals(other.rangeStart)) {
				minIndex.setMin(rangeStart);
				minIndex.setMin(other.rangeStart);
			}
			if (!rangeEnd.equals(other.rangeEnd)) {
				minIndex.setMin(rangeEnd);
				minIndex.setMin(other.rangeEnd);
			}
			return minIndex.index;
		}
		/** Get the maximum index which has changed between two sets of parameters */
		public int getMaxChangedIndex(ChartParameters other)
		{
			PointIndex maxIndex = new PointIndex();
			if (!selectedPoint.equals(other.selectedPoint)) {
				maxIndex.setMax(selectedPoint);
				maxIndex.setMax(other.selectedPoint);
			}
			if (!rangeStart.equals(other.rangeStart)) {
				maxIndex.setMax(rangeStart);
				maxIndex.setMax(other.rangeStart);
			}
			if (!rangeEnd.equals(other.rangeEnd)) {
				maxIndex.setMax(rangeEnd);
				maxIndex.setMax(other.rangeEnd);
			}
			return maxIndex.index;
		}
		/** @return true if the parameters are completely empty (cleared) */
		public boolean isEmpty()
		{
			return !selectedPoint.hasValue && !rangeStart.hasValue && !rangeEnd.hasValue;
		}
	}

	/** Current scale factor in x direction*/
	private double _xScaleFactor = 0.0;
	/** Data to show on chart */
	private ProfileData _data = null;
	/** Label for chart type, units */
	private JLabel _label = null;
	/** Right-click popup menu */
	private JPopupMenu _popup = null;
	/** Parameters last time chart was drawn */
	private ChartParameters _previousParameters = new ChartParameters();

	/** Possible scales to use */
	private static final int[] LINE_SCALES = {10000, 5000, 2000, 1000, 500, 200, 100, 50, 10, 5, 2, 1};
	/** Border width around black line */
	private static final int BORDER_WIDTH = 6;
	/** Minimum size for profile chart in pixels */
	private static final Dimension MINIMUM_SIZE = new Dimension(200, 110);
	/** Colour to use for text if no data found */
	private static final Color COLOR_NODATA_TEXT = Color.GRAY;


	/**
	 * Constructor
	 * @param inTrackInfo Track info object
	 */
	public ProfileChart(TrackInfo inTrackInfo)
	{
		super(inTrackInfo);
		_data = new AltitudeData(inTrackInfo.getTrack());
		addMouseListener(this);
		setLayout(new FlowLayout(FlowLayout.LEFT));
		_label = new JLabel("Altitude"); // text will be replaced later
		add(_label);
		makePopup();
	}


	/**
	 * Override minimum size method to restrict slider
	 */
	public Dimension getMinimumSize()
	{
		return MINIMUM_SIZE;
	}

	/**
	 * Override paint method to draw map
	 * @param g Graphics object
	 */
	public void paint(Graphics g)
	{
		super.paint(g);
		// Set antialiasing depending on Config
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			Config.getConfigBoolean(Config.KEY_ANTIALIAS) ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
		ColourScheme colourScheme = Config.getColourScheme();
		paintBackground(g, colourScheme);

		if (_track == null || _track.getNumPoints() <= 0)
		{
			return;
		}

		_label.setText(_data.getLabel());
		int width = getWidth();
		int height = getHeight();

		// Set up colours
		final Color barColour = colourScheme.getColour(ColourScheme.IDX_POINT);
		final Color rangeColour = colourScheme.getColour(ColourScheme.IDX_SELECTION);
		final Color currentColour = colourScheme.getColour(ColourScheme.IDX_PRIMARY);
		final Color secondColour = colourScheme.getColour(ColourScheme.IDX_SECONDARY);
		final Color lineColour = colourScheme.getColour(ColourScheme.IDX_LINES);

		// message if no data for the current field in track
		if (!_data.hasData())
		{
			g.setColor(lineColour);
			g.drawString(I18nManager.getText(_data.getNoDataKey()), 50, (height+_label.getHeight())/2);
			paintChildren(g);
			return;
		}

		// Find minimum and maximum values to plot
		double minValue = _data.getMinValue();
		double maxValue = _data.getMaxValue();
		if (maxValue <= minValue) {maxValue = minValue + 1; minValue--;}

		final int numPoints = _track.getNumPoints();
		_xScaleFactor = 1.0 * (width - 2 * BORDER_WIDTH - 1) / numPoints;
		int usableHeight = height - 2 * BORDER_WIDTH - _label.getHeight();
		double yScaleFactor = 1.0 * usableHeight / (maxValue - minValue);
		int barWidth = (int) (_xScaleFactor + 1.0);
		int selectedPoint = _trackInfo.getSelection().getCurrentPointIndex();
		// selection start, end
		int selectionStart = -1, selectionEnd = -1;
		if (_trackInfo.getSelection().hasRangeSelected())
		{
			selectionStart = _trackInfo.getSelection().getStart();
			selectionEnd = _trackInfo.getSelection().getEnd();
		}

		int y = 0;
		double value = 0.0;
		// horizontal lines for scale - set to round numbers eg 500
		final int lineScale = getLineScale(minValue, maxValue);
		double scaleValue = Math.ceil(minValue/lineScale) * lineScale;
		final int zeroY = height - BORDER_WIDTH - (int) (yScaleFactor * (0.0 - minValue));

		g.setColor(lineColour);
		if (lineScale >= 1)
		{
			while (scaleValue < maxValue)
			{
				y = height - BORDER_WIDTH - (int) (yScaleFactor * (scaleValue - minValue));
				g.drawLine(BORDER_WIDTH + 1, y, width - BORDER_WIDTH - 1, y);
				scaleValue += lineScale;
			}
		}
		else if (minValue < 0.0)
		{
			// just draw zero line
			y = zeroY;
			g.drawLine(BORDER_WIDTH + 1, y, width - BORDER_WIDTH - 1, y);
		}

		try
		{
			// loop through points
			g.setColor(barColour);
			for (int p = 0; p < numPoints; p++)
			{
				if (p == selectionStart)
					g.setColor(rangeColour);
				else if (p == (selectionEnd+1))
					g.setColor(barColour);

				final int x = (int) (_xScaleFactor * p) + 1;
				if (_data.hasData(p))
				{
					value = _data.getData(p);
					// Normal case is the minimum value greater than zero
					if (minValue >= 0)
					{
						y = (int) (yScaleFactor * (value - minValue));
						g.fillRect(BORDER_WIDTH+x, height-BORDER_WIDTH - y, barWidth, y);
					}
					else if (value >= 0.0)
					{
						// Bar upwards from the zero line
						y = height-BORDER_WIDTH - (int) (yScaleFactor * (value - minValue));
						g.fillRect(BORDER_WIDTH+x, y, barWidth, zeroY - y);
					}
					else
					{
						// Bar downwards from the zero line
						int barHeight = (int) (yScaleFactor * value);
						g.fillRect(BORDER_WIDTH+x, zeroY, barWidth, -barHeight);
					}
				}
			}

			// current point (make sure it's drawn last)
			if (selectedPoint >= 0)
			{
				final int sel_x = (int) (_xScaleFactor * selectedPoint) + 1;
				g.setColor(secondColour);
				g.fillRect(BORDER_WIDTH + sel_x, height-usableHeight-BORDER_WIDTH+1, barWidth, usableHeight-2);
				if (_data.hasData(selectedPoint))
				{
					g.setColor(currentColour);
					value = _data.getData(selectedPoint);
					y = (int) (yScaleFactor * (value - minValue));
					g.fillRect(BORDER_WIDTH + sel_x, height-BORDER_WIDTH - y, barWidth, y);
				}
			}
		}
		catch (NullPointerException npe)
		{ // ignore, probably due to data being changed
		}
		// Draw numbers on top of the graph to mark scale
		if (lineScale >= 1)
		{
			int textHeight = g.getFontMetrics().getHeight();
			scaleValue = (int) (minValue / lineScale + 1) * lineScale;
			if (minValue < 0.0) {scaleValue -= lineScale;}
			y = 0;
			g.setColor(currentColour);
			while (scaleValue < maxValue)
			{
				y = height - BORDER_WIDTH - (int) (yScaleFactor * (scaleValue - minValue));
				// Limit y so String isn't above border
				if (y < (BORDER_WIDTH + textHeight)) {
					y = BORDER_WIDTH + textHeight;
				}
				g.drawString(""+(int)scaleValue, BORDER_WIDTH + 5, y);
				scaleValue += lineScale;
			}
		}
		// Paint label on top
		paintChildren(g);
	}


	/**
	 * Paint the background for the chart
	 * @param inG graphics object
	 * @param inColourScheme colour scheme
	 */
	private void paintBackground(Graphics inG, ColourScheme inColourScheme)
	{
		final int width = getWidth();
		final int height = getHeight();
		// Get colours
		final Color borderColour = inColourScheme.getColour(ColourScheme.IDX_BORDERS);
		final Color backgroundColour = inColourScheme.getColour(ColourScheme.IDX_BACKGROUND);
		// background
		inG.setColor(backgroundColour);
		inG.fillRect(0, 0, width, height);
		if (width < 2*BORDER_WIDTH || height < 2*BORDER_WIDTH) return;
		// Display message if no data to be displayed
		if (_track == null || _track.getNumPoints() <= 0)
		{
			inG.setColor(COLOR_NODATA_TEXT);
			inG.drawString(I18nManager.getText("display.nodata"), 50, height/2);
		}
		else
		{
			inG.setColor(borderColour);
			inG.drawRect(BORDER_WIDTH, BORDER_WIDTH + _label.getHeight(),
				width - 2*BORDER_WIDTH, height-2*BORDER_WIDTH-_label.getHeight());
		}
	}

	/**
	 * Make the popup menu for right-clicking the chart
	 */
	private synchronized void makePopup()
	{
		_popup = new JPopupMenu();
		JMenuItem altItem = new JMenuItem(I18nManager.getText("fieldname.altitude"));
		altItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				changeView(Field.ALTITUDE);
			}});
		_popup.add(altItem);
		JMenuItem speedItem = new JMenuItem(I18nManager.getText("fieldname.speed"));
		speedItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				changeView(Field.SPEED);
			}});
		_popup.add(speedItem);
		JMenuItem vertSpeedItem = new JMenuItem(I18nManager.getText("fieldname.verticalspeed"));
		vertSpeedItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				changeView(Field.VERTICAL_SPEED);
			}});
		_popup.add(vertSpeedItem);
		// Go through track's master field list, see if any other fields to list
		boolean addSeparator = true;
		FieldList fields = _track.getFieldList();
		for (int i=0; i<fields.getNumFields(); i++)
		{
			Field field = fields.getField(i);
			if (!field.isBuiltIn())
			{
				if (addSeparator) {_popup.addSeparator();}
				addSeparator = false;
				JMenuItem item = new JMenuItem(field.getName());
				item.addActionListener(new MenuClicker(field));
				_popup.add(item);
			}
		}
	}

	/**
	 * Work out the scale for the horizontal lines
	 * @param inMin min value of data
	 * @param inMax max value of data
	 * @return scale separation, or -1 for no scale
	 */
	private int getLineScale(double inMin, double inMax)
	{
		if ((inMax - inMin) < 2.0) {
			return -1;
		}
		int numScales = LINE_SCALES.length;
		for (int i=0; i<numScales; i++)
		{
			int scale = LINE_SCALES[i];
			int numLines = (int)(inMax / scale) - (int)(inMin / scale);
			// Check for too many lines
			if (numLines > 10) return -1;
			// If more than 1 line then use this scale
			if (numLines > 1) return scale;
		}
		// no suitable scale found so just use minimum
		return LINE_SCALES[numScales-1];
	}


	/**
	 * Method to inform map that data has changed
	 */
	public void dataUpdated(byte inUpdateType)
	{
		// Try not to recalculate all the values unless necessary
		if (inUpdateType != SELECTION_CHANGED)
		{
			_data.init(Config.getUnitSet());
			_previousParameters.clear();
		}
		// Update the menu if necessary
		if ((inUpdateType & DATA_ADDED_OR_REMOVED) > 0) {
			makePopup();
		}

		ChartParameters currentParameters = new ChartParameters();
		currentParameters.selectedPoint.set(_trackInfo.getSelection().getCurrentPointIndex());
		if (_trackInfo.getSelection().hasRangeSelected())
		{
			currentParameters.rangeStart.set(_trackInfo.getSelection().getStart());
			currentParameters.rangeEnd.set(_trackInfo.getSelection().getEnd());
		}
		if (inUpdateType == SELECTION_CHANGED)
		{
			triggerPartialRepaint(currentParameters);
		}
		else
		{
			repaint();
		}
		_previousParameters = currentParameters;
	}

	/**
	 * For performance reasons, only repaint the part of the graphics affected by
	 * the change in selection
	 * @param currentParameters - contains the current selected point, range
	 */
	private void triggerPartialRepaint(ChartParameters currentParameters)
	{
		int minPointIndex = currentParameters.getMinChangedIndex(_previousParameters);
		minPointIndex = Math.max(minPointIndex, 0);
		int maxPointIndex = currentParameters.getMaxChangedIndex(_previousParameters);
		if (maxPointIndex < minPointIndex) {
			maxPointIndex = _trackInfo.getTrack().getNumPoints() - 1;
		}
		// System.out.println("Redraw from index: " + minPointIndex + " to " + maxPointIndex);
		final int region_x = (int) (_xScaleFactor * minPointIndex) + BORDER_WIDTH - 2;
		final int region_width = (int) (_xScaleFactor * (maxPointIndex-minPointIndex+2)) + 6;
		repaint(region_x, 0, region_width, getHeight());
		// System.out.println("Partial repaint, x=" + region_x + ", region_width=" + region_width);
	}

	/**
	 * React to click on profile display
	 */
	public void mouseClicked(MouseEvent e)
	{
		if (_track == null || _track.getNumPoints() < 1) {return;}
		// left clicks
		if (!e.isMetaDown())
		{
			int xClick = e.getX();
			int yClick = e.getY();
			// Check click is within main area (not in border)
			if (xClick > BORDER_WIDTH && yClick > BORDER_WIDTH && xClick < (getWidth() - BORDER_WIDTH)
				&& yClick < (getHeight() - BORDER_WIDTH))
			{
				// work out which data point is nearest and select it
				int pointNum = (int) ((e.getX() - BORDER_WIDTH) / _xScaleFactor);
				// If shift clicked, then extend selection
				if (e.isShiftDown()) {
					_trackInfo.extendSelection(pointNum);
				}
				else {
					_trackInfo.selectPoint(pointNum);
				}
			}
		}
		else
		{
			// right clicks
			_popup.show(this, e.getX(), e.getY());
		}
	}

	/**
	 * Called by clicking on popup menu to change the view
	 * @param inField field to show
	 */
	private void changeView(Field inField)
	{
		if (inField == Field.ALTITUDE)
		{
			if (!(_data instanceof AltitudeData)) {
				_data = new AltitudeData(_track);
			}
		}
		else if (inField == Field.SPEED)
		{
			if (!(_data instanceof SpeedData)) {
				_data = new SpeedData(_track);
			}
		}
		else if (inField == Field.VERTICAL_SPEED)
		{
			if (!(_data instanceof VerticalSpeedData)) {
				_data = new VerticalSpeedData(_track);
			}
		}
		else
		{
			if (!(_data instanceof ArbitraryData) || ((ArbitraryData)_data).getField() != inField) {
				_data = new ArbitraryData(_track, inField);
			}
		}
		_data.init(Config.getUnitSet());
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
