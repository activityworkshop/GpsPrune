package tim.prune.function.browser;

import java.net.URI;
import javax.swing.JOptionPane;


/**
 * Class to launch a browser window to show an external map
 * Some code and ideas taken from BareBonesBrowserLaunch at centerkey.com
 */
public abstract class BrowserLauncher
{
	private static boolean _initialised = false;
	private static String[] _browserCommand = null;
	private static boolean _urlNeedsQuotes = false;


	/**
	 * Init method to set up browser
	 */
	private static void init()
	{
		_browserCommand = null;
		// First check if "which" command is available
		if (commandExists("which"))
		{
			// which exists, so try browsers in turn
			String[] browsersToTry = {"firefox", "iceweasel", "konqueror", "opera", "epiphany",
				"mozilla", "safari", "google-chrome", "lynx"};
			String browserFound = null;
			for (int i=0; i<browsersToTry.length && browserFound == null; i++)
			{
				if (commandExists(browsersToTry[i]))
					browserFound = browsersToTry[i];
			}
			if (browserFound != null) {
				_browserCommand = new String[] {browserFound, null};
			}
		}

		if (_browserCommand == null)
		{
			// no which command (or none of the browsers found), so check if os name looks like a mac
			String osName = System.getProperty("os.name").toLowerCase();
			boolean isMacOsx = osName.indexOf("mac os") >= 0 || osName.indexOf("darwin") >= 0;
			if (isMacOsx) {
				// for Mac Osx just use "open" command
				_browserCommand = new String[] {"open", null};
			}
			else {
				// assume it's not linux or mac, so try windows method using "start" command
				_browserCommand = new String[] {"cmd.exe", "/C", "start", "\"\"", null};
				_urlNeedsQuotes = true;
			}
		}
		_initialised = true;
	}


	/**
	 * Check if the specified command exists on the system
	 * @param inCommand command to check
	 * @return true if the command exists
	 */
	private static boolean commandExists(String inCommand)
	{
		try
		{
			String[] commands = {"which", inCommand};
			if (Runtime.getRuntime().exec(commands).waitFor() == 0)
			{
				return true;
			}
		}
		catch (Exception e) {} // failed
		return false;
	}


	/**
	 * Launch a browser window to show the given url
	 * @param inUrl url to show
	 */
	public static void launchBrowser(String inUrl)
	{
		// First choice is to try the Desktop library from java 6, if available
		try {
			Class<?> d = Class.forName("java.awt.Desktop");
			d.getDeclaredMethod("browse", new Class[] {URI.class}).invoke(
				d.getDeclaredMethod("getDesktop").invoke(null), new Object[] {URI.create(inUrl)});
			//above code mimics: Desktop.getDesktop().browse(URI.create(inUrl));
		}
		catch (Exception ignore)
		{
			// The Desktop call failed, need to try backup methods
			if (!_initialised) {init();}
			if (_browserCommand == null) {
				JOptionPane.showMessageDialog(null, "Cannot show url: " + inUrl);
			}
			else
			{
				try
				{
					// enclose url in quotes if necessary
					String url = inUrl;
					if (_urlNeedsQuotes) {url = "\"" + url + "\"";}
					// Fill in url in last element of command array
					_browserCommand[_browserCommand.length - 1] = url;
					// execute command to launch browser
					Runtime.getRuntime().exec(_browserCommand);
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Failed to show url: " + inUrl);
				}
			}
		}
	}
}
