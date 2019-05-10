package tim.prune.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import tim.prune.App;
import tim.prune.UpdateMessageBroker;
import tim.prune.config.Config;
import tim.prune.data.RecentFile;
import tim.prune.load.BabelLoadFromFile;

/**
 * Class to act as a trigger when a menu item for a recent file is clicked
 */
public class RecentFileTrigger implements ActionListener
{
	private App _app = null;
	private int _index = 0;


	/**
	 * Constructor
	 * @param inApp App object
	 * @param inIndex menu index from 0
	 */
	public RecentFileTrigger(App inApp, int inIndex)
	{
		_app = inApp;
		_index = inIndex;
	}

	/**
	 * React to click on menu item
	 */
	public void actionPerformed(ActionEvent arg0)
	{
		RecentFile rf = Config.getRecentFileList().getFile(_index);
		if (rf != null && rf.isValid())
		{
			if (rf.isRegularLoad())
			{
				// Trigger a regular file load
				ArrayList<File> dataFiles = new ArrayList<File>();
				dataFiles.add(rf.getFile());
				_app.loadDataFiles(dataFiles);
			}
			else
			{
				// Trigger a load via gpsbabel
				new BabelLoadFromFile(_app).beginWithFile(rf.getFile());
			}
		}
		else
		{
			_app.showErrorMessage("function.open", "error.load.noread");
			Config.getRecentFileList().verifyAll(); // Called on a file which no longer exists
			UpdateMessageBroker.informSubscribers();
		}
	}
}
