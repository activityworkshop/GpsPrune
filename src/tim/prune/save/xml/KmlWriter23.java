package tim.prune.save.xml;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import tim.prune.config.ColourUtils;
import tim.prune.data.Coordinate;
import tim.prune.data.DataPoint;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;
import tim.prune.data.UnitSetLibrary;

/** KmlWriter for version 2.3 */
public class KmlWriter23 extends KmlWriter
{
	public KmlWriter23(TrackInfo inTrackInfo, KmlExportOptions inOptions, ProgressUpdater inUpdater) {
		super(inTrackInfo, inOptions, inUpdater);
	}

	/** Xml 2.3 header without extension */
	protected void writeXmlHeader(OutputStreamWriter inWriter) throws IOException
	{
		// Header must still say 2.2 even though it's 2.3
		inWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<kml xmlns=\"http://earth.google.com/kml/2.2\">\n");
	}

	// TODO: Write timestamps too in the KML 2.3 version (without gx)
	protected int writeTrack(OutputStreamWriter inWriter, boolean inAbsoluteAltitudes,
			int inSelStart, int inSelEnd) throws IOException
	{
		int numSaved = 0;
		// Set up strings for start and end of track segment
		String trackStart = "\t<Placemark>\n\t\t<name>track</name>\n\t\t<Style>\n\t\t\t<LineStyle>\n"
			+ "\t\t\t\t<color>cc" + reverseRGB(ColourUtils.makeHexCode(_exportOptions.getTrackColour())) + "</color>\n"
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

		boolean justSelection = _exportOptions.getExportJustSelection();

		// Start segment
		inWriter.write(trackStart);
		// Loop over track points
		final Track track = _trackInfo.getTrack();
		boolean firstTrackpoint = true;
		final int numPoints = track.getNumPoints();
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = track.getPoint(i);
			boolean writeCurrentPoint = !justSelection || (i>=inSelStart && i<=inSelEnd);
			// ignore points with photos or audios here, just write the track points
			if (!point.isWaypoint() && writeCurrentPoint && !point.hasMedia())
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
	 * Export the specified trackpoint into the file
	 * @param inPoint trackpoint to export
	 * @param inWriter writer object
	 */
	private void exportTrackpoint(DataPoint inPoint, Writer inWriter) throws IOException
	{
		inWriter.write(inPoint.getLongitude().output(Coordinate.Format.DECIMAL_FORCE_POINT));
		inWriter.write(',');
		inWriter.write(inPoint.getLatitude().output(Coordinate.Format.DECIMAL_FORCE_POINT));
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
}
