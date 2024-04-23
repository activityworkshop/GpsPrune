package tim.prune.save;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import tim.prune.GpsPrune;
import tim.prune.data.AudioClip;
import tim.prune.data.Coordinate;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.MediaObject;
import tim.prune.data.Photo;
import tim.prune.data.Selection;
import tim.prune.data.Timestamp;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;
import tim.prune.data.UnitSetLibrary;
import tim.prune.gui.ProgressDialog;
import tim.prune.save.xml.GpxCacherList;
import tim.prune.save.xml.XmlUtils;

/**
 * Class to generate the Gpx contents for any kind of GPX output
 * (whether to file or not)
 */
public class GpxWriter
{
	private final ProgressDialog _progress;
	private final SettingsForExport _settings;
	private int _selectionStart = -1, _selectionEnd = -1;

	/** this program name */
	private static final String GPX_CREATOR = "GpsPrune v" + GpsPrune.VERSION_NUMBER + " activityworkshop.net";


	/** Constructor */
	public GpxWriter(ProgressDialog inProgress, SettingsForExport inSettings)
	{
		_progress = inProgress;
		_settings = inSettings;
	}

	/**
	 * Export the information to the given writer
	 * @param inWriter writer object
	 * @param inInfo track info object
	 * @param inName name of track (optional)
	 * @param inDesc description of track (optional)
	 * @param inGpxCachers list of Gpx cachers containing input data
	 * @return number of points written
	 * @throws IOException if io errors occur on write
	 */
	public int exportData(OutputStreamWriter inWriter, TrackInfo inInfo, String inName,
		String inDesc, GpxCacherList inGpxCachers) throws IOException
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

		setSelectionRange(inInfo.getSelection());
		Track track = inInfo.getTrack();
		final int numPoints = track.getNumPoints();
		if (_progress != null) {
			_progress.setMaximumValue(numPoints);
		}
		int numSaved = 0;
		List<String> pointSources = getPointSources(track, inGpxCachers);
		// Export waypoints
		if (_settings.getExportWaypoints()) {
			numSaved += writeWaypoints(inWriter, track, pointSources);
		}
		// Export both route points and then track points
		if (_settings.getExportTrackPoints() || _settings.getExportPhotoPoints() || _settings.getExportAudioPoints())
		{
			// Output all route points (if any)
			numSaved += writeTrackPoints(inWriter, track,
				true, pointSources, "<rtept", "\t<rte><number>1</number>\n",
				null, "\t</rte>\n");
			// Output all track points, if any
			String trackStart = "\t<trk>\n\t\t<name>" + trackName + "</name>\n\t\t<number>1</number>\n\t\t<trkseg>\n";
			numSaved += writeTrackPoints(inWriter, track,
				false, pointSources, "<trkpt", trackStart,
				"\t</trkseg>\n\t<trkseg>\n", "\t\t</trkseg>\n\t</trk>\n");
		}

