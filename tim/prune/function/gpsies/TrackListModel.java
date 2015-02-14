package tim.prune.function.gpsies;

import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import tim.prune.Config;
import tim.prune.I18nManager;
import tim.prune.data.Distance;

/**
 * Model for list of tracks from gpsies.com
 */
public class TrackListModel extends AbstractTableModel
{
	/** List of tracks */
	private ArrayList<GpsiesTrack> _trackList = null;
	/** Column heading for track name */
	private static final String _nameColLabel = I18nManager.getText("dialog.gpsies.column.name");
	/** Column heading for length */
	private static final String _lengthColLabel = I18nManager.getText("dialog.gpsies.column.length");
	/** Formatter for distances */
	private NumberFormat _distanceFormatter = NumberFormat.getInstance();

	/**
	 * Constructor
	 */
	public TrackListModel()
	{
		_distanceFormatter.setMaximumFractionDigits(1);
	}

	/**
	 * @return column count
	 */
	public int getColumnCount()
	{
		return 2;
	}

	/**
	 * @return number of rows
	 */
	public int getRowCount()
	{
		if (_trackList == null) return 0;
		return _trackList.size();
	}

	/**
	 * @param inColNum column number
	 * @return column label for given column
	 */
	public String getColumnName(int inColNum)
	{
		if (inColNum == 0) {return _nameColLabel;}
		return _lengthColLabel;
	}

	/**
	 * @param inRowNum row number
	 * @param inColNum column number
	 * @return cell entry at given row and column
	 */
	public Object getValueAt(int inRowNum, int inColNum)
	{
		GpsiesTrack track = _trackList.get(inRowNum);
		if (inColNum == 0) {return track.getTrackName();}
		double lengthM = track.getLength();
		if (Config.getConfigBoolean(Config.KEY_METRIC_UNITS)) {
			return _distanceFormatter.format(lengthM / 1000.0) + " " + I18nManager.getText("units.kilometres.short");
		}
		// must be imperial
		return _distanceFormatter.format(Distance.convertMetresToMiles(lengthM))
			+ " " + I18nManager.getText("units.miles.short");
	}

	/**
	 * Add a list of tracks to this model
	 * @param inList list of tracks to add
	 */
	public void addTracks(ArrayList<GpsiesTrack> inList)
	{
		if (_trackList == null) {_trackList = new ArrayList<GpsiesTrack>();}
		if (inList != null && inList.size() > 0) {
			_trackList.addAll(inList);
		}
		fireTableDataChanged();
	}

	/**
	 * @param inRowNum row number from 0
	 * @return track object for this row
	 */
	public GpsiesTrack getTrack(int inRowNum)
	{
		return _trackList.get(inRowNum);
	}

	/**
	 * Clear the list of tracks
	 */
	public void clear()
	{
		_trackList = null;
	}
}
