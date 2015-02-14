package tim.prune;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Abstract class to hold application-wide configuration
 */
public abstract class Config
{
	/** File from which Config was loaded */
	private static File _configFile = null;

	/** Hashtable containing all config values */
	private static Properties _configValues = new Properties();

	/** Default config file */
	private static final File DEFAULT_CONFIG_FILE = new File(".pruneconfig");

	/** Key for track directory */
	public static final String KEY_TRACK_DIR = "prune.trackdirectory";
	/** Key for photo directory */
	public static final String KEY_PHOTO_DIR = "prune.photodirectory";
	/** Key for language code */
	public static final String KEY_LANGUAGE_CODE = "prune.languagecode";
	/** Key for GPS device */
	public static final String KEY_GPS_DEVICE = "prune.gpsdevice";
	/** Key for GPS format */
	public static final String KEY_GPS_FORMAT = "prune.gpsformat";
	/** Key for Povray font */
	public static final String KEY_POVRAY_FONT = "prune.povrayfont";
	/** Key for metric/imperial */
	public static final String KEY_METRIC_UNITS = "prune.metricunits";
	/** Key for map server index */
	public static final String KEY_MAPSERVERINDEX = "prune.mapserverindex";
	/** Key for map server url */
	public static final String KEY_MAPSERVERURL = "prune.mapserverurl";
	/** Key for show pace flag */
	public static final String KEY_SHOW_PACE = "prune.showpace";
	/** Key for width of thumbnails in kmz */
	public static final String KEY_KMZ_IMAGE_WIDTH = "prune.kmzimagewidth";
	/** Key for height of thumbnails in kmz */
	public static final String KEY_KMZ_IMAGE_HEIGHT = "prune.kmzimageheight";
	/** Key for gpsbabel path */
	public static final String KEY_GPSBABEL_PATH = "prune.gpsbabelpath";
	/** Key for gnuplot path */
	public static final String KEY_GNUPLOT_PATH = "prune.gnuplotpath";
	/** Key for exiftool path */
	public static final String KEY_EXIFTOOL_PATH = "prune.exiftoolpath";


	/**
	 * Load the default configuration file
	 */
	public static void loadDefaultFile()
	{
		try
		{
			loadFile(DEFAULT_CONFIG_FILE);
		}
		catch (ConfigException ce) {} // ignore
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
		props.put(KEY_SHOW_PACE, "0"); // hide by default
		props.put(KEY_EXIFTOOL_PATH, "exiftool");
		props.put(KEY_GNUPLOT_PATH, "gnuplot");
		props.put(KEY_GPSBABEL_PATH, "gpsbabel");
		props.put(KEY_KMZ_IMAGE_WIDTH, "240");
		props.put(KEY_KMZ_IMAGE_HEIGHT, "180");
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
		return _configValues;
	}

	/**
	 * Store the given configuration setting
	 * @param inKey key (from constants)
	 * @param inValue value as string
	 */
	public static void setConfigString(String inKey, String inValue)
	{
		if (inKey != null && !inKey.equals(""))
		{
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
	 * @return configuration setting as a boolean
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
		// Only two boolean keys so far
		return inKey != null && (inKey.equals(KEY_METRIC_UNITS) || inKey.equals(KEY_SHOW_PACE));
	}
}
