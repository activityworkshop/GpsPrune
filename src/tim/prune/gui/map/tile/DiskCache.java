package tim.prune.gui.map.tile;

import tim.prune.gui.map.CacheFailure;
import tim.prune.gui.map.MapTile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DiskCache
{
	private String _basePath = null;
	/** Flag to remember whether we have already tried to create the base path */
	private boolean _triedToCreateBasePath = false;
	/** Time limit to cache images for */
	private static final long CACHE_TIME_LIMIT = 20 * 24 * 60 * 60 * 1000; // 20 days in ms

	/**
	 * @param inBasePath path to root of the tile cache
	 */
	public void setBasePath(String inBasePath) {
		_basePath = inBasePath;
	}

	/**
	 * Get a tile from the disk cache
	 * @param inTileDef tile definition
	 * @return tile object if possible, otherwise null
	 */
	public MapTile getTile(TileDef inTileDef)
	{
		String tilePath = inTileDef.getFilePath();
		if (_basePath == null || tilePath == null) {
			return null;
		}
		// System.out.println("Disk Cache asked for tile: " + inTilePath);
		File tileFile = new File(_basePath, tilePath);
		if (tileFile.exists() && tileFile.canRead() && tileFile.length() > 0)
		{
			long fileStamp = tileFile.lastModified();
			boolean isExpired = ((System.currentTimeMillis()-fileStamp) > CACHE_TIME_LIMIT);
			// System.out.println("Disk Cache found tile: " + tilePath + (isExpired ? " (expired)" : ""));
			try
			{
				Image image = Toolkit.getDefaultToolkit().createImage(tileFile.getAbsolutePath());
				image.getWidth(null);
				return new MapTile(image, isExpired);
			}
			catch (Exception e) {
				System.err.println("error creating image: " + e.getClass().getName() + ": " + e.getMessage());
			}
		}
		return null;
	}

	/**
	 * Save the bytes of a downloaded tile to disk
	 */
	public void saveTileBytes(TileBytes inBytes, TileDef inDefinition) throws CacheFailure
	{
		if (_basePath == null) {return;} // no cache specified
		File tileFile = new File(_basePath, inDefinition.getFilePath());
		if (directoryOk(tileFile))
		{
			try (FileOutputStream fos = new FileOutputStream(tileFile)) {
				fos.write(inBytes.data);
			} catch (IOException ioe) {
				System.out.println("Failed to write to: " + inDefinition.getFilePath());
				ioe.printStackTrace();
				throw new CacheFailure();
			}
		}
	}

	/**
	 * Check the cache directories exist, and create them if necessary
	 * @param tileFile file which we will want to write
	 * @return true if the specified file can be written
	 * @throws CacheFailure if creation not possible (eg read-only)
	 */
	private boolean directoryOk(File tileFile) throws CacheFailure
	{
		if (!checkBasePath()) {
			throw new CacheFailure();
		}
		File dir = tileFile.getParentFile();
		return ((dir.exists() || dir.mkdirs()) && dir.canWrite());
	}


	/**
	 * Save a sliced Image from a downloaded tile to disk
	 * @param inImage rendered image to be saved
	 * @param inDefinition tile definition which determines path
	 */
	public void saveTileImage(RenderedImage inImage, TileDef inDefinition) throws CacheFailure
	{
		if (_basePath == null) {return;} // no cache specified
		if (!inDefinition._mapSource.getFileExtension(inDefinition._layerIdx).equals("png")) {
			return; // can only write png tiles
		}
		File tileFile = new File(_basePath, inDefinition.getFilePath());
		if (directoryOk(tileFile))
		{
			try {
				if (!ImageIO.write(inImage, "png", tileFile))
				{
					System.err.println("Failed to write file: " + tileFile.getAbsolutePath());
					throw new CacheFailure();
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new CacheFailure();
			}
		}
	}

	/**
	 * Check the given base path, and try (once) to create it if necessary
	 * @return true if base path can be written to
	 */
	private boolean checkBasePath()
	{
		File basePath = new File(_basePath);
		if (!basePath.exists() && !_triedToCreateBasePath)
		{
			_triedToCreateBasePath = true;
			System.out.println("Base path '" + basePath.getAbsolutePath() + "' does not exist, trying to create");
			return basePath.mkdirs();
		}
		return basePath.exists() && basePath.isDirectory() && basePath.canWrite();
	}
}
