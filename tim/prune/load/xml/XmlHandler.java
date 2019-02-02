package tim.prune.load.xml;

import org.xml.sax.helpers.DefaultHandler;

import tim.prune.data.Field;
import tim.prune.load.TrackNameList;

/**
 * Abstract superclass of xml handlers
 */
public abstract class XmlHandler extends DefaultHandler
{
	/**
	 * Method for returning data loaded from file
	 * @return 2d String array containing data
	 */
	public abstract String[][] getDataArray();

	/**
	 * @return field array describing fields of data
	 */
	public abstract Field[] getFieldArray();

	/**
	 * Can be overridden (eg by gpx handler) to provide a track name list
	 * @return track name list object if any, or null
	 */
	public TrackNameList getTrackNameList() {
		return null;
	}

	/**
	 * Can be overridden (eg by gpx handler) to provide an array of links to media
	 * @return array of Strings if any, or null
	 */
	public String[] getLinkArray() {
		return null;
	}

	/**
	 * Can be overridden (eg by gpx handler) to provide the title of the file
	 * @return file title, or null
	 */
	public String getFileTitle() {
		return null;
	}
}
