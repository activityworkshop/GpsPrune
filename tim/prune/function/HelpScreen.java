package tim.prune.function;

import javax.swing.JOptionPane;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.function.browser.BrowserLauncher;

/**
 * Class to show a simple help screen
 */
public class HelpScreen extends GenericFunction
{
	/**
	 * Constructor
	 * @param inApp app object
	 */
	public HelpScreen(App inApp)
	{
		super(inApp);
	}

	/**
	 * Get the name key
	 */
	public String getNameKey() {
		return "function.help";
	}

	/**
	 * Show the help screen
	 */
	public void begin()
	{
		// show the dialog and offer to open home page
		Object[] buttonTexts = {I18nManager.getText("button.showwebpage"), I18nManager.getText("button.cancel")};
		if (JOptionPane.showOptionDialog(_parentFrame, I18nManager.getText("dialog.help.help"),
				I18nManager.getText("function.help"), JOptionPane.YES_NO_OPTION,
				JOptionPane.INFORMATION_MESSAGE, null, buttonTexts, buttonTexts[1])
			== JOptionPane.YES_OPTION)
		{
			// User selected to launch home page
			BrowserLauncher.launchBrowser("http://activityworkshop.net/software/prune/index.html");
		}
	}
}
