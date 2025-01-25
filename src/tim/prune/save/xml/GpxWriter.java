package tim.prune.save.xml;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import tim.prune.GpsPrune;
import tim.prune.data.Coordinate;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.FieldGpx;
import tim.prune.data.FileInfo;
import tim.prune.data.MediaObject;
import tim.prune.data.Selection;
import tim.prune.data.Timestamp;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;
import tim.prune.data.UnitSetLibrary;
import tim.prune.gui.ProgressDialog;
import tim.prune.save.SettingsForExport;

/**
 * Class to generate the Gpx contents for any kind of GPX output
 * (whether to file or not)
 */
public abstract class GpxWriter
{
	private final ProgressDialog _progress;
	private final SettingsForExport _settings;
	private int _selectionStart = -1, _selectionEnd = -1;

	/** this program name */
	protected static final String GPX_CREATOR = "GpsPrune v" + GpsPrune.VERSION_NUMBER + " activityworkshop.net";


	/** Constructor */
	public GpxWriter(ProgressDialog inProgress, SettingsForExport inSettings)
	{
		_progress = inProgress;
		_settings = inSettings;
	}

	/** @return the default gpx header string without extensions */
	protected abstract String getDefaultGpxHeader();

	/** @return the default gpx header string with extensions from the loaded files */
	protected abstract String getGpxHeader(FileInfo inInfo);

	/**
	 * Export the information to the given writer
	 * @param inWriter writer object
	 * @param inInfo track info object
	 * @param inName name of track (optional)
	 * @param inDesc description of track (optional)
	 * @return number of points written
	 * @throws IOException if io errors occur on write
	 */
	public int exportData(OutputStreamWriter inWriter, TrackInfo inInfo, String inName,
		String inDesc, FileInfo inFileInfo) throws IOException
	{
		// Write or copy headers
		inWriter.write(getXmlHeaderString(inWriter));
		final String gpxHeader = getGpxHeaderString(inFileInfo);
		inWriter.write(gpxHeader);
		// name and description
		String trackName = (isEmpty(inName) ? "GpsPruneTrack" : XmlUtils.fixCdata(inName));
		String desc = (isEmpty(inDesc) ? "Export from GpsPrune" : XmlUtils.fixCdata(inDesc));
		writeMetadata(inWriter, trackName, desc);

		setSelectionRange(inInfo.getSelection());
		Track track = inInfo.getTrack();
		final int numPoints = track.getNumPoints();
		if (_progress != null) {
			_progress.setMaximumValue(numPoints);
		}
		int numSaved = 0;
		// Export waypoints
		if (_settings.getExportWaypoints()) {
			numSaved += writeWaypoints(inWriter, track);
		}
		// Export both route points and then track points
		if (_settings.getExportTrackPoints() || _settings.getExportPhotoPoints() || _settings.getExportAudioPoints())
		{
			// Output track points, if any
			numSaved += writeTrackPoints(inWriter, track, trackName);
		}

		inWriter.write("</gpx>\n");
		return numSaved;
	}

	private void setSelectionRange(Selection inSelection)
	{
		_selectionStart = _settings.getExportJustSelection() ? inSelection.getStart() : -1;
		_selectionEnd = _settings.getExportJustSelection() ? inSelection.getEnd() : -1;
	}

	private boolean shouldExportIndex(int inIndex)
	{
		if (_selectionStart == -1 || _selectionEnd == -1) {
			return true;
		}
		return inIndex >= _selectionStart && inIndex <= _selectionEnd;
	}

	/** Write the metadata including name and description (depends on file version) */
	protected abstract void writeMetadata(OutputStreamWriter inWriter,
		String inName, String inDesc) throws IOException;

