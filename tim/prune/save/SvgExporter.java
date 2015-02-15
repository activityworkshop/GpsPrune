package tim.prune.save;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.config.Config;
import tim.prune.data.Track;
import tim.prune.function.Export3dFunction;
import tim.prune.gui.DialogCloser;
import tim.prune.load.GenericFileFilter;
import tim.prune.threedee.ThreeDModel;

/**
 * Class to export a 3d scene of the track to a specified Svg file
 */
public class SvgExporter extends Export3dFunction
{
	private Track _track = null;
	private JDialog _dialog = null;
	private JFileChooser _fileChooser = null;
	private double _phi = 0.0, _theta = 0.0;
	private JTextField _phiField = null, _thetaField = null;
	private JTextField _altitudeFactorField = null;
	private JCheckBox _gradientsCheckbox = null;
	private static double _scaleFactor = 1.0;


	/**
	 * Constructor
	 * @param inApp App object
	 */
	public SvgExporter(App inApp)
	{
		super(inApp);
		_track = inApp.getTrackInfo().getTrack();
		// Set default rotation angles
		_phi = 30;  _theta = 55;
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.exportsvg";
	}

	/**
	 * Set the rotation angles using coordinates for the camera
	 * @param inX X coordinate of camera
	 * @param inY Y coordinate of camera
	 * @param inZ Z coordinate of camera
	 */
	public void setCameraCoordinates(double inX, double inY, double inZ)
	{
		// Calculate phi and theta based on camera x,y,z
		_phi = Math.toDegrees(Math.atan2(inX, inZ));
		_theta = Math.toDegrees(Math.atan2(inY, Math.sqrt(inX*inX + inZ*inZ)));
	}


	/**
	 * Show the dialog to select options and export file
	 */
	public void begin()
	{
		// Make dialog window to select angles, colours etc
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, I18nManager.getText(getNameKey()), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.getContentPane().add(makeDialogComponents());
		}

