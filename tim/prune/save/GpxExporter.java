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

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import tim.prune.GpsPruner;
import tim.prune.I18nManager;
import tim.prune.data.Altitude;
import tim.prune.data.Coordinate;
import tim.prune.data.DataPoint;
import tim.prune.data.Timestamp;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;

/**
 * Class to export track information
 * into a specified Gpx file
 */
public class GpxExporter implements Runnable
{
	private JFrame _parentFrame = null;
	private Track _track = null;
	private JDialog _dialog = null;
	private JTextField _nameField = null;
	private JTextField _descriptionField = null;
	private JFileChooser _fileChooser = null;
	private File _exportFile = null;

	/** version number of Gpx */
	private static final String GPX_VERSION_NUMBER = "1.1";
	/** this program name */
	private static final String GPX_CREATOR = "Prune v" + GpsPruner.VERSION_NUMBER + " activityworkshop.net";


	/**
	 * Constructor giving frame and track
	 * @param inParentFrame parent frame
	 * @param inTrackInfo track info object to save
	 */
	public GpxExporter(JFrame inParentFrame, TrackInfo inTrackInfo)
	{
		_parentFrame = inParentFrame;
		_track = inTrackInfo.getTrack();
	}


	/**
	 * Show the dialog to select options and export file
	 */
	public void showDialog()
	{
		// Make dialog window
		if (_dialog == null)
		{
			_dialog = new JDialog(_parentFrame, I18nManager.getText("dialog.exportgpx.title"), true);
			_dialog.setLocationRelativeTo(_parentFrame);
			_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			_dialog.getContentPane().add(makeDialogComponents());
			_dialog.pack();
		}
		_dialog.show();
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
		return dialogPanel;
	}


	/**
	 * Start the export process based on the input parameters
	 */
	private void startExport()
	{
		// OK pressed, so choose output file
		if (_fileChooser == null)
			{_fileChooser = new JFileChooser();}
		_fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		_fileChooser.setFileFilter(new FileFilter() {
			public boolean accept(File f)
			{
				return (f != null && (f.isDirectory()
					|| f.getName().toLowerCase().endsWith(".gpx")));
			}
			public String getDescription()
			{
				return I18nManager.getText("dialog.exportgpx.filetype");
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
			// write file
			int numPoints = exportData(writer);

			// close file
			writer.close();
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("dialog.save.ok1")
				 + " " + numPoints + " " + I18nManager.getText("dialog.save.ok2")
				 + " " + _exportFile.getAbsolutePath(),
				I18nManager.getText("dialog.save.oktitle"), JOptionPane.INFORMATION_MESSAGE);
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
	 * @return number of points written
	 */
	private int exportData(OutputStreamWriter inWriter) throws IOException
	{
		inWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<gpx version=\"");
		inWriter.write(GPX_VERSION_NUMBER);
		inWriter.write("\" creator=\"");
		inWriter.write(GPX_CREATOR);
		inWriter.write("\">\n");
		// Name field
		if (_nameField != null && _nameField.getText() != null && !_nameField.getText().equals(""))
		{
			inWriter.write("\t<name>");
			inWriter.write(_nameField.getText());
			inWriter.write("</name>\n");
		}
		// Description field
		inWriter.write("\t<desc>");
		if (_descriptionField != null && _descriptionField.getText() != null && !_descriptionField.getText().equals(""))
		{
			inWriter.write(_descriptionField.getText());
		}
		else
		{
			inWriter.write("Export from Prune");
		}
		inWriter.write("</desc>\n");

		int i = 0;
		DataPoint point = null;
		boolean hasTrackpoints = false;
		// Loop over waypoints
		int numPoints = _track.getNumPoints();
		for (i=0; i<numPoints; i++)
		{
			point = _track.getPoint(i);
			// Make a blob for each waypoint
			if (point.isWaypoint())
			{
				exportWaypoint(point, inWriter);
			}
			else
			{
				hasTrackpoints = true;
			}
		}
		// Make a line for the track, if there is one
		// TODO: Look at segments of track, and split into separate track segments in Gpx if necessary
		if (hasTrackpoints)
		{
			inWriter.write("\t<trk><trkseg>\n");
			// Loop over track points
			for (i=0; i<numPoints; i++)
			{
				point = _track.getPoint(i);
				if (!point.isWaypoint())
				{
					exportTrackpoint(point, inWriter);
				}
			}
			inWriter.write("\t</trkseg></trk>\n");
		}
		inWriter.write("</gpx>\n");
		return numPoints;
	}


	/**
	 * Export the specified waypoint into the file
	 * @param inPoint waypoint to export
	 * @param inWriter writer object
	 * @throws IOException on write failure
	 */
	private void exportWaypoint(DataPoint inPoint, Writer inWriter) throws IOException
	{
		inWriter.write("\t<wpt lat=\"");
		inWriter.write(inPoint.getLatitude().output(Coordinate.FORMAT_DEG_WITHOUT_CARDINAL));
		inWriter.write("\" lon=\"");
		inWriter.write(inPoint.getLongitude().output(Coordinate.FORMAT_DEG_WITHOUT_CARDINAL));
		inWriter.write("\">\n");
		inWriter.write("\t\t<name>");
		inWriter.write(inPoint.getWaypointName().trim());
		inWriter.write("</name>\n");
		// altitude if available
		if (inPoint.hasAltitude())
		{
			inWriter.write("\t\t<ele>");
			inWriter.write("" + inPoint.getAltitude().getValue(Altitude.FORMAT_METRES));
			inWriter.write("</ele>\n");
		}
		// timestamp if available (point might have altitude and then be turned into a waypoint)
		if (inPoint.hasTimestamp())
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
	 */
	private void exportTrackpoint(DataPoint inPoint, Writer inWriter) throws IOException
	{
		inWriter.write("\t\t<trkpt lat=\"");
		inWriter.write(inPoint.getLatitude().output(Coordinate.FORMAT_DEG_WITHOUT_CARDINAL));
		inWriter.write("\" lon=\"");
		inWriter.write(inPoint.getLongitude().output(Coordinate.FORMAT_DEG_WITHOUT_CARDINAL));
		inWriter.write("\">");
		// altitude
		if (inPoint.hasAltitude())
		{
			inWriter.write("<ele>");
			inWriter.write("" + inPoint.getAltitude().getValue(Altitude.FORMAT_METRES));
			inWriter.write("</ele>");
		}
		// timestamp if available
		if (inPoint.hasTimestamp())
		{
			inWriter.write("<time>");
			inWriter.write(inPoint.getTimestamp().getText(Timestamp.FORMAT_ISO_8601));
			inWriter.write("</time>");
		}
		inWriter.write("</trkpt>\n");
	}
}
