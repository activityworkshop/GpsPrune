package tim.prune.gui.map;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Class to handle the caching of map tiles from openstreetmap
 */
public class MapTileCacher implements ImageObserver
{
	/** Parent to be informed of updates */
	private MapCanvas _parent = null;
	/** Default grid size */
	private static final int GRID_SIZE = 11;
	/** Array of images to hold tiles */
	private Image[] _tiles = new Image[GRID_SIZE * GRID_SIZE];
	/** Current zoom level */
	private int _zoom = -1;
	/** X coordinate of central tile */
	private int _tileX = -1;
	/** Y coordinate of central tile */
	private int _tileY = -1;
	/** X coord of grid centre */
	private int _gridCentreX = 0;
	/** Y coord of grid centre */
	private int _gridCentreY = 0;


	/**
	 * Constructor
	 * @param inParent parent canvas to be informed of updates
	 */
	public MapTileCacher(MapCanvas inParent)
	{
		_parent = inParent;
	}

	/**
	 * Recentre the map and clear the cache
	 * @param inZoom zoom level
	 * @param inTileX x coord of central tile
	 * @param inTileY y coord of central tile
	 */
	public void centreMap(int inZoom, int inTileX, int inTileY)
	{
		if (inZoom != _zoom)
		{
			_zoom = inZoom;
			clearAll();
		}
		_gridCentreX = getCacheCoordinate(_gridCentreX + inTileX - _tileX);
		_gridCentreY = getCacheCoordinate(_gridCentreY + inTileY - _tileY);
		_tileX = inTileX;
		_tileY = inTileY;
		// Mark boundaries as invalid
		for (int i=0; i<GRID_SIZE; i++)
		{
			_tiles[getArrayIndex(_tileX + GRID_SIZE/2 + 1, _tileY + i - GRID_SIZE/2)] = null;
			_tiles[getArrayIndex(_tileX + i - GRID_SIZE/2, _tileY + GRID_SIZE/2 + 1)] = null;
		}
	}

	/**
	 * Clear all the cached images
	 */
	public void clearAll()
	{
		// Clear all images if zoom changed
		for (int i=0; i<_tiles.length; i++) {
			_tiles[i] = null;
		}
	}

	/**
	 * @param inX x index of tile
	 * @param inY y index of tile
	 * @return selected tile if already loaded, or null otherwise
	 */
	public Image getTile(int inX, int inY)
	{
		int arrayIndex = getArrayIndex(inX, inY);
		Image image = _tiles[arrayIndex];
		if (image != null)
		{
			// image already finished loading so return it
			return image;
		}

		// Trigger load if not already triggered
		// Work out tile coords for URL
		int urlX = getUrlCoordinate(inX, _zoom);
		int urlY = getUrlCoordinate(inY, _zoom);
		try
		{
			String url = "http://tile.openstreetmap.org/" + _zoom + "/" + urlX + "/" + urlY + ".png";
			// Load image asynchronously, using observer
			image = Toolkit.getDefaultToolkit().createImage(new URL(url));
			_tiles[arrayIndex] = image;
			if (image.getWidth(this) > 0) {return image;}
		}
		catch (MalformedURLException urle) {} // ignore
		return null;
	}


	/**
	 * Get the array index for the given coordinates
	 * @param inX x coord of tile
	 * @param inY y coord of tile
	 * @return array index
	 */
	private int getArrayIndex(int inX, int inY)
	{
		//System.out.println("Getting array index for (" + inX + ", " + inY + ") where the centre is at ("  + _tileX + ", " + _tileY
		//	+ ") and grid coords (" + _gridCentreX + ", " + _gridCentreY + ")");
		int x = getCacheCoordinate(inX - _tileX + _gridCentreX);
		int y = getCacheCoordinate(inY - _tileY + _gridCentreY);
		//System.out.println("Transformed to (" + x + ", " + y + ")");
		return (x + y * GRID_SIZE);
	}

	/**
	 * Transform a coordinate from map tiles to array coordinates
	 * @param inTile coordinate of tile
	 * @return coordinate in array (wrapping around cache grid)
	 */
	private static int getCacheCoordinate(int inTile)
	{
		int tile = inTile;
		while (tile >= GRID_SIZE) {tile -= GRID_SIZE;}
		while (tile < 0) {tile += GRID_SIZE;}
		return tile;
	}

	/**
	 * Make sure a url coordinate is within range
	 * @param inTile coordinate of tile in map system
	 * @param inZoom zoom factor
	 * @return coordinate for url (either vertical or horizontal)
	 */
	private static int getUrlCoordinate(int inTile, int inZoom)
	{
		int mapSize = 1 << inZoom;
		int coord = inTile;
		while (coord >= mapSize) {coord -= mapSize;}
		while (coord < 0) {coord += mapSize;}
		// coord is now between 0 and mapsize
		return coord;
	}

	/**
	 * Convert to string for debug
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuffer result = new StringBuffer("Grid centre (" + _gridCentreX + "," + _gridCentreY + ") - (" + _tileX + "," + _tileY + ")\n");
		for (int i=0; i<GRID_SIZE; i++)
		{
			for (int j=0; j<GRID_SIZE; j++) {
				if (i == _gridCentreY && j == _gridCentreX) {
					result.append(_tiles[j + i*GRID_SIZE] == null?"c":"C");
				}
				else {
					result.append(_tiles[j + i*GRID_SIZE] == null?".":"*");
				}
			}
			result.append("\n");
		}
		return result.toString();
	}

	/**
	 * Method called by image loader to inform of updates to the tiles
	 * @param img the image
	 * @param infoflags flags describing how much of the image is known
	 * @param x ignored
	 * @param y ignored
	 * @param width ignored
	 * @param height ignored
	 * @return false to carry on loading, true to stop
	 */
	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
	{
		boolean loaded = (infoflags & ImageObserver.ALLBITS) > 0;
		boolean error = (infoflags & ImageObserver.ERROR) > 0;
		if (loaded || error) {
			_parent.tilesUpdated(loaded);
		}
		return !loaded;
	}
}
