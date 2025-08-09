package tim.prune;

import java.io.File;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import tim.prune.cmd.Command;
import tim.prune.config.Config;
import tim.prune.data.LatLonRectangle;
import tim.prune.data.RecentFile;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;
import tim.prune.function.media.BlockMultipleMediaDialog;
import tim.prune.function.media.LinkedMediaLoader;
import tim.prune.function.settings.SaveConfig;
import tim.prune.gui.IconManager;
import tim.prune.gui.SidebarController;
import tim.prune.gui.Viewport;
import tim.prune.gui.colour.ColourerCaretaker;
import tim.prune.gui.colour.PointColourer;
import tim.prune.load.FileLoader;
import tim.prune.load.ItemToLoad;
import tim.prune.load.JpegLoader;
import tim.prune.load.ItemToLoad.BlockStatus;
import tim.prune.save.ExifSaver;
import tim.prune.save.FileSaver;
import tim.prune.tips.TipManager;
import tim.prune.undo.RedoManager;
import tim.prune.undo.UndoManager;
import tim.prune.undo.UndoStack;


/**
 * Main controller for the application
 */
public class App
{
	// Instance variables
	private final JFrame _frame;
	private final Config _config;
	private final IconManager _iconManager;
	private final Track _track;
	private final TrackInfo _trackInfo;
	private int _lastSavePosition = 0;
	private SidebarController _sidebarController = null;
	private FileLoader _fileLoader = null;
	private LinkedMediaLoader _linkedMediaLoader = null;
	private JpegLoader _jpegLoader = null;
	private FileSaver _fileSaver = null;
	private final UndoStack _undoStack;
	private final UndoStack _redoStack;
	private final ColourerCaretaker _colCaretaker;
	private Viewport _viewport = null;
	private final ArrayList<ItemToLoad> _itemsToLoad = new ArrayList<>();
	private AppMode _appMode = AppMode.NORMAL;

	/** Enum for the app mode - may expand later */
	public enum AppMode {NORMAL, DRAWRECT_INSIDE, DRAWRECT_OUTSIDE}


	/**
	 * Constructor
	 * @param inFrame frame object for application
	 * @param inConfig config object
	 */
	public App(JFrame inFrame, Config inConfig)
	{
		_frame = inFrame;
		_config = inConfig == null ? new Config() : inConfig;
		_iconManager = new IconManager(_config.getConfigBoolean(Config.KEY_ICONS_DOUBLE_SIZE));
		_undoStack = new UndoStack();
		_redoStack = new UndoStack();
		_track = new Track();
		_trackInfo = new TrackInfo(_track);
		FunctionLibrary.initialise(this);
		_colCaretaker = new ColourerCaretaker(this);
		UpdateMessageBroker.addSubscriber(_colCaretaker);
		_colCaretaker.setColourer(_config.getPointColourer());
	}


	/**
	 * @return the current TrackInfo
	 */
	public TrackInfo getTrackInfo() {
		return _trackInfo;
	}

	/**
	 * @return the dialog frame
	 */
	public JFrame getFrame() {
		return _frame;
	}

	/**
	 * Check if the application has unsaved data
	 * @return true if data is unsaved, false otherwise
	 */
	public boolean hasDataUnsaved()
	{
		return (_undoStack.size() > _lastSavePosition
			&& (_track.getNumPoints() > 0 || _trackInfo.getPhotoList().hasModifiedMedia()));
	}

	/**
	 * @return the undo stack
	 */
	public UndoStack getUndoStack() {
		return _undoStack;
	}

	/**
	 * @return the redo stack
	 */
	public UndoStack getRedoStack() {
		return _redoStack;
	}

	/**
	 * Update the system's point colourer using the one in the Config
	 */
	public void updatePointColourer()
	{
		if (_colCaretaker != null) {
			_colCaretaker.setColourer(_config.getPointColourer());
		}
	}

	/**
	 * @return colourer object, or null
	 */
	public PointColourer getPointColourer() {
		return _colCaretaker == null ? null : _colCaretaker.getColourer();
	}

