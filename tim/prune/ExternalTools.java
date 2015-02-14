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
		try
		{
			Runtime.getRuntime().exec("povray");
			return true;
		}
		catch (IOException ioe)
		{
			// exception thrown, povray not found
			return false;
		}
	}


	/**
	 * Attempt to call Exiftool to see if it's installed / available in path
	 * @return true if found, false otherwise
	 */
	public static boolean isExiftoolInstalled()
	{
		try
		{
			Runtime.getRuntime().exec("exiftool -v");
			return true;
		}
		catch (IOException ioe)
		{
			// exception thrown, exiftool not found
			return false;
		}
	}
}
