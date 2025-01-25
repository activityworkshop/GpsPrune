package tim.prune.save.xml;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.FieldGpx;
import tim.prune.data.FieldXml;
import tim.prune.data.FileInfo;
import tim.prune.data.FileType;
import tim.prune.data.MediaObject;
import tim.prune.gui.ProgressDialog;
import tim.prune.save.SettingsForExport;

/** Responsible for Gpx1.0-specific parts of Gpx writing */
public class GpxWriter10 extends GpxWriter
{
	public GpxWriter10(ProgressDialog inProgress, SettingsForExport inSettings) {
		super(inProgress, inSettings);
	}

	/** @return the default Gpx header without extensions */
	protected String getDefaultGpxHeader()
	{
		return "<gpx version=\"1.0\" creator=\"" + GPX_CREATOR
				+ "\"\n xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
				+ " xmlns=\"http://www.topografix.com/GPX/1/0\""
				+ " xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd\">";
	}

	/** @return the default schema location string without extensions */
	private String getDefaultSchemaLocation() {
		return "http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd";
	}

	/** @return the default Gpx header with extensions */
	protected String getGpxHeader(FileInfo inInfo)
	{
		HeaderCombiner combiner = new HeaderCombiner(GpxVersion.GPX_1_0);
		for (int i=0; i<inInfo.getNumFiles(); i++) {
			combiner.addSourceInfo(inInfo.getSource(i));
		}

		return "<gpx version=\"1.0\" creator=\"" + GPX_CREATOR
				+ "\"\n xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
				+ " xmlns=\"http://www.topografix.com/GPX/1/0\"\n"
				+ " xsi:schemaLocation=\"" + combiner.getAllLocations(getDefaultSchemaLocation()) + "\"\n"
				+ combiner.getNamespaces() + ">";
	}

	/** Write the name and description in 1.0 format */
	protected void writeMetadata(OutputStreamWriter inWriter, String inName, String inDesc) throws IOException
	{
		// GPX 1.0 doesn't have a metadata tag
		writeNameAndDescription(inWriter, inName, inDesc, "\t");
	}

	/** No, version 1.0 does not support link tags */
	protected boolean versionSupportsPointLinks() {
		return false;
	}

	protected boolean versionSupportsTag(FieldGpx inField) {
		return true;
	}

	/**
	 * Make the media link for a single media item
	 * @param inMedia media item, either photo or audio
	 * @return link for this media
	 */
	protected String makeMediaLink(MediaObject inMedia)
	{
		if (inMedia.getFile() != null)
		{
			// file link
			return "<url>" + inMedia.getFile().getAbsolutePath() + "</url>";
		}
		if (inMedia.getUrl() != null)
		{
			// url link
			return "<url>" + inMedia.getUrl() + "</url>";
		}
		// No link available, must have been loaded from zip file - no link possible
		return "";
	}

	/** Export the extension tags from the given waypoint to the writer */
	protected void exportWaypointExtensions(DataPoint inPoint, Writer inWriter) throws IOException
	{
		for (Field field : inPoint.getFieldList().getFields(FileType.GPX))
		{
			if (field instanceof FieldXml)
			{
				FieldXml xmlField = (FieldXml) field;
				String value = inPoint.getFieldValue(xmlField);
				if (!isEmpty(value))
				{
					// Don't include tag category for Gpx 1.0, just use the tag
					inWriter.write("\t\t\t\t");
					inWriter.write(xmlField.getTag(value));
					inWriter.write("\n");
				}
			}
		}
	}

	/** Export the extension tags from the given trackpoint to the writer */
	protected void exportTrackpointExtensions(DataPoint inPoint, Writer inWriter) throws IOException
	{
		// TODO: Maybe just the same as above?
		exportWaypointExtensions(inPoint, inWriter);
	}
}
