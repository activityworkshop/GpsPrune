package tim.prune.config;

import java.io.File;

/**
 * Responsible for knowing about the paths of the config files
 * with the old name .pruneconfig and the new name gpsprune.config
 * as well as the old path ~ and the new paths ~/.config or %APPDATA%/gpsprune
 */
public class ConfigPaths
{
	private static final File OLD_DEFAULT_CURRENT_DIR = new File(".pruneconfig");
	private static final File OLD_DEFAULT_HOME_DIR = new File(System.getProperty("user.home"), ".pruneconfig");
	private static final String NEW_DEFAULT_FILENAME = "gpsprune.config";

	private ConfigPaths() {}

	/**
	 * @param config Config object to be filled with the data from the file
	 * @return recommended file to migrate to, or null if no migration is required
	 */
	public static File loadConfig(Config config)
	{
		// Look first in the current directory
		File defaultCurrentDir = new File(NEW_DEFAULT_FILENAME);
		if (defaultCurrentDir.exists() && defaultCurrentDir.canRead()
				&& config.loadFile(defaultCurrentDir)) {
			return null;
		}
		if (OLD_DEFAULT_CURRENT_DIR.exists() && OLD_DEFAULT_CURRENT_DIR.canRead()
				&& config.loadFile(OLD_DEFAULT_CURRENT_DIR)) {
			return defaultCurrentDir;
		}
		// Look in new config directory
		File defaultConfigDir = new File(getConfigDir(), NEW_DEFAULT_FILENAME);
		if (defaultConfigDir.exists() && defaultConfigDir.canRead()
				&& config.loadFile(defaultConfigDir)) {
			return null;
		}
		// Finally, look in home
		if (OLD_DEFAULT_HOME_DIR.exists() && OLD_DEFAULT_HOME_DIR.canRead()
				&& config.loadFile(OLD_DEFAULT_HOME_DIR)) {
			return defaultConfigDir;
		}
		// Nothing found, so just use defaults
		return null;
	}

	/** @return the default place to save the config now */
	public static File getSuggestedConfigFile() {
		return new File(getConfigDir(), NEW_DEFAULT_FILENAME);
	}

	/** @return just the filename of the default config file, to check if another name has been entered */
	public static String getDefaultFilename() {
		return NEW_DEFAULT_FILENAME;
	}

	/** Use the platform-specific properties to determine the config directory */
	private static File getConfigDir()
	{
		String configDir = System.getenv("XDG_CONFIG_HOME");
		if (configDir != null && !configDir.isEmpty()) {
			return new File(configDir);
		}
		configDir = System.getenv("APPDATA");
		if (configDir != null && !configDir.isEmpty()) {
			return new File(configDir, "gpsprune");
		}
		return new File(System.getProperty("user.home"), ".config");
	}
}
