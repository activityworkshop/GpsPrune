package tim.prune.gui.map;

import java.awt.Image;
import java.awt.image.ImageObserver;
import java.net.MalformedURLException;
import java.net.URL;

import tim.prune.config.Config;


/**
 * Class responsible for managing the map tiles,
 * including invoking the correct memory cacher(s) and/or disk cacher(s)
 */
public class MapTileManager implements ImageObserver
{
	/** Consumer object to inform when tiles received */
	private TileConsumer _consumer = null;
	/** Current map source */
	private MapSource _mapSource = null;
	/** Array of tile caches, one per layer */
	private MemTileCacher[] _tempCaches = null;
	/** Flag for whether to download any tiles or just pull from disk */
	private boolean _downloadTiles = true;
	/** Flag for whether to return incomplete images or just pass to tile cache until they're done */
	private boolean _returnIncompleteImages = false;
	/** Number of layers */
	private int _numLayers = -1;
	/** Current zoom level */
	private int _zoom = 0;
	/** Number of tiles in each direction for this zoom level */
	private int _numTileIndices = 1;


	/**
	 * Constructor
	 * @param inConsumer consumer object to be notified
	 */
	public MapTileManager(TileConsumer inConsumer)
	{
		_consumer = inConsumer;
	}

	/**
	 * Recentre the map
	 * @param inZoom zoom level
	 * @param inTileX x coord of central tile
	 * @param inTileY y coord of central tile
	 */
	public void centreMap(int inZoom, int inTileX, int inTileY)
	{
		setZoom(inZoom);
		// Pass params onto all memory cachers
		if (_tempCaches != null) {
			for (int i=0; i<_tempCaches.length; i++) {
				_tempCaches[i].centreMap(inZoom, inTileX, inTileY);
			}
		}
	}

	/** @param inZoom zoom level to set */
	public void setZoom(int inZoom)
	{
		_zoom = inZoom;
		// Calculate number of tiles = 2^^zoom
		_numTileIndices = 1 << _zoom;
	}

	/**
	 * @return true if zoom is too high for tiles
	 */
	public boolean isOverzoomed()
	{
		// Ask current map source what maximum zoom is
		int maxZoom = (_mapSource == null?0:_mapSource.getMaxZoomLevel());
		return (_zoom > maxZoom);
	}

	/**
	 * Enable or disable tile downloading
	 * @param inEnabled true to enable downloading, false to just get tiles from disk
	 */
	public void enableTileDownloading(boolean inEnabled)
	{
		_downloadTiles = inEnabled;
	}

	/** Configure to return incomplete images instead of going via caches (and another call) */
	public void setReturnIncompleteImages()
	{
		_returnIncompleteImages = true;
	}

	/**
	 * Clear all the memory caches due to changed config / zoom
	 */
	public void clearMemoryCaches()
	{
		int numLayers = _mapSource.getNumLayers();
		if (_tempCaches == null || _tempCaches.length != numLayers)
		{
			// Cachers don't match, so need to create the right number of them
			_tempCaches = new MemTileCacher[numLayers];
			for (int i=0; i<numLayers; i++) {
				_tempCaches[i] = new MemTileCacher();
			}
		}
		else {
			// Cachers already there, just need to be cleared
			for (int i=0; i<numLayers; i++) {
				_tempCaches[i].clearAll();
			}
		}
	}

	/**
	 * @param inSourceNum selected map source index
	 */
	public void setMapSource(int inSourceNum)
	{
		setMapSource(MapSourceLibrary.getSource(inSourceNum));
	}

	/**
	 * @param inMapSource selected map source
	 */
	public void setMapSource(MapSource inMapSource)
	{
		_mapSource = inMapSource;
		if (_mapSource == null) {_mapSource = MapSourceLibrary.getSource(0);}
		clearMemoryCaches();
		_numLayers = _mapSource.getNumLayers();
	}

	/**
	 * @return the number of layers in the map
	 */
	public int getNumLayers()
	{
		return _numLayers;
	}

