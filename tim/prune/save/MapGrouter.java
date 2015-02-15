package tim.prune.save;

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


/**
 * Class to handle the sticking together (grouting) of map tiles
 * to create a single map image for the current track
 */
public class MapGrouter implements TileConsumer
{
	/** The most recently produced image */
	private GroutedImage _lastGroutedImage = null;

	/**
	 * Clear the last image, it's not needed any more
	 */
	public void clearMapImage() {
		_lastGroutedImage = null;
	}

	/**
	 * Grout the required map tiles together according to the track's extent
	 * @param inTrack track object
	 * @param inMapSource map source to use (may have one or two layers)
	 * @param inZoom selected zoom level
	 * @return grouted image, or null if no image could be created
	 */
	public GroutedImage createMapImage(Track inTrack, MapSource inMapSource, int inZoom)
	{
		return createMapImage(inTrack, inMapSource, inZoom, false);
	}

	/**
	 * Grout the required map tiles together according to the track's extent
	 * @param inTrack track object
	 * @param inMapSource map source to use (may have one or two layers)
	 * @param inZoom selected zoom level
	 * @param inDownload true to download tiles, false (by default) to just pull from disk
	 * @return grouted image, or null if no image could be created
	 */
	public GroutedImage createMapImage(Track inTrack, MapSource inMapSource, int inZoom, boolean inDownload)
	{
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
		if (pixCount < 2 || inZoom == 0) {return null;}
		BufferedImage resultImage = new BufferedImage(pixCount, pixCount, BufferedImage.TYPE_INT_RGB);
		Graphics g = resultImage.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, pixCount, pixCount);
		// Work out where to start drawing the tiles on the image
		int xOffset = (int) ((minTileX - xRange.getMinimum() * zoomFactor) * 256);

		// Make a map tile manager to load (or download) the tiles
		MapTileManager tileManager = new MapTileManager(this);
		tileManager.setMapSource(inMapSource);
		tileManager.enableTileDownloading(inDownload);
		tileManager.setReturnIncompleteImages();
		tileManager.setZoom(inZoom);

		int numTilesUsed = 0;
		int numTilesMissing = 0;

		// Loop over the tiles
		for (int x = minTileX; x <= maxTileX; x++)
		{
			int yOffset = (int) ((minTileY - yRange.getMinimum() * zoomFactor) * 256);
			for (int y = minTileY; y <= maxTileY; y++)
			{
				for (int layer=0; layer < inMapSource.getNumLayers(); layer++)
				{
					Image tile = tileManager.getTile(layer, x, y, true);
					// If we're downloading tiles, wait until the tile isn't null
					int waitCount = 0;
					while (tile == null && inDownload && waitCount < 3)
					{
						// System.out.println("wait " + waitCount + " for tile to be not null");
						try {Thread.sleep(250);} catch (InterruptedException e) {}
						tile = tileManager.getTile(layer, x, y, false); // don't request another download
						waitCount++;
					}
					// See if there's a tile or not
					if (tile != null)
					{
						// Wait until tile is available (loaded asynchronously)
						while (tile.getWidth(null) < 0 && !inDownload)
						{
							// System.out.println("Wait for tile width");
							try {
								Thread.sleep(inDownload ? 500 : 100);
							}
							catch (InterruptedException ie) {}
						}
						// work out where to copy it to, paint it
						// System.out.println("Painting tile " + x + "," + y + " at " + xOffset + "," + yOffset);
						numTilesUsed++;
						g.drawImage(tile, xOffset, yOffset, null);
					}
					else
					{
						// null tile, that means it's either not available or really slow to start downloading
						numTilesMissing++;
					}
				}
				yOffset += 256;
			}
			xOffset += 256;
		}
		// Get rid of the image if it's empty
		if (numTilesUsed == 0) {
			resultImage = null;
		}
		// Store the xy limits in the GroutedImage to make it easier to draw on top
		GroutedImage result = new GroutedImage(resultImage, numTilesUsed, numTilesMissing);
		result.setXRange(xRange);
		result.setYRange(yRange);
		return result;
	}

	/**
	 * Get the grouted map image, using the previously-created one if available
	 * @param inTrack track object
	 * @param inMapSource map source to use (may have one or two layers)
	 * @param inZoom selected zoom level
	 * @return grouted image, or null if no image could be created
	 */
	public synchronized GroutedImage getMapImage(Track inTrack, MapSource inMapSource, int inZoom)
	{
		if (_lastGroutedImage == null) {
			_lastGroutedImage = createMapImage(inTrack, inMapSource, inZoom);
		}
		return _lastGroutedImage;
	}

	/**
	 * @param inTrack track object
	 * @param inZoom selected zoom level
	 * @return true if the image size is acceptable
	 */
	public static boolean isZoomLevelOk(Track inTrack, int inZoom)
	{
		// Get the extents of the track including a standard (10%) border around the data
		TrackExtents extents = new TrackExtents(inTrack);
		extents.applySquareBorder();

		// Work out how big the final image will be
		final int zoomFactor = 1 << inZoom;
		final int pixCount = (int) (extents.getXRange().getRange() * zoomFactor * 256);
		return pixCount > 2 && pixCount < 4000;
	}

	/** React to tiles being updated by the tile manager */
	public void tilesUpdated(boolean inIsOk)
	{
		// Doesn't need any action
	}
}
