package tim.prune.gui.map;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import tim.prune.config.Config;
import tim.prune.gui.map.tile.*;


/**
 * Class responsible for managing the map tiles,
 * including invoking the correct memory cacher(s) and/or disk cacher(s)
 */
public class MapTileManager implements TileManager
{
	/** Consumer object to inform when tiles received */
	private final TileConsumer _consumer;
	/** Current map source */
	private MapSource _mapSource = null;
	/** Array of tile caches, one per layer */
	private final MemTileCacher[] _tempCaches;
	/** Handler for reading from and writing to the disk cache */
	private final DiskCache _diskCache = new DiskCache();
	/** Coordinator of the asynchronous downloaders */
	private final TileWorkerCoordinator _coordinator = new TileWorkerCoordinator(this, TileDownloader::new);
	/** Flag for whether to download any tiles or just pull from disk */
	private boolean _downloadTiles = true;
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
		_tempCaches = new MemTileCacher[2];
		for (int i=0; i<2; i++) {
			_tempCaches[i] = new MemTileCacher();
		}
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
		for (MemTileCacher cacher : _tempCaches) {
			cacher.centreMap(inZoom, inTileX, inTileY);
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
		int maxZoom = (_mapSource == null ? 0 : _mapSource.getMaxZoomLevel());
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

	/**
	 * Clear all the memory caches due to changed config / zoom
	 */
	public void clearMemoryCaches()
	{
		for (MemTileCacher cacher : _tempCaches) {
			cacher.clearAll();
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
		if (inY < 0 || inY >= _numTileIndices) {return null;}
		if (inLayer < 0 || inLayer >= _mapSource.getNumLayers()) {return null;}
		// Wrap tile indices which are too big or too small
		inX = ((inX % _numTileIndices) + _numTileIndices) % _numTileIndices;

		// Check first in memory cache for tile
		MemTileCacher tempCache = _tempCaches[inLayer];
		Image tileImage = tempCache.getTile(inX, inY);
		if (tileImage != null) {
			return tileImage;
		}

		TileDef tileDef = new TileDef(_mapSource, inLayer, inX, inY, _zoom);
		// Tile wasn't in memory, but maybe it's in disk cache (if there is one)
		final String diskCachePath = Config.getConfigString(Config.KEY_DISK_CACHE);
		_diskCache.setBasePath(diskCachePath);
		MapTile mapTile = _diskCache.getTile(tileDef);
		if (mapTile != null && mapTile.getImage() != null)
		{
			tileImage = mapTile.getImage();
			// System.out.println("Got an image from the disk cache, width = " + tileImage.getWidth(null));
			// Pass tile to memory cache
			tempCache.setTile(tileImage, inX, inY, _zoom);
			// trigger the load from file
			tileImage.getWidth((img, infoFlags, x, y, width, height) -> tileUpdate(infoFlags));
		}

		// Maybe we've got an image now, maybe it's expired
		final boolean shouldDownload = (tileImage == null || mapTile.isExpired());
		// If we're online then try to download the tile
		final boolean onlineMode = Config.getConfigBoolean(Config.KEY_ONLINE_MODE);
		if (onlineMode && _downloadTiles && inDownloadIfNecessary && shouldDownload)
		{
			// Use coordinator to trigger an asynchronous download
			_coordinator.triggerDownload(_mapSource.isDoubleRes(inLayer) ? tileDef.zoomOut() : tileDef);
		}

		return tileImage;
	}

	/**
	 * Method called by image loader to inform of updates to the tiles
	 * @param infoFlags flags describing how much of the image is known
	 * @return false to carry on loading, true to stop
	 */
	public boolean tileUpdate(int infoFlags)
	{
		boolean loaded = (infoFlags & ImageObserver.ALLBITS) > 0;
		boolean error = (infoFlags & ImageObserver.ERROR) > 0;
		if ((loaded || error) && _consumer != null) {
			_consumer.tilesUpdated(loaded);
		}
		return !loaded;
	}

	/**
	 * Callback method from download coordinator
	 * @param inDef tile definition
	 * @param inResult bytes of result
	 */
	@Override
	public void returnTile(TileDef inDef, TileBytes inResult)
	{
		if (inDef == null || inDef._mapSource == null || inResult == null || inResult.isEmpty()) {
			return;
		}
		// construct image from result bytes
		Image image = Toolkit.getDefaultToolkit().createImage(inResult.getData());
		image.getWidth((img, infoFlags, x, y, width, height) -> processReturnedTile(inDef, inResult, image, infoFlags));
	}

	private boolean processReturnedTile(TileDef inDef, TileBytes inResult, Image image, int infoFlags)
	{
		final boolean imgComplete = (infoFlags & ImageObserver.ALLBITS) > 0;
		final boolean hasError = (infoFlags & (ImageObserver.ERROR | ImageObserver.ABORT)) > 0;
		if (!imgComplete || hasError) {
			return !imgComplete;
		}
		try {
			final int imgWidth = image.getWidth(null);
			if (imgWidth == 512)
			{
				// Double resolution, so need to slice and store as 4 separate tiles
				_mapSource.setDoubleRes(inDef._layerIdx);
				// Construct four different images, pass each one in turn
				for (int subtile=0; subtile<4; subtile++)
				{
					BufferedImage quarter = createSubtile(image, subtile);
					processDownloadedTile(quarter, inDef.zoomIn(subtile));
				}
			} else
			{
				// Regular resolution, so store the single image
				processDownloadedTile(image, inResult, inDef);
			}
		}
		catch (CacheFailure cacheFailure) {
			System.err.println("Cache failure - report to consumer?");
		}
		if (_consumer != null) {
			_consumer.tilesUpdated(true);
		}
		return false; // no more information needed
	}

	private BufferedImage createSubtile(Image image, int subtile)
	{
		BufferedImage result = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
		Graphics g = result.createGraphics();
		final int xOffset = (subtile % 2) * 256;
		final int yOffset = (subtile / 2) * 256;
		g.drawImage(image, -xOffset, -yOffset, null);
		return result;
	}

	// Got a possible race condition here where a tile is requested and triggered, and then it's requested _again_
	// exactly when the coordinator has finished downloading it but it's not in the memcaches yet - in worst case
	// a second download will be triggered for the same tile

	private void processDownloadedTile(Image inImage, TileBytes inBytes, TileDef inDefinition) throws CacheFailure
	{
		// Pass image to appropriate memcacher
		_tempCaches[inDefinition._layerIdx].setTile(inImage, inDefinition._x, inDefinition._y, inDefinition._zoom);
		_diskCache.saveTileBytes(inBytes, inDefinition);
	}

	private void processDownloadedTile(BufferedImage inImage, TileDef inDefinition) throws CacheFailure
	{
		// Pass image to appropriate memcacher
		_tempCaches[inDefinition._layerIdx].setTile(inImage, inDefinition._x, inDefinition._y, inDefinition._zoom);
		_diskCache.saveTileImage(inImage, inDefinition);
	}
}
