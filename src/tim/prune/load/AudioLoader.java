package tim.prune.load;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JFileChooser;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.cmd.AppendMediaCmd;
import tim.prune.config.Config;
import tim.prune.data.AudioClip;
import tim.prune.data.MediaObject;
import tim.prune.function.Describer;

/**
 * Class to manage the loading of audio clips
 */
public class AudioLoader extends GenericFunction
{
	private JFileChooser _fileChooser = null;
	private final GenericFileFilter _fileFilter;
	private ArrayList<MediaObject> _audioList = null;


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
			_fileChooser.setDialogTitle(getName());
			// start from directory in config if already set by other operations
			String configDir = getConfig().getConfigString(Config.KEY_PHOTO_DIR);
			if (configDir == null) {configDir = getConfig().getConfigString(Config.KEY_TRACK_DIR);}
			if (configDir != null) {
				_fileChooser.setCurrentDirectory(new File(configDir));
			}
		}
		// Show file dialog to choose file / directory(ies)
		if (_fileChooser.showOpenDialog(_parentFrame) == JFileChooser.APPROVE_OPTION)
		{
			_audioList = new ArrayList<>();
			processFileList(_fileChooser.getSelectedFiles());
			if (_audioList.isEmpty()) {
				_app.showErrorMessage(getNameKey(), "error.audioload.nofilesfound");
			}
			else
			{
				_audioList.sort(new MediaSorter());
				AppendMediaCmd command = new AppendMediaCmd(_audioList);
				Describer confirmDescriber = new Describer("confirm.audiosloaded.single", "confirm.audiosloaded");
				command.setConfirmText(confirmDescriber.getDescriptionWithCount(_audioList.size()));
				Describer undoDescriber = new Describer("undo.loadaudio", "undo.loadaudios");
				String firstAudioName = _audioList.get(0).getName();
				command.setDescription(undoDescriber.getDescriptionWithNameOrCount(firstAudioName, _audioList.size()));
				_app.execute(command);
			}
		}
	}

	/**
	 * Process an array of File objects to load
	 * @param inFiles array of selected Files
	 */
	private void processFileList(File[] inFiles)
	{
		if (inFiles == null) {
			return;
		}
		for (File file : inFiles)
		{
			if (file.exists() && file.canRead())
			{
				if (file.isFile()) {
					if (_fileFilter.accept(file)) {
						_audioList.add(new AudioClip(file));
					}
				}
				else if (file.isDirectory()) {
					processFileList(file.listFiles());
				}
			}
		}
	}
}