	/**
	 * Get a tile from the currently selected map source
	 * @param inLayer layer number, starting from 0
	 * @param inX x index of tile
	 * @param inY y index of tile
	 * @param inDownloadIfNecessary true to download the file if it's not available
	 * @return selected tile if already loaded, or null otherwise
	 */
	public Image getTile(int inLayer, int inX, int inY, boolean inDownloadIfNecessary)
	{
		if (inY < 0 || inY >= _numTileIndices) return null;
		// Wrap tile indices which are too big or too small
		inX = ((inX % _numTileIndices) + _numTileIndices) % _numTileIndices;

		// Check first in memory cache for tile
		Image tileImage = null;
		MemTileCacher tempCache = null;
		if (_tempCaches != null)
		{
			tempCache = _tempCaches[inLayer]; // Should probably guard array indexes here
			tileImage = tempCache.getTile(inX, inY);
			if (tileImage != null) {
				//System.out.println("Got tile from memory: " + inX + ", " + inY);
				return tileImage;
			}
		}

		// Tile wasn't in memory, but maybe it's in disk cache (if there is one)
		String diskCachePath = Config.getConfigString(Config.KEY_DISK_CACHE);
		boolean useDisk = (diskCachePath != null);
		boolean onlineMode = Config.getConfigBoolean(Config.KEY_ONLINE_MODE);
		MapTile mapTile = null;
		if (useDisk)
		{
			// Get the map tile from cache
			mapTile = DiskTileCacher.getTile(diskCachePath, _mapSource.makeFilePath(inLayer, _zoom, inX, inY));
			if (mapTile != null && mapTile.getImage() != null)
			{
				tileImage = mapTile.getImage();
				if (_returnIncompleteImages) {return tileImage;}
				// Pass tile to memory cache
				if (tempCache != null) {
					tempCache.setTile(tileImage, inX, inY, _zoom);
				}
				tileImage.getWidth(this); // trigger the load from file
			}
		}
		// Maybe we've got an image now, maybe it's expired
		final boolean shouldDownload = (tileImage == null || mapTile == null || mapTile.isExpired());

		// If we're online then try to download the tile
		if (onlineMode && _downloadTiles && inDownloadIfNecessary && shouldDownload)
		{
			try
			{
				URL tileUrl = new URL(_mapSource.makeURL(inLayer, _zoom, inX, inY));
				if (useDisk)
				{
					DiskTileCacher.saveTile(tileUrl, diskCachePath,
						_mapSource.makeFilePath(inLayer, _zoom, inX, inY), this);
					// Image will now be copied directly from URL stream to disk cache
				}
				else
				{
					// Load image asynchronously, using observer
					// In order to set the http user agent, need to use a TileDownloader instead
					TileDownloader.triggerLoad(this, tileUrl, inLayer, inX, inY, _zoom);
				}
			}
			catch (MalformedURLException urle) {} // ignore
		}
		return tileImage;
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
			_consumer.tilesUpdated(loaded);
		}
		return !loaded;
	}

	/**
	 * Callback method from TileDownloader to let us know that an image has been loaded
	 * @param inTile Loaded Image object
	 * @param inLayer layer index from 0
	 * @param inX x coordinate of tile
	 * @param inY y coordinate of tile
	 * @param inZoom zoom level of loaded image
	 */
	public void notifyImageLoaded(Image inTile, int inLayer, int inX, int inY, int inZoom)
	{
		if (inTile != null && _tempCaches != null)
		{
			MemTileCacher tempCache = _tempCaches[inLayer]; // Should probably guard against nulls and array indexes here
			if (tempCache.getTile(inX, inY) == null)
			{
				// Check with cache that the zoom level is still valid
				tempCache.setTile(inTile, inX, inY, inZoom);
				inTile.getWidth(this); // trigger imageUpdate when image is ready
			}
		}
		else if (inTile != null) {
			inTile.getWidth(this);
		}
	}
}
