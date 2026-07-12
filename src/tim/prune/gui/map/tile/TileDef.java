package tim.prune.gui.map.tile;

import tim.prune.gui.map.MapSource;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

/**
 * Definition of a single tile including its source
 * and its coordinates
 */
public record TileDef(MapSource mapSource, int layerIdx, int x, int y, int zoom)
{
	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TileDef tileDef = (TileDef) o;
		return layerIdx == tileDef.layerIdx && x == tileDef.x && y == tileDef.y
				&& zoom == tileDef.zoom && mapSource.getName().equals(tileDef.mapSource.getName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(mapSource.getName(), layerIdx, x, y, zoom);
	}

	@Override
	public String toString() {
		return "TileDef{layer=" + layerIdx +
				", x=" + x + ", y=" + y + ", zoom=" + zoom + '}';
	}

	/**
	 * @return a new TileDef describing the next zoom level out
	 */
	public TileDef zoomOut() {
		return new TileDef(mapSource, layerIdx, x/2, y/2, zoom - 1);
	}

	/**
	 * @return a new TileDef describing one of the tiles in the next zoom level in
	 */
	public TileDef zoomIn(int inIndex) {
		return new TileDef(mapSource, layerIdx, x*2 + (inIndex%2), y*2 + (inIndex/2), zoom + 1);
	}

	/**
	 * @return a URL with which to get the specified tile
	 */
	public URL getUrl()
	{
		try {
			return new URI(mapSource.makeURL(layerIdx, zoom, x, y)).toURL();
		}
		catch (URISyntaxException | IOException ignored) {
			return null;
		}
	}

	/**
	 * @return the file path for this tile (assuming it's a single one)
	 */
	public String getFilePath() {
		return mapSource.makeFilePath(layerIdx, zoom, x, y);
	}
}
