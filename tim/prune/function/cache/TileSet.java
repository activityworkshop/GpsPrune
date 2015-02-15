package tim.prune.function.cache;

import java.io.File;
import java.util.ArrayList;


/**
 * Class to hold information about a single tile set
 * within the overall Tile Cache.
 */
public class TileSet
{
	/** Summary row info for whole tileset */
	private RowInfo _rowInfo = null;
	/** Path relative to mapcache root */
	private String _path = null;
	/** Comma-separated list of configs using this tileset */
	private String _usedBy = null;
	/** List of infos for each zoom level */
	private ArrayList<RowInfo> _zoomLevels = null;


	/**
	 * Constructor
	 * @param inDir directory of tileset
	 * @param inPath String describing relative path from cache root
	 * @param inUsedBy String describing which configs use this Tileset
	 */
	public TileSet(File inDir, String inPath, String inUsedBy)
	{
		_path = inPath;
		_usedBy = inUsedBy;
		_rowInfo = new RowInfo();
		_zoomLevels = new ArrayList<RowInfo>();
		// Go through zoom directories and construct row info objects
		if (inDir != null && inDir.exists() && inDir.isDirectory() && inDir.canRead())
		{
			for (File subdir : inDir.listFiles())
			{
				if (subdir != null && subdir.exists() && subdir.isDirectory()
					&& subdir.canRead() && isNumeric(subdir.getName()))
				{
					RowInfo row = makeRowInfo(subdir);
					_zoomLevels.add(row);
					_rowInfo.addRow(row);
				}
			}
		}
	}

	/**
	 * Check if a directory name is numeric
	 * @param inName name of directory
	 * @return true if it only contains characters 0-9
	 */
	public static boolean isNumeric(String inName)
	{
		if (inName == null || inName.equals("")) return false;
		for (int i=0; i<inName.length(); i++)
		{
			char a = inName.charAt(i);
			if (a < '0' || a > '9') return false;
		}
		return true;
	}

	/**
	 * Make a RowInfo object from the given directory
	 * @param inDir directory for a single zoom level
	 * @return RowInfo object describing files and size
	 */
	private static RowInfo makeRowInfo(File inDir)
	{
		RowInfo row = new RowInfo();
		row.setZoom(Integer.parseInt(inDir.getName()));
		for (File subdir : inDir.listFiles())
		{
			if (subdir != null && subdir.exists() && subdir.isDirectory()
				&& subdir.canRead() && isNumeric(subdir.getName()))
			{
				// Found a directory of images (finally!)
				for (File f : subdir.listFiles())
				{
					if (f != null && f.exists() && f.isFile() && f.canRead())
					{
						final String filename = f.getName();
						int dotpos = filename.lastIndexOf('.');
						if (dotpos > 0 && isNumeric(filename.substring(0, dotpos))) {
							row.addTile(f.length());
						}
					}
				}
			}
		}
		return row;
	}

	/**
	 * @return row info object
	 */
	public RowInfo getRowInfo() {
		return _rowInfo;
	}

	/** @return relative path to tileset */
	public String getPath() {
		return _path;
	}

	/** @return users of tileset */
	public String getUsedBy() {
		return _usedBy;
	}
}
