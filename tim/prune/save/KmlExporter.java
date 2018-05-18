package tim.prune.save;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.config.ColourUtils;
import tim.prune.config.Config;
import tim.prune.data.Coordinate;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.RecentFile;
import tim.prune.data.Timestamp;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;
import tim.prune.data.UnitSetLibrary;
import tim.prune.gui.DialogCloser;
import tim.prune.gui.ImageUtils;
import tim.prune.gui.WholeNumberField;
import tim.prune.gui.colour.ColourChooser;
import tim.prune.gui.colour.ColourPatch;
import tim.prune.load.GenericFileFilter;
import tim.prune.save.xml.XmlUtils;

/**
 * Class to export track information
 * into a specified Kml or Kmz file
 */
public class KmlExporter extends GenericFunction implements Runnable
{
	private TrackInfo _trackInfo = null;
	private Track _track = null;
	private JDialog _dialog = null;
	private JTextField _descriptionField = null;
	private PointTypeSelector _pointTypeSelector = null;
	private JRadioButton _gxExtensionsRadio = null;
	private JCheckBox _altitudesCheckbox = null;
	private JCheckBox _kmzCheckbox = null;
	private JCheckBox _exportImagesCheckbox = null;
	private JLabel _imageSizeLabel = null;
	private WholeNumberField _imageSizeField = null;
	private ColourPatch _colourPatch = null;
	private JLabel _progressLabel = null;
	private JProgressBar _progressBar = null;
	private Dimension[] _imageDimensions = null;
	private JFileChooser _fileChooser = null;
	private File _exportFile = null;
	private JButton _okButton = null;
	private boolean _cancelPressed = false;
	private ColourChooser _colourChooser = null;

	// Filename of Kml file within zip archive
	private static final String KML_FILENAME_IN_KMZ = "doc.kml";
	// Default width and height of thumbnail images in Kmz
	private static final int DEFAULT_THUMBNAIL_WIDTH = 240;
	// Default track colour
	private static final Color DEFAULT_TRACK_COLOUR = new Color(204, 0, 0); // red


	/**
	 * Constructor
	 * @param inApp app object
	 */
	public KmlExporter(App inApp)
	{
		super(inApp);
		_trackInfo = inApp.getTrackInfo();
		_track = _trackInfo.getTrack();
	}

	/** Get name key */
	public String getNameKey() {
		return "function.exportkml";
	}

