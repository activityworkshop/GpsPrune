package tim.prune.gui.map.tile;

import tim.prune.gui.map.MapSource;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * Definition of a single tile including its source
 * and its coordinates
 */
public class TileDef
{
	public final MapSource _mapSource;
	public final int _layerIdx;
	public final int _x;
	public final int _y;
	public final int _zoom;

	public TileDef(MapSource inSource, int layerIdx, int x, int y, int zoom)
	{
		_mapSource = inSource;
		_layerIdx = layerIdx;
		_x = x;
		_y = y;
		_zoom = zoom;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TileDef tileDef = (TileDef) o;
		return _layerIdx == tileDef._layerIdx && _x == tileDef._x && _y == tileDef._y
				&& _zoom == tileDef._zoom && _mapSource.getName().equals(tileDef._mapSource.getName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(_mapSource.getName(), _layerIdx, _x, _y, _zoom);
	}

	@Override
	public String toString() {
		return "TileDef{layer=" + _layerIdx +
				", x=" + _x + ", y=" + _y + ", zoom=" + _zoom +	'}';
	}

	/**
	 * @return a new TileDef describing the next zoom level out
	 */
	public TileDef zoomOut() {
		return new TileDef(_mapSource, _layerIdx, _x/2, _y/2, _zoom-1);
	}

	/**
	 * @return a new TileDef describing one of the tiles in the next zoom level in
	 */
	public TileDef zoomIn(int inIndex) {
		return new TileDef(_mapSource, _layerIdx, _x*2 + (inIndex%2), _y*2 + (inIndex/2), _zoom+1);
	}

	/**
	 * @return a URL with which to get the specified tile
	 */
	public URL getUrl()
	{
		try {
			return new URL(_mapSource.makeURL(_layerIdx, _zoom, _x, _y));
		}
		catch (MalformedURLException ignored) {
			return null;
		}
	}

	/**
	 * @return the file path for this tile (assuming it's a single one)
	 */
	public String getFilePath() {
		return _mapSource.makeFilePath(_layerIdx, _zoom, _x, _y);
	}
}
