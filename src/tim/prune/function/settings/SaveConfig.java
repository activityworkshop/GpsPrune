package tim.prune.function.settings;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JFileChooser;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.config.Config;

/**
 * Class to provide the function to save the config settings
 */
public class SaveConfig extends GenericFunction
{
	/**
	 * Constructor
	 * @param inApp application object for callback
	 */
	public SaveConfig(App inApp)
	{
		super(inApp);
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.saveconfig";
	}

	/**
	 * Begin the function
	 */
	public void begin()
	{
		File configFile = Config.getConfigFile();
		if (configFile == null) {configFile = Config.HOME_CONFIG_FILE;}
		JFileChooser chooser = new JFileChooser(configFile.getAbsoluteFile().getParent());
		chooser.setSelectedFile(configFile);
		int response = chooser.showSaveDialog(_parentFrame);
		if (response == JFileChooser.APPROVE_OPTION)
		{
			File saveFile = chooser.getSelectedFile();
			saveConfig(saveFile);
		}
	}

	/**
	 * Autosave the settings file without any prompts
	 */
	public void silentSave()
	{
		saveConfig(Config.getConfigFile());
	}

	/**
	 * Autosave has been turned on or off, so maybe need to save
	 * @param inSaveOn true if autosave was switched on
	 */
	public void autosaveSwitched(boolean inSaveOn)
	{
		File configFile = Config.getConfigFile();
		if (inSaveOn && configFile == null)
		{
			begin();
		}
		else if (!inSaveOn && configFile != null)
		{
			// TODO: Ask whether to save or not?
			silentSave();
		}
	}

	/**
	 * Actually save the config file
	 * @param inSaveFile file to save to
	 */
	private void saveConfig(File inSaveFile)
	{
		// Set current window position in config
		Rectangle currBounds = _app.getFrame().getBounds();
		String windowBounds = "" + currBounds.x + "x" + currBounds.y + "x"
			+ currBounds.width + "x" + currBounds.height;
		Config.setConfigString(Config.KEY_WINDOW_BOUNDS, windowBounds);

		FileOutputStream outStream = null;
		try
		{
			outStream = new FileOutputStream(inSaveFile);
			Config.getAllConfig().store(outStream, "GpsPrune config file");
		}
		catch (IOException ioe) {
			_app.showErrorMessageNoLookup(getNameKey(),
				I18nManager.getText("error.save.failed") + " : " + ioe.getMessage());
		}
		catch (NullPointerException npe) {} // no config file given
		finally {
			try {outStream.close();} catch (Exception e) {}
		}
		// Remember where it was saved to
		Config.setConfigFile(inSaveFile);
	}
}
