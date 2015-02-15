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
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.config.Config;
import tim.prune.data.NumberUtils;
import tim.prune.data.Track;
import tim.prune.function.Export3dFunction;
import tim.prune.gui.DialogCloser;
import tim.prune.load.GenericFileFilter;
import tim.prune.threedee.LineDialog;
import tim.prune.threedee.ThreeDModel;

/**
 * Class to export a 3d scene of the track to a specified Pov file
 */
public class PovExporter extends Export3dFunction
{
	private Track _track = null;
	private JDialog _dialog = null;
	private JFileChooser _fileChooser = null;
	private String _cameraX = null, _cameraY = null, _cameraZ = null;
	private JTextField _cameraXField = null, _cameraYField = null, _cameraZField = null;
	private JTextField _fontName = null, _altitudeFactorField = null;
	private JRadioButton _ballsAndSticksButton = null;

	// defaults
	private static final double DEFAULT_CAMERA_DISTANCE = 30.0;
	private static final String DEFAULT_FONT_FILE = "crystal.ttf";


	/**
	 * Constructor
	 * @param inApp App object
	 */
	public PovExporter(App inApp)
	{
		super(inApp);
		_track = inApp.getTrackInfo().getTrack();
		// Set default camera coordinates
		_cameraX = "17"; _cameraY = "13"; _cameraZ = "-20";
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.exportpov";
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
			_cameraX = NumberUtils.formatNumber(inX / cameraDist * DEFAULT_CAMERA_DISTANCE, 5);
			_cameraY = NumberUtils.formatNumber(inY / cameraDist * DEFAULT_CAMERA_DISTANCE, 5);
			// Careful! Need to convert from java3d (right-handed) to povray (left-handed) coordinate system!
			_cameraZ = NumberUtils.formatNumber(-inZ / cameraDist * DEFAULT_CAMERA_DISTANCE, 5);
		}
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
		_cameraXField.setText(_cameraX);
		_cameraYField.setText(_cameraY);
		_cameraZField.setText(_cameraZ);
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
		JLabel introLabel = new JLabel(I18nManager.getText("dialog.exportpov.text"));
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
		String defaultFont = Config.getConfigString(Config.KEY_POVRAY_FONT);
		if (defaultFont == null || defaultFont.equals("")) {
			defaultFont = DEFAULT_FONT_FILE;
		}
		_fontName = new JTextField(defaultFont, 12);
		_fontName.setAlignmentX(Component.LEFT_ALIGNMENT);
		_fontName.addKeyListener(new DialogCloser(_dialog));
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
		// Altitude exaggeration
		JLabel altitudeCapLabel = new JLabel(I18nManager.getText("dialog.3d.altitudefactor"));
		altitudeCapLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		centralPanel.add(altitudeCapLabel);
		_altitudeFactorField = new JTextField("1.0");
		centralPanel.add(_altitudeFactorField);