	/**
	 * Show the dialog to select options and export file
	 */
	public void begin()
	{
		// Make dialog window including whether to compress to kmz (and include pictures) or not
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, I18nManager.getText(getNameKey()), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			_dialog.getContentPane().add(makeDialogComponents());
			_dialog.pack();
			_colourChooser = new ColourChooser(_dialog);
		}
		// Fill in image size from config
		_imageSizeField.setValue(Config.getConfigInt(Config.KEY_KMZ_IMAGE_SIZE));
		enableCheckboxes();
		_descriptionField.setEnabled(true);
		_okButton.setEnabled(true);
		_progressLabel.setText("");
		_progressBar.setVisible(false);
		_dialog.setVisible(true);
	}


	/**
	 * Create dialog components
	 * @return Panel containing all gui elements in dialog
	 */
	private Component makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout(0, 5));
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		// Make a central panel with the text box and checkboxes
		JPanel descPanel = new JPanel();
		descPanel.setLayout(new FlowLayout());
		descPanel.add(new JLabel(I18nManager.getText("dialog.exportkml.text")));
		_descriptionField = new JTextField(20);
		_descriptionField.addKeyListener(new DialogCloser(_dialog));
		descPanel.add(_descriptionField);
		descPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainPanel.add(descPanel);
		dialogPanel.add(mainPanel, BorderLayout.CENTER);
		// point type selection
		_pointTypeSelector = new PointTypeSelector();
		_pointTypeSelector.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainPanel.add(_pointTypeSelector);
		// Colour definition
		Color trackColour = ColourUtils.colourFromHex(Config.getConfigString(Config.KEY_KML_TRACK_COLOUR));
		if (trackColour == null) {
			trackColour = DEFAULT_TRACK_COLOUR;
		}
		_colourPatch = new ColourPatch(trackColour);
		_colourPatch.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				_colourChooser.showDialog(_colourPatch.getBackground());
				Color colour = _colourChooser.getChosenColour();
				if (colour != null) _colourPatch.setColour(colour);
			}
		});
		JPanel colourPanel = new JPanel();
		colourPanel.add(new JLabel(I18nManager.getText("dialog.exportkml.trackcolour")));
		colourPanel.add(_colourPatch);
		mainPanel.add(colourPanel);
		// Pair of radio buttons for standard/extended KML
		JRadioButton standardKmlRadio = new JRadioButton(I18nManager.getText("dialog.exportkml.standardkml"));
		_gxExtensionsRadio = new JRadioButton(I18nManager.getText("dialog.exportkml.extendedkml"));
		ButtonGroup bGroup = new ButtonGroup();
		bGroup.add(standardKmlRadio); bGroup.add(_gxExtensionsRadio);
		JPanel radioPanel = new JPanel();
		radioPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 1));
		radioPanel.add(standardKmlRadio);
		radioPanel.add(_gxExtensionsRadio);
		standardKmlRadio.setSelected(true);
		mainPanel.add(radioPanel);
		// Checkbox for altitude export
		_altitudesCheckbox = new JCheckBox(I18nManager.getText("dialog.exportkml.altitude"));
		_altitudesCheckbox.setHorizontalTextPosition(SwingConstants.LEFT);
		_altitudesCheckbox.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainPanel.add(_altitudesCheckbox);

		// Checkboxes for kmz export and image export
		_kmzCheckbox = new JCheckBox(I18nManager.getText("dialog.exportkml.kmz"));
		_kmzCheckbox.setHorizontalTextPosition(SwingConstants.LEFT);
		_kmzCheckbox.setAlignmentX(Component.CENTER_ALIGNMENT);
		// enable image checkbox if kmz activated
		_kmzCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				enableCheckboxes();
			}
		});
		mainPanel.add(_kmzCheckbox);
		_exportImagesCheckbox = new JCheckBox(I18nManager.getText("dialog.exportkml.exportimages"));
		_exportImagesCheckbox.setHorizontalTextPosition(SwingConstants.LEFT);
		_exportImagesCheckbox.setAlignmentX(Component.CENTER_ALIGNMENT);
		// enable image size fields if image checkbox changes
		_exportImagesCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				enableImageSizeFields();
			}
		});
		mainPanel.add(_exportImagesCheckbox);
		// Panel for the image size
		JPanel imageSizePanel = new JPanel();
		imageSizePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		_imageSizeLabel = new JLabel(I18nManager.getText("dialog.exportkml.imagesize"));
		_imageSizeLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		imageSizePanel.add(_imageSizeLabel);
		_imageSizeField = new WholeNumberField(4);
		imageSizePanel.add(_imageSizeField);
		mainPanel.add(imageSizePanel);

		mainPanel.add(Box.createVerticalStrut(10));
		_progressLabel = new JLabel("...");
		_progressLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainPanel.add(_progressLabel);
		_progressBar = new JProgressBar(0, 100);
		_progressBar.setVisible(false);
		_progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainPanel.add(_progressBar);
		mainPanel.add(Box.createVerticalStrut(10));
		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		_okButton = new JButton(I18nManager.getText("button.ok"));
		ActionListener okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				startExport();
			}
		};
		_okButton.addActionListener(okListener);
		_descriptionField.addActionListener(okListener);
		buttonPanel.add(_okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_cancelPressed = true;
				_dialog.dispose();
			}
		});
		buttonPanel.add(cancelButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		return dialogPanel;
	}


	/**
	 * Enable the checkboxes according to data
	 */
	private void enableCheckboxes()
	{
		_pointTypeSelector.init(_trackInfo);
		boolean hasAltitudes = _track.hasData(Field.ALTITUDE);
		if (!hasAltitudes) {_altitudesCheckbox.setSelected(false);}
		boolean hasPhotos = _trackInfo.getPhotoList() != null && _trackInfo.getPhotoList().getNumPhotos() > 0;
		_exportImagesCheckbox.setSelected(hasPhotos && _kmzCheckbox.isSelected());
		_exportImagesCheckbox.setEnabled(hasPhotos && _kmzCheckbox.isSelected());
		enableImageSizeFields();
	}

	/**
	 * Enable and disable the image size fields according to the checkboxes
	 */
	private void enableImageSizeFields()
	{
		boolean exportImages = _exportImagesCheckbox.isEnabled() && _exportImagesCheckbox.isSelected();
		_imageSizeField.setEnabled(exportImages);
		_imageSizeLabel.setEnabled(exportImages);
	}


	/**
	 * @return true if using gx extensions for kml export
	 */
	private boolean useGxExtensions() {
		return _gxExtensionsRadio.isSelected();
	}
	/**
	 * Start the export process based on the input parameters
	 */
	private void startExport()
	{
		// OK pressed, now validate selection checkboxes
		if (!_pointTypeSelector.getAnythingSelected()) {
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("dialog.save.notypesselected"),
				I18nManager.getText("dialog.saveoptions.title"), JOptionPane.WARNING_MESSAGE);
			return;
		}
		// Choose output file
		if (_fileChooser == null)
		{
			_fileChooser = new JFileChooser();
			_fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
			_fileChooser.setFileFilter(new GenericFileFilter("filetype.kmlkmz", new String[] {"kml", "kmz"}));
			// start from directory in config which should be set
			String configDir = Config.getConfigString(Config.KEY_TRACK_DIR);
			if (configDir != null) {_fileChooser.setCurrentDirectory(new File(configDir));}
		}
		String requiredExtension = null, otherExtension = null;
		if (_kmzCheckbox.isSelected()) {
			requiredExtension = ".kmz"; otherExtension = ".kml";
		}
		else {
			requiredExtension = ".kml"; otherExtension = ".kmz";
		}
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
				if (file.getName().toLowerCase().endsWith(otherExtension))
				{
					String path = file.getAbsolutePath();
					file = new File(path.substring(0, path.length()-otherExtension.length()) + requiredExtension);
				}
				else if (!file.getName().toLowerCase().endsWith(requiredExtension))
				{
					file = new File(file.getAbsolutePath() + requiredExtension);
				}
				// Check if file exists and if necessary prompt for overwrite
				Object[] buttonTexts = {I18nManager.getText("button.overwrite"), I18nManager.getText("button.cancel")};
				if (!file.exists() || JOptionPane.showOptionDialog(_parentFrame,
						I18nManager.getText("dialog.save.overwrite.text"),
						I18nManager.getText("dialog.save.overwrite.title"), JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE, null, buttonTexts, buttonTexts[1])
					== JOptionPane.YES_OPTION)
				{
					// New file or overwrite confirmed, so initiate export in separate thread
					_exportFile = file;
					_cancelPressed = false;
					new Thread(this).start();
				}
				else
				{
					chooseAgain = true;
				}
			}
		} while (chooseAgain);
	}


	/**
	 * Run method for controlling separate thread for exporting
	 */
	public void run()
	{
		// Disable ok button to stop second go
		_okButton.setEnabled(false);
		_descriptionField.setEnabled(false);
		// Initialise progress indicators
		_progressLabel.setText(I18nManager.getText("confirm.running"));
		_progressBar.setVisible(true);
		_progressBar.setValue(0);
		boolean exportToKmz = _kmzCheckbox.isSelected();
		boolean exportImages = exportToKmz && _exportImagesCheckbox.isSelected();
		_progressBar.setMaximum(exportImages?getNumPhotosToExport():1);

		// Create array for image dimensions in case it's required
		_imageDimensions = new Dimension[_track.getNumPoints()];

		OutputStreamWriter writer = null;
		ZipOutputStream zipOutputStream = null;
		try
		{
			// Select writer according to whether kmz requested or not
			if (!_kmzCheckbox.isSelected())
			{
				// normal writing to file
				writer = new OutputStreamWriter(new FileOutputStream(_exportFile));
			}
			else
			{
				// kmz requested - need zip output stream
				zipOutputStream = new ZipOutputStream(new FileOutputStream(_exportFile));
				// Export images into zip file too if requested
				if (exportImages)
				{
					// Get entered value for image size, store in config
					int thumbSize = _imageSizeField.getValue();
					if (thumbSize < DEFAULT_THUMBNAIL_WIDTH) {thumbSize = DEFAULT_THUMBNAIL_WIDTH;}
					Config.setConfigInt(Config.KEY_KMZ_IMAGE_SIZE, thumbSize);

					// Create thumbnails of each photo in turn and add to zip as images/image<n>.jpg
					// This is done first so that photo sizes are known for later
					exportThumbnails(zipOutputStream, thumbSize);
				}
				writer = new OutputStreamWriter(zipOutputStream);
				// Make an entry in the zip file for the kml file
				ZipEntry kmlEntry = new ZipEntry(KML_FILENAME_IN_KMZ);
				zipOutputStream.putNextEntry(kmlEntry);
			}
			// write file
			final int numPoints = exportData(writer, exportImages);
			// update config with selected track colour
			Config.setConfigString(Config.KEY_KML_TRACK_COLOUR, ColourUtils.makeHexCode(_colourPatch.getBackground()));
			// update progress bar
			_progressBar.setValue(1);

			// close zip entry if necessary
			if (zipOutputStream != null)
			{
				// Make sure all buffered data in writer is flushed
				writer.flush();
				// Close off this entry in the zip file
				zipOutputStream.closeEntry();
			}

			// close file
			writer.close();
			_imageDimensions = null;
			// Store directory in config for later
			Config.setConfigString(Config.KEY_TRACK_DIR, _exportFile.getParentFile().getAbsolutePath());
			// Add to recent file list
			Config.getRecentFileList().addFile(new RecentFile(_exportFile, true));
			// show confirmation
			UpdateMessageBroker.informSubscribers();
			UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.save.ok1")
				 + " " + numPoints + " " + I18nManager.getText("confirm.save.ok2")
				 + " " + _exportFile.getAbsolutePath());
			// export successful so need to close dialog and return
			_dialog.dispose();
			return;
		}
		catch (IOException ioe)
		{
			try {
				if (writer != null) writer.close();
			}
			catch (IOException ioe2) {}
			JOptionPane.showMessageDialog(_parentFrame,
				I18nManager.getText("error.save.failed") + " : " + ioe.getMessage(),
				I18nManager.getText("error.save.dialogtitle"), JOptionPane.ERROR_MESSAGE);
		}
		// if not returned already, export failed so need to recall the file selection
		startExport();
	}


	/**
	 * Export the information to the given writer
	 * @param inWriter writer object
	 * @param inExportImages true if image thumbnails are to be referenced
	 * @return number of points written
	 */
	private int exportData(OutputStreamWriter inWriter, boolean inExportImages)
	throws IOException
	{
		boolean writeTrack = _pointTypeSelector.getTrackpointsSelected();
		boolean writeWaypoints = _pointTypeSelector.getWaypointsSelected();
		boolean writePhotos = _pointTypeSelector.getPhotopointsSelected();
		boolean writeAudios = _pointTypeSelector.getAudiopointsSelected();
		boolean justSelection = _pointTypeSelector.getJustSelection();
		// Define xml header (depending on whether extensions are used or not)
		if (useGxExtensions()) {
			inWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<kml xmlns=\"http://earth.google.com/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\">\n");
		}
		else {
			inWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<kml xmlns=\"http://earth.google.com/kml/2.1\">\n");
		}
		inWriter.write("<Folder>\n\t<name>");
		if (_descriptionField != null && _descriptionField.getText() != null && !_descriptionField.getText().equals(""))
		{
			inWriter.write(XmlUtils.fixCdata(_descriptionField.getText()));
		}
		else {
			inWriter.write("Export from GpsPrune");
		}
		inWriter.write("</name>\n");

		// Examine selection if required
		int selStart = -1, selEnd = -1;
		if (justSelection) {
			selStart = _trackInfo.getSelection().getStart();
			selEnd = _trackInfo.getSelection().getEnd();
		}

		boolean absoluteAltitudes = _altitudesCheckbox.isSelected();
		int i = 0;
		DataPoint point = null;
		boolean hasTrackpoints = false;
		boolean writtenPhotoHeader = false, writtenAudioHeader = false;
		final int numPoints = _track.getNumPoints();
		int numSaved = 0;
		int photoNum = 0;
		// Loop over waypoints
		for (i=0; i<numPoints; i++)
		{
			point = _track.getPoint(i);
			boolean writeCurrentPoint = !justSelection || (i>=selStart && i<=selEnd);
			// Make a blob for each waypoint
			if (point.isWaypoint())
			{
				if (writeWaypoints && writeCurrentPoint)
				{
					exportWaypoint(point, inWriter, absoluteAltitudes);
					numSaved++;
				}
			}
			else if (!point.hasMedia())
			{
				hasTrackpoints = true;
			}
			// Make a blob with description for each photo
			// Photos have already been written so picture sizes already known
			if (point.getPhoto() != null && point.getPhoto().isValid() && writePhotos && writeCurrentPoint)
			{
				if (!writtenPhotoHeader)
				{
					inWriter.write("<Style id=\"camera_icon\"><IconStyle><Icon><href>http://maps.google.com/mapfiles/kml/pal4/icon46.png</href></Icon></IconStyle></Style>");
					writtenPhotoHeader = true;
				}
				photoNum++;
				exportPhotoPoint(point, inWriter, inExportImages, i, photoNum, absoluteAltitudes);
				numSaved++;
			}
			// Make a blob with description for each audio clip
			if (point.getAudio() != null && writeAudios && writeCurrentPoint)
			{
				if (!writtenAudioHeader)
				{
					inWriter.write("<Style id=\"audio_icon\"><IconStyle><color>ff00ffff</color><Icon><href>http://maps.google.com/mapfiles/kml/shapes/star.png</href></Icon></IconStyle></Style>");
					writtenAudioHeader = true;
				}
				exportAudioPoint(point, inWriter, absoluteAltitudes);
				numSaved++;
			}
		}
		// Make a line for the track, if there is one
		if (hasTrackpoints && writeTrack)
		{
			boolean useGxExtensions = _gxExtensionsRadio.isSelected();
			if (useGxExtensions)
			{
				// Write track using the Google Extensions to KML including gx:Track
				numSaved += writeGxTrack(inWriter, absoluteAltitudes, selStart, selEnd);
			}
			else {
				// Write track using standard KML
				numSaved += writeStandardTrack(inWriter, absoluteAltitudes, selStart, selEnd);
			}
		}
		inWriter.write("</Folder>\n</kml>\n");
		return numSaved;
	}


	/**
	 * Write out the track using standard KML LineString tag
	 * @param inWriter writer object to write to
	 * @param inAbsoluteAltitudes true to use absolute altitudes, false to clamp to ground
	 * @param inSelStart start index of selection, or -1 if whole track
	 * @param inSelEnd     end index of selection, or -1 if whole track
	 * @return number of track points written
	 */
	private int writeStandardTrack(OutputStreamWriter inWriter, boolean inAbsoluteAltitudes, int inSelStart,
		int inSelEnd)
	throws IOException
	{
		int numSaved = 0;
		// Set up strings for start and end of track segment
		String trackStart = "\t<Placemark>\n\t\t<name>track</name>\n\t\t<Style>\n\t\t\t<LineStyle>\n"
			+ "\t\t\t\t<color>cc" + reverse(ColourUtils.makeHexCode(_colourPatch.getBackground())) + "</color>\n"
			+ "\t\t\t\t<width>4</width>\n\t\t\t</LineStyle>\n"
			+ "\t\t</Style>\n\t\t<LineString>\n";
		if (inAbsoluteAltitudes) {
			trackStart += "\t\t\t<extrude>1</extrude>\n\t\t\t<altitudeMode>absolute</altitudeMode>\n";
		}
		else {
			trackStart += "\t\t\t<altitudeMode>clampToGround</altitudeMode>\n";
		}
		trackStart += "\t\t\t<coordinates>";
		String trackEnd = "\t\t\t</coordinates>\n\t\t</LineString>\n\t</Placemark>";

		boolean justSelection = _pointTypeSelector.getJustSelection();

		// Start segment
		inWriter.write(trackStart);
		// Loop over track points
		boolean firstTrackpoint = true;
		final int numPoints = _track.getNumPoints();
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = _track.getPoint(i);
			boolean writeCurrentPoint = !justSelection || (i>=inSelStart && i<=inSelEnd);
			if (!point.isWaypoint() && writeCurrentPoint)
			{
				// start new track segment if necessary
				if (point.getSegmentStart() && !firstTrackpoint) {
					inWriter.write(trackEnd);
					inWriter.write(trackStart);
				}
				if (point.getPhoto() == null)
				{
					exportTrackpoint(point, inWriter);
					numSaved++;
					firstTrackpoint = false;
				}
			}
		}
		// end segment
		inWriter.write(trackEnd);
		return numSaved;
	}


	/**
	 * Write out the track using Google's KML Extensions such as gx:Track
	 * @param inWriter writer object to write to
	 * @param inAbsoluteAltitudes true to use absolute altitudes, false to clamp to ground
	 * @param inSelStart start index of selection, or -1 if whole track
	 * @param inSelEnd     end index of selection, or -1 if whole track
	 * @return number of track points written
	 */
	private int writeGxTrack(OutputStreamWriter inWriter, boolean inAbsoluteAltitudes, int inSelStart,
		int inSelEnd)
	throws IOException
	{
		int numSaved = 0;
		// Set up strings for start and end of track segment
		String trackStart = "\t<Placemark>\n\t\t<name>track</name>\n\t\t<Style>\n\t\t\t<LineStyle>\n"
			+ "\t\t\t\t<color>cc" + reverse(ColourUtils.makeHexCode(_colourPatch.getBackground())) + "</color>\n"
			+ "\t\t\t\t<width>4</width>\n\t\t\t</LineStyle>\n"
			+ "\t\t</Style>\n\t\t<gx:Track>\n";
		if (inAbsoluteAltitudes) {
			trackStart += "\t\t\t<extrude>1</extrude>\n\t\t\t<altitudeMode>absolute</altitudeMode>\n";
		}
		else {
			trackStart += "\t\t\t<altitudeMode>clampToGround</altitudeMode>\n";
		}
		String trackEnd = "\n\t\t</gx:Track>\n\t</Placemark>\n";

		boolean justSelection = _pointTypeSelector.getJustSelection();

		// Start segment
		inWriter.write(trackStart);
		StringBuilder whenList = new StringBuilder();
		StringBuilder coordList = new StringBuilder();

		// Loop over track points
		boolean firstTrackpoint = true;
		final int numPoints = _track.getNumPoints();
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = _track.getPoint(i);
			boolean writeCurrentPoint = !justSelection || (i>=inSelStart && i<=inSelEnd);
			if (!point.isWaypoint() && writeCurrentPoint)
			{
				// start new track segment if necessary
				if (point.getSegmentStart() && !firstTrackpoint)
				{
					inWriter.write(whenList.toString());
					inWriter.write('\n');
					inWriter.write(coordList.toString());
					inWriter.write('\n');
					inWriter.write(trackEnd);
					whenList.setLength(0); coordList.setLength(0);
					inWriter.write(trackStart);
				}
				if (point.getPhoto() == null)
				{
					// Add timestamp (if any) to the list
					whenList.append("<when>");
					if (point.hasTimestamp()) {
						whenList.append(point.getTimestamp().getText(Timestamp.Format.ISO8601, null));
					}
					whenList.append("</when>\n");
					// Add coordinates to the list
					coordList.append("<gx:coord>");
					coordList.append(point.getLongitude().output(Coordinate.FORMAT_DECIMAL_FORCE_POINT)).append(' ');
					coordList.append(point.getLatitude().output(Coordinate.FORMAT_DECIMAL_FORCE_POINT)).append(' ');
					if (point.hasAltitude()) {
						coordList.append("" + point.getAltitude().getStringValue(UnitSetLibrary.UNITS_METRES));
					}
					else {
						coordList.append('0');
					}
					coordList.append("</gx:coord>\n");
					numSaved++;
					firstTrackpoint = false;
				}
			}
		}
		// end segment
		inWriter.write(whenList.toString());
		inWriter.write('\n');
		inWriter.write(coordList.toString());
		inWriter.write('\n');
		inWriter.write(trackEnd);
		return numSaved;
	}


	/**
	 * Reverse the hex code for the colours for KML's stupid backwards format
	 * @param inCode colour code rrggbb
	 * @return kml code bbggrr
	 */
	private static String reverse(String inCode)
	{
		return inCode.substring(4, 6) + inCode.substring(2, 4) + inCode.substring(0, 2);
	}

	/**
	 * Export the specified waypoint into the file
	 * @param inPoint waypoint to export
	 * @param inWriter writer object
	 * @param inAbsoluteAltitude true for absolute altitude
	 * @throws IOException on write failure
	 */
	private void exportWaypoint(DataPoint inPoint, Writer inWriter, boolean inAbsoluteAltitude) throws IOException
	{
		String name = inPoint.getWaypointName().trim();
		exportNamedPoint(inPoint, inWriter, name, inPoint.getFieldValue(Field.DESCRIPTION), null, inAbsoluteAltitude);
	}


	/**
	 * Export the specified audio point into the file
	 * @param inPoint audio point to export
	 * @param inWriter writer object
	 * @param inAbsoluteAltitude true for absolute altitude
	 * @throws IOException on write failure
	 */
	private void exportAudioPoint(DataPoint inPoint, Writer inWriter, boolean inAbsoluteAltitude) throws IOException
	{
		String name = inPoint.getAudio().getName();
		String desc = null;
		if (inPoint.getAudio().getFile() != null) {
			desc = inPoint.getAudio().getFile().getAbsolutePath();
		}
		exportNamedPoint(inPoint, inWriter, name, desc, "audio_icon", inAbsoluteAltitude);
	}


	/**
	 * Export the specified photo into the file
	 * @param inPoint data point including photo
	 * @param inWriter writer object
	 * @param inImageLink flag to set whether to export image links or not
	 * @param inPointNumber number of point for accessing dimensions
	 * @param inImageNumber number of image for filename
	 * @param inAbsoluteAltitude true for absolute altitudes
	 * @throws IOException on write failure
	 */
	private void exportPhotoPoint(DataPoint inPoint, Writer inWriter, boolean inImageLink,
		int inPointNumber, int inImageNumber, boolean inAbsoluteAltitude)
	throws IOException
	{
		String name = inPoint.getPhoto().getName();
		String desc = null;
		if (inImageLink)
		{
			Dimension imageSize = _imageDimensions[inPointNumber];
			// Create html for the thumbnail images
			desc = "<![CDATA[<br/><table border='0'><tr><td><center><img src='images/image"
				+ inImageNumber + ".jpg' width='" + imageSize.width + "' height='" + imageSize.height + "'></center></td></tr>"
				+ "<tr><td><center>" + name + "</center></td></tr></table>]]>";
		}
		// Export point
		exportNamedPoint(inPoint, inWriter, name, desc, "camera_icon", inAbsoluteAltitude);
	}


	/**
	 * Export the specified named point into the file, like waypoint or photo point
	 * @param inPoint data point
	 * @param inWriter writer object
	 * @param inName name of point
	 * @param inDesc description of point, or null
	 * @param inStyle style of point, or null
	 * @param inAbsoluteAltitude true for absolute altitudes
	 * @throws IOException on write failure
	 */
	private void exportNamedPoint(DataPoint inPoint, Writer inWriter, String inName,
		String inDesc, String inStyle, boolean inAbsoluteAltitude)
	throws IOException
	{
		inWriter.write("\t<Placemark>\n\t\t<name>");
		inWriter.write(XmlUtils.fixCdata(inName));
		inWriter.write("</name>\n");
		if (inDesc != null)
		{
			// Write out description
			inWriter.write("\t\t<description>");
			inWriter.write(XmlUtils.fixCdata(inDesc));
			inWriter.write("</description>\n");
		}
		if (inStyle != null)
		{
			inWriter.write("<styleUrl>#");
			inWriter.write(inStyle);
			inWriter.write("</styleUrl>\n");
		}
		inWriter.write("\t\t<Point>\n");
		if (inAbsoluteAltitude && inPoint.hasAltitude()) {
			inWriter.write("\t\t\t<altitudeMode>absolute</altitudeMode>\n");
		}
		else {
			inWriter.write("\t\t\t<altitudeMode>clampToGround</altitudeMode>\n");
		}
		inWriter.write("\t\t\t<coordinates>");
		inWriter.write(inPoint.getLongitude().output(Coordinate.FORMAT_DECIMAL_FORCE_POINT));
		inWriter.write(',');
		inWriter.write(inPoint.getLatitude().output(Coordinate.FORMAT_DECIMAL_FORCE_POINT));
		inWriter.write(',');
		// Altitude if point has one
		if (inPoint.hasAltitude()) {
			inWriter.write("" + inPoint.getAltitude().getStringValue(UnitSetLibrary.UNITS_METRES));
		}
		else {
			inWriter.write('0');
		}
		inWriter.write("</coordinates>\n\t\t</Point>\n\t</Placemark>\n");
	}


	/**
	 * Export the specified trackpoint into the file
	 * @param inPoint trackpoint to export
	 * @param inWriter writer object
	 */
	private void exportTrackpoint(DataPoint inPoint, Writer inWriter) throws IOException
	{
		inWriter.write(inPoint.getLongitude().output(Coordinate.FORMAT_DECIMAL_FORCE_POINT));
		inWriter.write(',');
		inWriter.write(inPoint.getLatitude().output(Coordinate.FORMAT_DECIMAL_FORCE_POINT));
		// Altitude if point has one
		inWriter.write(',');
		if (inPoint.hasAltitude()) {
			inWriter.write("" + inPoint.getAltitude().getStringValue(UnitSetLibrary.UNITS_METRES));
		}
		else {
			inWriter.write('0');
		}
		inWriter.write('\n');
	}


	/**
	 * Loop through the photos and create thumbnails
	 * @param inZipStream zip stream to save image files to
	 * @param inThumbSize thumbnail size
	 */
	private void exportThumbnails(ZipOutputStream inZipStream, int inThumbSize)
	throws IOException
	{
		// set up image writer
		Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
		if (writers == null || !writers.hasNext())
		{
			throw new IOException("no JPEG writer found");
		}
		ImageWriter imageWriter = writers.next();

		// Check selection checkbox
		final boolean justSelection = _pointTypeSelector.getJustSelection();
		int selStart = -1, selEnd = -1;
		if (justSelection) {
			selStart = _trackInfo.getSelection().getStart();
			selEnd = _trackInfo.getSelection().getEnd();
		}

		final int numPoints = _track.getNumPoints();
		DataPoint point = null;
		int photoNum = 0;
		// Loop over all points in track
		for (int i=0; i<numPoints && !_cancelPressed; i++)
		{
			point = _track.getPoint(i);
			if (point.getPhoto() != null && point.getPhoto().isValid() && (!justSelection || (i>=selStart && i<=selEnd)))
			{
				photoNum++;
				// Make a new entry in zip file
				ZipEntry entry = new ZipEntry("images/image" + photoNum + ".jpg");
				inZipStream.putNextEntry(entry);
				// Load image and write to outstream
				ImageIcon icon = point.getPhoto().createImageIcon();

				// Scale image to required size (not smoothed)
				BufferedImage bufferedImage = ImageUtils.rotateImage(icon.getImage(),
					inThumbSize, inThumbSize, point.getPhoto().getRotationDegrees());
				// Store image dimensions so that it doesn't have to be calculated again for the points
				_imageDimensions[i] = new Dimension(bufferedImage.getWidth(), bufferedImage.getHeight());

				imageWriter.setOutput(ImageIO.createImageOutputStream(inZipStream));
				imageWriter.write(bufferedImage);
				// Close zip file entry
				inZipStream.closeEntry();
				// Update progress bar
				_progressBar.setValue(photoNum+1);
			}
		}
	}


	/**
	 * @return number of correlated photos in the track
	 */
	private int getNumPhotosToExport()
	{
		int numPoints = _track.getNumPoints();
		int numPhotos = 0;
		DataPoint point = null;
		// Loop over all points in track
		for (int i=0; i<numPoints; i++)
		{
			point = _track.getPoint(i);
			if (point.getPhoto() != null) {
				numPhotos++;
			}
		}
		return numPhotos;
	}
}
