package tim.prune.load;

import javax.swing.table.AbstractTableModel;

import tim.prune.I18nManager;
import tim.prune.data.Field;

/**
 * Class to hold the table model for the field selection table
 */
public class FieldSelectionTableModel extends AbstractTableModel
{

	private int _numRows = 0;
	private Field[] _fieldArray = null;
	private String _customText = null;

	/**
	 * Constructor
	 */
	public FieldSelectionTableModel()
	{
		// Cache the custom text for the table so it doesn't
		// have to be looked up so often
		_customText = I18nManager.getText("fieldname.custom");
	}


	/**
	 * @return the column count
	 */
	public int getColumnCount()
	{
		return 3;
	}


	/**
	 * @param inColNum column number
	 * @return name of the column
	 */
	public String getColumnName(int inColNum)
	{
		if (inColNum == 0) return I18nManager.getText("dialog.load.table.field");
		else if (inColNum == 1) return I18nManager.getText("dialog.load.table.datatype");
		return I18nManager.getText("dialog.load.table.description");
	}


	/**
	 * @return the row count
	 */
	public int getRowCount()
	{
		if (_fieldArray == null)
			return 2;
		return _numRows;
	}


	/**
	 * @param inRowIndex row index
	 * @param inColumnIndex column index
	 * @return the value of the specified cell
	 */
	public Object getValueAt(int inRowIndex, int inColumnIndex)
	{
		if (_fieldArray == null) return "";
		if (inColumnIndex == 0) return ("" + (inRowIndex+1));
		Field field = _fieldArray[inRowIndex];
		if (inColumnIndex == 1)
		{
			// Field name - take name from built-in fields
			if (field.isBuiltIn())
				return field.getName();
			// Otherwise take custom name
			return _customText;
		}
		// description column - builtin fields don't have one
		if (field.isBuiltIn()) return "";
		return field.getName();
	}


	/**
	 * Make sure only second and third columns are editable
	 * @param inRowIndex row index
	 * @param inColumnIndex column index
	 * @return true if cell editable
	 */
	public boolean isCellEditable(int inRowIndex, int inColumnIndex)
	{
		if (inColumnIndex <= 1)
			return (inColumnIndex == 1);
		// Column is 2 so only edit non-builtin field names
		Field field = _fieldArray[inRowIndex];
		return !field.isBuiltIn();
	}


	/**
	 * Update the data
	 * @param inData 2-dimensional Object array containing the data
	 */
	public void updateData(Field[] inData)
	{
		_fieldArray = inData;
		if (_fieldArray != null)
		{
			_numRows = _fieldArray.length;
		}
		fireTableStructureChanged();
	}


	/**
	 * React to edits to the table data
	 * @param inValue value to set
	 * @param inRowIndex row index
	 * @param inColumnIndex column index
	 */
	public void setValueAt(Object inValue, int inRowIndex, int inColumnIndex)
	{
		super.setValueAt(inValue, inRowIndex, inColumnIndex);
		if (inColumnIndex == 1)
		{
			Field field = _fieldArray[inRowIndex];
			if (!field.getName().equals(inValue.toString()))
			{
				manageFieldChange(inRowIndex, inValue.toString());
			}
		}
		else if (inColumnIndex == 2)
		{
			// change description if it's custom
			Field field = _fieldArray[inRowIndex];
			if (!field.isBuiltIn())
				field.setName(inValue.toString());
		}
	}


	/**
	 * Move the selected item up one place
	 * @param inIndex index of item to move
	 */
	public void moveUp(int inIndex)
	{
		if (inIndex > 0)
		{
			swapItems(inIndex-1, inIndex);
		}
	}


	/**
	 * Move the selected item down one place
	 * @param inIndex index of item to move
	 */
	public void moveDown(int inIndex)
	{
		if (inIndex > -1 && inIndex < (_numRows - 1))
		{
			swapItems(inIndex, inIndex+1);
		}
	}


	/**
	 * Swap the specified items in the array
	 * @param inIndex1 index of first item
	 * @param inIndex2 index of second item (higher than inIndex1)
	 */
	private void swapItems(int inIndex1, int inIndex2)
	{
		Field temp = _fieldArray[inIndex1];
		_fieldArray[inIndex1] = _fieldArray[inIndex2];
		_fieldArray[inIndex2] = temp;
		fireTableRowsUpdated(inIndex1, inIndex2);
	}


	/**
	 * React to a requested change to one of the fields
	 * @param inRow row number of change
	 * @param inValue new string value
	 */
	private void manageFieldChange(int inRow, String inValue)
	{
		// check if it's lat or long - don't allow changes to these fields
		Field field = _fieldArray[inRow];
		if (field == Field.LATITUDE || field == Field.LONGITUDE)
			return;
		if (inValue.equals(I18nManager.getText("fieldname.latitude"))
		  || inValue.equals(I18nManager.getText("fieldname.longitude")))
			return;

		// Changes to custom field need to be handled differently
		boolean changeToCustom = inValue.equals(I18nManager.getText("fieldname.custom"));
		if (changeToCustom)
		{
			if (field.isBuiltIn())
			{
				String customPrefix = I18nManager.getText("fieldname.prefix") + " ";
				int index = inRow + 1;
				while (hasField(customPrefix + index))
					index++;
				_fieldArray[inRow] = new Field(customPrefix + index);
			}
			// ignore custom to custom changes
		}
		else
		{
			// Change to a fixed field - check we've not already got it
			if (!hasField(inValue))
			{
				// Change is ok - find new Field object corresponding to text
				_fieldArray[inRow] = Field.getField(inValue);
			}
		}
		// fire change
		fireTableRowsUpdated(inRow, inRow);
	}


	/**
	 * @return array of Field objects
	 */
	public Field[] getFieldArray()
	{
		return _fieldArray;
	}


	/**
	 * @param inName Name of field to find
	 * @return true if this field is already present
	 */
	private boolean hasField(String inName)
	{
		if (_fieldArray == null || inName == null) return false;
		for (int i=0; i<_numRows; i++)
			if (_fieldArray[i].getName().equals(inName))
				return true;
		return false;
	}
}
