package tim.prune.save.xml;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.swing.ImageIcon;

import tim.prune.data.Coordinate;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.Timestamp;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;
import tim.prune.data.UnitSetLibrary;
import tim.prune.gui.ImageUtils;

/**
 * Class to write the current track information
 * into a specified Kml or Kmz file
 */
public abstract class KmlWriter
{
	protected final TrackInfo _trackInfo;
	protected final KmlExportOptions _exportOptions;
	private final ProgressUpdater _progressUpdater;

	private boolean _cancelPressed = false;
	private Dimension[] _imageDimensions = null;

	/** Interface for progress callbacks */
	interface ProgressUpdater {
		void setProgress(int inValue);
	}

	/**
	 * Constructor
	 * @param inTrackInfo track info object
	 * @param inOptions all the export options
	 */
	public KmlWriter(TrackInfo inTrackInfo, KmlExportOptions inOptions,
		ProgressUpdater inUpdater)
	{
		_trackInfo = inTrackInfo;
		_exportOptions = inOptions;
		_progressUpdater = inUpdater;
	}

	/** Inform the writer to stop when possible */
	void cancelExport() {
		_cancelPressed = true;
	}

	/**
	 * Export the information to the given writer
	 * @param inWriter writer object
	 * @param inExportImages true if image thumbnails are to be referenced
	 * @return number of points written
	 */
	int exportData(OutputStreamWriter inWriter, boolean inExportImages)
	throws IOException
	{
		boolean writeTrack = _exportOptions.getExportTrackPoints();
		boolean writeWaypoints = _exportOptions.getExportWaypoints();
		boolean writePhotos = _exportOptions.getExportPhotos();
		boolean writeAudios = _exportOptions.getExportAudios();
		boolean justSelection = _exportOptions.getExportJustSelection();
		// Define xml header (depending on whether extensions are used or not)
		writeXmlHeader(inWriter);
		inWriter.write("<Folder>\n\t<name>");
		String title = _exportOptions.getTitle();
		if (title != null && !title.equals("")) {
			inWriter.write(XmlUtils.fixCdata(title));
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

		final Track track = _trackInfo.getTrack();
		final boolean absoluteAltitudes = _exportOptions.getAbsoluteAltitudes();
		final int numPoints = track.getNumPoints();
		boolean hasTrackpoints = false;
		boolean writtenPhotoHeader = false, writtenAudioHeader = false;
		int numSaved = 0;
		int photoNum = 0;
		// Loop over waypoints
		for (int i=0; i<numPoints; i++)
		{
			if (justSelection && (i < selStart || i > selEnd)) {
				continue;
			}
			DataPoint point = track.getPoint(i);
			if (!point.hasMedia())
			{
				// Make a blob for each waypoint
				if (point.isWaypoint())
				{
					if (writeWaypoints)
					{
						exportWaypoint(point, inWriter, absoluteAltitudes);
						numSaved++;
					}
				}
				else {
					hasTrackpoints = true;
				}
			}
			// Make a blob with description for each photo
			// Photos have already been written so picture sizes already known
			if (point.getPhoto() != null && point.getPhoto().isValid() && writePhotos)
			{
				if (!writtenPhotoHeader)
				{
					inWriter.write("\t<Style id=\"camera_icon\"><IconStyle><Icon><href>https://maps.google.com/mapfiles/kml/pal4/icon46.png</href></Icon></IconStyle></Style>\n");
					writtenPhotoHeader = true;
				}
				photoNum++;
				exportPhotoPoint(point, inWriter, inExportImages, i, photoNum, absoluteAltitudes);
				numSaved++;
			}
			// Make a blob with description for each audio clip
			if (point.getAudio() != null && writeAudios)
			{
				if (!writtenAudioHeader)
				{
					inWriter.write("\t<Style id=\"audio_icon\"><IconStyle><color>ff00ffff</color><Icon><href>https://maps.google.com/mapfiles/kml/shapes/star.png</href></Icon></IconStyle></Style>\n");
					writtenAudioHeader = true;
				}
				exportAudioPoint(point, inWriter, absoluteAltitudes);
				numSaved++;
			}
		}
		// Make a line for the track, if there is one
		if (hasTrackpoints && writeTrack)
		{
			numSaved += writeTrack(inWriter, absoluteAltitudes, selStart, selEnd);
		}
		inWriter.write("\n</Folder>\n</kml>\n");
		return numSaved;
	}

	/** Write a different header depending on the version */
	protected abstract void writeXmlHeader(OutputStreamWriter inWriter) throws IOException;

	/** Write the track depending on the version */
	protected abstract int writeTrack(OutputStreamWriter inWriter,
		boolean inAbsoluteAltitudes, int inSelStart, int inSelEnd) throws IOException;

	/**
	 * Reverse the hex code for the colours for KML's stupid backwards format
	 * @param inCode colour code rrggbb
	 * @return kml code bbggrr
	 */
	protected static String reverseRGB(String inCode)
	{
		if (inCode == null || inCode.length() != 6) {
			return inCode;
		}
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
	private void exportAudioPoint(DataPoint inPoint, Writer inWriter, boolean inAbsoluteAltitude)
		throws IOException
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
		if (inPoint.isWaypoint()) {
			name = inPoint.getWaypointName();
		}
		String desc = null;
		if (inImageLink)
		{
			Dimension imageSize = _imageDimensions[inPointNumber];
			// Create html for the thumbnail images
			desc = "<![CDATA[<br/><table border='0'><tr><td><center><img src='images/image"
				+ inImageNumber + ".jpg' width='" + imageSize.width + "' height='" + imageSize.height + "'></center></td></tr>"
				+ "<tr><td><center>" + name + wrapInBrackets(getPhotoTimeString(inPoint))
				+ "</center></td></tr>"
				+ wrapInTableRow(getPointCaption(inPoint)) + "</table>]]>";
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
			inWriter.write("\t\t<styleUrl>#");
			inWriter.write(inStyle);
			inWriter.write("</styleUrl>\n");
		}
		// I guess we can always support timestamps now?
		Timestamp pointTimestamp = getPointTimestamp(inPoint);
		if (pointTimestamp != null && pointTimestamp.isValid())
		{
			inWriter.write("\t\t<Timestamp>\n\t\t\t<when>");
			inWriter.write(pointTimestamp.getText(Timestamp.Format.ISO8601, null));
			inWriter.write("</when>\n\t\t</Timestamp>\n");
		}
		inWriter.write("\t\t<Point>\n");
		if (inAbsoluteAltitude && inPoint.hasAltitude()) {
			inWriter.write("\t\t\t<altitudeMode>absolute</altitudeMode>\n");
		}
		else {
			inWriter.write("\t\t\t<altitudeMode>clampToGround</altitudeMode>\n");
		}
		inWriter.write("\t\t\t<coordinates>");
		inWriter.write(inPoint.getLongitude().output(Coordinate.Format.DECIMAL_FORCE_POINT));
		inWriter.write(',');
		inWriter.write(inPoint.getLatitude().output(Coordinate.Format.DECIMAL_FORCE_POINT));
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
	 * Loop through the photos and create thumbnails
	 * @param inZipStream zip stream to save image files to
	 * @param inThumbSize thumbnail size
	 */
	void exportThumbnails(ZipOutputStream inZipStream, int inThumbSize)
	throws IOException
	{
		// set up image writer
		Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
		if (writers == null || !writers.hasNext()) {
			throw new IOException("no JPEG writer found");
		}
		ImageWriter imageWriter = writers.next();

		// Check selection checkbox
		final boolean justSelection = _exportOptions.getExportJustSelection();
		int selStart = -1, selEnd = -1;
		if (justSelection)
		{
			selStart = _trackInfo.getSelection().getStart();
			selEnd = _trackInfo.getSelection().getEnd();
		}

		final Track track = _trackInfo.getTrack();
		final int numPoints = track.getNumPoints();
		_imageDimensions = new Dimension[numPoints];
		int photoNum = 0;
		// Loop over all points in track
		for (int i=0; i<numPoints && !_cancelPressed; i++)
		{
			DataPoint point = track.getPoint(i);
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
				if (_progressUpdater != null) {
					_progressUpdater.setProgress(photoNum + 1);
				}
			}
		}
	}

	/**
	 * @param inPoint photo point
	 * @return either point timestamp or photo timestamp, as string
	 */
	private String getPhotoTimeString(DataPoint inPoint)
	{
		TimeZone timezone = _exportOptions.getTimezone();
		if (inPoint.hasTimestamp()) {
			return inPoint.getTimestamp().getTimeText(timezone);
		}
		if (inPoint.getPhoto().hasTimestamp()) {
			return inPoint.getPhoto().getTimestamp().getTimeText(timezone);
		}
		return null;
	}

	/**
	 * @return given string wrapped in brackets (unless it's missing)
	 */
	private static String wrapInBrackets(String fieldString)
	{
		if (fieldString == null || fieldString.isBlank()) {
			return "";
		}
		return " (" + fieldString + ")";
	}

	/**
	 * @return either point description or comment
	 */
	private static String getPointCaption(DataPoint inPoint)
	{
		final String desc = inPoint.getFieldValue(Field.DESCRIPTION);
		if (desc != null && !desc.isBlank()) {
			return desc;
		}
		return inPoint.getFieldValue(Field.COMMENT);
	}

	/**
	 * @return given string wrapped in a table row (unless it's missing)
	 */
	private static String wrapInTableRow(String fieldString)
	{
		if (fieldString == null || fieldString.isBlank()) {
			return "";
		}
		return "<tr><td>" + fieldString + "</td></tr>";
	}

	/**
	 * Get the timestamp from the point or its media
	 * @param inPoint point object
	 * @return Timestamp object if available, or null
	 */
	private static Timestamp getPointTimestamp(DataPoint inPoint)
	{
		if (inPoint.hasTimestamp()) {
			return inPoint.getTimestamp();
		}
		if (inPoint.getPhoto() != null)
		{
			if (inPoint.getPhoto().hasTimestamp()) {
				return inPoint.getPhoto().getTimestamp();
			}
		}
		if (inPoint.getAudio() != null)
		{
			if (inPoint.getAudio().hasTimestamp()) {
				return inPoint.getAudio().getTimestamp();
			}
		}
		return null;
	}
}
