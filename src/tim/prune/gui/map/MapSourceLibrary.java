package tim.prune.gui.map;

import java.util.ArrayList;

import tim.prune.config.Config;

/**
 * Class to hold a library for all the map sources
 * and provide access to each one
 */
public abstract class MapSourceLibrary
{
	/** list of map sources */
	private static ArrayList<MapSource> _sourceList = null;
	/** Number of fixed sources */
	private static int _numFixedSources = 0;

	// Static block to initialise source list
	static
	{
		_sourceList = new ArrayList<MapSource>();
		addFixedSources();
		_numFixedSources = _sourceList.size();
		addConfigSources();
	}

	/** Private constructor to block instantiation */
	private MapSourceLibrary() {}


	/** @return number of fixed sources which shouldn't be deleted */
	public static int getNumFixedSources() {
		return _numFixedSources;
	}

	/**
	 * Initialise source list by adding bare minimum
	 */
	private static void addFixedSources()
	{
		_sourceList.add(new OsmMapSource("Mapnik", "https://[abc].tile.openstreetmap.org/"));
		_sourceList.add(new OsmMapSource("Cycling Trails", "https://[abc].tile.openstreetmap.org/", "png",
			"https://tile.waymarkedtrails.org/cycling/", "png", 18));
		_sourceList.add(new OsmMapSource("Reitkarte", "http://topo[234].wanderreitkarte.de/topo/"));
		_sourceList.add(new MffMapSource("Mapsforfree", "https://maps-for-free.com/layer/relief/", "jpg",
			"https://maps-for-free.com/layer/water/", "gif", 11));
		_sourceList.add(new OsmMapSource("Hikebikemap", "https://tiles.wmflabs.org/hikebike/",
			"https://tiles.wmflabs.org/hillshading/", 18));
		_sourceList.add(new OsmMapSource("OpenSeaMap", "http://tile.openstreetmap.org/",
			"http://tiles.openseamap.org/seamark/", 18));
	}

	/**
	 * Add custom sources from Config to the library
	 */
	private static void addConfigSources()
	{
		String configString = Config.getConfigString(Config.KEY_MAPSOURCE_LIST);
		if (configString != null && configString.length() > 10)
		{
			// Loop over sources in string, separated by vertical bars
			int splitPos = configString.indexOf('|');
			while (splitPos > 0)
			{
				String sourceString = configString.substring(0, splitPos);
				MapSource source = OsmMapSource.fromConfig(sourceString);
				if (source != null) {
					_sourceList.add(source);
				}
				configString = configString.substring(splitPos+1);
				splitPos = configString.indexOf('|');
			}
		}
	}

	/**
	 * @return current number of sources
	 */
	public static int getNumSources() {
		return _sourceList.size();
	}

	/**
	 * Add the given MapSource to the list (at the end)
	 * @param inSource MapSource object
	 */
	public static void addSource(MapSource inSource) {
		// Check whether source is already there?  Check whether valid?
		_sourceList.add(inSource);
	}

	/**
	 * Edit the given MapSource object by replacing with a new one
	 * @param inOriginal existing MapSource object
	 * @param inNewSource new MapSource object
	 */
	public static void editSource(MapSource inOriginal, MapSource inNewSource)
	{
		// Check whether original source is still there
		int origPos = _sourceList.indexOf(inOriginal);
		if (origPos < 0) {
			addSource(inNewSource);
		}
		else {
			_sourceList.set(origPos, inNewSource);
		}
	}

	/**
	 * @param inIndex source index number
	 * @return corresponding map source object
	 */
	public static MapSource getSource(int inIndex)
	{
		// Check whether within range
		if (inIndex < 0 || inIndex >= _sourceList.size()) {return null;}
		return _sourceList.get(inIndex);
	}

	/**
	 * Delete the specified source
	 * @param inIndex index of source to delete
	 */
	public static void deleteSource(int inIndex)
	{
		if (inIndex >= _numFixedSources) {
			_sourceList.remove(inIndex);
		}
	}

	/**
	 * Check whether the given name already exists in the library (case-insensitive)
	 * @param inName name to check
	 * @return true if already exists, false otherwise
	 */
	public static boolean hasSourceName(String inName)
	{
		if (inName == null) {return false;}
		String checkName = inName.toLowerCase().trim();
		for (int i=0; i<getNumSources(); i++)
		{
			String name = getSource(i).getName().toLowerCase();
			if (name.equals(checkName)) {return true;}
		}
		return false;
	}

	/**
	 * @return String containing all custom-added sources as a |-separated list
	 */
	public static String getConfigString()
	{
		StringBuilder builder = new StringBuilder();
		for (int i=getNumFixedSources(); i<getNumSources(); i++) {
			builder.append(getSource(i).getConfigString()).append('|');
		}
		return builder.toString();
	}
}
