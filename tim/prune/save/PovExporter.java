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

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;

import tim.prune.I18nManager;
import tim.prune.data.Track;
import tim.prune.threedee.LineDialog;
import tim.prune.threedee.ThreeDModel;

/**
 * Class to export track information
 * into a specified Pov file
 */
public class PovExporter
{
	private JFrame _parentFrame = null;
	private Track _track = null;
	private JDialog _dialog = null;
	private JFileChooser _fileChooser = null;
	private String _cameraX = null, _cameraY = null, _cameraZ = null;
	private JTextField _cameraXField = null, _cameraYField = null, _cameraZField = null;
	private JTextField _fontName = null, _altitudeCapField = null;
	private int _altitudeCap = ThreeDModel.MINIMUM_ALTITUDE_CAP;

	// defaults
	private static final double DEFAULT_CAMERA_DISTANCE = 30.0;
	private static final String DEFAULT_FONT_FILE = "crystal.ttf";
	// alternative font: DejaVuSans-Bold.ttf


	/**
	 * Constructor giving frame and track
	 * @param inParentFrame parent frame
	 * @param inTrack track object to save
	 */
	public PovExporter(JFrame inParentFrame, Track inTrack)
	{
		_parentFrame = inParentFrame;
		_track = inTrack;
		// Set default camera coordinates
		_cameraX = "17"; _cameraY = "13"; _cameraZ = "-20";
	}


	/**
	 * Set the coordinates for the camera (can be any scale)
	 * @param inX X coordinate of camera
	 * @param inY Y coordinate of camera
	 * @param inZ Z coordinate of camera
	 */
	public void setCameraCoordinates(double inX, double inY, double inZ)
	{
		// calculate distance from origin
		double cameraDist = Math.sqrt(inX*inX + inY*inY + inZ*inZ);
		if (cameraDist > 0.0)
		{
			_cameraX = "" + (inX / cameraDist * DEFAULT_CAMERA_DISTANCE);
			_cameraY = "" + (inY / cameraDist * DEFAULT_CAMERA_DISTANCE);
			// Careful! Need to convert from java3d (right-handed) to povray (left-handed) coordinate system!
			_cameraZ = "" + (-inZ / cameraDist * DEFAULT_CAMERA_DISTANCE);
		}
	}


	/**
	 * @param inAltitudeCap altitude cap to use
	 */
	public void setAltitudeCap(int inAltitudeCap)
	{
		_altitudeCap = inAltitudeCap;
		if (_altitudeCap < ThreeDModel.MINIMUM_ALTITUDE_CAP)
		{
			_altitudeCap = ThreeDModel.MINIMUM_ALTITUDE_CAP;
		}
	}


	/**
	 * Show the dialog to select options and export file
	 */
	public void showDialog()
	{
		// Make dialog window to select angles, colours etc
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, I18nManager.getText("dialog.exportpov.title"), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.getContentPane().add(makeDialogComponents());
		}

