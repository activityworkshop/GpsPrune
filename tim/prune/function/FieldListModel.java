package tim.prune.function;

import java.util.ArrayList;
import javax.swing.AbstractListModel;
import tim.prune.data.Field;

/**
 * Class to act as a list model for the delete field values function
 */
public class FieldListModel extends AbstractListModel<String>
{
	/** ArrayList containing fields */
	private ArrayList<Field> _fields = new ArrayList<Field>();


	/**
	 * Add a field to the list
	 * @param inField field object to add
	 */
	public void addField(Field inField)
	{
		if (inField != null) {_fields.add(inField);}
	}

	/**
	 * @return number of elements in list
	 */
	public int getSize()
	{
		return _fields.size();
	}

	/**
	 * @param inRow row number
	 * @return String for specified row
	 */
	public String getElementAt(int inRow)
	{
		if (inRow < 0 || inRow >= getSize()) {return null;}
		return _fields.get(inRow).getName();
	}

	/**
	 * @param inRow row number
	 * @return specified Field object
	 */
	public Field getField(int inRow)
	{
		if (inRow < 0 || inRow >= getSize()) {return null;}
		return _fields.get(inRow);
	}
}
