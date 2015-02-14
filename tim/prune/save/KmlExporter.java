package tim.prune.save;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.data.Coordinate;
import tim.prune.data.DataPoint;
import tim.prune.data.Track;

/**
 * Class to export track information
 * into a specified Kml file
 */
public class KmlExporter
{
	private App _app = null;
	private JFrame _parentFrame = null;
	private Track _track = null;
	private JFileChooser _fileChooser = null;


	/**
	 * Constructor giving App object, frame and track
	 * @param inApp application object to inform of success
	 * @param inParentFrame parent frame
	 * @param inTrack track object to save
	 */
	public KmlExporter(App inApp, JFrame inParentFrame, Track inTrack)
	{
		_app = inApp;
		_parentFrame = inParentFrame;
		_track = inTrack;
	}


	/**
	 * Show the dialog to select options and export file
	 */
	public boolean showDialog()
	{
		boolean fileSaved = false;
		Object description = JOptionPane.showInputDialog(_parentFrame,
			I18nManager.getText("dialog.exportkml.text"),
			I18nManager.getText("dialog.exportkml.title"),
			JOptionPane.QUESTION_MESSAGE, null, null, "");
		// TODO: Make dialog window including colour selection, line width, track description
		if (description != null)
		{
			// OK pressed, so choose output file
			if (_fileChooser == null)
				_fileChooser = new JFileChooser();
			_fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
			_fileChooser.setFileFilter(new FileFilter() {
				public boolean accept(File f)
				{
					return (f != null && (f.isDirectory() || f.getName().toLowerCase().endsWith(".kml")));
				}
				public String getDescription()
				{
					return I18nManager.getText("dialog.exportkml.filetype");
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
					if (!file.getName().toLowerCase().endsWith(".kml"))
					{
						file = new File(file.getAbsolutePath() + ".kml");
					}
					// Check if file exists and if necessary prompt for overwrite
					Object[] buttonTexts = {I18nManager.getText("button.overwrite"), I18nManager.getText("button.cancel")};
					if (!file.exists() || JOptionPane.showOptionDialog(_parentFrame,
							I18nManager.getText("dialog.save.overwrite.text"),
							I18nManager.getText("dialog.save.overwrite.title"), JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE, null, buttonTexts, buttonTexts[1])
						== JOptionPane.YES_OPTION)
					{
						if (exportFile(file, description.toString()))
						{
							fileSaved = true;
						}
						else
						{
							chooseAgain = true;
						}
					}
					else
					{
						chooseAgain = true;
					}
				}
			} while (chooseAgain);
		}
		return fileSaved;
	}


	/**
	 * Export the track data to the specified file with description
	 * @param inFile File object to save to
	 * @param inDescription description to use, if any
	 */
	private boolean exportFile(File inFile, String inDescription)
	{
		FileWriter writer = null;
		try
		{
			writer = new FileWriter(inFile);
			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<kml xmlns=\"http://earth.google.com/kml/2.1\">\n<Folder>\n");
			writer.write("\t<name>");
			writer.write(inDescription);
			writer.write("</name>\n");

			int i = 0;
			DataPoint point = null;
			boolean hasTrackpoints = false;
			// Loop over waypoints
			boolean writtenPhotoHeader = false;
			int numPoints = _track.getNumPoints();
			for (i=0; i<numPoints; i++)
			{
				point = _track.getPoint(i);
				if (point.isWaypoint())
				{
					exportWaypoint(point, writer);
				}
				else if (point.getPhoto() != null)
				{
					if (!writtenPhotoHeader)
					{
						writer.write("<Style id=\"camera_icon\"><IconStyle><Icon><href>http://maps.google.com/mapfiles/kml/pal4/icon46.png</href></Icon></IconStyle></Style>");
						writtenPhotoHeader = true;
					}
					exportPhotoPoint(point, writer);
				}
				else
				{
					hasTrackpoints = true;
				}
			}
			if (hasTrackpoints)
			{
				writer.write("\t<Placemark>\n\t\t<name>track</name>\n\t\t<Style>\n\t\t\t<LineStyle>\n"
					+ "\t\t\t\t<color>cc0000cc</color>\n\t\t\t\t<width>4</width>\n\t\t\t</LineStyle>\n"
					+ "\t\t</Style>\n\t\t<LineString>\n\t\t\t<coordinates>");
				// Loop over track points
				for (i=0; i<numPoints; i++)
				{
					point = _track.getPoint(i);
					if (!point.isWaypoint())
					{
						exportTrackpoint(point, writer);
					}
				}
				writer.write("\t\t\t</coordinates>\n\t\t</LineString>\n\t</Placemark>");
			}
			writer.write("</Folder>\n</kml>");
			writer.close();
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("dialog.save.ok1")
				 + " " + numPoints + " " + I18nManager.getText("dialog.save.ok2")
				 + " " + inFile.getAbsolutePath(),
				I18nManager.getText("dialog.save.oktitle"), JOptionPane.INFORMATION_MESSAGE);
			return true;
		}
		catch (IOException ioe)
		{
			try {
				if (writer != null) writer.close();
			}
			catch (IOException ioe2) {}
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("error.save.failed") + ioe.getMessage(),
				I18nManager.getText("error.save.dialogtitle"), JOptionPane.ERROR_MESSAGE);
		}
		return false;
	}


	/**
	 * Export the specified waypoint into the file
	 * @param inPoint waypoint to export
	 * @param inWriter writer object
	 * @throws IOException on write failure
	 */
	private void exportWaypoint(DataPoint inPoint, Writer inWriter) throws IOException
	{
		inWriter.write("\t<Placemark>\n\t\t<name>");
		inWriter.write(inPoint.getWaypointName().trim());
		inWriter.write("</name>\n");
		inWriter.write("\t\t<Point>\n\t\t\t<coordinates>");
		inWriter.write(inPoint.getLongitude().output(Coordinate.FORMAT_DEG_WITHOUT_CARDINAL));
		inWriter.write(',');
		inWriter.write(inPoint.getLatitude().output(Coordinate.FORMAT_DEG_WITHOUT_CARDINAL));
		inWriter.write(",0</coordinates>\n\t\t</Point>\n\t</Placemark>\n");
	}


	/**
	 * Export the specified photo into the file
	 * @param inPoint data point including photo
	 * @param inWriter writer object
	 * @throws IOException on write failure
	 */
	private void exportPhotoPoint(DataPoint inPoint, Writer inWriter) throws IOException
	{
		// TODO: Export photos to KML too - for photos need kmz!
		inWriter.write("\t<Placemark>\n\t\t<name>");
		inWriter.write(inPoint.getPhoto().getFile().getName());
		inWriter.write("</name>\n");
		inWriter.write("<styleUrl>#camera_icon</styleUrl>\n");
		inWriter.write("\t\t<Point>\n\t\t\t<coordinates>");
		inWriter.write(inPoint.getLongitude().output(Coordinate.FORMAT_DEG_WITHOUT_CARDINAL));
		inWriter.write(',');
		inWriter.write(inPoint.getLatitude().output(Coordinate.FORMAT_DEG_WITHOUT_CARDINAL));
		inWriter.write(",0</coordinates>\n\t\t</Point>\n\t</Placemark>\n");
	}


	/**
	 * Export the specified trackpoint into the file
	 * @param inPoint trackpoint to export
	 * @param inWriter writer object
	 */
	private void exportTrackpoint(DataPoint inPoint, Writer inWriter) throws IOException
	{
		inWriter.write(inPoint.getLongitude().output(Coordinate.FORMAT_DEG_WITHOUT_CARDINAL));
		inWriter.write(',');
		inWriter.write(inPoint.getLatitude().output(Coordinate.FORMAT_DEG_WITHOUT_CARDINAL));
		// Altitude not exported, locked to ground by Google Earth
		inWriter.write(",0\n");
	}
}
