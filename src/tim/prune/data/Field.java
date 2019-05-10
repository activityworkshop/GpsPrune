package tim.prune.data;

import tim.prune.I18nManager;

/**
 * Class to represent a field of a data point
 */
public class Field
{
	private String _labelKey = null;
	private String _customLabel = null;
	private boolean _builtin = false;

	public static final Field LATITUDE = new Field("fieldname.latitude", true);
	public static final Field LONGITUDE = new Field("fieldname.longitude", true);
	public static final Field ALTITUDE = new Field("fieldname.altitude", true);
	public static final Field TIMESTAMP = new Field("fieldname.timestamp", true);
	public static final Field WAYPT_NAME = new Field("fieldname.waypointname", true);
	public static final Field WAYPT_TYPE = new Field("fieldname.waypointtype", true);
	public static final Field DESCRIPTION = new Field("fieldname.description", true);
	public static final Field NEW_SEGMENT = new Field("fieldname.newsegment", true);

	public static final Field SPEED          = new Field("fieldname.speed", true);
	public static final Field VERTICAL_SPEED = new Field("fieldname.verticalspeed", true);
	public static final Field MEDIA_FILENAME = new Field("fieldname.mediafilename", true);

	// TODO: Ability to load media (from text) and save (to text)

	/** List of all the available fields */
	private static final Field[] ALL_AVAILABLE_FIELDS = {
		LATITUDE, LONGITUDE, ALTITUDE, TIMESTAMP, WAYPT_NAME, WAYPT_TYPE, DESCRIPTION, NEW_SEGMENT,
		SPEED, VERTICAL_SPEED,
		new Field(I18nManager.getText("fieldname.custom"))
	};

	/**
	 * Private constructor
	 * @param inLabelKey Key for label texts
	 * @param inBuiltin true for built-in types, false for custom
	 */
	private Field(String inLabelKey, boolean inBuiltin)
	{
		if (inBuiltin) {
			_labelKey = inLabelKey;
			_customLabel = null;
		}
		else {
			_labelKey = null;
			_customLabel = inLabelKey;
		}
		_builtin = inBuiltin;
	}


	/**
	 * Public constructor for custom fields
	 * @param inLabel label to use for display
	 */
	public Field(String inLabel)
	{
		this(inLabel, false);
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
		for (int i=0; i<ALL_AVAILABLE_FIELDS.length; i++)
		{
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