	/**
	 * Show the specified tip if appropriate
	 * @param inTipNumber tip number from TipManager
	 */
	public void showTip(int inTipNumber)
	{
		String key = TipManager.fireTipTrigger(inTipNumber);
		if (key != null && !key.isEmpty())
		{
			JOptionPane.showMessageDialog(_frame, I18nManager.getText(key),
				I18nManager.getText("tip.title"), JOptionPane.INFORMATION_MESSAGE);
		}
	}


	/**
	 * Load the specified data files one by one
	 * @param inDataFiles arraylist containing File objects to load
	 */
	public void loadDataFiles(ArrayList<File> inDataFiles)
	{
		if (inDataFiles == null || inDataFiles.isEmpty()) {
			return;
		}
		for (File file : inDataFiles) {
			_itemsToLoad.add(ItemToLoad.dataFile(file));
		}
		loadNextItem(false); // don't auto-append, prompt when necessary
	}

	/**
	 * Load the specified linked media one by one
	 * @param inItems media items to load
	 */
	public void loadLinkedMedia(ArrayList<ItemToLoad> inItems)
	{
		if (inItems != null) {
			_itemsToLoad.addAll(0, inItems);	// prepended to start of list, not appended to end
		}
	}

	public boolean execute(Command inCommand)
	{
		if (inCommand.execute(_trackInfo))
		{
			_undoStack.add(inCommand);
			_redoStack.clear();
			UpdateMessageBroker.informSubscribers(inCommand.getConfirmText());
			UpdateMessageBroker.informSubscribers(inCommand.getUpdateFlags());
			return true;
		}
		return false;
	}

	/**
	 * Open a file containing track or waypoint data
	 */
	public void openFile()
	{
		if (_fileLoader == null) {
			_fileLoader = new FileLoader(this);
		}
		_fileLoader.openFile();
	}


	/**
	 * Add a photo or a directory of photos
	 */
	public void addPhotos()
	{
		if (_jpegLoader == null) {
			_jpegLoader = new JpegLoader(this, _frame);
		}
		_jpegLoader.openDialog(new LatLonRectangle(_track.getLatRange(), _track.getLonRange()));
	}

	/**
	 * Save the file in the selected format
	 */
	public void saveFile()
	{
		if (_track == null) {
			showErrorMessage("error.save.dialogtitle", "error.save.nodata");
		}
		else
		{
			if (_fileSaver == null) {
				_fileSaver = new FileSaver(this, _frame);
			}
			char delim = ',';
			if (_fileLoader != null) {delim = _fileLoader.getLastUsedDelimiter();}
			_fileSaver.showDialog(delim);
		}
	}


	/**
	 * Exit the application if confirmed
	 */
	public void exit()
	{
		// grab focus
		_frame.toFront();
		_frame.requestFocus();
		// check if ok to exit
		Object[] buttonTexts = {I18nManager.getText("button.exit"), I18nManager.getText("button.cancel")};

		// Has the user got unsaved data?
		if (hasDataUnsaved()
			&& JOptionPane.showOptionDialog(_frame, I18nManager.getText("dialog.exit.unsaveddata.text"),
				I18nManager.getText("dialog.exit.confirm.title"), JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE, null, buttonTexts, buttonTexts[1])
			!= JOptionPane.YES_OPTION)
		{
			// No, the user chose to cancel
			return;
		}

		// Has the user got unsaved settings?
		if (_config.hasUnsavedChanges()
			&& JOptionPane.showOptionDialog(_frame, I18nManager.getText("dialog.exit.unsavedsettings.text"),
				I18nManager.getText("dialog.exit.confirm.title"), JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE, null, buttonTexts, buttonTexts[1])
			!= JOptionPane.YES_OPTION)
		{
			// No, the user chose to cancel
			return;
		}

		// Checks passed, let's save settings and exit
		if (_config.getConfigBoolean(Config.KEY_AUTOSAVE_SETTINGS)) {
			new SaveConfig(this).silentSave();
		}
		System.exit(0);
	}

