package tim.prune.correlate;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

import tim.prune.I18nManager;
import tim.prune.data.Photo;

/**
 * Class to act as table model for the photo selection table
 */
public class PhotoSelectionTableModel extends AbstractTableModel
{
	private ArrayList _list = new ArrayList();


	/**
	 * @return the column count, always 4
	 */
	public int getColumnCount()
	{
		return 4;
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
		return I18nManager.getText("dialog.correlate.photoselect.photolater");
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
	public PhotoSelectionTableRow getRow(int inRowIndex)
	{
		PhotoSelectionTableRow row = (PhotoSelectionTableRow) _list.get(inRowIndex);
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
		// TODO: only show time of photos (not date) if dates all identical
		PhotoSelectionTableRow row = (PhotoSelectionTableRow) _list.get(inRowIndex);
		if (inColumnIndex == 0) return row.getPhoto().getFile().getName();
		else if (inColumnIndex == 1) return row.getPhoto().getTimestamp().getText();
		else if (inColumnIndex == 2) return row.getTimeDiff().getDescription();
		return (row.getTimeDiff().getIsPositive() ? I18nManager.getText("dialog.about.yes") :
			I18nManager.getText("dialog.about.no"));
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
	 * @param inPhoto photo to add
	 * @param inTimeDiff time difference
	 */
	public void addPhoto(Photo inPhoto, long inTimeDiff)
	{
		_list.add(new PhotoSelectionTableRow(inPhoto, inTimeDiff));
	}
}
