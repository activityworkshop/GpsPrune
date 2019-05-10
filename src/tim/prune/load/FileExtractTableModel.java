package tim.prune.load;

import javax.swing.table.AbstractTableModel;

/**
 * Class to hold the table model for the file extract table
 */
public class FileExtractTableModel extends AbstractTableModel
{

	private int _numRows = 0;
	private Object[][] _tableData = null;

	/**
	 * Get the column count
	 */
	public int getColumnCount()
	{
		if (_tableData == null)
			return 2;
		return _tableData[0].length;
	}

	/**
	 * Get the name of the column, in this case just the number
	 */
	public String getColumnName(int inColNum)
	{
		return "" + (inColNum + 1);
	}

	/**
	 * Get the row count
	 */
	public int getRowCount()
	{
		if (_tableData == null)
			return 2;
		return _numRows;
	}

	/**
	 * Get the value of the specified cell
	 */
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		if (_tableData == null) return "";
		return _tableData[rowIndex][columnIndex];
	}

	/**
	 * Make sure table data is not editable
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return false;
	}

	/**
	 * Update the data
	 * @param inData 2-dimensional Object array containing the data
	 */
	public void updateData(Object[][] inData)
	{
		_tableData = inData;
		if (_tableData != null)
		{
			_numRows = _tableData.length;
		}
		fireTableStructureChanged();
	}


	/**
	 * @return Object array of data
	 */
	public Object[][] getData()
	{
		return _tableData;
	}
}
