package tim.prune.config;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import tim.prune.data.RecentFileList;
import tim.prune.data.UnitSet;
import tim.prune.data.UnitSetLibrary;


/**
 * Abstract class to hold application-wide configuration
 */
public abstract class Config
{
	/** File from which Config was loaded */
	private static File _configFile = null;

	/** Hashtable containing all config values */
	private static Properties _configValues = null;
	/** Colour scheme object is also part of config */
	private static ColourScheme _colourScheme = new ColourScheme();
	/** Recently-used file list */
	private static RecentFileList _recentFiles = new RecentFileList();
	/** Current unit set */
	private static UnitSet _unitSet = UnitSetLibrary.getUnitSet(null);

	/** Default config file */
	public static final File DEFAULT_CONFIG_FILE = new File(".pruneconfig");
	public static final File HOME_CONFIG_FILE = new File(System.getProperty("user.home"), ".pruneconfig");

	/** Key for track directory */
	public static final String KEY_TRACK_DIR = "prune.trackdirectory";
	/** Key for photo directory */
	public static final String KEY_PHOTO_DIR = "prune.photodirectory";
	/** Key for language code */
	public static final String KEY_LANGUAGE_CODE = "prune.languagecode";
	/** Key for language file */
	public static final String KEY_LANGUAGE_FILE = "prune.languagefile";
	/** Key for GPS device */
	public static final String KEY_GPS_DEVICE = "prune.gpsdevice";
	/** Key for GPS format */
	public static final String KEY_GPS_FORMAT = "prune.gpsformat";
	/** Key for GPSBabel filter string */
	public static final String KEY_GPSBABEL_FILTER = "prune.gpsbabelfilter";
	/** Key for Povray font */
	public static final String KEY_POVRAY_FONT = "prune.povrayfont";
	/** Key for the selected unit set */
	public static final String KEY_UNITSET_KEY  = "prune.unitsetkey";
	/** Key for index of map source */
	public static final String KEY_MAPSOURCE_INDEX = "prune.mapsource";
	/** Key for number of fixed map sources */
	public static final String KEY_NUM_FIXED_MAPS = "prune.numfixedmapsources";
	/** Key for String containing custom map sources */
	public static final String KEY_MAPSOURCE_LIST = "prune.mapsourcelist";
	/** Key for show map flag */
	public static final String KEY_SHOW_MAP = "prune.showmap";
	/** Key for path to disk cache */
	public static final String KEY_DISK_CACHE = "prune.diskcache";
	/** Key for working online flag */
	public static final String KEY_ONLINE_MODE = "prune.onlinemode";
	/** Key for width of thumbnails in kmz */
	public static final String KEY_KMZ_IMAGE_SIZE = "prune.kmzimagewidth";
	/** Key for gpsbabel path */
	public static final String KEY_GPSBABEL_PATH = "prune.gpsbabelpath";
	/** Key for gnuplot path */
	public static final String KEY_GNUPLOT_PATH = "prune.gnuplotpath";
	/** Key for exiftool path */
	public static final String KEY_EXIFTOOL_PATH = "prune.exiftoolpath";
	/** Key for colour scheme */
	public static final String KEY_COLOUR_SCHEME = "prune.colourscheme";
	/** Key for line width used for drawing */
	public static final String KEY_LINE_WIDTH = "prune.linewidth";
	/** Key for kml track colour */
	public static final String KEY_KML_TRACK_COLOUR = "prune.kmltrackcolour";
	/** Key for autosaving settings */
	public static final String KEY_AUTOSAVE_SETTINGS = "prune.autosavesettings";
	/** Key for recently used files */
	public static final String KEY_RECENT_FILES = "prune.recentfiles";
	/** Key for estimation parameters */
	public static final String KEY_ESTIMATION_PARAMS = "prune.estimationparams";


	/** Initialise the default properties */
	static
	{
		_configValues = getDefaultProperties();
	}

	/**
	 * Load the default configuration file
	 */
	public static void loadDefaultFile()
	{
		if (DEFAULT_CONFIG_FILE.exists())
		{
			try {
				loadFile(DEFAULT_CONFIG_FILE);
				return;
			}
			catch (ConfigException ce) {} // ignore
		}
		if (HOME_CONFIG_FILE.exists())
		{
			try {
				loadFile(HOME_CONFIG_FILE);
			}
			catch (ConfigException ce) {} // ignore
		}
	}


	/**
	 * Load configuration from file
	 * @param inFile file to load
	 * @throws ConfigException if specified file couldn't be read
	 */
	public static void loadFile(File inFile) throws ConfigException
	{
		// Start with default properties
		Properties props = getDefaultProperties();
		// Try to load the file into a properties object
		boolean loadFailed = false;
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream(inFile);
			props.load(fis);
		}
		catch (Exception e) {
			loadFailed = true;
		}
		finally {
			if (fis != null) try {
				fis.close();
			}
			catch (Exception e) {}
		}
		// Save all properties from file
		_configValues.putAll(props);
		_colourScheme.loadFromHex(_configValues.getProperty(KEY_COLOUR_SCHEME));
		_recentFiles = new RecentFileList(_configValues.getProperty(KEY_RECENT_FILES));
		_unitSet = UnitSetLibrary.getUnitSet(_configValues.getProperty(KEY_UNITSET_KEY));

