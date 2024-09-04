package tim.prune.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import tim.prune.data.RecentFileList;
import tim.prune.data.UnitSet;
import tim.prune.data.UnitSetLibrary;
import tim.prune.gui.colour.ColourerFactory;
import tim.prune.gui.colour.PointColourer;


/**
 * Class to hold application-wide configuration
 */
public class Config
{
	/** File from which Config was loaded */
	private File _configFile = null;

	/** key/value pairs containing all config values */
	private Properties _configValues = null;
	/** Colour scheme object is also part of config */
	private final ColourScheme _colourScheme = new ColourScheme();
	/** Point colourer object, if any */
	private PointColourer _pointColourer = null;
	/** Recently-used file list */
	private RecentFileList _recentFiles = new RecentFileList();
	/** Current unit set */
	private UnitSet _unitSet = UnitSetLibrary.getUnitSet(null);

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
	/** Key for GPSBabel import file format */
	public static final String KEY_IMPORT_FILE_FORMAT = "prune.lastimportfileformat";
	/** Key for Povray font */
	public static final String KEY_POVRAY_FONT = "prune.povrayfont";
	/** Key for the selected unit set */
	public static final String KEY_UNITSET_KEY  = "prune.unitsetkey";
	/** Key for the selected coordinate display format */
	public static final String KEY_COORD_DISPLAY_FORMAT  = "prune.coorddisplay";
	/** Key for index of map source */
	public static final String KEY_MAPSOURCE_INDEX = "prune.mapsource";
	/** Key for number of fixed map sources */
	public static final String KEY_NUM_FIXED_MAPS = "prune.numfixedmapsources";
	/** Key for String containing custom map sources */
	public static final String KEY_MAPSOURCE_LIST = "prune.mapsourcelist";
	/** Key for show map flag */
	public static final String KEY_SHOW_MAP = "prune.showmap";
	/** Key for window position */
	public static final String KEY_WINDOW_BOUNDS = "prune.windowbounds";
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
	/** Key for point colourer */
	public static final String KEY_POINT_COLOURER = "prune.pointcolourer";
	/** Key for line width used for drawing */
	public static final String KEY_LINE_WIDTH = "prune.linewidth";
	/** Key for whether to use antialiasing or not */
	public static final String KEY_ANTIALIAS = "prune.antialias";
	/** Key for whether to allow OS to scale maps or not */
	public static final String KEY_OSSCALING = "prune.osscaling";
	/** Key for kml track colour */
	public static final String KEY_KML_TRACK_COLOUR = "prune.kmltrackcolour";
	/** Key for window style (name of look-and-feel) */
	public static final String KEY_WINDOW_STYLE = "prune.windowstyle";
	/** Key for autosaving settings */
	public static final String KEY_AUTOSAVE_SETTINGS = "prune.autosavesettings";
	/** Key for recently used files */
	public static final String KEY_RECENT_FILES = "prune.recentfiles";
	/** Key for estimation parameters */
	public static final String KEY_ESTIMATION_PARAMS = "prune.estimationparams";
	/** Key for 3D exaggeration factor */
	public static final String KEY_HEIGHT_EXAGGERATION = "prune.heightexaggeration";
	/** Key for terrain grid size */
	public static final String KEY_TERRAIN_GRID_SIZE = "prune.terraingridsize";
	/** Key for altitude tolerance */
	public static final String KEY_ALTITUDE_TOLERANCE = "prune.altitudetolerance";
	/** Key for waypoint icons to use */
	public static final String KEY_WAYPOINT_ICONS = "prune.waypointicons";
	/** Size of waypoint icons to use */
	public static final String KEY_WAYPOINT_ICON_SIZE = "prune.waypointiconsize";
	/** Salt to use for waypoint colouring */
	public static final String KEY_WPICON_SALT = "prune.waypointsalt";
	/** Id of selected timezone */
	public static final String KEY_TIMEZONE_ID = "prune.timezoneid";
	/** Last used latlon range */
	public static final String KEY_LATLON_RANGE = "prune.latlonrange";
	/** Username/password to the Earthdata server for SRTM 1-arcsecond tiles */
	public static final String KEY_EARTHDATA_AUTH = "prune.earthdataauth";
	/** Settings for track compression */
	public static final String KEY_COMPRESSION_SETTINGS = "prune.compression";
	/** true for double-sized icons */
	public static final String KEY_ICONS_DOUBLE_SIZE = "prune.iconsdoublesize";
	/** true if user has already been asked about config migration */
	public static final String KEY_ASKED_ABOUT_CONFIG_MIGRATION = "prune.configmigrationasked";


