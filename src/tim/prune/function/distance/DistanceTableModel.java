package tim.prune.function.distance;

import tim.prune.I18nManager;
import tim.prune.config.Config;
import tim.prune.data.DataPoint;
import tim.prune.data.Distance;
import tim.prune.data.Unit;

/**
 * Class to hold the table model for the distances table
 */
public class DistanceTableModel extends GenericTableModel
{
	/** Distances */
	private double[] _distances = null;
	/** Column heading */
	private static final String _toColLabel = I18nManager.getText("dialog.distances.column.to");
	/** Column heading (depends on metric/imperial settings) */
	private String _distanceLabel = null;
	/** Previous distance units */
	private Unit _previousDistUnit = null;

	/**
	 * @return column count
	 */
	public int getColumnCount()
	{
		return 2;
	}

	/**
	 * @param inRowIndex row index
	 * @param inColumnIndex column index
	 * @return cell value
	 */
	public Object getValueAt(int inRowIndex, int inColumnIndex)
	{
		if (inColumnIndex == 0) {return getPointName(inRowIndex);}
		if (_distances == null) {return 0.0;}
		return Double.valueOf(_distances[inRowIndex]);
	}

	/**
	 * @param inColumnIndex column index
	 * @return column name
	 */
	public String getColumnName(int inColumnIndex)
	{
		if (inColumnIndex == 0) {return _toColLabel;}
		return _distanceLabel;
	}

	/**
	 * Get the column class (required for sorting)
	 * @param inColumnIndex column index
	 * @return Class of specified column
	 */
	public Class<?> getColumnClass(int inColumnIndex)
	{
		if (inColumnIndex == 0) return String.class;
		return Double.class;
	}

	/**
	 * Recalculate the distances
	 * @param inIndex index of selected 'from' point
	 */
	public void recalculate(int inIndex)
	{
		// Which units to use?
		Unit distUnit = Config.getUnitSet().getDistanceUnit();
		_distanceLabel = I18nManager.getText("fieldname.distance") + " (" +
			I18nManager.getText(distUnit.getShortnameKey()) + ")";
		final boolean distUnitsChanged = (distUnit != _previousDistUnit);
		_previousDistUnit = distUnit;

		// Initialize array of distances
		int numRows = getRowCount();
		if (_distances == null || _distances.length != numRows) {
			_distances = new double[numRows];
		}
		DataPoint fromPoint = _pointList.get(inIndex);
		for (int i=0; i<numRows; i++) {
			if (i == inIndex) {
				_distances[i] = 0.0;
			}
			else {
				double rads = DataPoint.calculateRadiansBetween(fromPoint, _pointList.get(i));
				_distances[i] = Distance.convertRadiansToDistance(rads);
			}
		}
		// Let table know that it has to refresh data, and maybe the whole table too
		if (distUnitsChanged) {
			fireTableStructureChanged();
		}
		else {
			fireTableDataChanged();
		}
	}
}
