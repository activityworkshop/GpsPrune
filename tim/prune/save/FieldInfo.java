package tim.prune.save;

import tim.prune.data.Field;

/**
 * Class to hold field information for save dialog
 */
public class FieldInfo
{
	private Field _field = null;
	private boolean _data = false;
	private boolean _selected = true;


	/**
	 * Constructor
	 * @param inField Field object
	 * @param inData true if Field contains data which can be saved
	 */
	public FieldInfo(Field inField, boolean inData)
	{
		_field = inField;
		_selected = _data = inData;
	}


	/**
	 * @return the field object
	 */
	public Field getField()
	{
		return _field;
	}


	/**
	 * @return true if field has data
	 */
	public boolean hasData()
	{
		return _data;
	}


	/**
	 * @return true if field is selected
	 */
	public boolean isSelected()
	{
		return _selected;
	}


	/**
	 * Set whether the field is selected or not
	 * @param inSelected true to select field
	 */
	public void setSelected(boolean inSelected)
	{
		_selected = inSelected;
	}


	/**
	 * @return String for debug
	 */
	public String toString()
	{
		return _field.getName() + (_data?"(data)":"(no data)") + ", " + (_selected?"(sel)":"(---)");
	}
}
