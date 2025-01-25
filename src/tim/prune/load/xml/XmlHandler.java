package tim.prune.load.xml;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import tim.prune.data.ExtensionInfo;
import tim.prune.data.Field;
import tim.prune.data.FileType;

/**
 * Abstract superclass of xml handlers
 */
public abstract class XmlHandler extends DefaultHandler
{
	private FileType _fileType = null;
	private int _fileVersion = VERSION_UNKNOWN;
	private final ArrayList<Field> _fieldList = new ArrayList<>();
	private String[] _currentValues = null;

	private static final int VERSION_UNKNOWN = -1;


	/**
	 * Method for returning data loaded from file
	 * @return 2d String array containing data
	 */
	public abstract String[][] getDataArray();

	/** Add a field to the list */
	protected void addField(Field inField) {
		_fieldList.add(inField);
	}

	/** @return true if the given field is already present in the list */
	protected boolean hasField(Field inField)
	{
		for (Field field : _fieldList)
		{
			if (field.equals(inField)) {
				return true;
			}
		}
		return false;
	}

	/** @return index of the given field in the list */
	protected int getFieldIndex(Field inField)
	{
		int index = 0;
		for (Field field : _fieldList)
		{
			if (field.equals(inField)) {
				return index;
			}
			index++;
		}
		return -1;
	}

	/**
	 * @return field array describing fields of data
	 */
	public final Field[] getFieldArray() {
		return _fieldList.toArray(new Field[0]);
	}

	/** Reset the current values array for the next point */
	protected void resetCurrentValues() {
		_currentValues = new String[_fieldList.size()];
	}

	/**
	 * Add a field value to the current point
	 * @param inField field
	 * @param inValue value
	 */
	protected void addCurrentValue(Field inField, String inValue)
	{
		if (inValue == null || inValue.isEmpty()) {
			return;
		}
		int index = getFieldIndex(inField);
		if (_currentValues == null || _currentValues.length <= index)
		{
			String[] newValues = new String[_fieldList.size()];
			if (_currentValues != null) {
				System.arraycopy(_currentValues, 0, newValues, 0, _currentValues.length);
			}
			_currentValues = newValues;
		}
		_currentValues[index] = inValue;
	}

	/** @return the value array for the current point */
	protected String[] getCurrentValues() {
		return _currentValues;
	}

	/**
	 * Can be overridden (eg by gpx handler) to provide an array of links to media
	 * @return array of Strings if any, or null
	 */
	public String[] getLinkArray() {
		return null;
	}

	/** Set the file type */
	protected void setFileType(FileType inType) {
		_fileType = inType;
	}

	/** @return the identified file type */
	public FileType getFileType() {
		return _fileType;
	}

	public void setFileVersion(String inVersion)
	{
		if (inVersion != null && inVersion.length() == 3
			&& inVersion.charAt(1) == '.')
		{
			try {
				_fileVersion = 10 * Integer.parseInt(inVersion.substring(0, 1))
					+ Integer.parseInt(inVersion.substring(2, 3));
			}
			catch (NumberFormatException ignored) {}
		}
	}

	/** @return version number as string (like "1.0"), or "" if unknown */
	public String getFileVersion()
	{
		if (_fileVersion == VERSION_UNKNOWN) {
			return "";
		}
		return (_fileVersion / 10) + "." + (_fileVersion % 10);
	}

	/**
	 * @return the title of the file, or null
	 */
	public abstract String getFileTitle();

	/**
	 * Can be overridden (eg by gpx handler) to provide the description of the file
	 * @return file description, or null
	 */
	public String getFileDescription() {
		return null;
	}

	/**
	 * @return the information about the Xml extensions, or null
	 */
	public abstract ExtensionInfo getExtensionInfo();

	/** @return value of named attribute, or null if not found */
	protected static String getAttribute(Attributes inAttributes, String inName)
	{
		final String name = inName.toLowerCase();
		final int numAttributes = inAttributes.getLength();
		for (int i=0; i<numAttributes; i++)
		{
			String att = inAttributes.getQName(i).toLowerCase();
			if (att.equals(name)) {
				return inAttributes.getValue(i);
			}
		}
		return null;
	}
}
