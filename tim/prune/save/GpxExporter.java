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
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import tim.prune.App;
import tim.prune.Config;
import tim.prune.GenericFunction;
import tim.prune.GpsPruner;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.data.Altitude;
import tim.prune.data.Coordinate;
import tim.prune.data.DataPoint;
import tim.prune.data.Timestamp;
import tim.prune.data.Track;
import tim.prune.load.GenericFileFilter;

/**
 * Class to export track information
 * into a specified Gpx file
 */
public class GpxExporter extends GenericFunction implements Runnable
{
	private Track _track = null;
	private JDialog _dialog = null;
	private JTextField _nameField = null;
	private JTextField _descriptionField = null;
	private PointTypeSelector _pointTypeSelector = null;
	private JCheckBox _timestampsCheckbox = null;
	private JFileChooser _fileChooser = null;
	private File _exportFile = null;

	/** version number of Gpx */
	private static final String GPX_VERSION_NUMBER = "1.0";
	/** this program name */
	private static final String GPX_CREATOR = "Prune v" + GpsPruner.VERSION_NUMBER + " activityworkshop.net";


	/**
	 * Constructor
	 * @param inApp app object
	 */
	public GpxExporter(App inApp)
	{
		super(inApp);
		_track = inApp.getTrackInfo().getTrack();
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
		// Make a central panel with the text boxes
		JPanel descPanel = new JPanel();
		descPanel.setLayout(new GridLayout(2, 2));
		descPanel.add(new JLabel(I18nManager.getText("dialog.exportgpx.name")));
		_nameField = new JTextField(10);
		descPanel.add(_nameField);
		descPanel.add(new JLabel(I18nManager.getText("dialog.exportgpx.desc")));
		_descriptionField = new JTextField(10);
		descPanel.add(_descriptionField);
		mainPanel.add(descPanel);
		// point type selection (track points, waypoints, photo points)
		_pointTypeSelector = new PointTypeSelector();
		mainPanel.add(_pointTypeSelector);
		// checkbox for timestamps
		_timestampsCheckbox = new JCheckBox(I18nManager.getText("dialog.exportgpx.includetimestamps"));
		_timestampsCheckbox.setSelected(true);
		mainPanel.add(_timestampsCheckbox);
		dialogPanel.add(mainPanel, BorderLayout.CENTER);

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
			public void actionPerformed(ActionEvent e)
			{
				_dialog.dispose();
			}
		});
		buttonPanel.add(cancelButton);
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
		dialogPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 15));
		return dialogPanel;
	}


	/**
	 * Start the export process based on the input parameters
	 */
	private void startExport()
	{
		// OK pressed, so check selections
		if (!_pointTypeSelector.getAnythingSelected()) {
			return;
		}
		// Choose output file
		if (_fileChooser == null)
		{
			_fileChooser = new JFileChooser();
			_fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
			_fileChooser.setFileFilter(new GenericFileFilter("filetype.gpx", new String[] {"gpx"}));
			_fileChooser.setAcceptAllFileFilterUsed(false);
			// start from directory in config which should be set
			String configDir = Config.getConfigString(Config.KEY_TRACK_DIR);
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
				// Check file extension
				if (!file.getName().toLowerCase().endsWith(".gpx"))
				{
					file = new File(file.getAbsolutePath() + ".gpx");
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
		OutputStreamWriter writer = null;
		try
		{
			// normal writing to file
			writer = new OutputStreamWriter(new FileOutputStream(_exportFile));
			boolean[] saveFlags = {_pointTypeSelector.getTrackpointsSelected(), _pointTypeSelector.getWaypointsSelected(),
				_pointTypeSelector.getPhotopointsSelected(), _timestampsCheckbox.isSelected()};
			// write file
			final int numPoints = exportData(writer, _track, _nameField.getText(),
				_descriptionField.getText(), saveFlags);

			// close file
			writer.close();
			// Store directory in config for later
			Config.setConfigString(Config.KEY_TRACK_DIR, _exportFile.getParentFile().getAbsolutePath());
			// Show confirmation
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
	 * @param inTrack track object containing data
	 * @param inName name of track (optional)
	 * @param inDesc description of track (optional)
	 * @param inSaveFlags array of booleans to export tracks, waypoints, photos, timestamps
	 * @return number of points written
	 * @throws IOException if io errors occur on write
	 */
	public static int exportData(OutputStreamWriter inWriter, Track inTrack, String inName,
		String inDesc, boolean[] inSaveFlags) throws IOException
	{
		inWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<gpx version=\"");
		inWriter.write(GPX_VERSION_NUMBER);
		inWriter.write("\" creator=\"");
		inWriter.write(GPX_CREATOR);
		inWriter.write("\"\n xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
			+ " xmlns=\"http://www.topografix.com/GPX/1/0\""
			+ " xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd\">\n");
		// Name field
		String trackName = "PruneTrack";
		if (inName != null && !inName.equals(""))
		{
			trackName = inName;
			inWriter.write("\t<name>");
			inWriter.write(trackName);
			inWriter.write("</name>\n");
		}
		// Description field
		inWriter.write("\t<desc>");
		if (inDesc != null && !inDesc.equals("")) {
			inWriter.write(inDesc);
		}
		else
		{
			inWriter.write("Export from Prune");
		}
		inWriter.write("</desc>\n");

		int i = 0;
		DataPoint point = null;
		boolean hasTrackpoints = false;
		final boolean exportTrackpoints = inSaveFlags[0];
		final boolean exportWaypoints = inSaveFlags[1];
		final boolean exportPhotos = inSaveFlags[2];
		final boolean exportTimestamps = inSaveFlags[3];
		// Loop over waypoints
		final int numPoints = inTrack.getNumPoints();
		int numSaved = 0;
		for (i=0; i<numPoints; i++)
		{
			point = inTrack.getPoint(i);
			// Make a wpt element for each waypoint
			if (point.isWaypoint())
			{
				if (exportWaypoints) {
					exportWaypoint(point, inWriter, exportTimestamps);
					numSaved++;
				}
			}
			else
			{
				hasTrackpoints = true;
			}
		}
		// Output the track, if there is one
		if (hasTrackpoints)
		{
			boolean firstPoint = true;
			inWriter.write("\t<trk><name>" + trackName + "</name><number>1</number><trkseg>\n");
			// Loop over track points
			for (i=0; i<numPoints; i++)
			{
				point = inTrack.getPoint(i);
				// restart track segment if necessary
				if (point.getSegmentStart() && !firstPoint) {
					inWriter.write("\t</trkseg>\n\t<trkseg>\n");
				}
				if (!point.isWaypoint())
				{
					if ((point.getPhoto()==null && exportTrackpoints) || (point.getPhoto()!=null && exportPhotos))
					{
						// export the point
						exportTrackpoint(point, inWriter, exportTimestamps);
						numSaved++;
						firstPoint = false;
					}
				}
			}
			inWriter.write("\t</trkseg></trk>\n");
		}
		inWriter.write("</gpx>\n");
		return numSaved;
	}

	/**
	 * Export the specified waypoint into the file
	 * @param inPoint waypoint to export
	 * @param inWriter writer object
	 * @param inTimestamps true to export timestamps too
	 * @throws IOException on write failure
	 */
	private static void exportWaypoint(DataPoint inPoint, Writer inWriter, boolean inTimestamps)
		throws IOException
	{
		inWriter.write("\t<wpt lat=\"");
		inWriter.write(inPoint.getLatitude().output(Coordinate.FORMAT_DECIMAL_FORCE_POINT));
		inWriter.write("\" lon=\"");
		inWriter.write(inPoint.getLongitude().output(Coordinate.FORMAT_DECIMAL_FORCE_POINT));
		inWriter.write("\">\n");
		// altitude if available
		if (inPoint.hasAltitude())
		{
			inWriter.write("\t\t<ele>");
			inWriter.write("" + inPoint.getAltitude().getStringValue(Altitude.Format.METRES));
			inWriter.write("</ele>\n");
		}
		// write waypoint name after elevation
		inWriter.write("\t\t<name>");
		inWriter.write(inPoint.getWaypointName().trim());
		inWriter.write("</name>\n");
		// timestamp if available (point might have timestamp and then be turned into a waypoint)
		if (inPoint.hasTimestamp() && inTimestamps)
		{
			inWriter.write("\t\t<time>");
			inWriter.write(inPoint.getTimestamp().getText(Timestamp.FORMAT_ISO_8601));
			inWriter.write("</time>\n");
		}
		// TODO: Include waypt type in Gpx
		inWriter.write("\t</wpt>\n");
	}


	/**
	 * Export the specified trackpoint into the file
	 * @param inPoint trackpoint to export
	 * @param inWriter writer object
	 * @param inTimestamps true to export timestamps too
	 */
	private static void exportTrackpoint(DataPoint inPoint, Writer inWriter, boolean inTimestamps)
		throws IOException
	{
		inWriter.write("\t\t<trkpt lat=\"");
		inWriter.write(inPoint.getLatitude().output(Coordinate.FORMAT_DECIMAL_FORCE_POINT));
		inWriter.write("\" lon=\"");
		inWriter.write(inPoint.getLongitude().output(Coordinate.FORMAT_DECIMAL_FORCE_POINT));
		inWriter.write("\">");
		// altitude
		if (inPoint.hasAltitude())
		{
			inWriter.write("<ele>");
			inWriter.write("" + inPoint.getAltitude().getStringValue(Altitude.Format.METRES));
			inWriter.write("</ele>");
		}
		// timestamp if available (and selected)
		if (inPoint.hasTimestamp() && inTimestamps)
		{
			inWriter.write("<time>");
			inWriter.write(inPoint.getTimestamp().getText(Timestamp.FORMAT_ISO_8601));
			inWriter.write("</time>");
		}
		inWriter.write("</trkpt>\n");
	}
}
