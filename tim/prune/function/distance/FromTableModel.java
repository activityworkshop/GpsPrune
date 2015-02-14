package tim.prune.function.distance;

import tim.prune.I18nManager;

/**
 * Class to hold table model for "From" list of distance function
 */
public class FromTableModel extends GenericTableModel
{
	/** Column heading */
	private static final String _colLabel = I18nManager.getText("dialog.distances.column.from");

	/**
	 * @return column count
	 */
	public int getColumnCount() {
		return 1;
	}

	/**
	 * @param inRowIndex row index
	 * @param inColumnIndex column index
	 * @return cell value
	 */
	public Object getValueAt(int inRowIndex, int inColumnIndex) {
		return getPointName(inRowIndex);
	}

	/**
	 * @param inColumnIndex column index
	 * @return column name
	 */
	public String getColumnName(int inColumnIndex) {
		return _colLabel;
	}
}
