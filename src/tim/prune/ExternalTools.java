package tim.prune;

import java.io.IOException;

import tim.prune.config.Config;


/**
 * Class to manage interfaces to external tools, like exiftool
 */
public abstract class ExternalTools
{
	/** Constant for Exiftool */
	public static final int TOOL_EXIFTOOL = 0;
	/** Constant for Gpsbabel */
	public static final int TOOL_GPSBABEL = 1;
	/** Constant for Gnuplot */
	public static final int TOOL_GNUPLOT  = 2;
	/** Constant for Xerces xml library */
	public static final int TOOL_XERCES   = 3;
	/** Config keys in order that the tools are defined above */
	private static final String[] CONFIG_KEYS = {Config.KEY_EXIFTOOL_PATH, Config.KEY_GPSBABEL_PATH, Config.KEY_GNUPLOT_PATH};
	/** Verification flags for the tools in the order defined above */
	private static final String[] VERIFY_FLAGS = {"-v", "-V", "-V"};


	/**
	 * Check if the selected tool is installed
	 * @param inToolNum number of tool, from constants
	 * @return true if selected tool is installed
	 */
	public static boolean isToolInstalled(int inToolNum)
	{
		switch (inToolNum) {
			case TOOL_EXIFTOOL:
			case TOOL_GPSBABEL:
			case TOOL_GNUPLOT:
				String toolPath = Config.getConfigString(CONFIG_KEYS[inToolNum]);
				if (toolPath != null && toolPath.length() > 0) {
					return check(toolPath + " " + VERIFY_FLAGS[inToolNum]);
				}
				break;
			case TOOL_XERCES:
				try {
					return Class.forName("org.apache.xerces.parsers.SAXParser").getClassLoader() != null;
				}
				catch (ClassNotFoundException e) {
					// System.err.println(e.getClass().getName() + " : " + e.getMessage());
				}
				break;
		}
		// Not found
		return false;
	}

	/**
	 * Check if the selected tool is installed using the given path
	 * @param inToolNum number of tool, from constants
	 * @param inPath selected path to use instead of configured one
	 * @return true if selected tool is installed
	 */
	public static boolean isToolInstalled(int inToolNum, String inPath)
	{
		if (inPath == null || inPath.equals("")) {return false;}
		switch (inToolNum) {
			case TOOL_EXIFTOOL:
			case TOOL_GPSBABEL:
			case TOOL_GNUPLOT:
				return check(inPath + " " + VERIFY_FLAGS[inToolNum]);
		}
		// Not found
		return false;
	}

	/**
	 * Attempt to call the specified command
	 * @return true if found, false otherwise
	 */
	private static boolean check(String inCommand)
	{
		try
		{
			Runtime.getRuntime().exec(inCommand);
			return true;
		}
		catch (IOException ioe)
		{
			// exception thrown, command not found
			return false;
		}
	}
}
