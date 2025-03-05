package tim.prune.function.comparesegments;

import java.util.ArrayList;
import java.util.TimeZone;

import javax.swing.table.AbstractTableModel;

import tim.prune.I18nManager;
import tim.prune.data.Distance;
import tim.prune.data.Timestamp;
import tim.prune.data.Unit;


/**
 * Table model for the segments in the comparison function
 */
public class SegmentTableModel extends AbstractTableModel
{
	/** list of segments */
	private ArrayList<SegmentSummary> _segmentList = null;
	/** Units to use for distances */
	private Unit _distUnits = null;
	/** objects shown in table */
	private ArrayList<TableValues> _tableValuesList = null;

	private static final int NUM_COLUMNS = 4;

	/** Class to represent a single row of the table */
	private static class TableValues
	{
		private final Object[] _values = new Object[NUM_COLUMNS];
		private TableValues(SegmentSummary inSegment, TimeZone inTimezone, Unit inDistUnits)
		{
			_values[0] = new DateForTable(inSegment.getStartTimestamp(), inTimezone);
			_values[1] = inSegment.getStartTimestamp().getTimeText(inTimezone);
			_values[2] = Distance.convertRadiansToDistance(inSegment.getDistanceInRadians(), inDistUnits);
			_values[3] = new Duration(inSegment.getDurationInSeconds());
		}
		private Object getValue(int inColumnIndex) {
			return _values[inColumnIndex];
		}
	}



	/**
	 * Initialize the table model with the segment list
	 * @param inSegments list of segments
	 * @param inTimezone timezone to use for dates and times
	 * @param inDistanceUnits units to use for distances
	 */
	public void init(ArrayList<SegmentSummary> inSegments, TimeZone inTimezone, Unit inDistanceUnits)
	{
		_segmentList = inSegments;
		_distUnits = inDistanceUnits;
		_tableValuesList = new ArrayList<>();
		for (SegmentSummary segment : inSegments) {
			_tableValuesList.add(new TableValues(segment, inTimezone, inDistanceUnits));
		}
		fireTableStructureChanged();
	}

	/**
	 * @return row count
	 */
	public int getRowCount() {
		return _segmentList == null ? 0 : _segmentList.size();
	}

	public int getColumnCount() {
		return NUM_COLUMNS;
	}

	/**
	 * Get the column class
	 * @param inColumnIndex column index
	 * @return Class of specified column
	 */
	public Class<?> getColumnClass(int inColumnIndex)
	{
		if (inColumnIndex == 0) {
			return DateForTable.class;
		}
		if (inColumnIndex == 2) {
			return Double.class;
		}
		if (inColumnIndex == 3) {
			return Duration.class;
		}
		return String.class;
	}

	public Object getValueAt(int inRowIndex, int inColIndex)
	{
		try {
			TableValues values = _tableValuesList.get(inRowIndex);
			return values.getValue(inColIndex);
		}
		catch (IndexOutOfBoundsException ignored) {}
		throw new IllegalArgumentException("Unknown column: " + inColIndex);
	}

	public String getColumnName(int inColIndex)
	{
		if (inColIndex == 0) {
			return I18nManager.getText("dialog.comparesegments.startdate");
		}
		if (inColIndex == 1) {
			return I18nManager.getText("dialog.comparesegments.starttime");
		}
		if (inColIndex == 2)
		{
			if (_distUnits == null) {
				return I18nManager.getText("fieldname.distance");
			}
			return I18nManager.getText("fieldname.distance") + " ("
				+ I18nManager.getText(_distUnits.getShortnameKey()) + ")";
		}
		if (inColIndex == 3) {
			return I18nManager.getText("fieldname.duration");
		}
		throw new IllegalArgumentException("Unknown column: " + inColIndex);
	}

	/** @return the start index of the requested segment */
	int getSegmentStartIndex(int inIndex) {
		return _segmentList.get(inIndex).getStartIndex();
	}

	/** @return true if the first segment is earlier */
	boolean areSegmentsInTimeOrder(int inIndex1, int inIndex2)
	{
		Timestamp timestamp1 = _segmentList.get(inIndex1).getStartTimestamp();
		Timestamp timestamp2 = _segmentList.get(inIndex2).getStartTimestamp();
		return timestamp1.isBefore(timestamp2);
	}
}
