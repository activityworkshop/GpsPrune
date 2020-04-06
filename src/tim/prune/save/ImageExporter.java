package tim.prune.save;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.config.ColourScheme;
import tim.prune.config.Config;
import tim.prune.data.DataPoint;
import tim.prune.data.DoubleRange;
import tim.prune.data.Track;
import tim.prune.gui.BaseImageDefinitionPanel;
import tim.prune.gui.GuiGridLayout;
import tim.prune.gui.WholeNumberField;
import tim.prune.gui.colour.PointColourer;
import tim.prune.gui.map.MapSource;
import tim.prune.gui.map.MapSourceLibrary;
import tim.prune.gui.map.MapUtils;
import tim.prune.gui.map.WpIconDefinition;
import tim.prune.gui.map.WpIconLibrary;
import tim.prune.load.GenericFileFilter;
import tim.prune.threedee.ImageDefinition;

/**
 * Class to handle the exporting of map images, optionally with track data drawn on top.
 * This allows images larger than the screen to be generated.
 */
public class ImageExporter extends GenericFunction implements BaseImageConsumer
{
	private JDialog   _dialog = null;
	private JCheckBox _drawDataCheckbox = null;
	private JCheckBox _drawTrackPointsCheckbox = null;
	private WholeNumberField _textScaleField = null;
	private BaseImageDefinitionPanel _baseImagePanel = null;
	private JFileChooser _fileChooser = null;
	private JButton   _okButton = null;

	/**
	 * Constructor
	 * @param inApp App object
	 */
	public ImageExporter(App inApp)
	{
		super(inApp);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.exportimage";
	}

	/**
	 * Begin the function by showing the input dialog
	 */
	public void begin()
	{
		// Make dialog window
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, I18nManager.getText(getNameKey()), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.getContentPane().add(makeDialogComponents());
			_dialog.pack();
			_textScaleField.setValue(100);
		}

		// Check if there is a cache to use
		if (!BaseImageConfigDialog.isImagePossible())
		{
			_app.showErrorMessage(getNameKey(), "dialog.exportimage.noimagepossible");
			return;
		}

