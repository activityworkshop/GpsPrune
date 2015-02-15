package tim.prune.data;

/**
 * Class to hold an ordered list of fields
 * to match the value list in a data point
 */
public class FieldList
{
	/** Array of Field objects making the list */
	private Field[] _fieldArray;


	/**
	 * Constructor for an empty field list
	 */
	public FieldList()
	{
		_fieldArray = new Field[0];
	}

	/**
	 * Constructor for a given number of empty fields
	 * @param inNumFields
	 */
	public FieldList(int inNumFields)
	{
		if (inNumFields < 0) inNumFields = 0;
		_fieldArray = new Field[inNumFields];
	}

	/**
	 * Constructor giving array of Field objects
	 * @param inFieldArray array of Field objects
	 */
	public FieldList(Field[] inFieldArray)
	{
		if (inFieldArray == null || inFieldArray.length == 0)
		{
			_fieldArray = new Field[0];
		}
		else
		{
			_fieldArray = new Field[inFieldArray.length];
			System.arraycopy(inFieldArray, 0, _fieldArray, 0, inFieldArray.length);
		}
	}

	/**
	 * Get the index of the given field
	 * @param inField field to look for
	 * @return index number of the field starting at zero
	 */
	public int getFieldIndex(Field inField)
	{
		if (inField == null) return -1;
		for (int f=0; f<_fieldArray.length; f++)
		{
			if (_fieldArray[f] != null && _fieldArray[f].equals(inField))
				return f;
		}
		return -1;
	}


	/**
	 * Check whether the FieldList contains the given Field object
	 * @param inField Field to check
	 * @return true if the FieldList contains the given field
	 */
	public boolean contains(Field inField)
	{
		return (getFieldIndex(inField) >= 0);
	}


	/**
	 * @return number of fields in list
	 */
	public int getNumFields()
	{
		if (_fieldArray == null) return 0;
		return _fieldArray.length;
	}


	/**
	 * Get the specified Field object
	 * @param inIndex index to retrieve
	 * @return Field object
	 */
	public Field getField(int inIndex)
	{
		if (_fieldArray == null || inIndex < 0 || inIndex >= _fieldArray.length)
		{
			return null;
		}
		return _fieldArray[inIndex];
	}


	/**
	 * Merge this list with a second list, giving a superset
	 * @param inOtherList other FieldList object to merge
	 * @return Merged FieldList object
	 */
	public FieldList merge(FieldList inOtherList)
	{
		// count number of fields
		int totalFields = _fieldArray.length;
		for (int f=0; f<inOtherList._fieldArray.length; f++)
		{
			if (inOtherList._fieldArray[f] != null && !contains(inOtherList._fieldArray[f]))
			{
				totalFields++;
			}
		}
		FieldList list = new FieldList(totalFields);
		// copy these fields into array
		System.arraycopy(_fieldArray, 0, list._fieldArray, 0, _fieldArray.length);
		// copy additional fields from other array if any
		if (totalFields > _fieldArray.length)
		{
			int fieldCounter = _fieldArray.length;
			for (int f=0; f<inOtherList._fieldArray.length; f++)
			{
				if (inOtherList._fieldArray[f] != null && !contains(inOtherList._fieldArray[f]))
				{
					list._fieldArray[fieldCounter] = inOtherList._fieldArray[f];
					fieldCounter++;
				}
			}
		}
		// return the merged list
		return list;
	}


	/**
	 * Extend the field list to include the specified field
	 * @param inField Field to add
	 * @return new index of added Field
	 */
	public int extendList(Field inField)
	{
		// See if field is already in list
		int currIndex = getFieldIndex(inField);
		if (currIndex >= 0) return currIndex;
		// Need to extend - increase array size
		int oldNumFields = _fieldArray.length;
		Field[] fields = new Field[oldNumFields + 1];
		System.arraycopy(_fieldArray, 0, fields, 0, oldNumFields);
		_fieldArray = fields;
		// Add new field and return index
		_fieldArray[oldNumFields] = inField;
		return oldNumFields;
	}


	/**
	 * Convert to String for debug
	 */
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append('(');
		for (int i=0; i<_fieldArray.length; i++)
		{
			buffer.append(_fieldArray[i].getName()).append(',');
		}
		buffer.append(')');
		return buffer.toString();
	}
}
