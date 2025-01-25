package tim.prune.save.xml;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.FieldGpx;
import tim.prune.data.FieldXml;
import tim.prune.data.FileInfo;
import tim.prune.data.FileType;
import tim.prune.data.MediaObject;
import tim.prune.gui.ProgressDialog;
import tim.prune.save.SettingsForExport;

public class GpxWriter11 extends GpxWriter
{
	public GpxWriter11(ProgressDialog inProgress, SettingsForExport inSettings) {
		super(inProgress, inSettings);
	}

	/** @return the default Gpx header without extensions */
	protected String getDefaultGpxHeader() {
		return "<gpx version=\"1.1\" creator=\"" + GPX_CREATOR
				+ "\"\n xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
				+ " xmlns=\"http://www.topografix.com/GPX/1/1\""
				+ " xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">";
	}

	/** @return the default schema location string without extensions */
	private String getDefaultSchemaLocation() {
		return "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd";
	}

	/** @return the default Gpx header with extensions */
	protected String getGpxHeader(FileInfo inInfo)
	{
		HeaderCombiner combiner = new HeaderCombiner(GpxVersion.GPX_1_1);
		for (int i=0; i<inInfo.getNumFiles(); i++) {
			combiner.addSourceInfo(inInfo.getSource(i));
		}

		return "<gpx version=\"1.1\" creator=\"" + GPX_CREATOR
				+ "\"\n xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
				+ " xmlns=\"http://www.topografix.com/GPX/1/1\"\n"
				+ " xsi:schemaLocation=\"" + combiner.getAllLocations(getDefaultSchemaLocation()) + "\"\n"
				+ combiner.getNamespaces() + ">";
	}

	/** Write the name and description in 1.1 format */
	protected void writeMetadata(OutputStreamWriter inWriter, String inName, String inDesc) throws IOException
	{
		// GPX 1.1 has the name and description inside a metadata tag
		inWriter.write("\t<metadata>\n");
		writeNameAndDescription(inWriter, inName, inDesc, "\t\t");
		inWriter.write("\t</metadata>\n");
	}

	/** Yes, version 1.1 does support link tags */
	protected boolean versionSupportsPointLinks() {
		return true;
	}

	/** Speed and course tags are only supported by Gpx 1.0 */
	protected boolean versionSupportsTag(FieldGpx inField)
	{
		if (inField.getName().equals("speed")
			|| inField.getName().equals("course"))
		{
			return false;
		}
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

	/** Export the extension tags from the given waypoint to the writer */
	protected void exportWaypointExtensions(DataPoint inPoint, Writer inWriter) throws IOException
	{
		List<Field> extensionFields = inPoint.getFieldList().getFields(FileType.GPX);
		if (extensionFields == null || extensionFields.isEmpty()) {
			return;
		}
		// Build a tree of all the used extension tags
		ExtensionTree extensionTree = new ExtensionTree();
		for (Field field : extensionFields)
		{
			String value = inPoint.getFieldValue(field);
			if (isEmpty(value)) {
				continue;
			}
			if (field instanceof FieldXml)
			{
				FieldXml xmlField = (FieldXml) field;
				extensionTree.addTag(xmlField.getCategories(), xmlField.getTag(value));
			}
		}
		// Loop over the output of the tree and write to the output
		boolean needToOpen = true;
		boolean needToClose = false;
		for (String line : extensionTree.getAllTags(5))
		{
			if (needToOpen)
			{
				inWriter.write("\t\t\t\t<extensions>\n");
				needToOpen = false;
				needToClose = true;
			}
			inWriter.write(line);
			inWriter.write('\n');
		}
		if (needToClose) {
			inWriter.write("\t\t\t\t</extensions>\n");
		}
	}

	/** Export the extension tags from the given trackpoint to the writer */
	protected void exportTrackpointExtensions(DataPoint inPoint, Writer inWriter) throws IOException
	{
		// TODO: Maybe just the same as above?
		exportWaypointExtensions(inPoint, inWriter);
	}
}