		if (loadFailed) {
			throw new ConfigException();
		}
		// Store location of successfully loaded config file
		_configFile = inFile;
	}

	/**
	 * @return Properties object containing default values
	 */
	private static Properties getDefaultProperties()
	{
		Properties props = new Properties();
		// Fill in defaults
		props.put(KEY_GPS_DEVICE, "usb:");
		props.put(KEY_GPS_FORMAT, "garmin");
		props.put(KEY_POVRAY_FONT, "crystal.ttf"); // alternative: DejaVuSans-Bold.ttf
		props.put(KEY_SHOW_MAP, "0"); // hide by default
		props.put(KEY_EXIFTOOL_PATH, "exiftool");
		props.put(KEY_GNUPLOT_PATH, "gnuplot");
		props.put(KEY_GPSBABEL_PATH, "gpsbabel");
		props.put(KEY_KMZ_IMAGE_SIZE, "240");
		props.put(KEY_AUTOSAVE_SETTINGS, "0"); // autosave false by default
		props.put(KEY_UNITSET_KEY, "unitset.kilometres"); // metric by default
		return props;
	}

	/**
	 * @param inString String to parse
	 * @return int value of String, or 0 if unparseable
	 */
	private static int parseInt(String inString)
	{
		int val = 0;
		try {
			val = Integer.parseInt(inString);
		}
		catch (Exception e) {} // ignore, value stays zero
		return val;
	}

	/** @return File from which config was loaded (or null) */
	public static File getConfigFile()
	{
		return _configFile;
	}

	/**
	 * @return config Properties object to allow all config values to be saved
	 */
	public static Properties getAllConfig()
	{
		// Update recently-used files
		_configValues.setProperty(KEY_RECENT_FILES, _recentFiles.getConfigString());
		return _configValues;
	}

	/**
	 * @return the current colour scheme
	 */
	public static ColourScheme getColourScheme()
	{
		return _colourScheme;
	}

	/**
	 * @return list of recently used files
	 */
	public static RecentFileList getRecentFileList()
	{
		return _recentFiles;
	}

	/**
	 * Store the given configuration setting
	 * @param inKey key (from constants)
	 * @param inValue value as string
	 */
	public static void setConfigString(String inKey, String inValue)
	{
		if (inValue == null || inValue.equals("")) {
			_configValues.remove(inKey);
		}
		else {
			_configValues.put(inKey, inValue);
		}
	}

	/**
	 * Store the given configuration setting
	 * @param inKey key (from constants)
	 * @param inValue value as boolean
	 */
	public static void setConfigBoolean(String inKey, boolean inValue)
	{
		if (inKey != null && !inKey.equals(""))
		{
			_configValues.put(inKey, (inValue?"1":"0"));
		}
	}

	/**
	 * Store the given configuration setting
	 * @param inKey key (from constants)
	 * @param inValue value as int
	 */
	public static void setConfigInt(String inKey, int inValue)
	{
		if (inKey != null && !inKey.equals(""))
		{
			_configValues.put(inKey, "" + inValue);
		}
	}

	/**
	 * Get the given configuration setting as a String
	 * @param inKey key
	 * @return configuration setting as a String
	 */
	public static String getConfigString(String inKey)
	{
		return _configValues.getProperty(inKey);
	}

	/**
	 * Get the given configuration setting as a boolean
	 * @param inKey key
	 * @return configuration setting as a boolean (default to true)
	 */
	public static boolean getConfigBoolean(String inKey)
	{
		String val = _configValues.getProperty(inKey);
		return (val == null || val.equals("1"));
	}

	/**
	 * Get the given configuration setting as an int
	 * @param inKey key
	 * @return configuration setting as an int
	 */
	public static int getConfigInt(String inKey)
	{
		return parseInt(_configValues.getProperty(inKey));
	}

	/**
	 * Check whether the given key corresponds to a boolean property
	 * @param inKey key to check
	 * @return true if corresponding property is boolean
	 */
	public static boolean isKeyBoolean(String inKey)
	{
		// Only one boolean key so far (after metric flag was removed)
		return inKey != null && (
			inKey.equals(KEY_SHOW_MAP));
	}

	/**
	 * Update the colour scheme property from the current settings
	 */
	public static void updateColourScheme()
	{
		setConfigString(KEY_COLOUR_SCHEME, _colourScheme.toString());
	}

	/**
	 * @return the current unit set
	 */
	public static UnitSet getUnitSet() {
		return _unitSet;
	}

	public static void selectUnitSet(int inIndex)
	{
		_unitSet = UnitSetLibrary.getUnitSet(inIndex);
		// Set name of set in config
		setConfigString(KEY_UNITSET_KEY, _unitSet.getNameKey());
	}
}