		// Set angles
		NumberFormat threeDP = NumberFormat.getNumberInstance();
		threeDP.setMaximumFractionDigits(3);
		_phiField.setText(threeDP.format(_phi));
		_thetaField.setText(threeDP.format(_theta));
		// Set vertical scale
		_altitudeFactorField.setText("" + _altFactor);
		// Show dialog
		_dialog.pack();
		_dialog.setVisible(true);
	}


	/**
	 * Make the dialog components to select the export options
	 * @return Component holding gui elements
	 */
	private Component makeDialogComponents()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		JLabel introLabel = new JLabel(I18nManager.getText("dialog.exportsvg.text"));
		introLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 6, 4));
		panel.add(introLabel, BorderLayout.NORTH);
		// OK, Cancel buttons
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton okButton = new JButton(I18nManager.getText("button.ok"));
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				doExport();
				_dialog.dispose();
			}
		});
		buttonPanel.add(okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_dialog.dispose();
			}
		});
		buttonPanel.add(cancelButton);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		// central panel
		JPanel centralPanel = new JPanel();
		centralPanel.setLayout(new GridLayout(0, 2, 10, 4));

		// rotation angles
		JLabel phiLabel = new JLabel(I18nManager.getText("dialog.exportsvg.phi"));
		phiLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		centralPanel.add(phiLabel);
		_phiField = new JTextField("" + _phi);
		_phiField.addKeyListener(new DialogCloser(_dialog));
		centralPanel.add(_phiField);
		JLabel thetaLabel = new JLabel(I18nManager.getText("dialog.exportsvg.theta"));
		thetaLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		centralPanel.add(thetaLabel);
		_thetaField = new JTextField("" + _theta);
		centralPanel.add(_thetaField);
		// Altitude exaggeration
		JLabel altFactorLabel = new JLabel(I18nManager.getText("dialog.3d.altitudefactor"));
		altFactorLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		centralPanel.add(altFactorLabel);
		_altitudeFactorField = new JTextField("" + _altFactor);
		centralPanel.add(_altitudeFactorField);
		// Checkbox for gradients or not
		JLabel gradientsLabel = new JLabel(I18nManager.getText("dialog.exportsvg.gradients"));
		gradientsLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		centralPanel.add(gradientsLabel);
		_gradientsCheckbox = new JCheckBox();
		_gradientsCheckbox.setSelected(true);
		centralPanel.add(_gradientsCheckbox);

		// add this grid to the holder panel
		JPanel holderPanel = new JPanel();
		holderPanel.setLayout(new BorderLayout(5, 5));
		JPanel boxPanel = new JPanel();
		boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.Y_AXIS));
		boxPanel.add(centralPanel);
		holderPanel.add(boxPanel, BorderLayout.CENTER);

		panel.add(holderPanel, BorderLayout.CENTER);
		return panel;
	}


	/**
	 * Select the file and export data to it
	 */
	private void doExport()
	{
		// Copy camera coordinates
		_phi = checkAngle(_phiField.getText());
		_theta = checkAngle(_thetaField.getText());

		// OK pressed, so choose output file
		if (_fileChooser == null)
		{
			_fileChooser = new JFileChooser();
			_fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
			_fileChooser.setFileFilter(new GenericFileFilter("filetype.svg", new String[] {"svg"}));
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
				File file = _fileChooser.getSelectedFile();
				if (!file.getName().toLowerCase().endsWith(".svg")) {
					file = new File(file.getAbsolutePath() + ".svg");
				}
				// Check if file exists and if necessary prompt for overwrite
				Object[] buttonTexts = {I18nManager.getText("button.overwrite"), I18nManager.getText("button.cancel")};
				if (!file.exists() || JOptionPane.showOptionDialog(_parentFrame,
						I18nManager.getText("dialog.save.overwrite.text"),
						I18nManager.getText("dialog.save.overwrite.title"), JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE, null, buttonTexts, buttonTexts[1])
					== JOptionPane.YES_OPTION)
				{
					// Export the file
					if (exportFile(file))
					{
						// file saved - store directory in config for later
						Config.setConfigString(Config.KEY_TRACK_DIR, file.getParentFile().getAbsolutePath());
					}
					else {
						// export failed so need to choose again
						chooseAgain = true;
					}
				}
				else {
					// overwrite cancelled so need to choose again
					chooseAgain = true;
				}
			}
		} while (chooseAgain);
	}


	/**
	 * Export the track data to the specified file
	 * @param inFile File object to save to
	 * @return true if successful
	 */
	private boolean exportFile(File inFile)
	{
		FileWriter writer = null;
		// find out the line separator for this system
		String lineSeparator = System.getProperty("line.separator");
		try
		{
			// create and scale model
			ThreeDModel model = new ThreeDModel(_track);
			try
			{
				// try to use given altitude factor
				_altFactor = Double.parseDouble(_altitudeFactorField.getText());
				model.setAltitudeFactor(_altFactor);
			}
			catch (NumberFormatException nfe) {}
			model.scale();
			_scaleFactor = 200 / model.getModelSize();

			boolean useGradients = _gradientsCheckbox.isSelected();

			// Create file and write basics
			writer = new FileWriter(inFile);
			writeStartOfFile(writer, useGradients, lineSeparator);
			writeBasePlane(writer, model.getModelSize(), lineSeparator);
			// write out cardinal letters NESW
			writeCardinals(writer, model.getModelSize(), lineSeparator);

			// write out points
			writeDataPoints(writer, model, useGradients, lineSeparator);
			writeEndOfFile(writer, lineSeparator);

			// everything worked
			UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.save.ok1")
				 + " " + _track.getNumPoints() + " " + I18nManager.getText("confirm.save.ok2")
				 + " " + inFile.getAbsolutePath());
			return true;
		}
		catch (IOException ioe)
		{
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("error.save.failed") + " : " + ioe.getMessage(),
				I18nManager.getText("error.save.dialogtitle"), JOptionPane.ERROR_MESSAGE);
		}
		finally
		{
			// close file ignoring exceptions
			try {
				writer.close();
			}
			catch (Exception e) {}
		}
		return false;
	}


	/**
	 * Write the start of the Svg file
	 * @param inWriter Writer to use for writing file
	 * @param inUseGradients true to use gradients, false for flat fills
	 * @param inLineSeparator line separator to use
	 * @throws IOException on file writing error
	 */
	private static void writeStartOfFile(FileWriter inWriter, boolean inUseGradients,
		String inLineSeparator)
	throws IOException
	{
		inWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
		inWriter.write(inLineSeparator);
		inWriter.write("<!-- Svg file produced by GpsPrune - see http://activityworkshop.net/ -->");
		inWriter.write(inLineSeparator);
		inWriter.write("<svg width=\"800\" height=\"700\">");
		inWriter.write(inLineSeparator);
		if (inUseGradients)
		{
			final String defs = "<defs>" +
				"<radialGradient id=\"wayfill\" cx=\"0.5\" cy=\"0.5\" r=\"0.5\" fx=\"0.5\" fy=\"0.5\">" +
				"<stop offset=\"0%\" stop-color=\"#2323aa\"/>" +
				"<stop offset=\"100%\" stop-color=\"#000080\"/>" +
				"</radialGradient>" + inLineSeparator +
				"<radialGradient id=\"trackfill\" cx=\"0.5\" cy=\"0.5\" r=\"0.5\" fx=\"0.5\" fy=\"0.5\">" +
				"<stop offset=\"0%\" stop-color=\"#23aa23\"/>" +
				"<stop offset=\"100%\" stop-color=\"#008000\"/>" +
				"</radialGradient>" +
				"</defs>";
		    inWriter.write(defs);
			inWriter.write(inLineSeparator);
		}
		inWriter.write("<g inkscape:label=\"Layer 1\" inkscape:groupmode=\"layer\" id=\"layer1\">");
		inWriter.write(inLineSeparator);
	}

	/**
	 * Write the base plane
	 * @param inWriter Writer to use for writing file
	 * @param inModelSize model size
	 * @param inLineSeparator line separator to use
	 * @throws IOException on file writing error
	 */
	private void writeBasePlane(FileWriter inWriter, double inModelSize, String inLineSeparator)
	throws IOException
	{
		// Use model size and camera angles to draw path for base rectangle (using 3d transform)
		int[] coords1 = convertCoordinates(-inModelSize, -inModelSize, 0);
		int[] coords2 = convertCoordinates(inModelSize, -inModelSize, 0);
		int[] coords3 = convertCoordinates(inModelSize, inModelSize, 0);
		int[] coords4 = convertCoordinates(-inModelSize, inModelSize, 0);
		final String corners = "M " + coords1[0] + "," + coords1[1]
			+ " L " + coords2[0] + "," + coords2[1]
			+ " L " + coords3[0] + "," + coords3[1]
			+ " L " + coords4[0] + "," + coords4[1] + " z";
		inWriter.write("<path style=\"fill:#446666;stroke:#000000;\" d=\"" + corners + "\" id=\"rect1\" />");
		inWriter.write(inLineSeparator);
	}

	/**
	 * Write the cardinal letters NESW
	 * @param inWriter Writer to use for writing file
	 * @param inModelSize model size
	 * @param inLineSeparator line separator to use
	 * @throws IOException on file writing error
	 */
	private void writeCardinals(FileWriter inWriter, double inModelSize, String inLineSeparator)
	throws IOException
	{
		// Use model size and camera angles to calculate positions
		int[] coordsN = convertCoordinates(0, inModelSize, 0);
		writeCardinal(inWriter, coordsN[0], coordsN[1], "cardinal.n", inLineSeparator);
		int[] coordsE = convertCoordinates(inModelSize, 0, 0);
		writeCardinal(inWriter, coordsE[0], coordsE[1], "cardinal.e", inLineSeparator);
		int[] coordsS = convertCoordinates(0, -inModelSize, 0);
		writeCardinal(inWriter, coordsS[0], coordsS[1], "cardinal.s", inLineSeparator);
		int[] coordsW = convertCoordinates(-inModelSize, 0, 0);
		writeCardinal(inWriter, coordsW[0], coordsW[1], "cardinal.w", inLineSeparator);
	}

	/**
	 * Write a single cardinal letter
	 * @param inWriter Writer to use for writing file
	 * @param inX x coordinate
	 * @param inY y coordinate
	 * @param inKey key for string to write
	 * @param inLineSeparator line separator to use
	 * @throws IOException on file writing error
	 */
	private static void writeCardinal(FileWriter inWriter, int inX, int inY, String inKey, String inLineSeparator)
	throws IOException
	{
		inWriter.write("<text x=\"" + inX + "\" y=\"" + inY + "\" font-size=\"26\" fill=\"black\" " +
			"stroke=\"white\" stroke-width=\"0.5\">");
		inWriter.write(I18nManager.getText(inKey));
		inWriter.write("</text>");
		inWriter.write(inLineSeparator);
	}

	/**
	 * Convert the given 3d coordinates into 2d coordinates by rotating and mapping
	 * @param inX x coordinate (east)
	 * @param inY y coordinate (north)
	 * @param inZ z coordinate (up)
	 * @return 2d coordinates as integer array
	 */
	private int[] convertCoordinates(double inX, double inY, double inZ)
	{
		// Rotate by phi degrees around vertical axis
		final double cosPhi = Math.cos(Math.toRadians(_phi));
		final double sinPhi = Math.sin(Math.toRadians(_phi));
		final double x2 = inX * cosPhi + inY * sinPhi;
		final double y2 = inY * cosPhi - inX * sinPhi;
		final double z2 = inZ;
		// Rotate by theta degrees around horizontal axis
		final double cosTheta = Math.cos(Math.toRadians(_theta));
		final double sinTheta = Math.sin(Math.toRadians(_theta));
		double x3 = x2;
		double y3 = y2 * sinTheta + z2 * cosTheta;
		// don't need to calculate z3
		// Scale results to sensible scale for svg
		x3 = x3 * _scaleFactor + 400;
		y3 = -y3 * _scaleFactor + 350;
		return new int[] {(int) x3, (int) y3};
	}

	/**
	 * Finish off the file by closing the tags
	 * @param inWriter Writer to use for writing file
	 * @param inLineSeparator line separator to use
	 * @throws IOException on file writing error
	 */
	private static void writeEndOfFile(FileWriter inWriter, String inLineSeparator)
	throws IOException
	{
		inWriter.write(inLineSeparator);
		inWriter.write("</g></svg>");
		inWriter.write(inLineSeparator);
	}

	/**
	 * Write out all the data points to the file in the balls-and-sticks style
	 * @param inWriter Writer to use for writing file
	 * @param inModel model object for getting data points
	 * @param inUseGradients true to use gradients, false for flat fills
	 * @param inLineSeparator line separator to use
	 * @throws IOException on file writing error
	 */
	private void writeDataPoints(FileWriter inWriter, ThreeDModel inModel, boolean inUseGradients,
		String inLineSeparator)
	throws IOException
	{
		final int numPoints = inModel.getNumPoints();
		TreeSet<SvgFragment> fragments = new TreeSet<SvgFragment>();
		for (int i=0; i<numPoints; i++)
		{
			StringBuilder builder = new StringBuilder();
			int[] coords = convertCoordinates(inModel.getScaledHorizValue(i), inModel.getScaledVertValue(i),
				inModel.getScaledAltValue(i));
			// vertical rod (if altitude positive)
			if (inModel.getScaledAltValue(i) > 0.0)
			{
				int[] baseCoords = convertCoordinates(inModel.getScaledHorizValue(i), inModel.getScaledVertValue(i), 0);
				builder.append("<line x1=\"").append(baseCoords[0]).append("\" y1=\"").append(baseCoords[1])
					.append("\" x2=\"").append(coords[0]).append("\" y2=\"").append(coords[1])
					.append("\" stroke=\"gray\" stroke-width=\"3\" />");
				builder.append(inLineSeparator);
			}
			// ball (different according to type)
			if (inModel.getPointType(i) == ThreeDModel.POINT_TYPE_WAYPOINT)
			{
				// waypoint ball
				builder.append("<circle cx=\"").append(coords[0]).append("\" cy=\"").append(coords[1])
					.append("\" r=\"11\" ").append(inUseGradients?"fill=\"url(#wayfill)\"":"fill=\"blue\"")
					.append(" stroke=\"green\" stroke-width=\"0.2\" />");
			}
			else
			{
				// normal track point ball
				builder.append("<circle cx=\"").append(coords[0]).append("\" cy=\"").append(coords[1])
					.append("\" r=\"7\" ").append(inUseGradients?"fill=\"url(#trackfill)\"":"fill=\"green\"")
					.append(" stroke=\"blue\" stroke-width=\"0.2\" />");
			}
			builder.append(inLineSeparator);
			// add to set
			fragments.add(new SvgFragment(builder.toString(), coords[1]));
		}

		// Iterate over the sorted set and write to file
		Iterator<SvgFragment> iterator = fragments.iterator();
		while (iterator.hasNext()) {
			inWriter.write(iterator.next().getFragment());
		}
	}


	/**
	 * Check the given angle value
	 * @param inString String entered by user
	 * @return validated value
	 */
	private static double checkAngle(String inString)
	{
		double value = 0.0;
		try {
			value = Double.parseDouble(inString);
		}
		catch (Exception e) {} // ignore parse failures
		return value;
	}
}
