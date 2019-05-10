package tim.prune.function.edit;

import javax.swing.table.AbstractTableModel;

import tim.prune.I18nManager;

/**
 * Class to hold table model information for edit dialog
 */
public class EditFieldsTableModel extends AbstractTableModel
{
	private String[] _fieldNames = null;
	private String[] _originalValues = null;
	private String[] _fieldValues = null;
	private boolean[] _valueChanged = null;


	/**
	 * Constructor giving list size
	 * @param inSize number of fields
	 */
	public EditFieldsTableModel(int inSize)
	{
		_fieldNames     = new String[inSize];
		_originalValues = new String[inSize];
		_fieldValues    = new String[inSize];
		_valueChanged   = new boolean[inSize];
	}


	/**
	 * Set the given data in the array
	 * @param inName field name
	 * @param inValue field value
	 * @param inIndex index to place in array
	 */
	public void addFieldInfo(String inName, String inValue, int inIndex)
	{
		_fieldNames[inIndex] = inName;
		_originalValues[inIndex] = inValue;
		_fieldValues[inIndex] = inValue;
		_valueChanged[inIndex] = false;
	}


	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount()
	{
		return 2;
	}


	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount()
	{
		return _fieldNames.length;
	}


	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int inRowIndex, int inColumnIndex)
	{
		if (inColumnIndex == 0)
		{
			return _fieldNames[inRowIndex];
		}
		return _fieldValues[inRowIndex];
	}


	/**
	 * @return true if cell is editable
	 */
	public boolean isCellEditable(int inRowIndex, int inColumnIndex)
	{
		// no
		return false;
	}


	/**
	 * Set the given cell value
	 * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
	 */
	public void setValueAt(Object inValue, int inRowIndex, int inColumnIndex)
	{
		// ignore edits
	}


	/**
	 * @return Class of cell data
	 */
	public Class<?> getColumnClass(int inColumnIndex)
	{
		if (inColumnIndex <= 1) return String.class;
		return Boolean.class;
	}


	/**
	 * Get the name of the column
	 */
	public String getColumnName(int inColNum)
	{
		if (inColNum == 0) return I18nManager.getText("dialog.pointedit.table.field");
		return I18nManager.getText("dialog.pointedit.table.value");
	}


	/**
	 * Update the value of the given row
	 * @param inRowNum number of row, starting at 0
	 * @param inValue new value
	 */
	public void updateValue(int inRowNum, String inValue)
	{
		String origValue = _originalValues[inRowNum];
		String currValue = _fieldValues[inRowNum];
		// Update model if changed from original value
		_valueChanged[inRowNum] = areStringsDifferent(origValue, inValue);
		// Update model if changed from current value
		if (areStringsDifferent(currValue, inValue))
		{
			_fieldValues[inRowNum] = inValue;
			fireTableRowsUpdated(inRowNum, inRowNum);
		}
	}

	/**
	 * Compare two strings to see if they're equal or not (nulls treated the same as empty strings)
	 * @param inString1 first string
	 * @param inString2 second string
	 * @return true if the strings are different
	 */
	private static boolean areStringsDifferent(String inString1, String inString2)
	{
		// if both empty then same
		if ((inString1 == null || inString1.equals("")) && (inString2 == null || inString2.equals("")))
		{
			return false;
		}
		return (inString1 == null || inString2 == null || !inString1.equals(inString2));
	}

	/**
	 * Get the value at the given index
	 * @param inIndex index of field, starting at 0
	 * @return string value
	 */
	public String getValue(int inIndex)
	{
		return _fieldValues[inIndex];
	}

	/**
	 * Get the changed flag at the given index
	 * @param inIndex index of field, starting at 0
	 * @return true if field changed
	 */
	public boolean getChanged(int inIndex)
	{
		return _valueChanged[inIndex];
	}
}
