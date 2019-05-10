package tim.prune.function.cache;

import javax.swing.table.AbstractTableModel;

import tim.prune.I18nManager;

/**
 * Class to act as a table model for the list of tile sets
 */
public final class TileSetTableModel extends AbstractTableModel
{
	/** Model from which values are drawn */
	private TileCacheModel _model = null;


	/**
	 * Constructor
	 * @param inModel model to use
	 */
	public TileSetTableModel(TileCacheModel inModel) {
		_model = inModel;
	}

	/** @return the column count (always constant) */
	public int getColumnCount() {
		return 5;
	}

	/** @return name of specified column */
	public String getColumnName(int inColumnIndex)
	{
		switch (inColumnIndex)
		{
			case 0: return I18nManager.getText("dialog.diskcache.table.path");
			case 1: return I18nManager.getText("dialog.diskcache.table.usedby");
			case 2: return I18nManager.getText("dialog.diskcache.table.zoom");
			case 3: return I18nManager.getText("dialog.diskcache.table.tiles");
			case 4: return I18nManager.getText("dialog.diskcache.table.megabytes");
		}
		return "";
	}

	/**
	 * @return number of rows in the table
	 */
	public int getRowCount()
	{
		if (_model == null)
			return 0;
		return _model.getNumTileSets();
	}

	/**
	 * @param inRowIndex row index
	 * @param inColumnIndex column index
	 * @return the value of the specified cell
	 */
	public Object getValueAt(int inRowIndex, int inColumnIndex)
	{
		if (_model != null && inColumnIndex >= 0 && inColumnIndex < getColumnCount())
		{
			TileSet set = _model.getTileSet(inRowIndex);
			if (set != null)
			{
				switch (inColumnIndex)
				{
					case 0: return set.getPath();
					case 1: return set.getUsedBy();
					case 2: return set.getRowInfo().getZoomRange();
					case 3: return "" + set.getRowInfo().getNumTiles();
					case 4: return "" + (set.getRowInfo().getTotalSize() / 1024 / 1024) + " MB";
				}
			}
		}
		return null;
	}
}
