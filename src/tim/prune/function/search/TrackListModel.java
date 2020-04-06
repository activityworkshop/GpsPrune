package tim.prune.function.search;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.table.AbstractTableModel;

import tim.prune.I18nManager;
import tim.prune.config.Config;
import tim.prune.data.Unit;

/**
 * Model for list of tracks from a search result (eg geonames, overpass)
 */
public class TrackListModel extends AbstractTableModel
{
	/** List of tracks */
	private ArrayList<SearchResult> _trackList = null;
	/** Column heading for track name */
	private String _nameColLabel = null;
	/** Column heading for length */
	private String _lengthColLabel = null;
	/** Number of columns */
	private int _numColumns = 2;
	/** Normally this model shows distances / lengths, except when this flag is true */
	private boolean _showPointTypes = false;
	/** Formatter for distances */
	private NumberFormat _distanceFormatter = NumberFormat.getInstance();

	/**
	 * Constructor
	 * @param inColumn1Key key for first column
	 * @param inColumn2Key key for second column
	 */
	public TrackListModel(String inColumn1Key, String inColumn2Key)
	{
		_nameColLabel = I18nManager.getText(inColumn1Key);
		if (inColumn2Key != null) {
			_lengthColLabel = I18nManager.getText(inColumn2Key);
		}
		_numColumns = (_lengthColLabel != null?2:1);
		_distanceFormatter.setMaximumFractionDigits(1);
	}

	/**
	 * @return column count
	 */
	public int getColumnCount()
	{
		return _numColumns;
	}

	/**
	 * @return number of rows
	 */
	public int getRowCount()
	{
		if (_trackList == null) return 0;
		return _trackList.size();
	}

	/** @return true if there are no rows */
	public boolean isEmpty()
	{
		return getRowCount() == 0;
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
	 * @param inShowTypes true to show point types, false for distances
	 */
	public void setShowPointTypes(boolean inShowTypes)
	{
		_showPointTypes = inShowTypes;
	}

	/**
	 * @param inRowNum row number
	 * @param inColNum column number
	 * @return cell entry at given row and column
	 */
	public Object getValueAt(int inRowNum, int inColNum)
	{
		SearchResult track = _trackList.get(inRowNum);
		if (inColNum == 0) {
			return track.getTrackName();
		}
		if (_showPointTypes)
		{
			return track.getPointType();
		}
		double lengthM = track.getLength();
		// convert to current distance units
		Unit distUnit = Config.getUnitSet().getDistanceUnit();
		double length = lengthM * distUnit.getMultFactorFromStd();
		// Make text
		return _distanceFormatter.format(length) + " " + I18nManager.getText(distUnit.getShortnameKey());
	}

	/**
	 * Add a list of tracks to this model
	 * @param inList list of tracks to add
	 */
	public void addTracks(ArrayList<SearchResult> inList)
	{
		addTracks(inList, false);
	}

	/**
	 * Add a list of tracks to this model and optionally sort them
	 * @param inList list of tracks to add
	 * @param inSort true to sort results after adding
	 */
	public void addTracks(ArrayList<SearchResult> inList, boolean inSort)
	{
		if (_trackList == null) {_trackList = new ArrayList<SearchResult>();}
		final int prevCount = _trackList.size();
		if (inList != null && inList.size() > 0)
		{
			_trackList.addAll(inList);
			if (inSort) {
				Collections.sort(_trackList);
			}
		}
		final int updatedCount = _trackList.size();
		if (prevCount <= 0)
			fireTableDataChanged();
		else
			fireTableRowsInserted(prevCount, updatedCount-1);
	}

	/**
	 * @param inRowNum row number from 0
	 * @return track object for this row
	 */
	public SearchResult getTrack(int inRowNum)
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
