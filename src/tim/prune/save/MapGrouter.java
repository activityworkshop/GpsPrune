package tim.prune.save;

import tim.prune.config.Config;
import tim.prune.data.DoubleRange;
import tim.prune.data.Track;
import tim.prune.data.TrackExtents;
import tim.prune.gui.map.MapSource;
import tim.prune.gui.map.MapTileManager;
import tim.prune.gui.map.TileConsumer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;


/**
 * Class to handle the sticking together (grouting) of map tiles
 * to create a single map image for the current track
 */
public class MapGrouter implements TileConsumer, Grouter
{
	/** The most recently produced image */
	private GroutedImage _lastGroutedImage = null;
	/** The most recently selected zoom level */
	private int _currentZoom = -1;
	/** The image currently being assembled */
	private BufferedImage _tempImage = null;
	/** Lock to control access to image from different threads */
	private Object _imageLock = new Object();

	/**
	 * Clear the last image, it's not needed any more
	 */
	public synchronized void clearMapImage()
	{
		if (_lastGroutedImage != null) {
			_lastGroutedImage.flush();
		}
		_lastGroutedImage = null;
	}

	/** Set the current zoom level */
	private synchronized void setCurrentZoom(int inZoom) {
		_currentZoom = inZoom;
	}

	/**
	 * Grout the required map tiles together according to the track's extent
	 * @param inTrack track object
	 * @param inMapSource map source to use (may have one or two layers)
	 * @param inZoom selected zoom level
	 * @param inConfig config object
	 * @return grouted image, or null if no image could be created
	 */
	public GroutedImage createMapImage(Track inTrack, MapSource inMapSource,
		int inZoom, Config inConfig)
	{
		return createMapImage(inTrack, inMapSource, inZoom, false, inConfig);
	}

	/**
	 * Grout the required map tiles together according to the track's extent
	 * @param inTrack track object
	 * @param inMapSource map source to use (may have one or two layers)
	 * @param inZoom selected zoom level
	 * @param inDownload true to download tiles, false (by default) to just pull from disk
	 * @param inConfig config object
	 * @return grouted image, or null if no image could be created
	 */
	public GroutedImage createMapImage(Track inTrack, MapSource inMapSource, int inZoom,
		boolean inDownload, Config inConfig)
	{
		setCurrentZoom(inZoom);
		// Get the extents of the track including a standard (10%) border around the data
		TrackExtents extents = new TrackExtents(inTrack);
		extents.applySquareBorder();
		DoubleRange xRange = extents.getXRange();
		DoubleRange yRange = extents.getYRange();

		// Work out which tiles are required
		final int zoomFactor = 1 << inZoom;
		final int minTileX = (int) (xRange.getMinimum() * zoomFactor);
		final int maxTileX = (int) (xRange.getMaximum() * zoomFactor);
		final int minTileY = (int) (yRange.getMinimum() * zoomFactor);
		final int maxTileY = (int) (yRange.getMaximum() * zoomFactor);

		// Work out how big the final image will be, create a BufferedImage
		final int pixCount = (int) (extents.getXRange().getRange() * zoomFactor * 256);
		if (pixCount < 2 || inZoom == 0) {
			return null;
		}
		createImage(pixCount);

		// Make a map tile manager to load (or download) the tiles
		MapTileManager tileManager = new MapTileManager(this);
		tileManager.setMapSource(inMapSource);
		tileManager.enableTileDownloading(inDownload);
		tileManager.setZoom(inZoom);

		int numTilesUsed = 0;
		int numTilesMissing = 0;

		// Loop over the layers
		for (int layer=0; layer < inMapSource.getNumLayers(); layer++)
		{
			// Work out where to start drawing the tiles on the image
			int xOffset = (int) ((minTileX - xRange.getMinimum() * zoomFactor) * 256);

			ArrayList<TileFetcher> fetchers = new ArrayList<>();
			// Loop over the tiles
			for (int x = minTileX; x <= maxTileX; x++)
			{
				int yOffset = (int) ((minTileY - yRange.getMinimum() * zoomFactor) * 256);
				for (int y = minTileY; y <= maxTileY; y++)
				{
					// If zoom is no longer what it was at the beginning, then we can skip the rest
					if (inZoom != _currentZoom) {
						break;
					}
					Image tile = tileManager.getTile(layer, x, y, true, inConfig);
					// If we're downloading tiles, wait until the tile isn't null
					int waitCount = 0;
					while (tile == null && inDownload && waitCount < 3)
					{
						try {Thread.sleep(300);} catch (InterruptedException e) {}
						tile = tileManager.getTile(layer, x, y, false, inConfig); // don't request another download
						waitCount++;
					}
					// See if there's a tile or not
					if (tile != null)
					{
						TileFetcher fetcher = new TileFetcher(this, tile, xOffset, yOffset);
						fetchers.add(fetcher);
						fetcher.go();
						numTilesUsed++;
					}
					else
					{
						// null tile, that means it's either not available or really slow to start downloading
						numTilesMissing++;
					}
					yOffset += 256;
				}
				xOffset += 256;
			}
			// Wait until this layer is finished before starting the next
			boolean finishedLayer = areAllFetchersDone(fetchers);
			for (int i=0; i<10 && !finishedLayer; i++)
			{
				try {Thread.sleep(100);} catch (InterruptedException e) {}
				finishedLayer = areAllFetchersDone(fetchers);
			}
		}
		// Get rid of the image if it's empty
		if (numTilesUsed == 0) {
			_tempImage = null;
		}
		// Store the xy limits in the GroutedImage to make it easier to draw on top
		GroutedImage result = new GroutedImage(_tempImage, numTilesUsed, numTilesMissing);
		result.setXRange(xRange);
		result.setYRange(yRange);
		_lastGroutedImage = result;
		return result;
	}

	/** Check if all the given fetchers are complete */
	private boolean areAllFetchersDone(ArrayList<TileFetcher> inFetchers)
	{
		for (TileFetcher fetcher : inFetchers)
		{
			if (!fetcher.isDone()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Get the grouted map image, using the previously-created one if available
	 * @param inTrack track object
	 * @param inMapSource map source to use (may have one or two layers)
	 * @param inZoom selected zoom level
	 * @param inConfig config object
	 * @return grouted image, or null if no image could be created
	 */
	public synchronized GroutedImage getMapImage(Track inTrack, MapSource inMapSource,
		int inZoom, Config inConfig)
	{
		if (_lastGroutedImage == null) {
			_lastGroutedImage = createMapImage(inTrack, inMapSource, inZoom, inConfig);
		}
		return _lastGroutedImage;
	}

	/** React to tiles being updated by the tile manager */
	public void tilesUpdated(boolean inIsOk)
	{
		// Doesn't need any action
	}

	/** React to cache problem */
	public void reportCacheFailure()
	{
		// Doesn't need any action
	}

	private void createImage(int inPixelCount)
	{
		synchronized (_imageLock)
		{
			_tempImage = new BufferedImage(inPixelCount, inPixelCount, BufferedImage.TYPE_INT_RGB);
			Graphics g = _tempImage.getGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, inPixelCount, inPixelCount);
		}
	}

	/** A tile fetcher has loaded a tile for us to paint at the given offset */
	public void tileReady(Image inImage, int inXoffset, int inYoffset)
	{
		synchronized(_imageLock)
		{
			if (_tempImage != null) {
				_tempImage.getGraphics().drawImage(inImage, inXoffset, inYoffset, null);
			}
		}
	}
}