		inWriter.write("</gpx>\n");
		return numSaved;
	}

	private List<String> getPointSources(Track inTrack, GpxCacherList inGpxCachers)
	{
		ArrayList<String> sources = new ArrayList<>();
		final int numPoints = inTrack.getNumPoints();
		for (int i=0; i<numPoints; i++)
		{
			String src = null;
			if (shouldExportIndex(i)) {
				DataPoint point = inTrack.getPoint(i);
				src = (inGpxCachers == null ? null : getPointSource(inGpxCachers, point));
			}
			sources.add(src);
		}
		return sources;
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
		if (inIsVersion1_1) {
			inWriter.write("\t</metadata>\n");
		}
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

	private int writeWaypoints(Writer inWriter, Track inTrack, List<String> inPointSources)
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
			String pointSource = inPointSources.get(i);
			if (pointSource != null)
			{
				// If timestamp checkbox is off, strip time
				if (!_settings.getExportTimestamps()) {
					pointSource = stripTime(pointSource);
				}
				inWriter.write('\t');
				inWriter.write(pointSource);
				inWriter.write('\n');
			}
			else {
				exportWaypoint(point, inWriter);
			}
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
		// write waypoint name after elevation and time
		inWriter.write("\t\t<name>");
		inWriter.write(XmlUtils.fixCdata(inPoint.getWaypointName().trim()));
		inWriter.write("</name>\n");
		// description, if any
		final String desc = XmlUtils.fixCdata(inPoint.getFieldValue(Field.DESCRIPTION));
		if (desc != null && !desc.equals(""))
		{
			inWriter.write("\t\t<desc>");
			inWriter.write(desc);
			inWriter.write("</desc>\n");
		}
		// comment, if any
		String comment = XmlUtils.fixCdata(inPoint.getFieldValue(Field.COMMENT));
		if (comment == null || comment.equals("") && _settings.getCopyDescriptionsToComments()) {
			comment = desc;
		}
		if (comment != null && !comment.equals(""))
		{
			inWriter.write("\t\t<cmt>");
			inWriter.write(comment);
			inWriter.write("</cmt>\n");
		}
		// symbol, if any
		final String symbol = XmlUtils.fixCdata(inPoint.getFieldValue(Field.SYMBOL));
		if (symbol != null && !symbol.equals(""))
		{
			inWriter.write("\t\t<sym>");
			inWriter.write(symbol);
			inWriter.write("</sym>\n");
		}
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
	 * Get the point source for the specified point
	 * @param inCachers list of GPX cachers to ask for source
	 * @param inPoint point object
	 * @return xml source if available, or null otherwise
	 */
	private static String getPointSource(GpxCacherList inCachers, DataPoint inPoint)
	{
		if (inCachers == null || inPoint == null) {
			return null;
		}
		String source = inCachers.getSourceString(inPoint);
		if (source == null || !inPoint.isModified()) {
			return source;
		}
		// Point has been modified - maybe it's possible to modify the source
		source = replaceGpxTags(source, "lat=\"", "\"", inPoint.getLatitude().output(Coordinate.Format.DECIMAL_FORCE_POINT));
		source = replaceGpxTags(source, "lon=\"", "\"", inPoint.getLongitude().output(Coordinate.Format.DECIMAL_FORCE_POINT));
		source = replaceGpxTags(source, "<ele>", "</ele>", inPoint.getAltitude().getStringValue(UnitSetLibrary.UNITS_METRES));
		source = replaceGpxTags(source, "<time>", "</time>", inPoint.getTimestamp().getText(Timestamp.Format.ISO8601, null));
		if (inPoint.isWaypoint())
		{
			source = replaceGpxTags(source, "<name>", "</name>", XmlUtils.fixCdata(inPoint.getWaypointName()));
			if (source != null) {
				source = source.replaceAll("<description>", "<desc>").replaceAll("</description>", "</desc>");
			}
			source = replaceGpxTags(source, "<desc>", "</desc>",
				XmlUtils.fixCdata(inPoint.getFieldValue(Field.DESCRIPTION)));
			source = replaceGpxTags(source, "<cmt>", "</cmt>", inPoint.getFieldValue(Field.COMMENT));
			source = replaceGpxTags(source, "<type>", "</type>", inPoint.getFieldValue(Field.WAYPT_TYPE));
			source = replaceGpxTags(source, "<sym>", "</sym>", inPoint.getFieldValue(Field.SYMBOL));
		}
		// photo / audio links
		if (source != null && (inPoint.hasMedia() || source.indexOf("</link>") > 0)) {
			source = replaceMediaLinks(source, makeMediaLink(inPoint));
		}
		return source;
	}

	/**
	 * Loop through the track outputting the relevant track points
	 * @param inWriter writer object for output
	 * @param inTrack track object
	 * @param inOnlyCopies true to only export if source can be copied
	 * @param inPointSources list of point sources
	 * @param inPointTag tag to match for each point
	 * @param inStartTag start tag to output
	 * @param inSegmentTag tag to output between segments (or null)
	 * @param inEndTag end tag to output
	 */
	private int writeTrackPoints(OutputStreamWriter inWriter, Track inTrack,
		boolean inOnlyCopies, List<String> inPointSources, String inPointTag,
		String inStartTag, String inSegmentTag, String inEndTag)
	throws IOException
	{
		// Note: Too many input parameters to this method but avoids duplication
		// of output functionality for writing track points and route points
		int numPoints = inTrack.getNumPoints();
		int numSaved = 0;
		final boolean exportTrackPoints = _settings.getExportTrackPoints();
		final boolean exportPhotos = _settings.getExportPhotoPoints();
		final boolean exportAudios = _settings.getExportAudioPoints();
		final boolean exportTimestamps = _settings.getExportTimestamps();
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
				// get the source from the point (if any)
				String pointSource = inPointSources.get(i);
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
					if (numSaved == 0) {
						inWriter.write(inStartTag);
					}
					if (pointSource != null)
					{
						// If timestamps checkbox is off, strip the time
						if (!exportTimestamps) {
							pointSource = stripTime(pointSource);
						}
						inWriter.write(pointSource);
						inWriter.write('\n');
					}
					else if (!inOnlyCopies) {
						exportTrackpoint(point, inWriter);
					}
					numSaved++;
				}
			}
		}
		if (numSaved > 0) {
			inWriter.write(inEndTag);
		}
		return numSaved;
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
		if (inSource == null) {
			return null;
		}
		// Look for start and end tags within source
		final int startPos = inSource.indexOf(inStartTag);
		final int endPos = inSource.indexOf(inEndTag, startPos+inStartTag.length());
		if (startPos > 0 && endPos > 0)
		{
			String origValue = inSource.substring(startPos + inStartTag.length(), endPos);
			if (origValue.equals(inValue)) {
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
		if (inValue == null || inValue.equals("")) {
			return inSource;
		}
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
		if (inSource == null) {
			return null;
		}
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
			if (origValue.equals(inValue)) {
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
		if (inValue == null || inValue.equals("")) {
			return inSource;
		}
		return null;
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
		inWriter.write("\t\t\t</trkpt>\n");
	}


	/**
	 * Strip the time from a GPX point source string
	 * @param inPointSource point source to copy
	 * @return point source with timestamp removed
	 */
	private static String stripTime(String inPointSource) {
		return inPointSource.replaceAll("[ \t]*<time>.*?</time>", "");
	}

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
		{
			// file link
			return "<link href=\"" + inMedia.getFile().getAbsolutePath() + "\"><text>" + inMedia.getName() + "</text></link>";
		}
		if (inMedia.getUrl() != null)
		{
			// url link
			return "<link href=\"" + inMedia.getUrl() + "\"><text>" + inMedia.getName() + "</text></link>";
		}
		// No link available, must have been loaded from zip file - no link possible
		return "";
	}
}