	/**
	 * Write the name and description according to the GPX version number
	 * @param inWriter writer object
	 * @param inName name, or null if none supplied
	 * @param inDesc description, or null if none supplied
	 * @param inIndent indentation string to use
	 */
	protected static void writeNameAndDescription(OutputStreamWriter inWriter,
		String inName, String inDesc, String inIndent) throws IOException
	{
		if (!isEmpty(inName))
		{
			inWriter.write(inIndent);
			inWriter.write("<name>");
			inWriter.write(inName);
			inWriter.write("</name>\n");
		}
		inWriter.write(inIndent);
		inWriter.write("<desc>");
		inWriter.write(inDesc);
		inWriter.write("</desc>\n");
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
	 * @return header string from existing tags or from the default
	 */
	private String getGpxHeaderString(FileInfo inInfo)
	{
		String gpxHeader = null;
		if (inInfo != null)
		{
			// Build header according to selected version _and_ the existing extensions, if any
			gpxHeader = getGpxHeader(inInfo);
		}
		if (gpxHeader == null || gpxHeader.length() < 5) {
			gpxHeader = getDefaultGpxHeader();
		}
		return gpxHeader + "\n";
	}

	/** @return the number of waypoints written */
	private int writeWaypoints(Writer inWriter, Track inTrack)
		throws IOException
	{
		// Loop over waypoints
		final int numPoints = inTrack.getNumPoints();
		int numSaved = 0;
		for (int i=0; i<numPoints; i++)
		{
			if (!shouldExportIndex(i)) {
				continue;
			}
			DataPoint point = inTrack.getPoint(i);
			if (!point.isWaypoint()) {
				continue;
			}
			if (_progress != null)
			{
				if (_progress.wasCancelled()) {
					return -1;
				}
				_progress.setValue(i);
			}
			// Make a wpt element for each waypoint
			exportWaypoint(point, inWriter);
			numSaved++;
		}
		return numSaved;
	}

	/**
	 * Export the specified waypoint into the file
	 * @param inPoint waypoint to export
	 * @param inWriter writer object
	 * @throws IOException on write failure
	 */
	private void exportWaypoint(DataPoint inPoint, Writer inWriter)
		throws IOException
	{
		inWriter.write("\t<wpt lat=\"");
		inWriter.write(inPoint.getLatitude().output(Coordinate.Format.DECIMAL_FORCE_POINT));
		inWriter.write("\" lon=\"");
		inWriter.write(inPoint.getLongitude().output(Coordinate.Format.DECIMAL_FORCE_POINT));
		inWriter.write("\">\n");
		// altitude if available
		if (inPoint.hasAltitude() || _settings.getExportMissingAltitudesAsZero())
		{
			inWriter.write("\t\t<ele>");
			inWriter.write(inPoint.hasAltitude() ? inPoint.getAltitude().getStringValue(UnitSetLibrary.UNITS_METRES) : "0");
			inWriter.write("</ele>\n");
		}
		// timestamp if available (some waypoints have timestamps, some not)
		if (_settings.getExportTimestamps())
		{
			Timestamp waypointTimestamp = getPointTimestamp(inPoint);
			if (waypointTimestamp != null && waypointTimestamp.isValid())
			{
				inWriter.write("\t\t<time>");
				inWriter.write(getPointTimestamp(inPoint).getText(Timestamp.Format.ISO8601, null));
				inWriter.write("</time>\n");
			}
		}
		// magvar, geoidheight
		for (FieldGpx field : FieldGpx.getFirstFields()) {
			writeGpxTag(inWriter, "\t\t", inPoint, field);
		}
		// write waypoint name after elevation and time
		inWriter.write("\t\t<name>");
		inWriter.write(XmlUtils.fixCdata(inPoint.getWaypointName().trim()));
		inWriter.write("</name>\n");
		// comment, if any
		String comment = XmlUtils.fixCdata(inPoint.getFieldValue(Field.COMMENT));
		final String desc = XmlUtils.fixCdata(inPoint.getFieldValue(Field.DESCRIPTION));
		if (isEmpty(comment) && _settings.getCopyDescriptionsToComments()) {
			comment = desc;
		}
		if (!isEmpty(comment))
		{
			inWriter.write("\t\t<cmt>");
			inWriter.write(comment);
			inWriter.write("</cmt>\n");
		}
		// description, if any
		if (!isEmpty(desc))
		{
			inWriter.write("\t\t<desc>");
			inWriter.write(desc);
			inWriter.write("</desc>\n");
		}
		if (versionSupportsPointLinks())
		{
			// Media links, if any
			if (_settings.getExportPhotoPoints() && inPoint.getPhoto() != null)
			{
				inWriter.write("\t\t");
				inWriter.write(makeMediaLink(inPoint.getPhoto()));
				inWriter.write('\n');
			}
			if (_settings.getExportAudioPoints() && inPoint.getAudio() != null)
			{
				inWriter.write("\t\t");
				inWriter.write(makeMediaLink(inPoint.getAudio()));
				inWriter.write('\n');
			}
		}
		// symbol, if any
		final String symbol = XmlUtils.fixCdata(inPoint.getFieldValue(Field.SYMBOL));
		if (!isEmpty(symbol))
		{
			inWriter.write("\t\t<sym>");
			inWriter.write(symbol);
			inWriter.write("</sym>\n");
		}
		// write waypoint type if any
		String type = inPoint.getFieldValue(Field.WAYPT_TYPE);
		if (!isEmpty(type))
		{
			type = type.trim();
			if (!type.equals(""))
			{
				inWriter.write("\t\t<type>");
				inWriter.write(type);
				inWriter.write("</type>\n");
			}
		}
		// fix, sat, hdop, vdop, pdop, ageofdgpsdata, dgpsid
		for (FieldGpx field : FieldGpx.getSecondFields()) {
			writeGpxTag(inWriter, "\t\t", inPoint, field);
		}
		exportWaypointExtensions(inPoint, inWriter);
		inWriter.write("\t</wpt>\n");
	}

	/** @return true if the output version supports the link tag for points */
	protected abstract boolean versionSupportsPointLinks();

	/** Export the extension tags from the given waypoint to the writer */
	protected abstract void exportWaypointExtensions(DataPoint inPoint, Writer inWriter) throws IOException;

	/** Export the extension tags from the given trackpoint to the writer */
	protected abstract void exportTrackpointExtensions(DataPoint inPoint, Writer inWriter) throws IOException;

	/**
	 * Loop through the track outputting the relevant track points
	 * @param inWriter writer object for output
	 * @param inTrack track object
	 * @param inTrackName name of track
	 */
	private int writeTrackPoints(OutputStreamWriter inWriter, Track inTrack, String inTrackName)
	throws IOException
	{
		int numPoints = inTrack.getNumPoints();
		int numSaved = 0;
		final boolean exportTrackPoints = _settings.getExportTrackPoints();
		final boolean exportPhotos = _settings.getExportPhotoPoints();
		final boolean exportAudios = _settings.getExportAudioPoints();
		// Loop over track points
		for (int i=0; i<numPoints; i++)
		{
			if (!shouldExportIndex(i)) {
				continue;
			}
			DataPoint point = inTrack.getPoint(i);
			if (point.isWaypoint()) {
				continue;
			}
			if (_progress != null) {
				_progress.setValue(i);
			}
			if ((point.getPhoto()==null && exportTrackPoints) || (point.getPhoto()!=null && exportPhotos)
				|| (point.getAudio()!=null && exportAudios))
			{
				// restart track segment if necessary
				if ((numSaved > 0) && point.getSegmentStart()) {
					inWriter.write("\t\t</trkseg>\n\t\t<trkseg>\n");
				}
				if (numSaved == 0)
				{
					String trackStart = "\t<trk>\n\t\t<name>" + inTrackName + "</name>\n\t\t<number>1</number>\n\t\t<trkseg>\n";
					inWriter.write(trackStart);
				}
				exportTrackpoint(point, inWriter);
				numSaved++;
			}
		}
		if (numSaved > 0) {
			inWriter.write("\t\t</trkseg>\n\t</trk>\n");
		}
		return numSaved;
	}

	/**
	 * Export the specified trackpoint into the file
	 * @param inPoint trackpoint to export
	 * @param inWriter writer object
	 */
	private void exportTrackpoint(DataPoint inPoint, Writer inWriter)
		throws IOException
	{
		inWriter.write("\t\t\t<trkpt lat=\"");
		inWriter.write(inPoint.getLatitude().output(Coordinate.Format.DECIMAL_FORCE_POINT));
		inWriter.write("\" lon=\"");
		inWriter.write(inPoint.getLongitude().output(Coordinate.Format.DECIMAL_FORCE_POINT));
		inWriter.write("\">\n");
		// altitude
		if (inPoint.hasAltitude() || _settings.getExportMissingAltitudesAsZero())
		{
			inWriter.write("\t\t\t\t<ele>");
			inWriter.write(inPoint.hasAltitude() ? inPoint.getAltitude().getStringValue(UnitSetLibrary.UNITS_METRES) : "0");
			inWriter.write("</ele>\n");
		}
		// Maybe take timestamp from photo if the point hasn't got one
		Timestamp pointTimestamp = getPointTimestamp(inPoint);
		// timestamp if available (and selected)
		if (pointTimestamp != null && _settings.getExportTimestamps())
		{
			inWriter.write("\t\t\t\t<time>");
			inWriter.write(pointTimestamp.getText(Timestamp.Format.ISO8601, null));
			inWriter.write("</time>\n");
		}
		for (FieldGpx field : FieldGpx.getFirstFields()) {
			writeGpxTag(inWriter, "\t\t\t\t", inPoint, field);
		}
		for (FieldGpx field : FieldGpx.getSecondFields()) {
			writeGpxTag(inWriter, "\t\t\t\t", inPoint, field);
		}
		// photo, audio
		if (inPoint.getPhoto() != null && _settings.getExportPhotoPoints())
		{
			inWriter.write("\t\t\t\t");
			inWriter.write(makeMediaLink(inPoint.getPhoto()));
			inWriter.write("\n");
		}
		if (inPoint.getAudio() != null && _settings.getExportAudioPoints()) {
			inWriter.write(makeMediaLink(inPoint.getAudio()));
		}
		exportTrackpointExtensions(inPoint, inWriter);
		inWriter.write("\t\t\t</trkpt>\n");
	}

	private void writeGpxTag(Writer inWriter, String inIndent, DataPoint inPoint, FieldGpx field)
		throws IOException
	{
		String value = inPoint.getFieldValue(field);
		if (value != null && versionSupportsTag(field))
		{
			inWriter.write(inIndent);
			inWriter.write(field.getOpenTag());
			inWriter.write(value);
			inWriter.write(field.getCloseTag());
			inWriter.write('\n');
		}
	}

	/** @return true if this Gpx format supports the specified tag */
	protected abstract boolean versionSupportsTag(FieldGpx inField);

	/**
	 * Get the timestamp from the point or its media
	 * @param inPoint point object
	 * @return Timestamp object if available, or null
	 */
	private Timestamp getPointTimestamp(DataPoint inPoint)
	{
		if (inPoint.hasTimestamp()) {
			return inPoint.getTimestamp();
		}
		if (inPoint.getPhoto() != null && _settings.getExportPhotoPoints())
		{
			if (inPoint.getPhoto().hasTimestamp()) {
				return inPoint.getPhoto().getTimestamp();
			}
		}
		if (inPoint.getAudio() != null && _settings.getExportAudioPoints())
		{
			if (inPoint.getAudio().hasTimestamp()) {
				return inPoint.getAudio().getTimestamp();
			}
		}
		return null;
	}

	/** @return true if the given string is empty */
	protected static boolean isEmpty(String inString) {
		return inString == null || inString.isEmpty();
	}

	/** @return xml fragment linking to the file or Url for the given media */
	protected abstract String makeMediaLink(MediaObject inMedia);
}
