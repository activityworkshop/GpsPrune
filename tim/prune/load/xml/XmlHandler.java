package tim.prune.load.xml;

import org.xml.sax.helpers.DefaultHandler;

import tim.prune.data.Field;

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
}