		_baseImagePanel.updateBaseImageDetails();
		baseImageChanged();
		// Show dialog
		_dialog.setVisible(true);
	}

	/**
	 * Make the dialog components to select the export options
	 * @return Component holding gui elements
	 */
	private Component makeDialogComponents()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(4, 4));
		// Checkbox for drawing track or not
		_drawDataCheckbox = new JCheckBox(I18nManager.getText("dialog.exportimage.drawtrack"));
		_drawDataCheckbox.setSelected(true); // draw by default
		// Also whether to draw track points or not
		_drawTrackPointsCheckbox = new JCheckBox(I18nManager.getText("dialog.exportimage.drawtrackpoints"));
		_drawTrackPointsCheckbox.setSelected(true);
		// Add listener to en/disable trackpoints checkbox
		_drawDataCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				_drawTrackPointsCheckbox.setEnabled(_drawDataCheckbox.isSelected());
			}
		});

		// TODO: Maybe have other controls such as line width, symbol scale factor
		JPanel controlsPanel = new JPanel();
		GuiGridLayout grid = new GuiGridLayout(controlsPanel);
		grid.add(new JLabel(I18nManager.getText("dialog.exportimage.textscalepercent") + ": "));
		_textScaleField = new WholeNumberField(3);
		_textScaleField.setText("888");
		grid.add(_textScaleField);

		// OK, Cancel buttons
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		_okButton = new JButton(I18nManager.getText("button.ok"));
		_okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				doExport();
				_baseImagePanel.getGrouter().clearMapImage();
				_dialog.dispose();
			}
		});
		buttonPanel.add(_okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_baseImagePanel.getGrouter().clearMapImage();
				_dialog.dispose();
			}
		});
		buttonPanel.add(cancelButton);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		// Listener to close dialog if escape pressed
		KeyAdapter closer = new KeyAdapter() {
			public void keyReleased(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					_dialog.dispose();
					_baseImagePanel.getGrouter().clearMapImage();
				}
			}
		};
		_drawDataCheckbox.addKeyListener(closer);

		// Panel for the base image
		_baseImagePanel = new BaseImageDefinitionPanel(this, _dialog, _app.getTrackInfo().getTrack());

		// Panel for the checkboxes at the top
		JPanel checkPanel = new JPanel();
		checkPanel.setLayout(new BoxLayout(checkPanel, BoxLayout.Y_AXIS));
		checkPanel.add(_drawDataCheckbox);
		checkPanel.add(_drawTrackPointsCheckbox);

		// add these panels to the holder panel
		JPanel holderPanel = new JPanel();
		holderPanel.setLayout(new BorderLayout(5, 5));
		holderPanel.add(checkPanel, BorderLayout.NORTH);
		holderPanel.add(controlsPanel, BorderLayout.CENTER);
		holderPanel.add(_baseImagePanel, BorderLayout.SOUTH);
		holderPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

		panel.add(holderPanel, BorderLayout.NORTH);
		return panel;
	}


	/**
	 * Select the file and export data to it
	 */
	private void doExport()
	{
		_okButton.setEnabled(false);
		// OK pressed, so choose output file
		if (_fileChooser == null)
		{
			_fileChooser = new JFileChooser();
			_fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
			_fileChooser.setFileFilter(new GenericFileFilter("filetype.png", new String[] {"png"}));
			_fileChooser.setAcceptAllFileFilterUsed(false);
			// start from directory in config which should be set
			final String configDir = Config.getConfigString(Config.KEY_TRACK_DIR);
			if (configDir != null) {_fileChooser.setCurrentDirectory(new File(configDir));}
		}

		// Allow choose again if an existing file is selected
		boolean chooseAgain = false;
		do
		{
			chooseAgain = false;
			if (_fileChooser.showSaveDialog(_parentFrame) == JFileChooser.APPROVE_OPTION)
			{
				// OK pressed and file chosen
				File pngFile = _fileChooser.getSelectedFile();
				if (!pngFile.getName().toLowerCase().endsWith(".png"))
				{
					pngFile = new File(pngFile.getAbsolutePath() + ".png");
				}
				// Check if file exists and if necessary prompt for overwrite
				Object[] buttonTexts = {I18nManager.getText("button.overwrite"), I18nManager.getText("button.cancel")};
				if (!pngFile.exists() || JOptionPane.showOptionDialog(_parentFrame,
						I18nManager.getText("dialog.save.overwrite.text"),
						I18nManager.getText("dialog.save.overwrite.title"), JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE, null, buttonTexts, buttonTexts[1])
					== JOptionPane.YES_OPTION)
				{
					// Export the file
					if (!exportFile(pngFile))
					{
						// export failed so need to choose again
						chooseAgain = true;
					}
				}
				else
				{
					// overwrite cancelled so need to choose again
					chooseAgain = true;
				}
			}
		} while (chooseAgain);
	}

	/**
	 * Export the track data to the specified file
	 * @param inPngFile File object to save to
	 * @return true if successful
	 */
	private boolean exportFile(File inPngFile)
	{
		// Get the image file from the grouter
		ImageDefinition imageDef = _baseImagePanel.getImageDefinition();
		MapSource source = MapSourceLibrary.getSource(imageDef.getSourceIndex());
		MapGrouter grouter = _baseImagePanel.getGrouter();
		GroutedImage baseImage = grouter.getMapImage(_app.getTrackInfo().getTrack(), source,
			imageDef.getZoom());
		if (baseImage == null || !baseImage.isValid())
		{
			_app.showErrorMessage(getNameKey(), "dialog.exportpov.cannotmakebaseimage");
			return true;
		}
		try
		{
			if (_drawDataCheckbox.isSelected())
			{
				// Draw the track on top of this image
				drawData(baseImage);
			}
			// Write composite image to file
			if (!ImageIO.write(baseImage.getImage(), "png", inPngFile)) {
				_app.showErrorMessage(getNameKey(), "dialog.exportpov.cannotmakebaseimage");
				return false; // choose again - the image creation worked but the save failed
			}
		}
		catch (IOException ioe) {
			System.err.println("Can't write image: " + ioe.getClass().getName());
		}
		return true;
	}

	/**
	 * Draw the track and waypoint data from the current Track onto the given image
	 * @param inImage GroutedImage from map tiles
	 */
	private void drawData(GroutedImage inImage)
	{
		// Work out x, y limits for drawing
		DoubleRange xRange = inImage.getXRange();
		DoubleRange yRange = inImage.getYRange();
		final int zoomFactor = 1 << _baseImagePanel.getImageDefinition().getZoom();
		Graphics g = inImage.getImage().getGraphics();
		// TODO: Set line width, style etc
		final PointColourer pointColourer = _app.getPointColourer();
		final Color defaultPointColour = Config.getColourScheme().getColour(ColourScheme.IDX_POINT);
		g.setColor(defaultPointColour);

		// Loop to draw all track points
		final Track track = _app.getTrackInfo().getTrack();
		final int numPoints = track.getNumPoints();
		int prevX = 0, prevY = 0;
		boolean gotPreviousPoint = false;
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = track.getPoint(i);
			if (!point.isWaypoint())
			{
				// Determine what colour to use to draw the track point
				if (pointColourer != null)
				{
					Color c = pointColourer.getColour(i);
					g.setColor(c == null ? defaultPointColour : c);
				}
				double x = track.getX(i) - xRange.getMinimum();
				double y = track.getY(i) - yRange.getMinimum();
				// use zoom level to calculate pixel coords on image
				int px = (int) (x * zoomFactor * 256), py = (int) (y * zoomFactor * 256);
				// System.out.println("Point: x=" + x + ", px=" + px + ", y=" + y + ", py=" + py);
				if (!point.getSegmentStart() && gotPreviousPoint) {
					// draw from previous point to this one
					g.drawLine(prevX, prevY, px, py);
				}
				// Only draw points if requested
				if (_drawTrackPointsCheckbox.isSelected())
				{
					g.drawRect(px-2, py-2, 3, 3);
				}
				// save coordinates
				prevX = px; prevY = py;
				gotPreviousPoint = true;
			}
		}

		// Now the waypoints
		final Color textColour = Config.getColourScheme().getColour(ColourScheme.IDX_TEXT);
		g.setColor(textColour);
		WpIconDefinition wpIconDefinition = null;
		final int wpType = Config.getConfigInt(Config.KEY_WAYPOINT_ICONS);
		if (wpType != WpIconLibrary.WAYPT_DEFAULT)
		{
			wpIconDefinition = WpIconLibrary.getIconDefinition(wpType, WpIconLibrary.SIZE_MEDIUM);
		}
		// Loop again to draw waypoints
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = track.getPoint(i);
			if (point.isWaypoint())
			{
				// use zoom level to calculate pixel coords on image
				double x = track.getX(i) - xRange.getMinimum();
				double y = track.getY(i) - yRange.getMinimum();
				int px = (int) (x * zoomFactor * 256), py = (int) (y * zoomFactor * 256);
				// Fill Rect or draw icon image?
				g.fillRect(px-3, py-3, 6, 6);
				if (wpIconDefinition == null)
				{
					g.fillRect(px-3, py-3, 6, 6);
				}
				else
				{
					g.drawImage(wpIconDefinition.getImageIcon().getImage(), px-wpIconDefinition.getXOffset(),
						py-wpIconDefinition.getYOffset(), null);
				}
			}
		}
		// Set text size according to input
		int fontScalePercent = _textScaleField.getValue();
		if (fontScalePercent > 0 && fontScalePercent <= 999)
		{
			Font gFont = g.getFont();
			g.setFont(gFont.deriveFont((float) (gFont.getSize() * 0.01 * fontScalePercent)));
		}
		FontMetrics fm = g.getFontMetrics();
		final int nameHeight = fm.getHeight();
		final int imageSize = inImage.getImageSize();

		// Loop over points again, draw photo points
		final Color photoColour = Config.getColourScheme().getColour(ColourScheme.IDX_SECONDARY);
		g.setColor(photoColour);
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = track.getPoint(i);
			if (point.hasMedia())
			{
				// draw blob for each photo
				double x = track.getX(i) - xRange.getMinimum();
				double y = track.getY(i) - yRange.getMinimum();
				// use zoom level to calculate pixel coords on image
				int px = (int) (x * zoomFactor * 256), py = (int) (y * zoomFactor * 256);
				g.fillRect(px-3, py-3, 6, 6);
			}
		}

		// Loop over points again, now draw names for waypoints
		g.setColor(textColour);
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = track.getPoint(i);
			if (point.isWaypoint() && fontScalePercent > 0)
			{
				double x = track.getX(i) - xRange.getMinimum();
				double y = track.getY(i) - yRange.getMinimum();
				int px = (int) (x * zoomFactor * 256), py = (int) (y * zoomFactor * 256);

				// Figure out where to draw waypoint name so it doesn't obscure track
				String waypointName = point.getWaypointName();
				int nameWidth = fm.stringWidth(waypointName);
				boolean drawnName = false;
				// Make arrays for coordinates right left up down
				int[] nameXs = {px + 2, px - nameWidth - 2, px - nameWidth/2, px - nameWidth/2};
				int[] nameYs = {py + (nameHeight/2), py + (nameHeight/2), py - 2, py + nameHeight + 2};
				for (int extraSpace = 4; extraSpace < 13 && !drawnName; extraSpace+=2)
				{
					// Shift arrays for coordinates right left up down
					nameXs[0] += 2; nameXs[1] -= 2;
					nameYs[2] -= 2; nameYs[3] += 2;
					// Check each direction in turn right left up down
					for (int a=0; a<4; a++)
					{
						if (nameXs[a] > 0 && (nameXs[a] + nameWidth) < imageSize
							&& nameYs[a] < imageSize && (nameYs[a] - nameHeight) > 0
							&& !MapUtils.overlapsPoints(inImage.getImage(), nameXs[a], nameYs[a],
								nameWidth, nameHeight, textColour))
						{
							// Found a rectangle to fit - draw name here and quit
							g.drawString(waypointName, nameXs[a], nameYs[a]);
							drawnName = true;
							break;
						}
					}
				}
			}
		}

		// Maybe draw note at the bottom, export from GpsPrune?  Filename?
		// Note: Differences from main map: No mapPosition (modifying position and visible points),
		//       no selection, no opacities, maybe different scale/text factors
	}

	/**
	 * Base image has changed, need to enable/disable ok button
	 */
	public void baseImageChanged()
	{
		final boolean useImage = _baseImagePanel.getImageDefinition().getUseImage();
		final int zoomLevel = _baseImagePanel.getImageDefinition().getZoom();
		final boolean okEnabled = useImage && _baseImagePanel.getFoundData()
			&& MapGrouter.isZoomLevelOk(_app.getTrackInfo().getTrack(), zoomLevel);
		_okButton.setEnabled(okEnabled);
	}
}
