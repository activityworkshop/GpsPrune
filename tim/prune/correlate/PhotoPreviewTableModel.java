package tim.prune.correlate;

import java.text.NumberFormat;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;
import tim.prune.I18nManager;
import tim.prune.data.Distance;

/**
 * Class to act as table model for the photo preview table
 */
public class PhotoPreviewTableModel extends AbstractTableModel
{
	/** ArrayList containing TableRow objects */
	private ArrayList _list = new ArrayList();
	/** Distance units */
	private int _distanceUnits = Distance.UNITS_KILOMETRES;
	/** Number formatter */
	private static final NumberFormat FORMAT_ONE_DP = NumberFormat.getNumberInstance();


	/** Static block to initialise the one d.p. formatter */
	static
	{
		FORMAT_ONE_DP.setMaximumFractionDigits(1);
		FORMAT_ONE_DP.setMinimumFractionDigits(1);
	}


	/**
	 * @return the column count, always 5
	 */
	public int getColumnCount()
	{
		return 5;
	}


	/**
	 * Get the name of the column
	 * @param inColNum column number
	 * @return column name
	 */
	public String getColumnName(int inColNum)
	{
		if (inColNum == 0) return I18nManager.getText("dialog.correlate.photoselect.photoname");
		else if (inColNum == 1) return I18nManager.getText("fieldname.timestamp");
		else if (inColNum == 2) return I18nManager.getText("dialog.correlate.photoselect.timediff");
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
	public PhotoPreviewTableRow getRow(int inRowIndex)
	{
		PhotoPreviewTableRow row = (PhotoPreviewTableRow) _list.get(inRowIndex);
		return row;
	}


	/**
	 * Get the value of the specified cell
	 * @param inRowIndex row index
	 * @param inColumnIndex column index
	 * @return value of specified cell
	 */
	public Object getValueAt(int inRowIndex, int inColumnIndex)
	{
		PhotoPreviewTableRow row = (PhotoPreviewTableRow) _list.get(inRowIndex);
		if (inColumnIndex == 0) return row.getPhoto().getFile().getName();
		else if (inColumnIndex == 1) {
			return row.getPhoto().getTimestamp().getText();
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
	public void setDistanceUnits(int inUnits)
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
	 * Add a photo to the list
	 * @param inRow row to add
	 */
	public void addPhotoRow(PhotoPreviewTableRow inRow)
	{
		_list.add(inRow);
	}


	/**
	 * Get the class of objects in the given column
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	public Class getColumnClass(int inColumnIndex)
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
	public boolean hasPhotosSelected()
	{
		for (int i=0; i<getRowCount(); i++)
		{
			if (getRow(i).getCorrelateFlag().booleanValue())
			{
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
			PhotoPreviewTableRow row = getRow(inRowIndex);
			// Don't allow setting of photos which can't be correlated
			if (row.getPointPair().isValid())
			{
				row.setCorrelateFlag(((Boolean) inValue).booleanValue());
			}
		}
	}
}
