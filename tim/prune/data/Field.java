package tim.prune.data;

import tim.prune.I18nManager;

/**
 * Class to represent a field of a data point
 * including its type
 */
public class Field
{
	private String _labelKey = null;
	private String _customLabel = null;
	private FieldType _type = null;
	private boolean _builtin = false;

	public static final Field LATITUDE = new Field("fieldname.latitude", FieldType.COORD);
	public static final Field LONGITUDE = new Field("fieldname.longitude", FieldType.COORD);
	public static final Field ALTITUDE = new Field("fieldname.altitude", FieldType.INT);
	public static final Field TIMESTAMP = new Field("fieldname.timestamp", FieldType.TIME);
	public static final Field WAYPT_NAME = new Field("fieldname.waypointname", FieldType.NONE);
	public static final Field WAYPT_TYPE = new Field("fieldname.waypointtype", FieldType.NONE);
	public static final Field NEW_SEGMENT = new Field("fieldname.newsegment", FieldType.BOOL);

	// TODO: Field for photo filename, ability to load (from text) and save (to text)

	private static final Field[] ALL_AVAILABLE_FIELDS = {
		LATITUDE, LONGITUDE, ALTITUDE, TIMESTAMP, WAYPT_NAME, WAYPT_TYPE, NEW_SEGMENT,
		new Field("fieldname.custom", FieldType.NONE)
	};

	/**
	 * Private constructor
	 * @param inLabelKey Key for label texts
	 * @param inType type of field
	 */
	private Field(String inLabelKey, FieldType inType)
	{
		_labelKey = inLabelKey;
		_customLabel = null;
		_type = inType;
		_builtin = true;
	}


	/**
	 * Public constructor for custom fields
	 * @param inLabel label to use for display
	 */
	public Field(String inLabel)
	{
		_labelKey = null;
		_customLabel = inLabel;
		_type = FieldType.NONE;
	}

	/**
	 * @return the name of the field
	 */
	public String getName()
	{
		if (_labelKey != null)
			return I18nManager.getText(_labelKey);
		return _customLabel;
	}

	/**
	 * Change the name of the (non built-in) field
	 * @param inName new name
	 */
	public void setName(String inName)
	{
		if (!isBuiltIn()) _customLabel = inName;
	}

	/**
	 * @return true if this is a built-in field
	 */
	public boolean isBuiltIn()
	{
		return _builtin;
	}

	/**
	 * @return field type
	 */
	public FieldType getType()
	{
		return _type;
	}

	/**
	 * Checks if the two fields are equal
	 * @param inOther other Field object
	 * @return true if Fields identical
	 */
	public boolean equals(Field inOther)
	{
		return (isBuiltIn() == inOther.isBuiltIn() && getName().equals(inOther.getName()));
	}

	/**
	 * Get the field for the given field name
	 * @param inFieldName name of field to look for
	 * @return Field if found, or null otherwise
	 */
	public static Field getField(String inFieldName)
	{
		for (int i=0; i<ALL_AVAILABLE_FIELDS.length; i++) {
			Field field = ALL_AVAILABLE_FIELDS[i];
			if (field.getName().equals(inFieldName)) {
				return field;
			}
		}
		// not found
		return null;
	}

	/**
	 * @return array of field names
	 */
	public static String[] getFieldNames()
	{
		String[] names = new String[ALL_AVAILABLE_FIELDS.length];
		for (int i=0; i<ALL_AVAILABLE_FIELDS.length; i++) {
			names[i] = ALL_AVAILABLE_FIELDS[i].getName();
		}
		return names;
	}
}
