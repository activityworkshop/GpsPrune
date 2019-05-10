package tim.prune.load;

import java.io.File;
import java.util.TreeSet;
import javax.swing.JFileChooser;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.config.Config;
import tim.prune.data.AudioClip;
import tim.prune.undo.UndoLoadAudios;

/**
 * Class to manage the loading of audio clips
 */
public class AudioLoader extends GenericFunction
{
	private JFileChooser _fileChooser = null;
	private GenericFileFilter _fileFilter = null;
	private TreeSet<AudioClip> _fileList = null;


	/**
	 * Constructor
	 * @param inApp Application object to inform of data load
	 */
	public AudioLoader(App inApp)
	{
		super(inApp);
		_fileFilter = new AudioFileFilter();
	}

	/** Get the name key */
	public String getNameKey() {
		return "function.loadaudio";
	}

	/**
	 * Open the GUI to select options and start the load
	 */
	public void begin()
	{
		// Create file chooser if necessary
		if (_fileChooser == null)
		{
			_fileChooser = new JFileChooser();
			_fileChooser.setMultiSelectionEnabled(true);
			_fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			_fileChooser.setFileFilter(_fileFilter);
			_fileChooser.setDialogTitle(I18nManager.getText(getNameKey()));
			// start from directory in config if already set by other operations
			String configDir = Config.getConfigString(Config.KEY_PHOTO_DIR);
			if (configDir == null) {configDir = Config.getConfigString(Config.KEY_TRACK_DIR);}
			if (configDir != null) {_fileChooser.setCurrentDirectory(new File(configDir));}
		}
		// Show file dialog to choose file / directory(ies)
		if (_fileChooser.showOpenDialog(_parentFrame) == JFileChooser.APPROVE_OPTION)
		{
			_fileList = new TreeSet<AudioClip>(new MediaSorter());
			processFileList(_fileChooser.getSelectedFiles());
			final int numFiles = _fileList.size();
			if (numFiles == 0) {
				_app.showErrorMessage(getNameKey(), "error.audioload.nofilesfound");
			}
			else
			{
				// Construct undo object
				UndoLoadAudios undo = new UndoLoadAudios(numFiles);
				_app.getTrackInfo().addAudios(_fileList);
				_app.completeFunction(undo, I18nManager.getText("confirm.audioload"));
				UpdateMessageBroker.informSubscribers();
			}
		}
	}

	/**
	 * Process an array of File objects to load
	 * @param inFiles array of selected Files
	 */
	private void processFileList(File[] inFiles)
	{
		for (File file : inFiles)
		{
			if (file.exists() && file.canRead())
			{
				if (file.isFile()) {
					if (_fileFilter.accept(file)) {
						_fileList.add(new AudioClip(file));
					}
				}
				else if (file.isDirectory()) {
					processFileList(file.listFiles());
				}
			}
		}
	}
}
