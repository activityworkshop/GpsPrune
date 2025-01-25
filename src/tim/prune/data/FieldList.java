package tim.prune.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class to hold an ordered list of fields
 * to match the value list in a data point
 */
public class FieldList
{
	/** List of Field objects */
	private final ArrayList<Field> _fields;


	/**
	 * Constructor for an empty field list
	 */
	public FieldList() {
		_fields = new ArrayList<>();
	}

	/**
	 * Constructor giving multiple Field objects
	 * @param inFields Field objects
	 */
	public FieldList(Field ... inFields)
	{
		this();
		Collections.addAll(_fields, inFields);
	}

	/**
	 * Get the index of the given field
	 * @param inField field to look for
	 * @return index number of the field starting at zero
	 */
	public int getFieldIndex(Field inField)
	{
		if (inField == null) return -1;
		int index = 0;
		for (Field field : _fields)
		{
			if (field != null && field.equals(inField)) {
				return index;
			}
			index++;
		}
		return -1;
	}


	/**
	 * Check whether the FieldList contains the given Field object
	 * @param inField Field to check
	 * @return true if the FieldList contains the given field
	 */
	public boolean contains(Field inField) {
		return getFieldIndex(inField) >= 0;
	}


	/**
	 * @return number of fields in list
	 */
	public int getNumFields() {
		return _fields.size();
	}


	/**
	 * Get the specified Field object
	 * @param inIndex index to retrieve
	 * @return Field object
	 */
	public Field getField(int inIndex)
	{
		try {
			return _fields.get(inIndex);
		}
		catch (IndexOutOfBoundsException e) {
			return null;
		}
	}


	/**
	 * Merge this list with a second list, giving a superset
	 * @param inOtherList other FieldList object to merge
	 * @return Merged FieldList object
	 */
	public FieldList merge(FieldList inOtherList)
	{
		FieldList superset = new FieldList();
		superset._fields.addAll(_fields);
		if (inOtherList != null)
		{
			for (Field field : inOtherList._fields)
			{
				if (!contains(field)) {
					superset._fields.add(field);
				}
			}
		}
		return superset;
	}


	/**
	 * Extend the field list to include the specified field
	 * @param inField Field to add
	 * @return new index of added Field
	 */
	public int addField(Field inField)
	{
		// See if field is already in list
		int currIndex = getFieldIndex(inField);
		if (currIndex >= 0) {
			return currIndex;
		}
		// Add new field and return index
		_fields.add(inField);
		return getNumFields() - 1;
	}

	/**
	 * Extend the field list to include the specified fields
	 * @param inFields Field objects to add
	 */
	public void addFields(Field ... inFields)
	{
		for (Field field : inFields) {
			addField(field);
		}
	}

	/**
	 * Convert to String for debug
	 */
	public String toString()
	{
		StringBuilder result = new StringBuilder();
		result.append('(');
		for (Field field : _fields) {
			result.append(field.getName()).append(',');
		}
		result.append(')');
		return result.toString();
	}

	/** @return list of fields specific to the given file type */
	public List<Field> getFields(FileType inFileType)
	{
		ArrayList<Field> fields = new ArrayList<Field>();
		for (Field field : _fields)
		{
			if (field.isSpecificToFileType(inFileType)) {
				fields.add(field);
			}
		}
		return fields;
	}
}
