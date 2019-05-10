package tim.prune.function;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;
import javax.swing.JOptionPane;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.GpsPrune;
import tim.prune.I18nManager;
import tim.prune.function.browser.BrowserLauncher;

/**
 * Class to check the version of GpsPrune
 * and show an appropriate dialog
 */
public class CheckVersionScreen extends GenericFunction
{
	/**
	 * Constructor
	 * @param inApp app object
	 */
	public CheckVersionScreen(App inApp)
	{
		super(inApp);
	}

	/**
	 * Get the name key
	 */
	public String getNameKey() {
		return "function.checkversion";
	}

	/**
	 * Show the check version dialog
	 */
	public void begin()
	{
		final String filePathStart = "https://activityworkshop.net/software/gpsprune/gpsprune_versioncheck_";
		final String filePathEnd = ".txt";
		String latestVer = null;
		String nextVersion = null;
		String releaseDate = null;
		Properties props = new Properties();
		try
		{
			// Load properties from the url on the server
			InputStream inStream = new URL(filePathStart + GpsPrune.VERSION_NUMBER + filePathEnd).openStream();
			props.load(inStream);

			// Extract the three fields we want, ignore others
			latestVer = props.getProperty("prune.latestversion");
			nextVersion = props.getProperty("prune.nextversion");
			releaseDate = props.getProperty("prune.releasedate");
		}
		catch (IOException ioe) {
			System.err.println(ioe.getClass().getName() + " - " + ioe.getMessage());
		}

		if (latestVer == null) {
			// Couldn't get version number, show error message
			JOptionPane.showMessageDialog(_parentFrame, I18nManager.getText("dialog.checkversion.error"),
				I18nManager.getText(getNameKey()), JOptionPane.ERROR_MESSAGE);
		}
		else if (latestVer.equals(GpsPrune.VERSION_NUMBER))
		{
			// Version on the server is the same as this one
			String displayMessage = I18nManager.getText("dialog.checkversion.uptodate");
			if (nextVersion != null && !nextVersion.equals(""))
			{
				displayMessage += "\n\n" + nextVersion;
			}
			// Show information message that the current version is already running
			JOptionPane.showMessageDialog(_parentFrame, displayMessage,
				I18nManager.getText(getNameKey()), JOptionPane.INFORMATION_MESSAGE);
		}
		else
		{
			// A new version is available!
			String displayMessage = I18nManager.getText("dialog.checkversion.newversion1") + " " + latestVer
				+ " " + I18nManager.getText("dialog.checkversion.newversion2");
			try
			{
				if (releaseDate != null && !releaseDate.equals("")) {
					displayMessage += "\n\n" + I18nManager.getText("dialog.checkversion.releasedate1") + " "
						+ DateFormat.getDateInstance(DateFormat.LONG).format(new SimpleDateFormat("y-M-d").parse(releaseDate))
						+ " " + I18nManager.getText("dialog.checkversion.releasedate2");
				}
			}
			catch (ParseException pe) {
				System.err.println("Oops, couldn't parse date: '" + releaseDate + "'");
			}
			displayMessage += "\n\n" + I18nManager.getText("dialog.checkversion.download");

			// Show information message to download the new version
			Object[] buttonTexts = {I18nManager.getText("button.showwebpage"), I18nManager.getText("button.cancel")};
			if (JOptionPane.showOptionDialog(_parentFrame, displayMessage,
					I18nManager.getText(getNameKey()), JOptionPane.YES_NO_OPTION,
					JOptionPane.INFORMATION_MESSAGE, null, buttonTexts, buttonTexts[1])
				== JOptionPane.YES_OPTION)
			{
				// User selected to launch home page
				BrowserLauncher.launchBrowser("https://activityworkshop.net/software/gpsprune/download.html");
			}
		}
	}
}
