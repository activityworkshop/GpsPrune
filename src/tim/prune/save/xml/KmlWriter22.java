package tim.prune.save.xml;

import java.io.IOException;
import java.io.OutputStreamWriter;

import tim.prune.config.ColourUtils;
import tim.prune.data.Coordinate;
import tim.prune.data.DataPoint;
import tim.prune.data.Timestamp;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;
import tim.prune.data.UnitSetLibrary;

/** KmlWriter for version 2.2 */
public class KmlWriter22 extends KmlWriter
{
	public KmlWriter22(TrackInfo inTrackInfo, KmlExportOptions inOptions, ProgressUpdater inUpdater) {
		super(inTrackInfo, inOptions, inUpdater);
	}

	/** Xml 2.2 header with gx extension */
	protected void writeXmlHeader(OutputStreamWriter inWriter) throws IOException
	{
		inWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<kml xmlns=\"http://earth.google.com/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\">\n");
	}

	/**
	 * Write out the track using Google's KML Extensions such as gx:Track
	 * @param inWriter writer object to write to
	 * @param inAbsoluteAltitudes true to use absolute altitudes, false to clamp to ground
	 * @param inSelStart start index of selection, or -1 if whole track
	 * @param inSelEnd end index of selection, or -1 if whole track
	 * @return number of track points written
	 */
	protected int writeTrack(OutputStreamWriter inWriter, boolean inAbsoluteAltitudes,
		int inSelStart, int inSelEnd)
	throws IOException
	{
		int numSaved = 0;
		// Set up strings for start and end of track segment
		String trackStart = "\t<Placemark>\n\t\t<name>track</name>\n\t\t<Style>\n\t\t\t<LineStyle>\n"
			+ "\t\t\t\t<color>cc" + reverseRGB(ColourUtils.makeHexCode(_exportOptions.getTrackColour())) + "</color>\n"
			+ "\t\t\t\t<width>4</width>\n\t\t\t</LineStyle>\n"
			+ "\t\t</Style>\n\t\t<gx:Track>\n";
		if (inAbsoluteAltitudes) {
			trackStart += "\t\t\t<extrude>1</extrude>\n\t\t\t<altitudeMode>absolute</altitudeMode>\n";
		}
		else {
			trackStart += "\t\t\t<altitudeMode>clampToGround</altitudeMode>\n";
		}
		String trackEnd = "\n\t\t</gx:Track>\n\t</Placemark>\n";

		boolean justSelection = _exportOptions.getExportJustSelection();

		// Start segment
		inWriter.write(trackStart);
		StringBuilder whenList = new StringBuilder();
		StringBuilder coordList = new StringBuilder();

		// Loop over track points
		final Track track = _trackInfo.getTrack();
		boolean firstTrackpoint = true;
		final int numPoints = track.getNumPoints();
		for (int i=0; i<numPoints; i++)
		{
			DataPoint point = track.getPoint(i);
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
					coordList.append(point.getLongitude().output(Coordinate.Format.DECIMAL_FORCE_POINT)).append(' ');
					coordList.append(point.getLatitude().output(Coordinate.Format.DECIMAL_FORCE_POINT)).append(' ');
					if (point.hasAltitude()) {
						coordList.append(point.getAltitude().getStringValue(UnitSetLibrary.UNITS_METRES));
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
}