	/**
	 * Select nothing
	 */
	public void selectNone()
	{
		// deselect point, range and photo
		_trackInfo.getSelection().clearAll();
		_trackInfo.clearAllMarkers();
	}


	/**
	 * Inform that a file has either been loaded or saved, and the recent file list should be modified
	 * @param inFile file loaded or saved
	 * @param inIsRegularLoad true for regular load, false for GPSBabel import
	 */
	public void addRecentFile(File inFile, boolean inIsRegularLoad)
	{
		_config.getRecentFileList().addFile(new RecentFile(inFile, inIsRegularLoad));
		UpdateMessageBroker.informSubscribers(DataSubscriber.FILE_LOADED);
		if (inIsRegularLoad && getTrackInfo().getTrack().hasSingleSourceFile()) {
			informDataSaved(); // allow exit without saving, because we've only just loaded
		}
	}

	/**
	 * Inform the app that a file load process is complete, either successfully or cancelled
	 */
	public void informDataLoadComplete()
	{
		_trackInfo.clearFileInfo();
		UpdateMessageBroker.informSubscribers(DataSubscriber.FILE_LOADED);
		// Load next item if there's a queue
		loadNextItem(true); // with auto-append
	}

	/**
	 * Load the next item in the waiting list, if any
	 * @param inAutoAppend true to automatically append, false otherwise
	 */
	private void loadNextItem(boolean inAutoAppend)
	{
		new Thread(() -> {
			checkForMultipleMediaDomains();
			while (!_itemsToLoad.isEmpty())
			{
				ItemToLoad item = _itemsToLoad.remove(0);
				if (item.isDataFile())
				{
					if (_fileLoader == null) {
						_fileLoader = new FileLoader(this);
					}
					_fileLoader.openFile(item.getDataFile(), inAutoAppend);
					return;
				}
				if (item.isUrl())
				{
					getLinkedMediaLoader().loadFromUrl(item.getUrl(), item.getPoint(), item.getBlockStatus());
					return;
				}
				else if (item.isArchivedFile()) {
					getLinkedMediaLoader().loadFromArchive(item.getArchiveFile(), item.getItemPath(), item.getPoint());
					return;
				}
			}
		}).start();
	}

	private LinkedMediaLoader getLinkedMediaLoader()
	{
		if (_linkedMediaLoader == null) {
			_linkedMediaLoader = new LinkedMediaLoader(this);
		}
		return _linkedMediaLoader;
	}

	private void checkForMultipleMediaDomains()
	{
		HashSet<String> domains = new HashSet<>();
		for (ItemToLoad item : _itemsToLoad)
		{
			if (!item.isUrl() || item.getBlockStatus() != BlockStatus.NOT_ASKED) {
				continue;
			}
			String domain = item.getUrl().getHost();
			if (!getLinkedMediaLoader().isDomainKnown(domain)) {
				domains.add(domain);
			}
		}
		if (domains.size() < 3) {
			return;
		}
		new BlockMultipleMediaDialog(_frame, domains, _itemsToLoad).setVisible(true);
	}


	/**
	 * Save the coordinates of photos in their exif data
	 */
	public void saveExif()
	{
		ExifSaver saver = new ExifSaver(_frame, _config.getConfigString(Config.KEY_EXIFTOOL_PATH));
		saver.saveExifInformation(_trackInfo.getPhotoList());
	}


	/**
	 * Inform the app that the data has been saved
	 */
	public void informDataSaved() {
		_lastSavePosition = _undoStack.size();
	}


	/**
	 * Begin undo process
	 */
	public void beginUndo()
	{
		if (_undoStack.isEmpty())
		{
			// Nothing to undo
			JOptionPane.showMessageDialog(_frame, I18nManager.getText("dialog.undo.none.text"),
				I18nManager.getText("dialog.undo.none.title"), JOptionPane.INFORMATION_MESSAGE);
		}
		else {
			new UndoManager(this, _frame).show(getUndoStack());
		}
	}

