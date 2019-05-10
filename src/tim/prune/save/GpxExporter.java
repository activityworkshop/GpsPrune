package tim.prune.save;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.GpsPrune;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.config.Config;
import tim.prune.data.AudioClip;
import tim.prune.data.Coordinate;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.MediaObject;
import tim.prune.data.Photo;
import tim.prune.data.RecentFile;
import tim.prune.data.SourceInfo;
import tim.prune.data.Timestamp;
import tim.prune.data.TrackInfo;
import tim.prune.data.UnitSetLibrary;
import tim.prune.gui.DialogCloser;
import tim.prune.load.GenericFileFilter;
import tim.prune.save.xml.GpxCacherList;
import tim.prune.save.xml.XmlUtils;


/**
 * Class to export track information
 * into a specified Gpx file
 */
public class GpxExporter extends GenericFunction implements Runnable
{
	private TrackInfo _trackInfo = null;
	private JDialog _dialog = null;
	private JTextField _nameField = null;
	private JTextField _descriptionField = null;
	private PointTypeSelector _pointTypeSelector = null;
	private JCheckBox _timestampsCheckbox = null;
	private JCheckBox _copySourceCheckbox = null;
	private JPanel _encodingsPanel = null;
	private JRadioButton _useSystemRadio = null, _forceUtf8Radio = null;
	private File _exportFile = null;
	/** Remember the previous sourceInfo object to tell whether it has changed */
	private SourceInfo _previousSourceInfo = null;

	/** this program name */
	private static final String GPX_CREATOR = "GpsPrune v" + GpsPrune.VERSION_NUMBER + " activityworkshop.net";


	/**
	 * Constructor
	 * @param inApp app object
	 */
	public GpxExporter(App inApp)
	{
		super(inApp);
		_trackInfo = inApp.getTrackInfo();
	}

	/** Get name key */
	public String getNameKey() {
		return "function.exportgpx";
	}

