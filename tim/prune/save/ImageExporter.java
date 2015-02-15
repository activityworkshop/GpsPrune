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
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import tim.prune.App;
import tim.prune.DataSubscriber;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.config.ColourScheme;
import tim.prune.config.Config;
import tim.prune.data.DataPoint;
import tim.prune.data.DoubleRange;
import tim.prune.data.Track;
import tim.prune.gui.GuiGridLayout;
import tim.prune.gui.WholeNumberField;
import tim.prune.gui.map.MapSource;
import tim.prune.gui.map.MapSourceLibrary;
import tim.prune.gui.map.MapUtils;
import tim.prune.load.GenericFileFilter;

/**
 * Class to handle the exporting of map images, optionally with track data drawn on top.
 * This allows images larger than the screen to be generated.
 */
public class ImageExporter extends GenericFunction implements DataSubscriber
{
	private JDialog   _dialog = null;
	private JCheckBox _drawDataCheckbox = null;
	private WholeNumberField _textScaleField = null;
	private JLabel    _baseImageLabel = null;
	private BaseImageConfigDialog _baseImageConfig = null;
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
		// Make base image dialog too
		if (_baseImageConfig == null) {
			_baseImageConfig = new BaseImageConfigDialog(this, _dialog, _app.getTrackInfo().getTrack());
		}

		// Check if there is a cache to use
		if (!BaseImageConfigDialog.isImagePossible())
		{
			_app.showErrorMessage(getNameKey(), "dialog.exportimage.noimagepossible");
			return;
		}

		updateBaseImageDetails();
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
				MapGrouter.clearMapImage();
				_dialog.dispose();
			}
		});
		buttonPanel.add(_okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				MapGrouter.clearMapImage();
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
					MapGrouter.clearMapImage();
				}
			}
		};
		_drawDataCheckbox.addKeyListener(closer);

		// Panel for the base image
		JPanel imagePanel = new JPanel();
		imagePanel.setLayout(new BorderLayout(10, 4));
		imagePanel.add(new JLabel(I18nManager.getText("dialog.exportpov.baseimage") + ": "), BorderLayout.WEST);
		_baseImageLabel = new JLabel("Typical sourcename");
		imagePanel.add(_baseImageLabel, BorderLayout.CENTER);
		JButton baseImageButton = new JButton(I18nManager.getText("button.edit"));
		baseImageButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				changeBaseImage();
			}
		});
		baseImageButton.addKeyListener(closer);
		imagePanel.add(baseImageButton, BorderLayout.EAST);
		imagePanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(4, 4, 4, 4))
		);

		// add these panels to the holder panel
		JPanel holderPanel = new JPanel();
		holderPanel.setLayout(new BorderLayout(5, 5));
		holderPanel.add(_drawDataCheckbox, BorderLayout.NORTH);
		holderPanel.add(controlsPanel, BorderLayout.CENTER);
		holderPanel.add(imagePanel, BorderLayout.SOUTH);
		holderPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

		panel.add(holderPanel, BorderLayout.NORTH);
		return panel;
	}

	/**
	 * Change the base image by calling the BaseImageConfigDialog
	 */
	private void changeBaseImage()
	{
		// Check if there is a cache to use
		if (BaseImageConfigDialog.isImagePossible())
		{
			// Show new dialog to choose image details
			_baseImageConfig.beginWithImageYes();
		}
	}

	/**
	 * Callback from base image config dialog
	 */
	public void dataUpdated(byte inUpdateType)
	{
		updateBaseImageDetails();
	}

	/** Not required */
	public void actionCompleted(String inMessage) {
	}

	/**
	 * Update the description label according to the selected base image details
	 */
	private void updateBaseImageDetails()
	{
		String desc = null;
		if (_baseImageConfig.useImage())
		{
			MapSource source = MapSourceLibrary.getSource(_baseImageConfig.getSourceIndex());
			if (source != null) {
				desc = source.getName() + " ("
					+ _baseImageConfig.getZoomLevel() + ")";
			}
		}
		if (desc == null) {
			desc = I18nManager.getText("dialog.about.no");
		}
		_baseImageLabel.setText(desc);
		_okButton.setEnabled(_baseImageConfig.useImage() && _baseImageConfig.getFoundData()
			&& MapGrouter.isZoomLevelOk(_app.getTrackInfo().getTrack(), _baseImageConfig.getZoomLevel()));
	}

	/**
	 * Select the file and export data to it
	 */
	private void doExport()
	{
		// OK pressed, so choose output file
		_okButton.setEnabled(false);
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
		MapSource source = MapSourceLibrary.getSource(_baseImageConfig.getSourceIndex());
		GroutedImage baseImage = MapGrouter.getMapImage(_app.getTrackInfo().getTrack(), source,
			_baseImageConfig.getZoomLevel());
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
		int zoomFactor = 1 << _baseImageConfig.getZoomLevel();
		Graphics g = inImage.getImage().getGraphics();
		// TODO: Set colour, line width
		g.setColor(Config.getColourScheme().getColour(ColourScheme.IDX_POINT));

		// Loop over points
		final Track track = _app.getTrackInfo().getTrack();
		final int numPoints = track.getNumPoints();
		int prevX = 0, prevY = 0;
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = track.getPoint(i);
			if (!point.isWaypoint())
			{
				double x = track.getX(i) - xRange.getMinimum();
				double y = track.getY(i) - yRange.getMinimum();
				// use zoom level to calculate pixel coords on image
				int px = (int) (x * zoomFactor * 256), py = (int) (y * zoomFactor * 256);
				// System.out.println("Point: x=" + x + ", px=" + px + ", y=" + y + ", py=" + py);
				if (!point.getSegmentStart()) {
					// draw from previous point to this one
					g.drawLine(prevX, prevY, px, py);
				}
				// draw this point
				g.drawRect(px-2, py-2, 3, 3);
				// save coordinates
				prevX = px; prevY = py;
			}
		}
		// Draw waypoints
		final Color textColour = Config.getColourScheme().getColour(ColourScheme.IDX_TEXT);
		g.setColor(textColour);
		// Loop over points
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = track.getPoint(i);
			if (point.isWaypoint())
			{
				// draw blob for each waypoint
				double x = track.getX(i) - xRange.getMinimum();
				double y = track.getY(i) - yRange.getMinimum();
				// use zoom level to calculate pixel coords on image
				int px = (int) (x * zoomFactor * 256), py = (int) (y * zoomFactor * 256);
				g.fillRect(px-3, py-3, 6, 6);
			}
		}
		// Set text size according to input
		int fontScalePercent = _textScaleField.getValue();
		if (fontScalePercent > 10 && fontScalePercent <= 999)
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
			if (point.isWaypoint())
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
}
