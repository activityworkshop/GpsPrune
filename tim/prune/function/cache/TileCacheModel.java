package tim.prune.function.cache;

import java.io.File;
import java.util.ArrayList;

import tim.prune.gui.map.MapSource;
import tim.prune.gui.map.MapSourceLibrary;

/**
 * Class to obtain and hold information about the current
 * tile cache including its subdirectories
 */
public class TileCacheModel
{
	/** Cache directory */
	private File _cacheDir = null;
	/** Array of tilesets */
	private ArrayList<TileSet> _tileSets = null;
	/** Summary information */
	private RowInfo _summaryRow = null;
	/** Cancelled flag */
	private boolean _cancelled = false;


	/**
	 * Constructor
	 * @param inDir start directory
	 */
	public TileCacheModel(File inDir)
	{
		if (inDir != null && inDir.exists() && inDir.isDirectory() && inDir.canRead()) {
			_cacheDir = inDir;
		}
		_cancelled = false;
	}

	/**
	 * Build the tilesets by searching recursively
	 */
	public void buildTileSets()
	{
		if (_cacheDir == null) return;

		_tileSets = new ArrayList<TileSet>();
		// go through subdirectories, if any
		File[] subdirs = _cacheDir.listFiles();
		for (File subdir : subdirs)
		{
			if (subdir != null && subdir.isDirectory() && subdir.exists() && subdir.canRead()
				&& !_cancelled)
			{
				getTileSets(subdir, null, _tileSets);
			}
		}
		// Loop over found tile sets and create summary rowinfo
		_summaryRow = new RowInfo();
		for (TileSet ts : _tileSets)
		{
			_summaryRow.addRow(ts.getRowInfo());
		}
	}

	/**
	 * Get all the tilesets from the given directory
	 * @param inDir directory to search
	 * @return array of TileSet objects
	 */
	private static void getTileSets(File inDir, String inParentPath, ArrayList<TileSet> inTsList)
	{
		final String wholePath = (inParentPath == null ? "" : inParentPath)
			+ inDir.getName() + File.separator;
		// See if any configured backgrounds use this directory
		// or if the directories match OSM structure
		String usedByDesc = matchConfig(wholePath);
		boolean tsFound = false;
		if (usedByDesc != null || looksLikeCacheDir(inDir))
		{
			TileSet ts = new TileSet(inDir, wholePath, usedByDesc);
			if (usedByDesc != null || ts.getRowInfo().getNumTiles() > 0)
			{
				tsFound = true;
				inTsList.add(ts);
			}
		}
		// If a tileset wasn't found, look through subdirectories
		if (!tsFound)
		{
			// Go through subdirectories and look at each of them too
			File[] subdirs = inDir.listFiles();
			if (subdirs != null) {
				for (File subdir : subdirs)
				{
					if (subdir != null && subdir.exists() && subdir.isDirectory()
						&& subdir.canRead())
					{
						getTileSets(subdir, wholePath, inTsList);
					}
				}
			}
		}
	}

	/**
	 * Match the given directory name to find the configs which use it
	 * @param inName name of directory to match
	 * @return null if not used, otherwise comma-separated list of background names
	 */
	private static String matchConfig(String inName)
	{
		if (inName == null || inName.equals(""))
			return null;
		String usedBy = null;
		for (int i=0; i<MapSourceLibrary.getNumSources(); i++)
		{
			MapSource ms = MapSourceLibrary.getSource(i);
			for (int l=0; l<2; l++)
			{
				String msdir = ms.getSiteName(l);
				if (msdir != null && msdir.equals(inName))
				{
					if (usedBy == null)
						usedBy = ms.getName();
					else
						usedBy = usedBy + ", " + ms.getName();
				}
			}
		}
		return usedBy;
	}

	/**
	 * @param inDir directory to test
	 * @return true if the subdirectories meet the normal osm layout
	 */
	private static boolean looksLikeCacheDir(File inDir)
	{
		// look for at least one numeric directory, nothing else
		boolean numFound = false;
		if (inDir != null && inDir.exists() && inDir.isDirectory() && inDir.canRead())
		{
			for (File subdir : inDir.listFiles())
			{
				// Only consider readable things which exist
				if (subdir != null && subdir.exists() && subdir.canRead())
				{
					// subdirectories should have numeric names (for the zoom levels)
					if (subdir.isDirectory() && TileSet.isNumeric(subdir.getName())
						&& subdir.getName().length() < 3)
					{
						numFound = true;
					}
					else return false; // either a file or non-numeric directory
				}
			}
		}
		return numFound;
	}

	/**
	 * @return cache directory
	 */
	public File getCacheDir() {
		return _cacheDir;
	}

	/**
	 * @return number of tile sets
	 */
	public int getNumTileSets()
	{
		if (_tileSets == null) return 0;
		return _tileSets.size();
	}

	/**
	 * @return the total number of tile images found
	 */
	public int getTotalTiles()
	{
		return _summaryRow.getNumTiles();
	}

	/**
	 * @return the total number of bytes taken up with tile images
	 */
	public long getTotalBytes()
	{
		return _summaryRow.getTotalSize();
	}

	/**
	 * @param inIndex index of tileset
	 * @return requested tileset
	 */
	public TileSet getTileSet(int inIndex)
	{
		if (inIndex >= 0 && inIndex < getNumTileSets()) {
			return _tileSets.get(inIndex);
		}
		return null;
	}

	/**
	 * Cancel the search
	 */
	public void cancel() {
		_cancelled = true;
	}

	/**
	 * @return true if search was cancelled
	 */
	public boolean isAborted() {
		return _cancelled;
	}
}
