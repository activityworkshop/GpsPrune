package tim.prune.correlate;

import java.text.NumberFormat;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;
import tim.prune.I18nManager;
import tim.prune.data.Distance;

/**
 * Class to act as the table model for the correlation preview table
 */
public class MediaPreviewTableModel extends AbstractTableModel
{
	/** Text for first column heading */
	private String _firstColumnHeading = null;
	/** ArrayList containing TableRow objects */
	private ArrayList<MediaPreviewTableRow> _list = new ArrayList<MediaPreviewTableRow>();
	/** Distance units */
	private Distance.Units _distanceUnits = Distance.Units.KILOMETRES;
	/** Number formatter */
	private static final NumberFormat FORMAT_ONE_DP = NumberFormat.getNumberInstance();


	/** Static block to initialise the one d.p. formatter */
	static
	{
		FORMAT_ONE_DP.setMaximumFractionDigits(1);
		FORMAT_ONE_DP.setMinimumFractionDigits(1);
	}

	/**
	 * Constructor
	 * @param inFirstColumnKey key for first column heading
	 */
	public MediaPreviewTableModel(String inFirstColumnKey) {
		_firstColumnHeading = I18nManager.getText(inFirstColumnKey);
	}

	/**
	 * @return the column count, always 5
	 */
	public int getColumnCount() {
		return 5;
	}

	/**
	 * Get the name of the column
	 * @param inColNum column number
	 * @return column name
	 */
	public String getColumnName(int inColNum)
	{
		if (inColNum == 0) return _firstColumnHeading;
		else if (inColNum == 1) return I18nManager.getText("fieldname.timestamp");
		else if (inColNum == 2) return I18nManager.getText("dialog.correlate.select.timediff");
		else if (inColNum == 3) return I18nManager.getText("fieldname.distance");
		return I18nManager.getText("dialog.correlate.options.correlate");
	}


	/**
	 * @return the row count
	 */
	public int getRowCount()
	{
		return _list.size();
	}


	/**
	 * Get the selected row from the table
	 * @param inRowIndex row index
	 * @return table row object
	 */
	public MediaPreviewTableRow getRow(int inRowIndex)
	{
		return _list.get(inRowIndex);
	}


	/**
	 * Get the value of the specified cell
	 * @param inRowIndex row index
	 * @param inColumnIndex column index
	 * @return value of specified cell
	 */
	public Object getValueAt(int inRowIndex, int inColumnIndex)
	{
		MediaPreviewTableRow row = _list.get(inRowIndex);
		if (inColumnIndex == 0) return row.getMedia().getName();
		else if (inColumnIndex == 1) {
			return row.getMedia().getTimestamp().getText();
		}
		else if (inColumnIndex == 2) {
			if (row.getPointPair().isValid()) {
				return row.getTimeDiff().getDescription();
			}
			return "";
		}
		else if (inColumnIndex == 3) {
			if (row.getPointPair().isValid()) {
				return FORMAT_ONE_DP.format(row.getDistance(_distanceUnits));
			}
			return "";
		}
		return row.getCorrelateFlag();
	}


	/**
	 * @param inUnits the distance units to use
	 */
	public void setDistanceUnits(Distance.Units inUnits)
	{
		_distanceUnits = inUnits;
	}


	/**
	 * Clear the list
	 */
	public void reset()
	{
		_list.clear();
	}


	/**
	 * Add a row to the list
	 * @param inRow row to add
	 */
	public void addRow(MediaPreviewTableRow inRow)
	{
		_list.add(inRow);
	}


	/**
	 * Get the class of objects in the given column
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	public Class<?> getColumnClass(int inColumnIndex)
	{
		if (inColumnIndex == 4) {return Boolean.class;}
		return super.getColumnClass(inColumnIndex);
	}


	/**
	 * Get whether the given cell is editable
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int inRowIndex, int inColumnIndex)
	{
		if (inColumnIndex == 4) {return true;}
		return super.isCellEditable(inRowIndex, inColumnIndex);
	}


	/**
	 * @return true if any of the correlate flags are on
	 */
	public boolean hasAnySelected()
	{
		for (int i=0; i<getRowCount(); i++)
		{
			if (getRow(i).getCorrelateFlag().booleanValue()) {
				return true;
			}
		}
		// None switched on
		return false;
	}


	/**
	 * Set the value at the given table cell
	 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
	 */
	public void setValueAt(Object inValue, int inRowIndex, int inColumnIndex)
	{
		// can only edit the correlate column
		if (inColumnIndex == 4)
		{
			MediaPreviewTableRow row = getRow(inRowIndex);
			// Don't allow setting of items which can't be correlated
			if (row.getPointPair().isValid()) {
				row.setCorrelateFlag(((Boolean) inValue).booleanValue());
			}
		}
	}
}