	/** Initialise the default properties */
	public Config()	{
		_configValues = getDefaultProperties();
	}

	/**
	 * Load configuration from file
	 * @param inFile file to load
	 * @return true if successfully loaded
	 */
	public boolean loadFile(File inFile)
	{
		// Start with default properties
		Properties props = getDefaultProperties();
		// Try to load the file into a properties object
		boolean success = false;
		try (FileInputStream fis = new FileInputStream(inFile)) {
			props.load(fis);
			success = true;
		}
		catch (IOException e) {
			success = false;
		}

		// Save all properties from file
		_configValues.putAll(props);
		_colourScheme.loadFromHex(_configValues.getProperty(KEY_COLOUR_SCHEME));
		_pointColourer = ColourerFactory.createColourer(_configValues.getProperty(KEY_POINT_COLOURER));
		_recentFiles = new RecentFileList(_configValues.getProperty(KEY_RECENT_FILES));
		_unitSet = UnitSetLibrary.getUnitSet(_configValues.getProperty(KEY_UNITSET_KEY));
		// Reset coord display format
		setConfigString(KEY_COORD_DISPLAY_FORMAT, "NONE");

		if (success)
		{
			// Store location of successfully loaded config file
			_configFile = inFile;
		}
		return success;
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
		props.put(KEY_SHOW_MAP, "1"); // show by default
		props.put(KEY_EXIFTOOL_PATH, "exiftool");
		props.put(KEY_GNUPLOT_PATH, "gnuplot");
		props.put(KEY_GPSBABEL_PATH, "gpsbabel");
		props.put(KEY_IMPORT_FILE_FORMAT, "-1"); // no file format selected
		props.put(KEY_KMZ_IMAGE_SIZE, "240");
		props.put(KEY_ANTIALIAS, "1"); // antialias on by default
		props.put(KEY_OSSCALING, "0"); // OS shouldn't scale maps
		props.put(KEY_AUTOSAVE_SETTINGS, "1"); // autosave by default
		props.put(KEY_UNITSET_KEY, "unitset.kilometres"); // metric by default
		props.put(KEY_COORD_DISPLAY_FORMAT, "NONE"); // original
		props.put(KEY_HEIGHT_EXAGGERATION, "100"); // 100%, no exaggeration
		props.put(KEY_TERRAIN_GRID_SIZE, "50");
		props.put(KEY_ALTITUDE_TOLERANCE, "0"); // 0, all exact as before
		props.put(KEY_WAYPOINT_ICON_SIZE, "1"); // medium size
		props.put(KEY_WPICON_SALT, "-1"); // no waypoint colouring by default
		props.put(KEY_ICONS_DOUBLE_SIZE, "0"); // regular size by default
		props.put(KEY_ASKED_ABOUT_CONFIG_MIGRATION, "0"); // not asked
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

	/**
	 * @return File from which config was loaded (or null)
	 */
	public File getConfigFile() {
		return _configFile;
	}

	/**
	 * Set the file to which config was saved
	 */
	public void setConfigFile(File inFile) {
		_configFile = inFile;
	}

	/**
	 * @return config Properties object to allow all config values to be saved
	 */
	public Properties getAllConfig()
	{
		// Update recently-used files
		_configValues.setProperty(KEY_RECENT_FILES, _recentFiles.getConfigString());
		return _configValues;
	}

	/**
	 * @return the current colour scheme
	 */
	public ColourScheme getColourScheme() {
		return _colourScheme;
	}

	/**
	 * @return the current point colourer, if any
	 */
	public PointColourer getPointColourer() {
		return _pointColourer;
	}

	/**
	 * @return list of recently used files
	 */
	public RecentFileList getRecentFileList() {
		return _recentFiles;
	}

	/**
	 * Store the given configuration setting
	 * @param inKey key (from constants)
	 * @param inValue value as string
	 */
	public void setConfigString(String inKey, String inValue)
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
	public void setConfigBoolean(String inKey, boolean inValue)
	{
		if (inKey != null && !inKey.equals("")) {
			_configValues.put(inKey, (inValue ? "1" : "0"));
		}
	}

	/**
	 * Store the given configuration setting
	 * @param inKey key (from constants)
	 * @param inValue value as int
	 */
	public void setConfigInt(String inKey, int inValue)
	{
		if (inKey != null && !inKey.equals("")) {
			_configValues.put(inKey, "" + inValue);
		}
	}

	/**
	 * Get the given configuration setting as a String
	 * @param inKey key
	 * @return configuration setting as a String
	 */
	public String getConfigString(String inKey) {
		return _configValues.getProperty(inKey);
	}

	/**
	 * Get the given configuration setting as a boolean
	 * @param inKey key
	 * @return configuration setting as a boolean (default to true)
	 */
	public boolean getConfigBoolean(String inKey)
	{
		String val = _configValues.getProperty(inKey);
		return (val == null || val.equals("1"));
	}

	/**
	 * Get the given configuration setting as an int
	 * @param inKey key
	 * @return configuration setting as an int
	 */
	public int getConfigInt(String inKey) {
		return parseInt(_configValues.getProperty(inKey));
	}

	/**
	 * Update the colour scheme property from the current settings
	 */
	public void updateColourScheme() {
		setConfigString(KEY_COLOUR_SCHEME, _colourScheme.toString());
	}

	/**
	 * Update the point colourer from the given colourer
	 * @param inColourer point colourer object, or null
	 */
	public void updatePointColourer(PointColourer inColourer)
	{
		_pointColourer = inColourer;
		setConfigString(KEY_POINT_COLOURER, ColourerFactory.pointColourerToString(_pointColourer));
	}

	/**
	 * @return the current unit set
	 */
	public UnitSet getUnitSet() {
		return _unitSet;
	}

	/**
	 * @param inIndex index of unit set to select
	 */
	public void selectUnitSet(int inIndex)
	{
		_unitSet = UnitSetLibrary.getUnitSet(inIndex);
		// Set name of set in config
		setConfigString(KEY_UNITSET_KEY, _unitSet.getNameKey());
	}

	/**
	 * @param inKey config key
	 * @return true if there's a non-blank value stored
	 */
	private boolean hasString(String inKey)
	{
		final String value = getConfigString(inKey);
		return value != null && !value.isEmpty();
	}

	/**
	 * @return true if the config hasn't been saved but some things have been set
	 */
	public boolean hasUnsavedChanges()
	{
		return _configFile == null						// hasn't been saved
			&& getConfigBoolean(KEY_AUTOSAVE_SETTINGS)  // and user expects it to be saved
			&& (getConfigInt(KEY_MAPSOURCE_INDEX) > 0   // and something significant has been set
				|| hasString(KEY_MAPSOURCE_LIST)
				|| hasString(KEY_DISK_CACHE)
				|| hasString(KEY_EARTHDATA_AUTH)
				|| getConfigInt(KEY_POINT_COLOURER) > 0
				|| getConfigInt(KEY_WAYPOINT_ICONS) > 0);
	}
}
