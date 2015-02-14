package tim.prune;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Abstract class to hold application-wide configuration
 */
public abstract class Config
{
	/** Working directory for loading and saving */
	private static File _workingDir = null;
	/** Default language */
	private static String _langCode = null;
	/** GPS device name */
	private static String _gpsDevice = null;
	/** GPS format name */
	private static String _gpsFormat = null;
	/** Font to use for povray */
	private static String _povrayFont = null;
	/** True to use metric units */
	private static boolean _metricUnits = true;


	/** Default config file */
	private static final File DEFAULT_CONFIG_FILE = new File(".pruneconfig");

	/** Key for working directory */
	private static final String KEY_WORKING_DIR = "prune.directory";
	/** Key for language code */
	private static final String KEY_LANGUAGE_CODE = "prune.languagecode";
	/** Key for GPS device */
	private static final String KEY_GPS_DEVICE = "prune.gpsdevice";
	/** Key for GPS format */
	private static final String KEY_GPS_FORMAT = "prune.gpsformat";
	/** Key for Povray font */
	private static final String KEY_POVRAY_FONT = "prune.povrayfont";
	/** Key for metric/imperial */
	private static final String KEY_METRIC_UNITS = "prune.metricunits";
	// TODO: Save config file location so save possible


	/**
	 * @return working directory for loading and saving
	 */
	public static File getWorkingDirectory()
	{
		return _workingDir;
	}

	/**
	 * @param inDirectory working directory to use
	 */
	public static void setWorkingDirectory(File inDirectory)
	{
		_workingDir = inDirectory;
	}

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
	 */
	public static void loadFile(File inFile) throws ConfigException
	{
		// Start with default properties
		Properties props = getDefaultProperties();
		// Try to load the file into a properties object
		boolean loadFailed = false;
		try
		{
			props.load(new FileInputStream(inFile));
		}
		catch (Exception e)
		{
			loadFailed = true;
		}
		// Save the properties we know about, ignore the rest
		_langCode = props.getProperty(KEY_LANGUAGE_CODE);
		String dir = props.getProperty(KEY_WORKING_DIR);
		if (dir != null) {setWorkingDirectory(new File(dir));}
		_gpsDevice = props.getProperty(KEY_GPS_DEVICE);
		_gpsFormat = props.getProperty(KEY_GPS_FORMAT);
		_povrayFont = props.getProperty(KEY_POVRAY_FONT);
		String useMetric = props.getProperty(KEY_METRIC_UNITS);
		_metricUnits = (useMetric == null || useMetric.equals("") || useMetric.toLowerCase().equals("y"));
		if (loadFailed) {
			throw new ConfigException();
		}
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
		return props;
	}

	/** @return language code */
	public static String getLanguageCode()
	{
		return _langCode;
	}

	/** @return gps device */
	public static String getGpsDevice()
	{
		return _gpsDevice;
	}

	/** @return gps format */
	public static String getGpsFormat()
	{
		return _gpsFormat;
	}

	/** @return povray font */
	public static String getPovrayFont()
	{
		return _povrayFont;
	}

	/** @return true to use metric units */
	public static boolean getUseMetricUnits()
	{
		return _metricUnits;
	}
}
