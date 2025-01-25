package tim.prune.function.distance;

import tim.prune.I18nManager;
import tim.prune.config.Config;
import tim.prune.data.Bearing;
import tim.prune.data.DataPoint;
import tim.prune.data.Distance;
import tim.prune.data.Unit;
import tim.prune.data.UnitSetLibrary;

/**
 * Class to hold the table model for the distances table
 */
public class DistanceTableModel extends GenericTableModel
{
	/** Distances */
	private double[] _distances = null;
	/** Bearings */
	private String[] _bearings = null;
	/** Column heading */
	private static final String _toColLabel = I18nManager.getText("dialog.distances.column.to");
	/** Column heading (depends on metric/imperial settings) */
	private String _distanceLabel = null;
	/** Column heading for bearing */
	private final String _bearingLabel;
	/** Previous distance units */
	private Unit _previousDistUnit = null;
	/** Object to convert bearing to description */
	private BearingDescriber _bearingDescriber = new BearingDescriber();


	public DistanceTableModel() {
		_bearingLabel = I18nManager.getText("fieldname.bearing");
	}

	/**
	 * @return column count
	 */
	public int getColumnCount() {
		return 3;
	}

	/**
	 * @param inRowIndex row index
	 * @param inColumnIndex column index
	 * @return cell value
	 */
	public Object getValueAt(int inRowIndex, int inColumnIndex)
	{
		if (inColumnIndex == 0) {
			return getPointName(inRowIndex);
		}
		if (inColumnIndex == 1) {
			return _distances == null ? 0.0 : _distances[inRowIndex];
		}
		return _bearings == null ? "" : _bearings[inRowIndex];
	}

	/**
	 * @param inColumnIndex column index
	 * @return column name
	 */
	public String getColumnName(int inColumnIndex)
	{
		if (inColumnIndex == 0) {
			return _toColLabel;
		}
		if (inColumnIndex == 1) {
			return _distanceLabel;
		}
		return _bearingLabel;
	}

	/**
	 * Get the column class (required for sorting)
	 * @param inColumnIndex column index
	 * @return Class of specified column
	 */
	public Class<?> getColumnClass(int inColumnIndex)
	{
		if (inColumnIndex == 0 || inColumnIndex == 2) {
			return String.class;
		}
		return Double.class;
	}

	/**
	 * Recalculate the distances
	 * @param inIndex index of selected 'from' point
	 * @param inConfig config object, used to get units
	 */
	public void recalculate(int inIndex, Config inConfig)
	{
		// Which units to use?
		final Unit distUnit = inConfig.getUnitSet().getDistanceUnit();
		_distanceLabel = I18nManager.getText("fieldname.distance") + " (" +
			I18nManager.getText(distUnit.getShortnameKey()) + ")";
		final boolean distUnitsChanged = (distUnit != _previousDistUnit);
		_previousDistUnit = distUnit;

		// Initialize array of distances
		int numRows = getRowCount();
		if (_distances == null || _distances.length != numRows) {
			_distances = new double[numRows];
		}
		if (_bearings == null || _bearings.length != numRows) {
			_bearings = new String[numRows];
		}
		DataPoint fromPoint = _pointList.get(inIndex);
		for (int i=0; i<numRows; i++)
		{
			if (i == inIndex)
			{
				_distances[i] = 0.0;
				_bearings[i] = "";
			}
			else
			{
				final DataPoint toPoint = _pointList.get(i);
				final double rads = DataPoint.calculateRadiansBetween(fromPoint, toPoint);
				_distances[i] = Distance.convertRadiansToDistance(rads, distUnit);
				if (Distance.convertRadiansToDistance(rads, UnitSetLibrary.UNITS_METRES) < 20.0) {
					_bearings[i] = "";
				}
				else
				{
					final double bearing = Bearing.calculateDegrees(fromPoint, toPoint);
					_bearings[i] = _bearingDescriber.describeBearing(bearing);
				}
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
