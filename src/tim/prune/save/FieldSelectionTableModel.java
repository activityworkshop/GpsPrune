package tim.prune.save;

import javax.swing.table.AbstractTableModel;

import tim.prune.I18nManager;

/**
 * Class to hold table model information for save dialog
 */
public class FieldSelectionTableModel extends AbstractTableModel
{
	private FieldInfo[] _info = null;


	/**
	 * Constructor giving list size
	 * @param inSize number of fields
	 */
	public FieldSelectionTableModel(int inSize)
	{
		_info = new FieldInfo[inSize];
	}


	/**
	 * Set the given FieldInfo object in the array
	 * @param inInfo FieldInfo object describing the field
	 * @param inIndex index to place in array
	 */
	public void addFieldInfo(FieldInfo inInfo, int inIndex)
	{
		_info[inIndex] = inInfo;
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
		return _info.length;
	}


	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int inRowIndex, int inColumnIndex)
	{
		if (inColumnIndex == 0)
		{
			return _info[inRowIndex].getField().getName();
		}
		else if (inColumnIndex == 1)
		{
			return Boolean.valueOf(_info[inRowIndex].hasData());
		}
		return Boolean.valueOf(_info[inRowIndex].isSelected());
	}


	/**
	 * @return true if cell is editable
	 */
	public boolean isCellEditable(int inRowIndex, int inColumnIndex)
	{
		// only the select column is editable
		return inColumnIndex == 2;
	}


	/**
	 * Set the given cell value
	 * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
	 */
	public void setValueAt(Object inValue, int inRowIndex, int inColumnIndex)
	{
		// ignore edits to other columns
		if (inColumnIndex == 2)
			_info[inRowIndex].setSelected(((Boolean) inValue).booleanValue());
	}


	/**
	 * Swap the specified items in the array
	 * @param inIndex1 first index
	 * @param inIndex2 second index
	 */
	public void swapItems(int inIndex1, int inIndex2)
	{
		if (inIndex1 >= 0 && inIndex1 < _info.length && inIndex2 >= 0 && inIndex2 < _info.length)
		{
			FieldInfo temp = _info[inIndex1];
			_info[inIndex1] = _info[inIndex2];
			_info[inIndex2] = temp;
		}
	}


	/**
	 * @return Class of cell data
	 */
	public Class<?> getColumnClass(int inColumnIndex)
	{
		if (inColumnIndex==0) return String.class;
		return Boolean.class;
	}


	/**
	 * Get the name of the column
	 */
	public String getColumnName(int inColNum)
	{
		if (inColNum == 0) return I18nManager.getText("dialog.save.table.field");
		else if (inColNum == 1) return I18nManager.getText("dialog.save.table.hasdata");
		return I18nManager.getText("dialog.save.table.save");
	}


	/**
	 * Retrieve the FieldInfo object at the given index
	 * @param inIndex index, starting at 0
	 * @return FieldInfo object at this position
	 */
	public FieldInfo getFieldInfo(int inIndex)
	{
		if (inIndex < 0 || inIndex >= _info.length)
		{
			return null;
		}
		return _info[inIndex];
	}
}