		// Set angles
		_cameraXField.setText(_cameraX);
		_cameraYField.setText(_cameraY);
		_cameraZField.setText(_cameraZ);
		// Set vertical scale
		_altitudeCapField.setText("" + _altitudeCap);
		// Show dialog
		_dialog.pack();
		_dialog.show();
	}


	/**
	 * Make the dialog components to select the export options
	 * @return Component holding gui elements
	 */
	private Component makeDialogComponents()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(new JLabel(I18nManager.getText("dialog.exportpov.text")), BorderLayout.NORTH);
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
			public void actionPerformed(ActionEvent e)
			{
				_dialog.dispose();
			}
		});
		buttonPanel.add(cancelButton);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		// central panel
		JPanel centralPanel = new JPanel();
		centralPanel.setLayout(new GridLayout(0, 2, 10, 4));

		JLabel fontLabel = new JLabel(I18nManager.getText("dialog.exportpov.font"));
		fontLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		centralPanel.add(fontLabel);
		_fontName = new JTextField(DEFAULT_FONT_FILE, 12);
		_fontName.setAlignmentX(Component.LEFT_ALIGNMENT);
		centralPanel.add(_fontName);
		//coordinates of camera
		JLabel cameraXLabel = new JLabel(I18nManager.getText("dialog.exportpov.camerax"));
		cameraXLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		centralPanel.add(cameraXLabel);
		_cameraXField = new JTextField("" + _cameraX);
		centralPanel.add(_cameraXField);
		JLabel cameraYLabel = new JLabel(I18nManager.getText("dialog.exportpov.cameray"));
		cameraYLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		centralPanel.add(cameraYLabel);
		_cameraYField = new JTextField("" + _cameraY);
		centralPanel.add(_cameraYField);
		JLabel cameraZLabel = new JLabel(I18nManager.getText("dialog.exportpov.cameraz"));
		cameraZLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		centralPanel.add(cameraZLabel);
		_cameraZField = new JTextField("" + _cameraZ);
		centralPanel.add(_cameraZField);
		// Altitude capping
		JLabel altitudeCapLabel = new JLabel(I18nManager.getText("dialog.3d.altitudecap"));
		altitudeCapLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		centralPanel.add(altitudeCapLabel);
		_altitudeCapField = new JTextField("" + _altitudeCap);
		centralPanel.add(_altitudeCapField);

		JPanel flowPanel = new JPanel();
		flowPanel.add(centralPanel);
		
		// show lines button
		JButton showLinesButton = new JButton(I18nManager.getText("button.showlines"));
		showLinesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				// Need to scale model to find lines
				ThreeDModel model = new ThreeDModel(_track);
				model.scale();
				double[] latLines = model.getLatitudeLines();
				double[] lonLines = model.getLongitudeLines();
				LineDialog dialog = new LineDialog(_parentFrame, latLines, lonLines);
				dialog.showDialog();
			}
		});
		flowPanel.add(showLinesButton);
		panel.add(flowPanel, BorderLayout.CENTER);
		return panel;
	}


	/**
	 * Select the file and export data to it
	 */
	private void doExport()
	{
		// Copy camera coordinates
		_cameraX = checkCoordinate(_cameraXField.getText());
		_cameraY = checkCoordinate(_cameraYField.getText());
		_cameraZ = checkCoordinate(_cameraZField.getText());

		// OK pressed, so choose output file
		if (_fileChooser == null)
			_fileChooser = new JFileChooser();
		_fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		_fileChooser.setFileFilter(new FileFilter() {
			public boolean accept(File f)
			{
				return (f != null && (f.isDirectory() || f.getName().toLowerCase().endsWith(".pov")));
			}
			public String getDescription()
			{
				return I18nManager.getText("dialog.exportpov.filetype");
			}
		});
		_fileChooser.setAcceptAllFileFilterUsed(false);

		// Allow choose again if an existing file is selected
		boolean chooseAgain = false;
		do
		{
			chooseAgain = false;
			if (_fileChooser.showSaveDialog(_parentFrame) == JFileChooser.APPROVE_OPTION)
			{
				// OK pressed and file chosen
				File file = _fileChooser.getSelectedFile();
				if (!file.getName().toLowerCase().endsWith(".pov"))
				{
					file = new File(file.getAbsolutePath() + ".pov");
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
						// file saved
					}
					else
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
				// try to use given altitude cap
				_altitudeCap = Integer.parseInt(_altitudeCapField.getText());
				model.setAltitudeCap(_altitudeCap);
			}
			catch (NumberFormatException nfe) {}
			model.scale();

			// Create file and write basics
			writer = new FileWriter(inFile);
			writeStartOfFile(writer, model.getModelSize(), lineSeparator);

			// write out lat/long lines using model
			writeLatLongLines(writer, model, lineSeparator);

			// write out points
			writeDataPoints(writer, model, lineSeparator);

			// everything worked
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("dialog.save.ok1")
				 + " " + _track.getNumPoints() + " " + I18nManager.getText("dialog.save.ok2")
				 + " " + inFile.getAbsolutePath(),
				I18nManager.getText("dialog.save.oktitle"), JOptionPane.INFORMATION_MESSAGE);
			return true;
		}
		catch (IOException ioe)
		{
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("error.save.failed") + ioe.getMessage(),
				I18nManager.getText("error.save.dialogtitle"), JOptionPane.ERROR_MESSAGE);
		}
		finally
		{
			// close file ignoring exceptions
			try
			{
				writer.close();
			}
			catch (Exception e) {}
		}
		return false;
	}


	/**
	 * Write the start of the Pov file, including base plane and lights
	 * @param inWriter Writer to use for writing file
	 * @param inModelSize model size
	 * @param inLineSeparator line separator to use
	 * @throws IOException on file writing error
	 */
	private void writeStartOfFile(FileWriter inWriter, double inModelSize, String inLineSeparator)
	throws IOException
	{
		inWriter.write("// Pov file produced by Prune - see http://activityworkshop.net/");
		inWriter.write(inLineSeparator);
		inWriter.write(inLineSeparator);
		// Select font based on user input
		String fontPath = _fontName.getText();
		if (fontPath == null || fontPath.equals(""))
		{
			fontPath = DEFAULT_FONT_FILE;
		}
		// Set up output
		String[] outputLines = {
		  "global_settings { ambient_light rgb <4, 4, 4> }", "",
		  "// Background and camera",
		  "background { color rgb <0, 0, 0> }",
		  // camera position
		  "camera {",
		  "  location <" + _cameraX + ", " + _cameraY + ", " + _cameraZ + ">",
		  "  look_at  <0, 0, 0>",
		  "}", "",
		// global declares
		  "// Global declares",
		  "#declare lat_line =",
		  "  cylinder {",
		  "   <-" + inModelSize + ", 0.1, 0>,",
		  "   <" + inModelSize + ", 0.1, 0>,",
		  "   0.1            // Radius",
		  "   pigment { color rgb <0.5 0.5 0.5> }",
		  "  }",
		  "#declare lon_line =",
		  "  cylinder {",
		  "   <0, 0.1, -" + inModelSize + ">,",
		  "   <0, 0.1, " + inModelSize + ">,",
		  "   0.1            // Radius",
		  "   pigment { color rgb <0.5 0.5 0.5> }",
		  "  }",
		  "#declare point_rod =",
		  "  cylinder {",
		  "   <0, 0, 0>,",
		  "   <0, 1, 0>,",
		  "   0.15",
		  "   open",
		  "   pigment { color rgb <0.5 0.5 0.5> }",
		  "  }", "",
		  // TODO: Export rods to POV?  How to store in data?
		  "#declare waypoint_sphere =",
		  "  sphere {",
		  "   <0, 0, 0>, 0.4",
		  "    texture {",
		  "       pigment {color rgb <0.1 0.1 1.0>}",
		  "       finish { phong 1 }",
		  "    }",
		  "  }",
		  "#declare track_sphere0 =",
		  "  sphere {",
		  "   <0, 0, 0>, 0.3", // size should depend on model size
		  "   texture {",
		  "      pigment {color rgb <0.2 1.0 0.2>}",
		  "      finish { phong 1 }",
		  "   }",
		  " }",
		  "#declare track_sphere1 =",
		  "  sphere {",
		  "   <0, 0, 0>, 0.3", // size should depend on model size
		  "   texture {",
		  "      pigment {color rgb <0.6 1.0 0.2>}",
		  "      finish { phong 1 }",
		  "   }",
		  " }",
		  "#declare track_sphere2 =",
		  "  sphere {",
		  "   <0, 0, 0>, 0.3", // size should depend on model size
		  "   texture {",
		  "      pigment {color rgb <1.0 1.0 0.1>}",
		  "      finish { phong 1 }",
		  "   }",
		  " }",
		  "#declare track_sphere3 =",
		  "  sphere {",
		  "   <0, 0, 0>, 0.3", // size should depend on model size
		  "   texture {",
		  "      pigment {color rgb <1.0 1.0 1.0>}",
		  "      finish { phong 1 }",
		  "   }",
		  " }",
		  "#declare track_sphere4 =",
		  "  sphere {",
		  "   <0, 0, 0>, 0.3", // size should depend on model size
		  "   texture {",
		  "      pigment {color rgb <0.1 1.0 1.0>}",
		  "      finish { phong 1 }",
		  "   }",
		  " }", "",
		  "// Base plane",
		  "box {",
		  "   <-" + inModelSize + ", -0.15, -" + inModelSize + ">,  // Near lower left corner",
		  "   <" + inModelSize + ", 0.15, " + inModelSize + ">   // Far upper right corner",
		  "   pigment { color rgb <0.5 0.75 0.8> }",
		  "}", "",
		// write cardinals
		  "// Cardinal letters N,S,E,W",
		  "text {",
		  "  ttf \"" + fontPath + "\" \"" + I18nManager.getText("cardinal.n") + "\" 0.3, 0",
		  "  pigment { color rgb <1 1 1> }",
		  "  translate <0, 0.2, " + inModelSize + ">",
		  "}",
		  "text {",
		  "  ttf \"" + fontPath + "\" \"" + I18nManager.getText("cardinal.s") + "\" 0.3, 0",
		  "  pigment { color rgb <1 1 1> }",
		  "  translate <0, 0.2, -" + inModelSize + ">",
		  "}",
		  "text {",
		  "  ttf \"" + fontPath + "\" \"" + I18nManager.getText("cardinal.e") + "\" 0.3, 0",
		  "  pigment { color rgb <1 1 1> }",
		  "  translate <" + (inModelSize * 0.97) + ", 0.2, 0>",
		  "}",
		  "text {",
		  "  ttf \"" + fontPath + "\" \"" + I18nManager.getText("cardinal.w") + "\" 0.3, 0",
		  "  pigment { color rgb <1 1 1> }",
		  "  translate <-" + (inModelSize * 1.03) + ", 0.2, 0>",
		  "}", "",
		  // TODO: Light positions should relate to model size
		  "// lights",
		  "light_source { <-1, 9, -4> color rgb <0.5 0.5 0.5>}",
		  "light_source { <1, 6, -14> color rgb <0.6 0.6 0.6>}",
		  "light_source { <11, 12, 8> color rgb <0.3 0.3 0.3>}",
		  "",
		};
		// write strings to file
		int numLines = outputLines.length;
		for (int i=0; i<numLines; i++)
		{
			inWriter.write(outputLines[i]);
			inWriter.write(inLineSeparator);
		}
	}


	/**
	 * Write out all the lat and long lines to the file
	 * @param inWriter Writer to use for writing file
	 * @param inModel model object for getting lat/long lines
	 * @param inLineSeparator line separator to use
	 * @throws IOException on file writing error
	 */
	private void writeLatLongLines(FileWriter inWriter, ThreeDModel inModel, String inLineSeparator)
	throws IOException
	{
		inWriter.write("// Latitude and longitude lines:");
		inWriter.write(inLineSeparator);
		int numlines = inModel.getLatitudeLines().length;
		for (int i=0; i<numlines; i++)
		{
			// write cylinder to file
			inWriter.write("object { lat_line translate <0, 0, " + inModel.getScaledLatitudeLine(i) + "> }");
			inWriter.write(inLineSeparator);
		}
		numlines = inModel.getLongitudeLines().length;
		for (int i=0; i<numlines; i++)
		{
			// write cylinder to file
			inWriter.write("object { lon_line translate <" + inModel.getScaledLongitudeLine(i) + ", 0, 0> }");
			inWriter.write(inLineSeparator);
		}
		inWriter.write(inLineSeparator);
	}


	/**
	 * Write out all the data points to the file
	 * @param inWriter Writer to use for writing file
	 * @param inModel model object for getting data points
	 * @param inLineSeparator line separator to use
	 * @throws IOException on file writing error
	 */
	private void writeDataPoints(FileWriter inWriter, ThreeDModel inModel, String inLineSeparator)
	throws IOException
	{
		inWriter.write("// Data points:");
		inWriter.write(inLineSeparator);
		int numPoints = inModel.getNumPoints();
		for (int i=0; i<numPoints; i++)
		{
			// ball (different according to type)
			if (inModel.getPointType(i) == ThreeDModel.POINT_TYPE_WAYPOINT)
			{
				// waypoint ball
				inWriter.write("object { waypoint_sphere translate <" + inModel.getScaledHorizValue(i)
					+ "," + inModel.getScaledAltValue(i) + "," + inModel.getScaledVertValue(i) + "> }");
			}
			else
			{
				// normal track point ball
				inWriter.write("object { track_sphere" + checkHeightCode(inModel.getPointHeightCode(i))
					+ " translate <" + inModel.getScaledHorizValue(i) + "," + inModel.getScaledAltValue(i)
					+ "," + inModel.getScaledVertValue(i) + "> }");
			}
			inWriter.write(inLineSeparator);
			// vertical rod (if altitude positive)
			if (inModel.getScaledAltValue(i) > 0.0)
			{
				inWriter.write("object { point_rod translate <" + inModel.getScaledHorizValue(i) + ",0,"
					+ inModel.getScaledVertValue(i) + "> scale <1," + inModel.getScaledAltValue(i) + ",1> }");
				inWriter.write(inLineSeparator);
			}
		}
		inWriter.write(inLineSeparator);
	}


	/**
	 * @param inCode height code to check
	 * @return validated height code within range 0 to max
	 */
	private static byte checkHeightCode(byte inCode)
	{
		final byte maxHeightCode = 4;
		if (inCode < 0) return 0;
		if (inCode > maxHeightCode) return maxHeightCode;
		return inCode;
	}


	/**
	 * Check the given coordinate
	 * @param inString String entered by user
	 * @return validated String value
	 */
	private static String checkCoordinate(String inString)
	{
		double value = 0.0;
		try
		{
			value = Double.parseDouble(inString);
		}
		catch (Exception e) {} // ignore parse failures
		return "" + value;
	}
}
