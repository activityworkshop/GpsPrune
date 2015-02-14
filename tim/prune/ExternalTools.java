package tim.prune;

import java.io.IOException;


/**
 * Class to manage interfaces to external tools, like exiftool
 */
public abstract class ExternalTools
{

	/**
	 * Attempt to call Povray to see if it's installed / available in path
	 * @return true if found, false otherwise
	 */
	public static boolean isPovrayInstalled()
	{
		return check("povray");
	}


	/**
	 * Attempt to call Exiftool to see if it's installed / available in path
	 * @return true if found, false otherwise
	 */
	public static boolean isExiftoolInstalled()
	{
		return check("exiftool -v");
	}

	/**
	 * Attempt to call gpsbabel to see if it's installed / available in path
	 * @return true if found, false otherwise
	 */
	public static boolean isGpsbabelInstalled()
	{
		return check("gpsbabel -V");
	}

	/**
	 * Attempt to call gnuplot to see if it's installed / available in path
	 * @return true if found, false otherwise
	 */
	public static boolean isGnuplotInstalled()
	{
		return check(Config.getGnuplotPath() + " -V");
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