	/**
	 * Show the dialog to select options and export file
	 */
	public void begin()
	{
		// Make dialog window
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, I18nManager.getText(getNameKey()), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			_dialog.getContentPane().add(makeDialogComponents());
			_dialog.pack();
		}
		_pointTypeSelector.init(_app.getTrackInfo());
		_encodingsPanel.setVisible(!XmlUtils.isSystemUtf8());
		if (!XmlUtils.isSystemUtf8())
		{
			String systemEncoding = XmlUtils.getSystemEncoding();
			_useSystemRadio.setText(I18nManager.getText("dialog.exportgpx.encoding.system")
				+ " (" + (systemEncoding == null ? "unknown" : systemEncoding) + ")");
		}
		setFileTitle();
		_dialog.setVisible(true);
	}


	/**
	 * Create dialog components
	 * @return Panel containing all gui elements in dialog
	 */
	private Component makeDialogComponents()
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout());
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		// Make a panel for the name/desc text boxes
		JPanel descPanel = new JPanel();
		descPanel.setLayout(new GridLayout(2, 2));
		descPanel.add(new JLabel(I18nManager.getText("dialog.exportgpx.name")));
		_nameField = new JTextField(10);
		descPanel.add(_nameField);
		descPanel.add(new JLabel(I18nManager.getText("dialog.exportgpx.desc")));
		_descriptionField = new JTextField(10);
		descPanel.add(_descriptionField);
		mainPanel.add(descPanel);
		mainPanel.add(Box.createVerticalStrut(5));
		// point type selection (track points, waypoints, photo points)
		_pointTypeSelector = new PointTypeSelector();
		mainPanel.add(_pointTypeSelector);
		// checkboxes for timestamps and copying
		JPanel checkPanel = new JPanel();
		_timestampsCheckbox = new JCheckBox(I18nManager.getText("dialog.exportgpx.includetimestamps"));
		_timestampsCheckbox.setSelected(true);
		checkPanel.add(_timestampsCheckbox);
		_copySourceCheckbox = new JCheckBox(I18nManager.getText("dialog.exportgpx.copysource"));
		_copySourceCheckbox.setSelected(true);
		checkPanel.add(_copySourceCheckbox);
		mainPanel.add(checkPanel);
		// panel for selecting character encoding
		_encodingsPanel = new JPanel();
		if (!XmlUtils.isSystemUtf8())
		{
			// only add this panel if system isn't utf8 (or can't be identified yet)
			_encodingsPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(4, 4, 4, 4)));
			_encodingsPanel.setLayout(new BorderLayout());
			_encodingsPanel.add(new JLabel(I18nManager.getText("dialog.exportgpx.encoding")), BorderLayout.NORTH);
			JPanel radioPanel = new JPanel();
			radioPanel.setLayout(new FlowLayout());
			ButtonGroup radioGroup = new ButtonGroup();
			_useSystemRadio = new JRadioButton(I18nManager.getText("dialog.exportgpx.encoding.system"));
			_forceUtf8Radio = new JRadioButton(I18nManager.getText("dialog.exportgpx.encoding.utf8"));
			radioGroup.add(_useSystemRadio);
			radioGroup.add(_forceUtf8Radio);
			radioPanel.add(_useSystemRadio);
			radioPanel.add(_forceUtf8Radio);
			_useSystemRadio.setSelected(true);
			_encodingsPanel.add(radioPanel, BorderLayout.CENTER);
			mainPanel.add(_encodingsPanel);
		}
		dialogPanel.add(mainPanel, BorderLayout.CENTER);

		// close dialog if escape pressed
		_nameField.addKeyListener(new DialogCloser(_dialog));
		// button panel at bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton okButton = new JButton(I18nManager.getText("button.ok"));
		ActionListener okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				startExport();
			}
		};
		okButton.addActionListener(okListener);
		_descriptionField.addActionListener(okListener);
		buttonPanel.add(okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_dialog.dispose();
			}
		});
		buttonPanel.add(cancelButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		dialogPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 15));
		return dialogPanel;
	}

	/**
	 * Set the suggestion for the name with which to export
	 */
	private void setFileTitle()
	{
		// Get the most recent file info
		SourceInfo currentSource = _app.getTrackInfo().getFileInfo().getLastFileInfo();
		if (currentSource != _previousSourceInfo)
		{
			String lastTitle = currentSource.getFileTitle();
			if (lastTitle != null && !lastTitle.equals(""))
			{
				// Take the title of the last file loaded
				_nameField.setText(lastTitle);
			}
		}
		if (_nameField.getText().equals(""))
		{
			// no name given in the field already, so try to overwrite it
			String lastTitle = _app.getTrackInfo().getFileInfo().getLastFileTitle();
			if (lastTitle != null && !lastTitle.equals(""))
			{
				_nameField.setText(lastTitle);
			}
		}
		// Remember this source info so we don't use it again
		_previousSourceInfo = currentSource;
	}

	/**
	 * Start the export process based on the input parameters
	 */
	private void startExport()
	{
		// OK pressed, so check selections
		if (!_pointTypeSelector.getAnythingSelected())
		{
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("dialog.save.notypesselected"),
				I18nManager.getText("dialog.saveoptions.title"), JOptionPane.WARNING_MESSAGE);
			return;
		}
		// Choose output file
		File saveFile = chooseGpxFile(_parentFrame);
		if (saveFile != null)
		{
			// New file or overwrite confirmed, so initiate export in separate thread
			_exportFile = saveFile;
			new Thread(this).start();
		}
	}


	/**
	 * Select a GPX file to save to
	 * @param inParentFrame parent frame for file chooser dialog
	 * @return selected File, or null if selection cancelled
	 */
	public static File chooseGpxFile(JFrame inParentFrame)
	{
		File saveFile = null;
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		fileChooser.setFileFilter(new GenericFileFilter("filetype.gpx", new String[] {"gpx"}));
		fileChooser.setAcceptAllFileFilterUsed(false);
		// start from directory in config which should be set
		String configDir = Config.getConfigString(Config.KEY_TRACK_DIR);
		if (configDir != null) {fileChooser.setCurrentDirectory(new File(configDir));}

		// Allow choose again if an existing file is selected
		boolean chooseAgain = false;
		do
		{
			chooseAgain = false;
			if (fileChooser.showSaveDialog(inParentFrame) == JFileChooser.APPROVE_OPTION)
			{
				// OK pressed and file chosen
				File file = fileChooser.getSelectedFile();
				// Check file extension
				if (!file.getName().toLowerCase().endsWith(".gpx"))
				{
					file = new File(file.getAbsolutePath() + ".gpx");
				}
				// Check if file exists and if necessary prompt for overwrite
				Object[] buttonTexts = {I18nManager.getText("button.overwrite"), I18nManager.getText("button.cancel")};
				if (!file.exists() || JOptionPane.showOptionDialog(inParentFrame,
						I18nManager.getText("dialog.save.overwrite.text"),
						I18nManager.getText("dialog.save.overwrite.title"), JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE, null, buttonTexts, buttonTexts[1])
					== JOptionPane.YES_OPTION)
				{
					// new file or overwrite confirmed
					saveFile = file;
				}
				else
				{
					// file exists and overwrite cancelled - select again
					chooseAgain = true;
				}
			}
		} while (chooseAgain);
		return saveFile;
	}


	/**
	 * Run method for controlling separate thread for exporting
	 */
	public void run()
	{
		// Instantiate source file cachers in case we want to copy output
		GpxCacherList gpxCachers = null;
		if (_copySourceCheckbox.isSelected()) {
			gpxCachers = new GpxCacherList(_trackInfo.getFileInfo());
		}
		OutputStreamWriter writer = null;
		try
		{
			// normal writing to file - firstly specify UTF8 encoding if requested
			if (_forceUtf8Radio != null && _forceUtf8Radio.isSelected())
				writer = new OutputStreamWriter(new FileOutputStream(_exportFile), "UTF-8");
			else
				writer = new OutputStreamWriter(new FileOutputStream(_exportFile));
			// TODO: Move to new method
			SettingsForExport settings = new SettingsForExport();
			settings.setExportTrackPoints(_pointTypeSelector.getTrackpointsSelected());
			settings.setExportWaypoints(_pointTypeSelector.getWaypointsSelected());
			settings.setExportPhotoPoints(_pointTypeSelector.getPhotopointsSelected());
			settings.setExportAudiopoints(_pointTypeSelector.getAudiopointsSelected());
			settings.setExportJustSelection(_pointTypeSelector.getJustSelection());
			settings.setExportTimestamps(_timestampsCheckbox.isSelected());
			// write file
			final int numPoints = exportData(writer, _trackInfo, _nameField.getText(),
				_descriptionField.getText(), settings, gpxCachers);

			// close file
			writer.close();
			// Store directory in config for later
			Config.setConfigString(Config.KEY_TRACK_DIR, _exportFile.getParentFile().getAbsolutePath());
			// Add to recent file list
			Config.getRecentFileList().addFile(new RecentFile(_exportFile, true));
			// Show confirmation
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
			// System.out.println("Exception: " + ioe.getClass().getName() + " - " + ioe.getMessage());
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
	 * @param inInfo track info object
	 * @param inName name of track (optional)
	 * @param inDesc description of track (optional)
	 * @param inExportSettings flags for what to export and how
	 * @param inGpxCachers list of Gpx cachers containing input data
	 * @return number of points written
	 * @throws IOException if io errors occur on write
	 */
	public static int exportData(OutputStreamWriter inWriter, TrackInfo inInfo, String inName,
		String inDesc, SettingsForExport inSettings, GpxCacherList inGpxCachers) throws IOException
	{
		// Write or copy headers
		inWriter.write(getXmlHeaderString(inWriter));
		final String gpxHeader = getGpxHeaderString(inGpxCachers);
		final boolean isVersion1_1 = (gpxHeader.toUpperCase().indexOf("GPX/1/1") > 0);
		inWriter.write(gpxHeader);
		// name and description
		String trackName = (inName != null && !inName.equals("")) ? XmlUtils.fixCdata(inName) : "GpsPruneTrack";
		String desc      = (inDesc != null && !inDesc.equals("")) ? XmlUtils.fixCdata(inDesc) : "Export from GpsPrune";
		writeNameAndDescription(inWriter, trackName, desc, isVersion1_1);

		DataPoint point = null;
		final boolean exportWaypoints = inSettings.getExportWaypoints();
		final boolean exportSelection = inSettings.getExportJustSelection();
		final boolean exportTimestamps = inSettings.getExportTimestamps();
		// Examine selection
		int selStart = -1, selEnd = -1;
		if (exportSelection) {
			selStart = inInfo.getSelection().getStart();
			selEnd = inInfo.getSelection().getEnd();
		}
		// Loop over waypoints
		final int numPoints = inInfo.getTrack().getNumPoints();
		int numSaved = 0;
		for (int i=0; i<numPoints; i++)
		{
			point = inInfo.getTrack().getPoint(i);
			if (!exportSelection || (i>=selStart && i<=selEnd))
			{
				// Make a wpt element for each waypoint
				if (point.isWaypoint() && exportWaypoints)
				{
					String pointSource = (inGpxCachers == null? null : getPointSource(inGpxCachers, point));
					if (pointSource != null)
					{
						// If timestamp checkbox is off, strip time
						if (!exportTimestamps) {
							pointSource = stripTime(pointSource);
						}
						inWriter.write('\t');
						inWriter.write(pointSource);
						inWriter.write('\n');
					}
					else {
						exportWaypoint(point, inWriter, inSettings);
					}
					numSaved++;
				}
			}
		}
		// Export both route points and then track points
		if (inSettings.getExportTrackPoints() || inSettings.getExportPhotoPoints() || inSettings.getExportAudioPoints())
		{
			// Output all route points (if any)
			numSaved += writeTrackPoints(inWriter, inInfo, inSettings,
				true, inGpxCachers, "<rtept", "\t<rte><number>1</number>\n",
				null, "\t</rte>\n");
			// Output all track points, if any
			String trackStart = "\t<trk>\n\t\t<name>" + trackName + "</name>\n\t\t<number>1</number>\n\t\t<trkseg>\n";
			numSaved += writeTrackPoints(inWriter, inInfo, inSettings,
				false, inGpxCachers, "<trkpt", trackStart,
				"\t</trkseg>\n\t<trkseg>\n", "\t\t</trkseg>\n\t</trk>\n");
		}

		inWriter.write("</gpx>\n");
		return numSaved;
	}


	/**
	 * Write the name and description according to the GPX version number
	 * @param inWriter writer object
	 * @param inName name, or null if none supplied
	 * @param inDesc description, or null if none supplied
	 * @param inIsVersion1_1 true if gpx version 1.1, false for version 1.0
	 */
	private static void writeNameAndDescription(OutputStreamWriter inWriter,
		String inName, String inDesc, boolean inIsVersion1_1) throws IOException
	{
		// Position of name and description fields needs to be different for GPX1.0 and GPX1.1
		if (inIsVersion1_1)
		{
			// GPX 1.1 has the name and description inside a metadata tag
			inWriter.write("\t<metadata>\n");
		}
		if (inName != null && !inName.equals(""))
		{
			if (inIsVersion1_1) {inWriter.write('\t');}
			inWriter.write("\t<name>");
			inWriter.write(inName);
			inWriter.write("</name>\n");
		}
		if (inIsVersion1_1) {inWriter.write('\t');}
		inWriter.write("\t<desc>");
		inWriter.write(inDesc);
		inWriter.write("</desc>\n");
		if (inIsVersion1_1)
		{
			inWriter.write("\t</metadata>\n");
		}
	}

	/**
	 * Loop through the track outputting the relevant track points
	 * @param inWriter writer object for output
	 * @param inInfo track info object containing track
	 * @param inSettings export settings defining what should be exported
	 * @param inOnlyCopies true to only export if source can be copied
	 * @param inCachers list of GpxCachers
	 * @param inPointTag tag to match for each point
	 * @param inStartTag start tag to output
	 * @param inSegmentTag tag to output between segments (or null)
	 * @param inEndTag end tag to output
	 */
	private static int writeTrackPoints(OutputStreamWriter inWriter,
		TrackInfo inInfo, SettingsForExport inSettings,
		boolean inOnlyCopies, GpxCacherList inCachers, String inPointTag,
		String inStartTag, String inSegmentTag, String inEndTag)
	throws IOException
	{
		// Note: Too many input parameters to this method but avoids duplication
		// of output functionality for writing track points and route points
		int numPoints = inInfo.getTrack().getNumPoints();
		int selStart = inInfo.getSelection().getStart();
		int selEnd = inInfo.getSelection().getEnd();
		int numSaved = 0;
		final boolean exportSelection = inSettings.getExportJustSelection();
		final boolean exportTrackPoints = inSettings.getExportTrackPoints();
		final boolean exportPhotos = inSettings.getExportPhotoPoints();
		final boolean exportAudios = inSettings.getExportAudioPoints();
		final boolean exportTimestamps = inSettings.getExportTimestamps();
		// Loop over track points
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = inInfo.getTrack().getPoint(i);
			if ((!exportSelection || (i>=selStart && i<=selEnd)) && !point.isWaypoint())
			{
				if ((point.getPhoto()==null && exportTrackPoints) || (point.getPhoto()!=null && exportPhotos)
					|| (point.getAudio()!=null && exportAudios))
				{
					// get the source from the point (if any)
					String pointSource = getPointSource(inCachers, point);
					// Clear point source if it's the wrong type of point (eg changed from waypoint or route point)
					if (pointSource != null && !pointSource.trim().toLowerCase().startsWith(inPointTag)) {
						pointSource = null;
					}
					if (pointSource != null || !inOnlyCopies)
					{
						// restart track segment if necessary
						if ((numSaved > 0) && point.getSegmentStart() && (inSegmentTag != null)) {
							inWriter.write(inSegmentTag);
						}
						if (numSaved == 0) {inWriter.write(inStartTag);}
						if (pointSource != null)
						{
							// If timestamps checkbox is off, strip the time
							if (!exportTimestamps) {
								pointSource = stripTime(pointSource);
							}
							inWriter.write(pointSource);
							inWriter.write('\n');
						}
						else
						{
							if (!inOnlyCopies) {
								exportTrackpoint(point, inWriter, inSettings);
							}
						}
						numSaved++;
					}
				}
			}
		}
		if (numSaved > 0) {
			inWriter.write(inEndTag);
		}
		return numSaved;
	}


	/**
	 * Get the point source for the specified point
	 * @param inCachers list of GPX cachers to ask for source
	 * @param inPoint point object
	 * @return xml source if available, or null otherwise
	 */
	private static String getPointSource(GpxCacherList inCachers, DataPoint inPoint)
	{
		if (inCachers == null || inPoint == null) {return null;}
		String source = inCachers.getSourceString(inPoint);
		if (source == null || !inPoint.isModified()) {return source;}
		// Point has been modified - maybe it's possible to modify the source
		source = replaceGpxTags(source, "lat=\"", "\"", inPoint.getLatitude().output(Coordinate.FORMAT_DECIMAL_FORCE_POINT));
		source = replaceGpxTags(source, "lon=\"", "\"", inPoint.getLongitude().output(Coordinate.FORMAT_DECIMAL_FORCE_POINT));
		source = replaceGpxTags(source, "<ele>", "</ele>", inPoint.getAltitude().getStringValue(UnitSetLibrary.UNITS_METRES));
		source = replaceGpxTags(source, "<time>", "</time>", inPoint.getTimestamp().getText(Timestamp.Format.ISO8601, null));
		if (inPoint.isWaypoint())
		{
			source = replaceGpxTags(source, "<name>", "</name>", XmlUtils.fixCdata(inPoint.getWaypointName()));
			if (source != null)
			{
				source = source.replaceAll("<description>", "<desc>").replaceAll("</description>", "</desc>");
			}
			source = replaceGpxTags(source, "<desc>", "</desc>",
				XmlUtils.fixCdata(inPoint.getFieldValue(Field.DESCRIPTION)));
		}
		// photo / audio links
		if (source != null && (inPoint.hasMedia() || source.indexOf("</link>") > 0)) {
			source = replaceMediaLinks(source, makeMediaLink(inPoint));
		}
		return source;
	}

	/**
	 * Replace the given value into the given XML string
	 * @param inSource source XML for point
	 * @param inStartTag start tag for field
	 * @param inEndTag end tag for field
	 * @param inValue value to replace between start tag and end tag
	 * @return modified String, or null if not possible
	 */
	private static String replaceGpxTags(String inSource, String inStartTag, String inEndTag, String inValue)
	{
		if (inSource == null) {return null;}
		// Look for start and end tags within source
		final int startPos = inSource.indexOf(inStartTag);
		final int endPos = inSource.indexOf(inEndTag, startPos+inStartTag.length());
		if (startPos > 0 && endPos > 0)
		{
			String origValue = inSource.substring(startPos + inStartTag.length(), endPos);
			if (inValue != null && origValue.equals(inValue)) {
				// Value unchanged
				return inSource;
			}
			else if (inValue == null || inValue.equals("")) {
				// Need to delete value
				return inSource.substring(0, startPos) + inSource.substring(endPos + inEndTag.length());
			}
			else {
				// Need to replace value
				return inSource.substring(0, startPos+inStartTag.length()) + inValue + inSource.substring(endPos);
			}
		}
		// Value not found for this field in original source
		if (inValue == null || inValue.equals("")) {return inSource;}
		return null;
	}


	/**
	 * Replace the media tags in the given XML string
	 * @param inSource source XML for point
	 * @param inValue value for the current point
	 * @return modified String, or null if not possible
	 */
	private static String replaceMediaLinks(String inSource, String inValue)
	{
		if (inSource == null) {return null;}
		// Note that this method is very similar to replaceGpxTags except there can be multiple link tags
		// and the tags must have attributes.  So either one heavily parameterized method or two.
		// Look for start and end tags within source
		final String STARTTEXT = "<link";
		final String ENDTEXT = "</link>";
		final int startPos = inSource.indexOf(STARTTEXT);
		final int endPos = inSource.lastIndexOf(ENDTEXT);
		if (startPos > 0 && endPos > 0)
		{
			String origValue = inSource.substring(startPos, endPos + ENDTEXT.length());
			if (inValue != null && origValue.equals(inValue)) {
				// Value unchanged
				return inSource;
			}
			else if (inValue == null || inValue.equals("")) {
				// Need to delete value
				return inSource.substring(0, startPos) + inSource.substring(endPos + ENDTEXT.length());
			}
			else {
				// Need to replace value
				return inSource.substring(0, startPos) + inValue + inSource.substring(endPos + ENDTEXT.length());
			}
		}
		// Value not found for this field in original source
		if (inValue == null || inValue.equals("")) {return inSource;}
		return null;
	}


	/**
	 * Get the header string for the xml document including encoding
	 * @param inWriter writer object
	 * @return header string defining encoding
	 */
	private static String getXmlHeaderString(OutputStreamWriter inWriter)
	{
		return "<?xml version=\"1.0\" encoding=\"" + XmlUtils.getEncoding(inWriter) + "\"?>\n";
	}


	/**
	 * Get the header string for the gpx tag
	 * @param inCachers cacher list to ask for headers, if available
	 * @return header string from cachers or as default
	 */
	private static String getGpxHeaderString(GpxCacherList inCachers)
	{
		String gpxHeader = null;
		if (inCachers != null) {gpxHeader = inCachers.getFirstHeader();}
		if (gpxHeader == null || gpxHeader.length() < 5)
		{
			// TODO: Consider changing this to default to GPX 1.1
			// Create default (1.0) header
			gpxHeader = "<gpx version=\"1.0\" creator=\"" + GPX_CREATOR
				+ "\"\n xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
				+ " xmlns=\"http://www.topografix.com/GPX/1/0\""
				+ " xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd\">\n";
		}
		return gpxHeader + "\n";
	}


	/**
	 * Export the specified waypoint into the file
	 * @param inPoint waypoint to export
	 * @param inWriter writer object
	 * @param inSettings export settings
	 * @throws IOException on write failure
	 */
	private static void exportWaypoint(DataPoint inPoint, Writer inWriter,
		SettingsForExport inSettings)
		throws IOException
	{
		inWriter.write("\t<wpt lat=\"");
		inWriter.write(inPoint.getLatitude().output(Coordinate.FORMAT_DECIMAL_FORCE_POINT));
		inWriter.write("\" lon=\"");
		inWriter.write(inPoint.getLongitude().output(Coordinate.FORMAT_DECIMAL_FORCE_POINT));
		inWriter.write("\">\n");
		// altitude if available
		if (inPoint.hasAltitude() || inSettings.getExportMissingAltitudesAsZero())
		{
			inWriter.write("\t\t<ele>");
			inWriter.write(inPoint.hasAltitude() ? inPoint.getAltitude().getStringValue(UnitSetLibrary.UNITS_METRES) : "0");
			inWriter.write("</ele>\n");
		}
		// timestamp if available (some waypoints have timestamps, some not)
		if (inPoint.hasTimestamp() && inSettings.getExportTimestamps())
		{
			inWriter.write("\t\t<time>");
			inWriter.write(inPoint.getTimestamp().getText(Timestamp.Format.ISO8601, null));
			inWriter.write("</time>\n");
		}
		// write waypoint name after elevation and time
		inWriter.write("\t\t<name>");
		inWriter.write(XmlUtils.fixCdata(inPoint.getWaypointName().trim()));
		inWriter.write("</name>\n");
		// description, if any
		String desc = XmlUtils.fixCdata(inPoint.getFieldValue(Field.DESCRIPTION));
		if (desc != null && !desc.equals(""))
		{
			inWriter.write("\t\t<desc>");
			inWriter.write(desc);
			inWriter.write("</desc>\n");
		}
		// Media links, if any
		if (inSettings.getExportPhotoPoints() && inPoint.getPhoto() != null)
		{
			inWriter.write("\t\t");
			inWriter.write(makeMediaLink(inPoint.getPhoto()));
			inWriter.write('\n');
		}
		if (inSettings.getExportAudioPoints() && inPoint.getAudio() != null)
		{
			inWriter.write("\t\t");
			inWriter.write(makeMediaLink(inPoint.getAudio()));
			inWriter.write('\n');
		}
		// write waypoint type if any
		String type = inPoint.getFieldValue(Field.WAYPT_TYPE);
		if (type != null)
		{
			type = type.trim();
			if (!type.equals(""))
			{
				inWriter.write("\t\t<type>");
				inWriter.write(type);
				inWriter.write("</type>\n");
			}
		}
		inWriter.write("\t</wpt>\n");
	}


	/**
	 * Export the specified trackpoint into the file
	 * @param inPoint trackpoint to export
	 * @param inWriter writer object
	 * @param inSettings export settings
	 */
	private static void exportTrackpoint(DataPoint inPoint, Writer inWriter, SettingsForExport inSettings)
		throws IOException
	{
		inWriter.write("\t\t\t<trkpt lat=\"");
		inWriter.write(inPoint.getLatitude().output(Coordinate.FORMAT_DECIMAL_FORCE_POINT));
		inWriter.write("\" lon=\"");
		inWriter.write(inPoint.getLongitude().output(Coordinate.FORMAT_DECIMAL_FORCE_POINT));
		inWriter.write("\">\n");
		// altitude
		if (inPoint.hasAltitude() || inSettings.getExportMissingAltitudesAsZero())
		{
			inWriter.write("\t\t\t\t<ele>");
			inWriter.write(inPoint.hasAltitude() ? inPoint.getAltitude().getStringValue(UnitSetLibrary.UNITS_METRES) : "0");
			inWriter.write("</ele>\n");
		}
		// Maybe take timestamp from photo if the point hasn't got one
		Timestamp pointTimestamp = getPointTimestamp(inPoint, inSettings);
		// timestamp if available (and selected)
		if (pointTimestamp != null && inSettings.getExportTimestamps())
		{
			inWriter.write("\t\t\t\t<time>");
			inWriter.write(pointTimestamp.getText(Timestamp.Format.ISO8601, null));
			inWriter.write("</time>\n");
		}
		// photo, audio
		if (inPoint.getPhoto() != null && inSettings.getExportPhotoPoints())
		{
			inWriter.write("\t\t\t\t");
			inWriter.write(makeMediaLink(inPoint.getPhoto()));
			inWriter.write("\n");
		}
		if (inPoint.getAudio() != null && inSettings.getExportAudioPoints()) {
			inWriter.write(makeMediaLink(inPoint.getAudio()));
		}
		inWriter.write("\t\t\t</trkpt>\n");
	}


	/**
	 * Make the xml for the media link(s)
	 * @param inPoint point to generate text for
	 * @return link tags, or null if no links
	 */
	private static String makeMediaLink(DataPoint inPoint)
	{
		Photo photo = inPoint.getPhoto();
		AudioClip audio = inPoint.getAudio();
		if (photo == null && audio == null) {
			return null;
		}
		String linkText = "";
		if (photo != null) {
			linkText = makeMediaLink(photo);
		}
		if (audio != null) {
			linkText += makeMediaLink(audio);
		}
		return linkText;
	}

	/**
	 * Make the media link for a single media item
	 * @param inMedia media item, either photo or audio
	 * @return link for this media
	 */
	private static String makeMediaLink(MediaObject inMedia)
	{
		if (inMedia.getFile() != null)
			// file link
			return "<link href=\"" + inMedia.getFile().getAbsolutePath() + "\"><text>" + inMedia.getName() + "</text></link>";
		if (inMedia.getUrl() != null)
			// url link
			return "<link href=\"" + inMedia.getUrl() + "\"><text>" + inMedia.getName() + "</text></link>";
		// No link available, must have been loaded from zip file - no link possible
		return "";
	}


	/**
	 * Strip the time from a GPX point source string
	 * @param inPointSource point source to copy
	 * @return point source with timestamp removed
	 */
	private static String stripTime(String inPointSource)
	{
		return inPointSource.replaceAll("[ \t]*<time>.*?</time>", "");
	}

	/**
	 * Get the timestamp from the point or its media
	 * @param inPoint point object
	 * @param inSettings export settings
	 * @return Timestamp object if available, or null
	 */
	private static Timestamp getPointTimestamp(DataPoint inPoint, SettingsForExport inSettings)
	{
		if (inPoint.hasTimestamp())
		{
			return inPoint.getTimestamp();
		}
		if (inPoint.getPhoto() != null && inSettings.getExportPhotoPoints())
		{
			if (inPoint.getPhoto().hasTimestamp())
			{
				return inPoint.getPhoto().getTimestamp();
			}
		}
		if (inPoint.getAudio() != null && inSettings.getExportAudioPoints())
		{
			if (inPoint.getAudio().hasTimestamp())
			{
				return inPoint.getAudio().getTimestamp();
			}
		}
		return null;
	}
}