		// Radio buttons for style - balls on sticks or tubes
		JPanel stylePanel = new JPanel();
		stylePanel.setLayout(new GridLayout(0, 2, 10, 4));
		JLabel styleLabel = new JLabel(I18nManager.getText("dialog.exportpov.modelstyle"));
		styleLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		stylePanel.add(styleLabel);
		JPanel radioPanel = new JPanel();
		radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS));
		_ballsAndSticksButton = new JRadioButton(I18nManager.getText("dialog.exportpov.ballsandsticks"));
		_ballsAndSticksButton.setSelected(false);
		radioPanel.add(_ballsAndSticksButton);
		JRadioButton tubesButton = new JRadioButton(I18nManager.getText("dialog.exportpov.tubesandwalls"));
		tubesButton.setSelected(true);
		radioPanel.add(tubesButton);
		ButtonGroup group = new ButtonGroup();
		group.add(_ballsAndSticksButton); group.add(tubesButton);
		stylePanel.add(radioPanel);

		// add this grid to the holder panel
		JPanel holderPanel = new JPanel();
		holderPanel.setLayout(new BorderLayout(5, 5));
		JPanel boxPanel = new JPanel();
		boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.Y_AXIS));
		boxPanel.add(centralPanel);
		boxPanel.add(stylePanel);
		holderPanel.add(boxPanel, BorderLayout.CENTER);

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
		JPanel flowPanel = new JPanel();
		flowPanel.setLayout(new FlowLayout());
		flowPanel.add(showLinesButton);
		holderPanel.add(flowPanel, BorderLayout.EAST);
		panel.add(holderPanel, BorderLayout.CENTER);
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
		{
			_fileChooser = new JFileChooser();
			_fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
			_fileChooser.setFileFilter(new GenericFileFilter("filetype.pov", new String[] {"pov"}));
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
						// Store directory in config for later
						Config.setConfigString(Config.KEY_TRACK_DIR, file.getParentFile().getAbsolutePath());
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
				double altFactor = Double.parseDouble(_altitudeFactorField.getText());
				model.setAltitudeFactor(altFactor);
			}
			catch (NumberFormatException nfe) { // parse failed, reset
				_altitudeFactorField.setText("1.0");
			}
			model.scale();

			// Create file and write basics
			writer = new FileWriter(inFile);
			writeStartOfFile(writer, model.getModelSize(), lineSeparator);

			// write out lat/long lines using model
			writeLatLongLines(writer, model, lineSeparator);

			// write out points
			if (_ballsAndSticksButton.isSelected()) {
				writeDataPointsBallsAndSticks(writer, model, lineSeparator);
			}
			else {
				writeDataPointsTubesAndWalls(writer, model, lineSeparator);
			}

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
		inWriter.write("// Pov file produced by GpsPrune - see http://activityworkshop.net/");
		inWriter.write(inLineSeparator);
		inWriter.write(inLineSeparator);
		// Select font based on user input
		String fontPath = _fontName.getText();
		if (fontPath == null || fontPath.equals(""))
		{
			fontPath = DEFAULT_FONT_FILE;
		}
		else {
			Config.setConfigString(Config.KEY_POVRAY_FONT, fontPath);
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
		  // MAYBE: Export rods to POV?  How to store in data?
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
		  " }",
		  "#declare track_sphere_t =",
		  "  sphere {",
		  "   <0, 0, 0>, 0.25", // size should depend on model size
		  "   texture {",
		  "      pigment {color rgb <0.6 1.0 0.2>}",
		  "      finish { phong 1 }",
		  "   } no_shadow",
		  " }",
		  "#declare wall_colour = rgbt <0.5, 0.5, 0.5, 0.3>;", "",
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
		  // MAYBE: Light positions should relate to model size
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
	 * Write out all the data points to the file in the balls-and-sticks style
	 * @param inWriter Writer to use for writing file
	 * @param inModel model object for getting data points
	 * @param inLineSeparator line separator to use
	 * @throws IOException on file writing error
	 */
	private void writeDataPointsBallsAndSticks(FileWriter inWriter, ThreeDModel inModel, String inLineSeparator)
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
	 * Write out all the data points to the file in the tubes-and-walls style
	 * @param inWriter Writer to use for writing file
	 * @param inModel model object for getting data points
	 * @param inLineSeparator line separator to use
	 * @throws IOException on file writing error
	 */
	private void writeDataPointsTubesAndWalls(FileWriter inWriter, ThreeDModel inModel, String inLineSeparator)
	throws IOException
	{
		inWriter.write("// Data points:");
		inWriter.write(inLineSeparator);
		int numPoints = inModel.getNumPoints();
		int numTrackPoints = 0;
		// Loop over all points and write out waypoints as balls
		for (int i=0; i<numPoints; i++)
		{
			if (inModel.getPointType(i) == ThreeDModel.POINT_TYPE_WAYPOINT)
			{
				// waypoint ball
				inWriter.write("object { waypoint_sphere translate <" + inModel.getScaledHorizValue(i)
					+ "," + inModel.getScaledAltValue(i) + "," + inModel.getScaledVertValue(i) + "> }");
				// vertical rod (if altitude positive)
				if (inModel.getScaledAltValue(i) > 0.0)
				{
					inWriter.write(inLineSeparator);
					inWriter.write("object { point_rod translate <" + inModel.getScaledHorizValue(i) + ",0,"
						+ inModel.getScaledVertValue(i) + "> scale <1," + inModel.getScaledAltValue(i) + ",1> }");
				}
				inWriter.write(inLineSeparator);
			}
			else {numTrackPoints++;}
		}
		inWriter.write(inLineSeparator);

		// Loop over all the track segments
		ArrayList<ModelSegment> segmentList = getSegmentList(inModel);
		Iterator<ModelSegment> segmentIterator = segmentList.iterator();
		while (segmentIterator.hasNext())
		{
			ModelSegment segment = segmentIterator.next();
			int segLength = segment.getNumTrackPoints();

			// if the track segment is long enough, do a cubic spline sphere sweep
			if (segLength <= 1)
			{
				// single point in segment - just draw sphere
				int index = segment.getStartIndex();
				inWriter.write("object { track_sphere_t"
					+ " translate <" + inModel.getScaledHorizValue(index) + "," + inModel.getScaledAltValue(index)
					+ "," + inModel.getScaledVertValue(index) + "> }");
				// maybe draw some kind of polygon too or rod?
			}
			else
			{
				writeSphereSweep(inWriter, inModel, segment, inLineSeparator);
			}

			// Write wall underneath segment
			if (segLength > 1)
			{
				writePolygonWall(inWriter, inModel, segment, inLineSeparator);
			}
		}
	}


	/**
	 * Write out a single sphere sweep using either cubic spline or linear spline
	 * @param inWriter Writer to use for writing file
	 * @param inModel model object for getting data points
	 * @param inSegment model segment to draw
	 * @param inLineSeparator line separator to use
	 * @throws IOException on file writing error
	 */
	private static void writeSphereSweep(FileWriter inWriter, ThreeDModel inModel, ModelSegment inSegment, String inLineSeparator)
	throws IOException
	{
		// 3d sphere sweep
		inWriter.write("// Sphere sweep:");
		inWriter.write(inLineSeparator);
		String splineType = inSegment.getNumTrackPoints() < 5?"linear_spline":"cubic_spline";
		inWriter.write("sphere_sweep { "); inWriter.write(splineType);
		inWriter.write(" " + inSegment.getNumTrackPoints() + ",");
		inWriter.write(inLineSeparator);
		// Loop over all points in this segment and write out sphere sweep
		for (int i=inSegment.getStartIndex(); i<=inSegment.getEndIndex(); i++)
		{
			if (inModel.getPointType(i) != ThreeDModel.POINT_TYPE_WAYPOINT)
			{
				inWriter.write("  <" + inModel.getScaledHorizValue(i) + "," + inModel.getScaledAltValue(i)
					+ "," + inModel.getScaledVertValue(i) + ">, 0.25");
				inWriter.write(inLineSeparator);
			}
		}
		inWriter.write("  tolerance 0.1");
		inWriter.write(inLineSeparator);
		inWriter.write("  texture { pigment {color rgb <0.6 1.0 0.2>}  finish {phong 1} }");
		inWriter.write(inLineSeparator);
		inWriter.write("  no_shadow");
		inWriter.write(inLineSeparator);
		inWriter.write("}");
		inWriter.write(inLineSeparator);
	}


	/**
	 * Write out a single polygon-based wall for the tubes-and-walls style
	 * @param inWriter Writer to use for writing file
	 * @param inModel model object for getting data points
	 * @param inSegment model segment to draw
	 * @param inLineSeparator line separator to use
	 * @throws IOException on file writing error
	 */
	private static void writePolygonWall(FileWriter inWriter, ThreeDModel inModel, ModelSegment inSegment, String inLineSeparator)
	throws IOException
	{
		// wall
		inWriter.write(inLineSeparator);
		inWriter.write("// wall between sweep and floor:");
		inWriter.write(inLineSeparator);
		// Loop over all points in this segment again and write out polygons
		int prevIndex = -1;
		for (int i=inSegment.getStartIndex(); i<=inSegment.getEndIndex(); i++)
		{
			if (inModel.getPointType(i) != ThreeDModel.POINT_TYPE_WAYPOINT)
			{
				if (prevIndex >= 0)
				{
					double xDiff = inModel.getScaledHorizValue(i) - inModel.getScaledHorizValue(prevIndex);
					double yDiff = inModel.getScaledVertValue(i) - inModel.getScaledVertValue(prevIndex);
					double dist = Math.sqrt(xDiff * xDiff + yDiff * yDiff);
					if (dist > 0)
					{
						inWriter.write("polygon {");
						inWriter.write("  5, <" + inModel.getScaledHorizValue(prevIndex) + ", 0.0, " + inModel.getScaledVertValue(prevIndex) + ">,");
						inWriter.write(" <" + inModel.getScaledHorizValue(prevIndex) + ", " + inModel.getScaledAltValue(prevIndex) + ", "
							+ inModel.getScaledVertValue(prevIndex) + ">,");
						inWriter.write(" <" + inModel.getScaledHorizValue(i) + ", " + inModel.getScaledAltValue(i) + ", "
							+ inModel.getScaledVertValue(i) + ">,");
						inWriter.write(" <" + inModel.getScaledHorizValue(i) + ", 0.0, " + inModel.getScaledVertValue(i) + ">,");
						inWriter.write(" <" + inModel.getScaledHorizValue(prevIndex) + ", 0.0, " + inModel.getScaledVertValue(prevIndex) + ">");
						inWriter.write("  pigment { color wall_colour } no_shadow");
						inWriter.write("}");
						inWriter.write(inLineSeparator);
					}
				}
				prevIndex = i;
			}
		}
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

	/**
	 * Go through the points making a list of the segment starts and the number of track points in each segment
	 * @param inModel model containing data
	 * @return list of ModelSegment objects
	 */
	private static ArrayList<ModelSegment> getSegmentList(ThreeDModel inModel)
	{
		ArrayList<ModelSegment> segmentList = new ArrayList<ModelSegment>();
		if (inModel != null && inModel.getNumPoints() > 0)
		{
			ModelSegment currSegment = null;
			int numTrackPoints = 0;
			for (int i=0; i<inModel.getNumPoints(); i++)
			{
				if (inModel.getPointType(i) != ThreeDModel.POINT_TYPE_WAYPOINT)
				{
					if (inModel.getPointType(i) == ThreeDModel.POINT_TYPE_SEGMENT_START || currSegment == null)
					{
						// start of segment
						if (currSegment != null)
						{
							currSegment.setEndIndex(i-1);
							currSegment.setNumTrackPoints(numTrackPoints);
							segmentList.add(currSegment);
							numTrackPoints = 0;
						}
						currSegment = new ModelSegment(i);
					}
					numTrackPoints++;
				}
			}
			// Add last segment to list
			if (currSegment != null && numTrackPoints > 0)
			{
				currSegment.setEndIndex(inModel.getNumPoints()-1);
				currSegment.setNumTrackPoints(numTrackPoints);
				segmentList.add(currSegment);
			}
		}
		return segmentList;
	}
}
