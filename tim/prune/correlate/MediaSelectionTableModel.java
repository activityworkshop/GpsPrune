package tim.prune.correlate;

import java.util.ArrayList;
import java.util.TimeZone;

import javax.swing.table.AbstractTableModel;
import tim.prune.I18nManager;
import tim.prune.config.TimezoneHelper;
import tim.prune.data.MediaObject;


/**
 * Class to act as the table model for the selection table in the correlation functions.
 * Can be used by both photo correlation and audio correlation
 */
public class MediaSelectionTableModel extends AbstractTableModel
{
	/** Text for first column heading */
	private String _firstColumnHeading = null;
	/** Text for last column heading */
	private String _lastColumnHeading = null;
	/** List of rows */
	private ArrayList<MediaSelectionTableRow> _list = new ArrayList<MediaSelectionTableRow>();
	/** Current timezone */
	private TimeZone _timezone = null;


	/**
	 * Constructor
	 * @param inFirstColumnKey key for first column heading
	 * @param inLastColumnKey key for last column heading
	 */
	public MediaSelectionTableModel(String inFirstColumnKey, String inLastColumnKey)
	{
		_firstColumnHeading = I18nManager.getText(inFirstColumnKey);
		_lastColumnHeading = I18nManager.getText(inLastColumnKey);
		_timezone = TimezoneHelper.getSelectedTimezone();
	}

	/**
	 * @return the column count, always 4
	 */
	public int getColumnCount() {
		return 4;
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
		return _lastColumnHeading;
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
	public MediaSelectionTableRow getRow(int inRowIndex)
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
		// MAYBE: only show time of photos (not date) if dates all identical
		MediaSelectionTableRow row = _list.get(inRowIndex);
		if (inColumnIndex == 0) return row.getMedia().getName();
		else if (inColumnIndex == 1) {
			return (row.getMedia().hasTimestamp() ?
				row.getMedia().getTimestamp().getText(_timezone) : "");
		}
		else if (inColumnIndex == 2) return row.getTimeDiff().getDescription();
		return (row.getTimeDiff().getIsPositive() ? I18nManager.getText("dialog.about.yes") :
			I18nManager.getText("dialog.about.no"));
	}


	/**
	 * Clear the list
	 */
	public void reset() {
		_list.clear();
	}

	/**
	 * Add a media object to the list
	 * @param inMedia item to add
	 * @param inTimeDiff time difference
	 */
	public void addMedia(MediaObject inMedia, long inTimeDiff)
	{
		_list.add(new MediaSelectionTableRow(inMedia, inTimeDiff));
	}
}