	/**
	 * Clear the undo stack (losing all undo information)
	 */
	public void clearUndo()
	{
		// Exit if nothing to undo
		if (_undoStack == null || _undoStack.isEmpty()) {
			return;
		}
		// Has track got unsaved data?
		boolean unsaved = hasDataUnsaved();
		// Confirm operation with dialog
		int answer = JOptionPane.showConfirmDialog(_frame,
			I18nManager.getText("dialog.clearundo.text"),
			I18nManager.getText("dialog.clearundo.title"),
			JOptionPane.YES_NO_OPTION);
		if (answer == JOptionPane.YES_OPTION)
		{
			_undoStack.clear();
			_lastSavePosition = unsaved ? -1 : 0;
			UpdateMessageBroker.informSubscribers();
		}
	}


	/**
	 * Undo the specified number of actions
	 * @param inNumUndos number of actions to undo
	 */
	public void undoActions(int inNumUndos)
	{
		try
		{
			for (int i=0; i<inNumUndos; i++)
			{
				Command command = _undoStack.popCommand();
				_redoStack.add(command);
				command.getInverse().execute(_trackInfo);
			}
			String message = (inNumUndos == 1 ? I18nManager.getText("confirm.undo.single")
					: I18nManager.getTextWithNumber("confirm.undo.multi", inNumUndos));
			UpdateMessageBroker.informSubscribers(message);
		}
		catch (EmptyStackException empty) {}

		UpdateMessageBroker.informSubscribers();
	}

	/**
	 * Begin redo process
	 */
	public void beginRedo()
	{
		if (!_redoStack.isEmpty()) {
			new RedoManager(this, _frame).show(_redoStack);
		}
	}

	/**
	 * Redo the specified number of actions
	 * @param inNumActions number of actions to undo
	 */
	public void redoActions(int inNumActions)
	{
		try
		{
			for (int i=0; i<inNumActions; i++)
			{
				Command command = _redoStack.popCommand();
				_undoStack.add(command);
				command.execute(_trackInfo);
			}
			String message = (inNumActions == 1 ? I18nManager.getText("confirm.redo.single")
					: I18nManager.getTextWithNumber("confirm.redo.multi", inNumActions));
			UpdateMessageBroker.informSubscribers(message);
		}
		catch (EmptyStackException empty) {}

		UpdateMessageBroker.informSubscribers();
	}

	/**
	 * @return the current data status, used for later comparison
	 */
	public DataStatus getCurrentDataStatus() {
		return new DataStatus(_undoStack.size(), _undoStack.getNumUndos());
	}


	/**
	 * Display a standard error message
	 * @param inTitleKey key to lookup for window title
	 * @param inMessageKey key to lookup for error message
	 */
	public void showErrorMessage(String inTitleKey, String inMessageKey)
	{
		JOptionPane.showMessageDialog(_frame, I18nManager.getText(inMessageKey),
			I18nManager.getText(inTitleKey), JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Display a standard error message
	 * @param inTitleKey key to lookup for window title
	 * @param inMessage error message
	 */
	public void showErrorMessageNoLookup(String inTitleKey, String inMessage)
	{
		JOptionPane.showMessageDialog(_frame, inMessage,
			I18nManager.getText(inTitleKey), JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * @param inViewport viewport object
	 */
	public void setViewport(Viewport inViewport) {
		_viewport = inViewport;
	}

	/**
	 * @return current viewport object
	 */
	public Viewport getViewport() {
		return _viewport;
	}

	/**
	 * Set the controller for the full screen mode
	 * @param inController controller object
	 */
	public void setSidebarController(SidebarController inController) {
		_sidebarController = inController;
	}

	/**
	 * Toggle sidebars on and off
	 */
	public void toggleSidebars()
	{
		if (_sidebarController != null) {
			_sidebarController.toggle();
		}
	}

	/** @return current app mode */
	public AppMode getCurrentMode() {
		return _appMode;
	}

	/** @param inMode the current app mode */
	public void setCurrentMode(AppMode inMode) {
		_appMode = inMode;
	}

	/** @return config object */
	public Config getConfig() {
		return _config;
	}

	/** @return icon manager */
	public IconManager getIconManager() {
		return _iconManager;
	}
}
