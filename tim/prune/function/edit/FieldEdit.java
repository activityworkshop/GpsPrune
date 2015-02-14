package tim.prune.function.edit;

import tim.prune.data.Field;

/**
 * Class to hold a single field edit including Field and new value
 */
public class FieldEdit
{
	private Field _field = null;
	private String _value = null;

	/**
	 * Constructor
	 * @param inField field to edit
	 * @param inValue new value
	 */
	public FieldEdit(Field inField, String inValue)
	{
		_field = inField;
		_value = inValue;
	}


	/**
	 * @return the field
	 */
	public Field getField()
	{
		return _field;
	}

	/**
	 * @return the value
	 */
	public String getValue()
	{
		return _value;
	}
}
