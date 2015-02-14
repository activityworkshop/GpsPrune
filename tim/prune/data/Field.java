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

	public static final Field[] ALL_AVAILABLE_FIELDS = {
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
}
