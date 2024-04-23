package tim.prune.function.cache;

import java.io.File;
import java.util.ArrayList;

import tim.prune.fileutils.FileList;
import tim.prune.gui.map.MapSource;
import tim.prune.gui.map.MapSourceLibrary;

/**
 * Class to obtain and hold information about the current
 * tile cache including its subdirectories
 */
public class TileCacheModel
{
	/** Cache directory */
	private final File _cacheDir;
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
		else {
			_cacheDir = null;
		}
	}

	/**
	 * Build the tilesets by searching recursively
	 */
	public void buildTileSets()
	{
		if (_cacheDir == null) return;

		_tileSets = new ArrayList<>();
		// go through subdirectories, if any
		for (File subdir : FileList.filesIn(_cacheDir))
		{
			if (subdir != null && subdir.isDirectory() && subdir.exists() && subdir.canRead()
				&& !_cancelled)
			{
				getTileSets(subdir, null, _tileSets);
			}
		}
		// Loop over found tile sets and create summary rowinfo
		_summaryRow = new RowInfo();
		for (TileSet ts : _tileSets) {
			_summaryRow.addRow(ts.getRowInfo());
		}
	}

	/**
	 * Get all the tilesets from the given directory
	 * @param inDir directory to search
	 * @param inTsList arraylist to hold the results
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
			for (File subdir : FileList.filesIn(inDir))
			{
				if (subdir != null && subdir.exists() && subdir.isDirectory()
					&& subdir.canRead())
				{
					getTileSets(subdir, wholePath, inTsList);
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
		if (inName == null || inName.equals("")) {
			return null;
		}
		StringBuilder usedBy = new StringBuilder();
		for (int i=0; i<MapSourceLibrary.getNumSources(); i++)
		{
			MapSource ms = MapSourceLibrary.getSource(i);
			for (int l=0; l<2; l++)
			{
				String msdir = ms.getSiteName(l);
				if (msdir != null && msdir.equals(inName))
				{
					if (usedBy.length() > 0) {
						usedBy.append(", ");
					}
					usedBy.append(ms.getName());
				}
			}
		}
		return usedBy.toString();
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
			for (File subdir : FileList.filesIn(inDir))
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
