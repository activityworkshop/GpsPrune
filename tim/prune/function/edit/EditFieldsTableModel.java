package tim.prune.function.edit;

import javax.swing.table.AbstractTableModel;

import tim.prune.I18nManager;

/**
 * Class to hold table model information for edit dialog
 */
public class EditFieldsTableModel extends AbstractTableModel
{
	private String[] _fieldNames = null;
	private String[] _fieldValues = null;
	private boolean[] _valueChanged = null;


	/**
	 * Constructor giving list size
	 * @param inSize number of fields
	 */
	public EditFieldsTableModel(int inSize)
	{
		_fieldNames = new String[inSize];
		_fieldValues = new String[inSize];
		_valueChanged = new boolean[inSize];
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
		_fieldValues[inIndex] = inValue;
		_valueChanged[inIndex] = false;
	}


	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount()
	{
		return 3;
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
		else if (inColumnIndex == 1)
		{
			return _fieldValues[inRowIndex];
		}
		return Boolean.valueOf(_valueChanged[inRowIndex]);
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
		else if (inColNum == 1) return I18nManager.getText("dialog.pointedit.table.value");
		return I18nManager.getText("dialog.pointedit.table.changed");
	}


	/**
	 * Update the value of the given row
	 * @param inRowNum number of row, starting at 0
	 * @param inValue new value
	 * @return true if data updated
	 */
	public boolean updateValue(int inRowNum, String inValue)
	{
		String currValue = _fieldValues[inRowNum];
		// ignore empty-to-empty changes
		if ((currValue == null || currValue.equals("")) && (inValue == null || inValue.equals("")))
		{
			return false;
		}
		// ignore changes when strings equal
		if (currValue == null || inValue == null || !currValue.equals(inValue))
		{
			// really changed
			_fieldValues[inRowNum] = inValue;
			_valueChanged[inRowNum] = true;
			fireTableRowsUpdated(inRowNum, inRowNum);
			return true;
		}
		return false;
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
