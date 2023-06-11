package tim.prune.function.cache;

/**
 * Class to hold the information for a single table row.
 * Used to describe a tileset or for a single zoom level of a tileset.
 */
public class RowInfo
{
	private int _minZoom = -1, _maxZoom = -1;
	private int _numTiles = 0;
	private long _totalSize = 0L;


	/**
	 * Add a zoom level and adjust max/min
	 * @param inZoom zoom level
	 */
	public void addZoom(int inZoom)
	{
		if (inZoom > 0)
		{
			if (_minZoom < 0 || _minZoom > inZoom) {
				_minZoom = inZoom;
			}
			_maxZoom = Math.max(_maxZoom, inZoom);
		}
	}

	/**
	 * @return the zoom range as a string
	 */
	public String getZoomRange()
	{
		if (_minZoom < 0 && _maxZoom < 0) return "";
		if (_minZoom == _maxZoom || _maxZoom < 0) return "" + _minZoom;
		if (_minZoom < 0) return "" + _maxZoom;
		return "" + _minZoom + " - " + _maxZoom;
	}

	/**
	 * Add a single tile of the given size
	 * @param inSize size in bytes
	 */
	public void addTile(long inSize) {
		addTiles(1, inSize);
	}

	/**
	 * Add the given tiles
	 * @param inNumTiles number of tiles to add
	 * @param inSize total size of the tiles in bytes
	 */
	public void addTiles(int inNumTiles, long inSize)
	{
		_numTiles += inNumTiles;
		_totalSize += inSize;
	}

	/**
	 * @return the total number of tiles found
	 */
	public int getNumTiles() {
		return _numTiles;
	}

	/**
	 * @return the total size of the tiles in bytes
	 */
	public long getTotalSize() {
		return _totalSize;
	}


	/**
	 * Add the given RowInfo object to this one
	 * @param inOther other row object
	 */
	public void addRow(RowInfo inOther)
	{
		if (inOther == null)
			return;
		_numTiles  += inOther._numTiles;
		_totalSize += inOther._totalSize;
		// TODO: Max age
		// Zoom range
		addZoom(inOther._minZoom);
		addZoom(inOther._maxZoom);
	}
}
