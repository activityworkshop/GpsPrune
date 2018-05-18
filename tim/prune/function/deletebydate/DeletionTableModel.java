package tim.prune.function.deletebydate;

import javax.swing.table.AbstractTableModel;
import tim.prune.I18nManager;

/**
 * Table model for selecting which dates to delete
 */
public class DeletionTableModel extends AbstractTableModel
{
	/** info list, one for each row of table */
	private DateInfoList _infoList = null;

	/** Column heading for date */
	private static final String COLUMN_HEADING_DATE = I18nManager.getText("fieldname.date");
	/** Column heading for number of points */
	private static final String COLUMN_HEADING_NUMPOINTS = I18nManager.getText("details.track.points");
	/** Column heading for keep */
	private static final String COLUMN_HEADING_KEEP = I18nManager.getText("dialog.deletebydate.column.keep");
	/** Column heading for delete */
	private static final String COLUMN_HEADING_DELETE = I18nManager.getText("dialog.deletebydate.column.delete");


	/**
	 * Constructor
	 * @param inList date info list from function
	 */
	public DeletionTableModel(DateInfoList inList)
	{
		_infoList = inList;
	}

	/**
	 * @return column count
	 */
	public int getColumnCount()
	{
		return 4; // always fixed (date, numpoints, keep, delete)
	}

	/**
	 * @return row count
	 */
	public int getRowCount()
	{
		if (_infoList == null) {return 0;} // shouldn't happen
		return _infoList.getNumEntries();
	}

	/**
	 * Get the name of the column
	 * @param inColNum column number
	 * @return column name
	 */
	public String getColumnName(int inColNum)
	{
		if (inColNum == 0) return COLUMN_HEADING_DATE;
		else if (inColNum == 1) return COLUMN_HEADING_NUMPOINTS;
		else if (inColNum == 2) return COLUMN_HEADING_KEEP;
		else if (inColNum == 3) return COLUMN_HEADING_DELETE;
		return "unknown column!";
	}

	/**
	 * Get the class of objects in the given column
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	public Class<?> getColumnClass(int inColumnIndex)
	{
		if (inColumnIndex == 1) {return Integer.class;}
		if (inColumnIndex > 1) {return Boolean.class;}
		return super.getColumnClass(inColumnIndex);
	}

	/**
	 * Get whether the given cell is editable
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int inRowIndex, int inColumnIndex)
	{
		return (inColumnIndex > 1);
	}

	/**
	 * Set the value at the given table cell
	 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
	 */
	public void setValueAt(Object inValue, int inRowIndex, int inColumnIndex)
	{
		// can only edit the keep and delete columns
		final boolean isKeep = (inColumnIndex == 2);
		final boolean isDelete = (inColumnIndex == 3);
		// ignore all events for other columns
		if (isKeep || isDelete)
		{
			try {
				boolean setFlag = ((Boolean) inValue).booleanValue();
				if (setFlag)
				{
					_infoList.getDateInfo(inRowIndex).setDeleteFlag(isDelete);
					// make sure the other cell (keep or delete) on the same row is updated too
					fireTableCellUpdated(inRowIndex, 5 - inColumnIndex);
				}
			}
			catch (ClassCastException cce) {}
		}
	}

	/**
	 * @return cell contents at the given row, column index
	 */
	public Object getValueAt(int inRowIndex, int inColIndex)
	{
		try {
			DateInfo info = _infoList.getDateInfo(inRowIndex);
			if (info != null)
			{
				switch (inColIndex)
				{
					case 0: // date
						if (info.isDateless()) {
							return I18nManager.getText("dialog.deletebydate.nodate");
						}
						return info.getString();
					case 1: // number of points
						return info.getPointCount();
					case 2: // keep
						return !info.getDeleteFlag();
					case 3: // delete
						return info.getDeleteFlag();
				}
			}
		}
		catch (IndexOutOfBoundsException obe) {} // ignore, fallthrough
		return null;
	}
}
