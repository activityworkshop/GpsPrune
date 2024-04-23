package tim.prune.function.settings;

import java.io.File;

import javax.swing.JOptionPane;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.config.Config;

/**
 * Function responsible for migrating the config file
 * from the old location to the new one
 */
public class MigrateConfig extends GenericFunction
{
	private final File _targetFile;

	/**
	 * Constructor
	 * @param inApp application object for callback
	 * @param inTarget target file for new config location
	 */
	public MigrateConfig(App inApp, File inTarget) {
		super(inApp);
		_targetFile = inTarget;
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.migrateconfig";
	}

	/**
	 * Begin the function
	 */
	public void begin()
	{
		if (_targetFile == null) {
			return;
		}
		if (getConfig().getConfigBoolean(Config.KEY_ASKED_ABOUT_CONFIG_MIGRATION)) {
			return;
		}
		// Ask whether to do the migration or not
		File oldConfigFile = getConfig().getConfigFile();
		String oldPath = oldConfigFile.getAbsolutePath();
		String nextPath = _targetFile.getAbsolutePath();
		String message = I18nManager.getText("dialog.migrateconfig.confirm", oldPath, nextPath);
		int answer = JOptionPane.showConfirmDialog(_app.getFrame(), message, getName(),
			JOptionPane.YES_NO_CANCEL_OPTION);

		if (answer == JOptionPane.CANCEL_OPTION || answer == JOptionPane.CLOSED_OPTION) {
			return;
		}
		if (answer == JOptionPane.YES_OPTION)
		{
			File dirToCreate = _targetFile.getAbsoluteFile().getParentFile();
			if (dirToCreate != null
				&& (dirToCreate.isFile()
					|| (!dirToCreate.exists() && !dirToCreate.mkdirs()))) {
				String errorMessage = I18nManager.getText("error.migrateconfig.couldnotcreatedirectory", dirToCreate.getAbsolutePath());
				_app.showErrorMessageNoLookup(getNameKey(), errorMessage);
				return;
			}
			// Save config to new location
			getConfig().setConfigFile(_targetFile);
			SaveConfig saveFunction = new SaveConfig(_app);
			if (!saveFunction.silentSave()) {
				// if it didn't work, maybe we haven't got write permissions?
				return;
			}
			// Try to delete old file
			if (!deleteFile(oldConfigFile)) {
				String errorMessage = I18nManager.getText("error.migrateconfig.couldnotdeletefile", oldConfigFile.getAbsolutePath());
				_app.showErrorMessageNoLookup(getNameKey(), errorMessage);
			}
		}
		else
		{
			// user said no to the migration, so keep file where it is, but don't ask again
			getConfig().setConfigBoolean(Config.KEY_ASKED_ABOUT_CONFIG_MIGRATION, true);
			// save silently to avoid asking again
			new SaveConfig(_app).silentSave();
		}
	}

	/** @return true if the given file was deleted, false if it still exists */
	private boolean deleteFile(File inConfigFile) {
		return inConfigFile == null
				|| !inConfigFile.exists()
				|| inConfigFile.delete();
	}
}
