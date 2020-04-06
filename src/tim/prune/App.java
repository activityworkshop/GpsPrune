package tim.prune;

import java.io.File;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import tim.prune.config.Config;
import tim.prune.data.Checker;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.LatLonRectangle;
import tim.prune.data.NumberUtils;
import tim.prune.data.Photo;
import tim.prune.data.PhotoList;
import tim.prune.data.PointCreateOptions;
import tim.prune.data.RecentFile;
import tim.prune.data.SourceInfo;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;
import tim.prune.data.SourceInfo.FILE_TYPE;
import tim.prune.data.Unit;
import tim.prune.function.AsyncMediaLoader;
import tim.prune.function.SelectTracksFunction;
import tim.prune.function.edit.FieldEditList;
import tim.prune.function.edit.PointEditor;
import tim.prune.function.settings.SaveConfig;
import tim.prune.gui.MenuManager;
import tim.prune.gui.SidebarController;
import tim.prune.gui.UndoManager;
import tim.prune.gui.Viewport;
import tim.prune.gui.colour.ColourerCaretaker;
import tim.prune.gui.colour.PointColourer;
import tim.prune.load.FileLoader;
import tim.prune.load.JpegLoader;
import tim.prune.load.MediaLinkInfo;
import tim.prune.load.TrackNameList;
import tim.prune.save.ExifSaver;
import tim.prune.save.FileSaver;
import tim.prune.tips.TipManager;
import tim.prune.undo.*;


/**
 * Main controller for the application
 */
public class App
{
	// Instance variables
	private JFrame _frame = null;
	private Track _track = null;
	private TrackInfo _trackInfo = null;
	private int _lastSavePosition = 0;
	private MenuManager _menuManager = null;
	private SidebarController _sidebarController = null;
	private FileLoader _fileLoader = null;
	private JpegLoader _jpegLoader = null;
	private FileSaver _fileSaver = null;
	private UndoStack _undoStack = null;
	private ColourerCaretaker _colCaretaker = null;
	private boolean _mangleTimestampsConfirmed = false;
	private Viewport _viewport = null;
	private ArrayList<File> _dataFiles = null;
	private boolean _autoAppendNextFile = false;
	private boolean _busyLoading = false;
	private AppMode _appMode = AppMode.NORMAL;

	/** Enum for the app mode - currently only two options but may expand later */
	public enum AppMode {NORMAL, DRAWRECT}


	/**
	 * Constructor
	 * @param inFrame frame object for application
	 */
	public App(JFrame inFrame)
	{
		_frame = inFrame;
		_undoStack = new UndoStack();
		_track = new Track();
		_trackInfo = new TrackInfo(_track);
		FunctionLibrary.initialise(this);
		_colCaretaker = new ColourerCaretaker(this);
		UpdateMessageBroker.addSubscriber(_colCaretaker);
		_colCaretaker.setColourer(Config.getPointColourer());
	}


	/**
	 * @return the current TrackInfo
	 */
	public TrackInfo getTrackInfo()
	{
		return _trackInfo;
	}

	/**
	 * @return the dialog frame
	 */
	public JFrame getFrame()
	{
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
	public UndoStack getUndoStack()
	{
		return _undoStack;
	}

	/**
	 * Update the system's point colourer using the one in the Config
	 */
	public void updatePointColourer()
	{
		if (_colCaretaker != null) {
			_colCaretaker.setColourer(Config.getPointColourer());
		}
	}

	/**
	 * @return colourer object, or null
	 */
	public PointColourer getPointColourer()
	{
		if (_colCaretaker == null) {return null;}
		return _colCaretaker.getColourer();
	}

	/**
	 * Show the specified tip if appropriate
	 * @param inTipNumber tip number from TipManager
	 */
	public void showTip(int inTipNumber)
	{
		String key = TipManager.fireTipTrigger(inTipNumber);
		if (key != null && !key.equals(""))
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
		if (inDataFiles == null || inDataFiles.size() == 0) {
			_dataFiles = null;
		}
		else
		{
			_dataFiles = inDataFiles;
			File f = _dataFiles.get(0);
			_dataFiles.remove(0);
			// Start load of specified file
			if (_fileLoader == null)
				_fileLoader = new FileLoader(this, _frame);
			_autoAppendNextFile = false; // prompt for append
			_fileLoader.openFile(f);
		}
	}

	/**
	 * Complete a function execution
	 * @param inUndo undo object to be added to stack
	 * @param inConfirmText confirmation text
	 */
	public void completeFunction(UndoOperation inUndo, String inConfirmText)
	{
		_undoStack.add(inUndo);
		UpdateMessageBroker.informSubscribers(inConfirmText);
		setCurrentMode(AppMode.NORMAL);
	}

	/**
	 * Set the MenuManager object to be informed about changes
	 * @param inManager MenuManager object
	 */
	public void setMenuManager(MenuManager inManager)
	{
		_menuManager = inManager;
	}


	/**
	 * Open a file containing track or waypoint data
	 */
	public void openFile()
	{
		if (_fileLoader == null)
			_fileLoader = new FileLoader(this, _frame);
		_fileLoader.openFile();
	}


	/**
	 * Add a photo or a directory of photos
	 */
	public void addPhotos()
	{
		if (_jpegLoader == null)
			_jpegLoader = new JpegLoader(this, _frame);
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
		if (!hasDataUnsaved()
			|| JOptionPane.showOptionDialog(_frame, I18nManager.getText("dialog.exit.confirm.text"),
				I18nManager.getText("dialog.exit.confirm.title"), JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE, null, buttonTexts, buttonTexts[1])
			== JOptionPane.YES_OPTION)
		{
			// save settings
			if (Config.getConfigBoolean(Config.KEY_AUTOSAVE_SETTINGS)) {
				new SaveConfig(this).silentSave();
			}
			System.exit(0);
		}
	}


	/**
	 * Edit the currently selected point
	 */
	public void editCurrentPoint()
	{
		if (_track != null)
		{
			DataPoint currentPoint = _trackInfo.getCurrentPoint();
			if (currentPoint != null)
			{
				// Open point dialog to display details
				PointEditor editor = new PointEditor(this, _frame);
				editor.showDialog(_track, currentPoint);
			}
		}
	}


	/**
	 * Complete the point edit
	 * @param inEditList field values to edit
	 * @param inUndoList field values before edit
	 */
	public void completePointEdit(FieldEditList inEditList, FieldEditList inUndoList)
	{
		DataPoint currentPoint = _trackInfo.getCurrentPoint();
		if (inEditList != null && inEditList.getNumEdits() > 0 && currentPoint != null)
		{
			// add information to undo stack
			UndoOperation undo = new UndoEditPoint(currentPoint, inUndoList);
			// pass to track for completion
			if (_track.editPoint(currentPoint, inEditList, false))
			{
				_undoStack.add(undo);
				// Confirm point edit
				UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.point.edit"));
			}
		}
	}


	/**
	 * Delete the currently selected point
	 */
	public void deleteCurrentPoint()
	{
		if (_track == null) {return;}
		DataPoint currentPoint = _trackInfo.getCurrentPoint();
		if (currentPoint != null)
		{
			// Check for photo
			boolean deletePhoto = false;
			Photo currentPhoto = currentPoint.getPhoto();
			if (currentPhoto != null)
			{
				// Confirm deletion of photo or decoupling
				int response = JOptionPane.showConfirmDialog(_frame,
					I18nManager.getText("dialog.deletepoint.deletephoto") + " " + currentPhoto.getName(),
					I18nManager.getText("dialog.deletepoint.title"),
					JOptionPane.YES_NO_CANCEL_OPTION);
				if (response == JOptionPane.CANCEL_OPTION || response == JOptionPane.CLOSED_OPTION)
				{
					// cancel pressed- abort delete
					return;
				}
				if (response == JOptionPane.YES_OPTION) {deletePhoto = true;}
			}
			// store necessary information to undo it later
			int pointIndex = _trackInfo.getSelection().getCurrentPointIndex();
			int photoIndex = _trackInfo.getPhotoList().getPhotoIndex(currentPhoto);
			int audioIndex = _trackInfo.getAudioList().getAudioIndex(currentPoint.getAudio());
			DataPoint nextTrackPoint = _trackInfo.getTrack().getNextTrackPoint(pointIndex + 1);
			// Construct Undo object
			UndoDeletePoint undo = new UndoDeletePoint(pointIndex, currentPoint, photoIndex,
				audioIndex, nextTrackPoint != null && nextTrackPoint.getSegmentStart());
			undo.setAtBoundaryOfSelectedRange(pointIndex == _trackInfo.getSelection().getStart() ||
				pointIndex == _trackInfo.getSelection().getEnd());
			// call track to delete point
			if (_trackInfo.deletePoint())
			{
				// Delete was successful so add undo info to stack
				_undoStack.add(undo);
				if (currentPhoto != null)
				{
					// delete photo if necessary
					if (deletePhoto)
					{
						_trackInfo.getPhotoList().deletePhoto(photoIndex);
					}
					else
					{
						// decouple photo from point
						currentPhoto.setDataPoint(null);
					}
					UpdateMessageBroker.informSubscribers(DataSubscriber.PHOTOS_MODIFIED);
				}
				// Delete audio object (without bothering to ask)
				if (audioIndex > -1) {
					_trackInfo.getAudioList().deleteAudio(audioIndex);
				}
				// Confirm
				UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.deletepoint.single"));
				UpdateMessageBroker.informSubscribers(DataSubscriber.DATA_ADDED_OR_REMOVED);
			}
		}
	}


	/**
	 * Reverse the currently selected section of the track
	 */
	public void reverseRange()
	{
		// check whether Timestamp field exists, and if so confirm reversal
		int selStart = _trackInfo.getSelection().getStart();
		int selEnd = _trackInfo.getSelection().getEnd();
		if (!_track.hasData(Field.TIMESTAMP, selStart, selEnd)
			|| _mangleTimestampsConfirmed
			|| (JOptionPane.showConfirmDialog(_frame,
				 I18nManager.getText("dialog.confirmreversetrack.text"),
				 I18nManager.getText("dialog.confirmreversetrack.title"),
				 JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION && (_mangleTimestampsConfirmed = true)))
		{
			UndoReverseSection undo = new UndoReverseSection(_track, selStart, selEnd);
			// call track to reverse range
			if (_track.reverseRange(selStart, selEnd))
			{
				_undoStack.add(undo);
				// Confirm
				UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.reverserange"));
			}
		}
	}

	/**
	 * Complete the add time offset function with the specified offset in seconds
	 * @param inTimeOffset time offset to add (+ve for add, -ve for subtract)
	 */
	public void finishAddTimeOffsetSeconds(long inTimeOffset)
	{
		// Construct undo information
		int selStart = _trackInfo.getSelection().getStart();
		int selEnd = _trackInfo.getSelection().getEnd();
		UndoAddTimeOffset undo = new UndoAddTimeOffset(selStart, selEnd, inTimeOffset);
		if (_trackInfo.getTrack().addTimeOffsetSeconds(selStart, selEnd, inTimeOffset, false))
		{
			_undoStack.add(undo);
			UpdateMessageBroker.informSubscribers(DataSubscriber.DATA_EDITED);
			UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.addtimeoffset"));
		}
	}


	/**
	 * Complete the add altitude offset function with the specified offset
	 * @param inOffset altitude offset to add as String
	 * @param inUnit altitude units of offset (eg Feet, Metres)
	 */
	public void finishAddAltitudeOffset(String inOffset, Unit inUnit)
	{
		// Sanity check
		if (inOffset == null || inOffset.equals("") || inUnit == null) {
			return;
		}
		// Construct undo information
		UndoAddAltitudeOffset undo = new UndoAddAltitudeOffset(_trackInfo);
		int selStart = _trackInfo.getSelection().getStart();
		int selEnd = _trackInfo.getSelection().getEnd();
		// How many decimal places are given in the offset?
		int numDecimals = NumberUtils.getDecimalPlaces(inOffset);
		boolean success = false;
		// Decimal offset given
		try {
			double offsetd = Double.parseDouble(inOffset);
			success = _trackInfo.getTrack().addAltitudeOffset(selStart, selEnd, offsetd, inUnit, numDecimals);
		}
		catch (NumberFormatException nfe) {}
		if (success)
		{
			_undoStack.add(undo);
			_trackInfo.getSelection().markInvalid();
			UpdateMessageBroker.informSubscribers(DataSubscriber.DATA_EDITED);
			UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.addaltitudeoffset"));
		}
	}


	/**
	 * Merge the track segments within the current selection
	 */
	public void mergeTrackSegments()
	{
		if (_trackInfo.getSelection().hasRangeSelected())
		{
			// Maybe could check segment start flags to see if it's worth merging
			// If first track point is already start and no other seg starts then do nothing

			int selStart = _trackInfo.getSelection().getStart();
			int selEnd = _trackInfo.getSelection().getEnd();
			// Make undo object
			UndoMergeTrackSegments undo = new UndoMergeTrackSegments(_track, selStart, selEnd);
			// Call track to merge segments
			if (_trackInfo.mergeTrackSegments(selStart, selEnd)) {
				_undoStack.add(undo);
				UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.mergetracksegments"));
			}
		}
	}


	/**
	 * Average the selected points
	 */
	public void averageSelection()
	{
		// Find following track point
		DataPoint nextPoint = _track.getNextTrackPoint(_trackInfo.getSelection().getEnd() + 1);
		boolean segFlag = false;
		if (nextPoint != null) {segFlag = nextPoint.getSegmentStart();}
		UndoInsert undo = new UndoInsert(_trackInfo.getSelection().getEnd() + 1, 1, nextPoint != null, segFlag);
		// call track info object to do the averaging
		if (_trackInfo.average())
		{
			_undoStack.add(undo);
		}
	}


	/**
	 * Create a new point at the end of the track
	 * @param inPoint point to add
	 */
	public void createPoint(DataPoint inPoint)
	{
		createPoint(inPoint, true);
	}

	/**
	 * Create a new point at the end of the track
	 * @param inPoint point to add
	 * @param inNewSegment true for a single point, false for a continuation
	 */
	public void createPoint(DataPoint inPoint, boolean inNewSegment)
	{
		// create undo object
		UndoCreatePoint undo = new UndoCreatePoint();
		_undoStack.add(undo);
		// add point to track
		inPoint.setSegmentStart(inNewSegment);
		_track.appendPoints(new DataPoint[] {inPoint});
		// ensure track's field list contains point's fields
		_track.extendFieldList(inPoint.getFieldList());
		_trackInfo.selectPoint(_trackInfo.getTrack().getNumPoints()-1);
		// update listeners
		UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.createpoint"));
	}


	/**
	 * Create a new point before the given position
	 * @param inPoint point to add
	 * @param inIndex index of following point
	 */
	public void createPoint(DataPoint inPoint, int inIndex)
	{
		// create undo object
		UndoInsert undo = new UndoInsert(inIndex, 1);
		_undoStack.add(undo);
		// add point to track
		_track.insertPoint(inPoint, inIndex);
		// ensure track's field list contains point's fields
		_track.extendFieldList(inPoint.getFieldList());
		_trackInfo.selectPoint(inIndex);
		final int selStart = _trackInfo.getSelection().getStart();
		final int selEnd   = _trackInfo.getSelection().getEnd();
		if (selStart < inIndex && selEnd >= inIndex)
		{
			// Extend end of selection by 1
			_trackInfo.getSelection().selectRange(selStart, selEnd+1);
		}
		// update listeners
		UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.createpoint"));
	}


	/**
	 * Cut the current selection and move it to before the currently selected point
	 */
	public void cutAndMoveSelection()
	{
		int startIndex = _trackInfo.getSelection().getStart();
		int endIndex = _trackInfo.getSelection().getEnd();
		int pointIndex = _trackInfo.getSelection().getCurrentPointIndex();
		// If timestamps would be mangled by cut/move, confirm
		if (!_track.hasData(Field.TIMESTAMP, startIndex, endIndex)
			|| _mangleTimestampsConfirmed
			|| (JOptionPane.showConfirmDialog(_frame,
				 I18nManager.getText("dialog.confirmcutandmove.text"),
				 I18nManager.getText("dialog.confirmcutandmove.title"),
				 JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION && (_mangleTimestampsConfirmed = true)))
		{
			// Find points to set segment flags
			DataPoint firstTrackPoint = _track.getNextTrackPoint(startIndex, endIndex);
			DataPoint nextTrackPoint = _track.getNextTrackPoint(endIndex+1);
			DataPoint moveToTrackPoint = _track.getNextTrackPoint(pointIndex);
			// Make undo object
			UndoCutAndMove undo = new UndoCutAndMove(_track, startIndex, endIndex, pointIndex);
			// Call track info to move track section
			if (_track.cutAndMoveSection(startIndex, endIndex, pointIndex))
			{
				// Set segment start flags (first track point, next track point, move to point)
				if (firstTrackPoint != null) {firstTrackPoint.setSegmentStart(true);}
				if (nextTrackPoint != null) {nextTrackPoint.setSegmentStart(true);}
				if (moveToTrackPoint != null) {moveToTrackPoint.setSegmentStart(true);}

				// Add undo object to stack, set confirm message
				_undoStack.add(undo);
				_trackInfo.getSelection().selectRange(-1, -1);
				UpdateMessageBroker.informSubscribers();
				UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.cutandmove"));
			}
		}
	}

	/**
	 * Select nothing
	 */
	public void selectNone()
	{
		// deselect point, range and photo
		_trackInfo.getSelection().clearAll();
		_track.clearDeletionMarkers();
	}

	/**
	 * Receive loaded data and determine whether to filter on tracks or not
	 * @param inFieldArray array of fields
	 * @param inDataArray array of data
	 * @param inSourceInfo information about the source of the data
	 * @param inTrackNameList information about the track names
	 */
	public void informDataLoaded(Field[] inFieldArray, Object[][] inDataArray,
		SourceInfo inSourceInfo, TrackNameList inTrackNameList)
	{
		// no link array given
		informDataLoaded(inFieldArray, inDataArray, null, inSourceInfo,
			inTrackNameList, null);
	}

	/**
	 * Receive loaded data and determine whether to filter on tracks or not
	 * @param inFieldArray array of fields
	 * @param inDataArray array of data
	 * @param inOptions creation options such as units
	 * @param inSourceInfo information about the source of the data
	 * @param inTrackNameList information about the track names
	 */
	public void informDataLoaded(Field[] inFieldArray, Object[][] inDataArray,
		PointCreateOptions inOptions, SourceInfo inSourceInfo, TrackNameList inTrackNameList)
	{
		// no link array given
		informDataLoaded(inFieldArray, inDataArray, inOptions, inSourceInfo,
			inTrackNameList, null);
	}

	/**
	 * Receive loaded data and determine whether to filter on tracks or not
	 * @param inFieldArray array of fields
	 * @param inDataArray array of data
	 * @param inOptions creation options such as units
	 * @param inSourceInfo information about the source of the data
	 * @param inTrackNameList information about the track names
	 * @param inLinkInfo links to photo/audio clips
	 */
	public void informDataLoaded(Field[] inFieldArray, Object[][] inDataArray, PointCreateOptions inOptions,
		SourceInfo inSourceInfo, TrackNameList inTrackNameList, MediaLinkInfo inLinkInfo)
	{
		// Check whether loaded array can be properly parsed into a Track
		Track loadedTrack = new Track();
		loadedTrack.load(inFieldArray, inDataArray, inOptions);
		if (loadedTrack.getNumPoints() <= 0)
		{
			String msgKey = (inSourceInfo == null ? "error.load.nopointsintext" : "error.load.nopoints");
			showErrorMessage("error.load.dialogtitle", msgKey);
			// load next file if there's a queue
			loadNextFile();
			return;
		}
		// Check for doubled track
		if (Checker.isDoubledTrack(loadedTrack)) {
			JOptionPane.showMessageDialog(_frame, I18nManager.getText("dialog.open.contentsdoubled"),
				I18nManager.getText("function.open"), JOptionPane.WARNING_MESSAGE);
		}

		_busyLoading = true;
		// Attach photos and/or audio clips to points
		if (inLinkInfo != null)
		{
			String[] linkArray = inLinkInfo.getLinkArray();
			if (linkArray != null) {
				new AsyncMediaLoader(this, inLinkInfo.getZipFile(), linkArray, loadedTrack, inSourceInfo.getFile()).begin();
			}
		}
		// Look at TrackNameList, decide whether to filter or not
		if (inTrackNameList != null && inTrackNameList.getNumTracks() > 1)
		{
			// Launch a dialog to let the user choose which tracks to load, then continue
			new SelectTracksFunction(this, loadedTrack, inSourceInfo, inTrackNameList).begin();
		}
		else {
			// go directly to load
			informDataLoaded(loadedTrack, inSourceInfo);
		}
		setCurrentMode(AppMode.NORMAL);
	}


	/**
	 * Receive loaded data and optionally merge with current Track
	 * @param inLoadedTrack loaded track
	 * @param inSourceInfo information about the source of the data
	 */
	public void informDataLoaded(Track inLoadedTrack, SourceInfo inSourceInfo)
	{
		// Decide whether to load or append
		if (_track.getNumPoints() > 0)
		{
			// ask whether to replace or append
			int answer = 0;
			if (_autoAppendNextFile) {
				// Automatically append the next file
				answer = JOptionPane.YES_OPTION;
			}
			else {
				// Ask whether to append or not
				answer = JOptionPane.showConfirmDialog(_frame,
					I18nManager.getText("dialog.openappend.text"),
					I18nManager.getText("dialog.openappend.title"),
					JOptionPane.YES_NO_CANCEL_OPTION);
			}
			_autoAppendNextFile = false; // reset flag to cancel autoappend

			if (answer == JOptionPane.YES_OPTION)
			{
				// append data to current Track
				UndoLoad undo = new UndoLoad(_track.getNumPoints(), inLoadedTrack.getNumPoints());
				undo.setNumPhotosAudios(_trackInfo.getPhotoList().getNumPhotos(), _trackInfo.getAudioList().getNumAudios());
				_undoStack.add(undo);
				_track.combine(inLoadedTrack);
				if (inSourceInfo != null)
				{
					// set source information
					inSourceInfo.populatePointObjects(_track, inLoadedTrack.getNumPoints());
					_trackInfo.getFileInfo().addSource(inSourceInfo);
				}
			}
			else if (answer == JOptionPane.NO_OPTION)
			{
				// Don't append, replace data
				PhotoList photos = null;
				if (_trackInfo.getPhotoList().hasCorrelatedPhotos()) {
					photos = _trackInfo.getPhotoList().cloneList();
				}
				UndoLoad undo = new UndoLoad(_trackInfo, inLoadedTrack.getNumPoints(), photos);
				undo.setNumPhotosAudios(_trackInfo.getPhotoList().getNumPhotos(), _trackInfo.getAudioList().getNumAudios());
				_undoStack.add(undo);
				_lastSavePosition = _undoStack.size();
				_trackInfo.getSelection().clearAll();
				_track.load(inLoadedTrack);
				if (inSourceInfo != null)
				{
					// set source information
					inSourceInfo.populatePointObjects(_track, _track.getNumPoints());
					_trackInfo.getFileInfo().replaceSource(inSourceInfo);
				}
				_trackInfo.getPhotoList().removeCorrelatedPhotos();
				_trackInfo.getAudioList().removeCorrelatedAudios();
			}
		}
		else
		{
			// Currently no data held, so transfer received data
			UndoLoad undo = new UndoLoad(_trackInfo, inLoadedTrack.getNumPoints(), null);
			undo.setNumPhotosAudios(_trackInfo.getPhotoList().getNumPhotos(), _trackInfo.getAudioList().getNumAudios());
			_undoStack.add(undo);
			_lastSavePosition = _undoStack.size();
			_trackInfo.getSelection().clearAll();
			_track.load(inLoadedTrack);
			if (inSourceInfo != null)
			{
				inSourceInfo.populatePointObjects(_track, _track.getNumPoints());
				_trackInfo.getFileInfo().addSource(inSourceInfo);
			}
		}
		// Update config before subscribers are told
		if (inSourceInfo != null)
		{
			boolean isRegularLoad = (inSourceInfo.getFileType() != FILE_TYPE.GPSBABEL);
			Config.getRecentFileList().addFile(new RecentFile(inSourceInfo.getFile(), isRegularLoad));
			// Update status bar
			UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.loadfile")
				+ " '" + inSourceInfo.getName() + "'");
		}
		UpdateMessageBroker.informSubscribers();
		// update menu
		_menuManager.informFileLoaded();
		// Remove busy lock
		_busyLoading = false;
		// load next file if there's a queue
		loadNextFile();
	}

	/**
	 * Inform the app that NO data was loaded, eg cancel pressed
	 * Only needed if there's another file waiting in the queue
	 */
	public void informNoDataLoaded()
	{
		// Load next file if there's a queue
		loadNextFile();
	}

	/**
	 * External trigger to automatically append the next loaded file
	 * instead of prompting to replace or append
	 */
	public void autoAppendNextFile()
	{
		_autoAppendNextFile = true;
	}

	/**
	 * Load the next file in the waiting list, if any
	 */
	private void loadNextFile()
	{
		if (_dataFiles == null || _dataFiles.size() == 0) {
			_dataFiles = null;
		}
		else {
			new Thread(new Runnable() {
				public void run() {
					File f = _dataFiles.get(0);
					_dataFiles.remove(0);
					_autoAppendNextFile = true;
					_fileLoader.openFile(f);
				}
			}).start();
		}
	}


	/**
	 * Accept a list of loaded photos
	 * @param inPhotoSet Set of Photo objects
	 */
	public void informPhotosLoaded(Set<Photo> inPhotoSet)
	{
		if (inPhotoSet != null && !inPhotoSet.isEmpty())
		{
			int[] numsAdded = _trackInfo.addPhotos(inPhotoSet);
			int numPhotosAdded = numsAdded[0];
			int numPointsAdded = numsAdded[1];
			if (numPhotosAdded > 0)
			{
				// Save numbers so load can be undone
				_undoStack.add(new UndoLoadPhotos(numPhotosAdded, numPointsAdded));
			}
			if (numPhotosAdded == 1) {
				UpdateMessageBroker.informSubscribers("" + numPhotosAdded + " " + I18nManager.getText("confirm.jpegload.single"));
			}
			else {
				UpdateMessageBroker.informSubscribers("" + numPhotosAdded + " " + I18nManager.getText("confirm.jpegload.multi"));
			}
			// MAYBE: Improve message when photo(s) fail to load (eg already added)
			UpdateMessageBroker.informSubscribers();
			// update menu
			if (numPointsAdded > 0) _menuManager.informFileLoaded();
		}
	}


	/**
	 * Save the coordinates of photos in their exif data
	 */
	public void saveExif()
	{
		ExifSaver saver = new ExifSaver(_frame);
		saver.saveExifInformation(_trackInfo.getPhotoList());
	}


	/**
	 * Inform the app that the data has been saved
	 */
	public void informDataSaved()
	{
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
		else
		{
			new UndoManager(this, _frame).show();
		}
	}


	/**
	 * Clear the undo stack (losing all undo information
	 */
	public void clearUndo()
	{
		// Exit if nothing to undo
		if (_undoStack == null || _undoStack.isEmpty())
			return;
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
			_lastSavePosition = 0;
			if (unsaved) _lastSavePosition = -1;
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
				_undoStack.popOperation().performUndo(_trackInfo);
			}
			String message = "" + inNumUndos + " "
				 + (inNumUndos==1?I18nManager.getText("confirm.undo.single"):I18nManager.getText("confirm.undo.multi"));
			UpdateMessageBroker.informSubscribers(message);
		}
		catch (UndoException ue)
		{
			showErrorMessageNoLookup("error.undofailed.title",
				I18nManager.getText("error.undofailed.text") + " : " + ue.getMessage());
			_undoStack.clear();
		}
		catch (EmptyStackException empty) {}
		UpdateMessageBroker.informSubscribers();
	}

	/**
	 * @return the current data status, used for later comparison
	 */
	public DataStatus getCurrentDataStatus()
	{
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
	public void setViewport(Viewport inViewport)
	{
		_viewport = inViewport;
	}

	/**
	 * @return current viewport object
	 */
	public Viewport getViewport()
	{
		return _viewport;
	}

	/**
	 * Set the controller for the full screen mode
	 * @param inController controller object
	 */
	public void setSidebarController(SidebarController inController)
	{
		_sidebarController = inController;
	}

	/**
	 * Toggle sidebars on and off
	 */
	public void toggleSidebars()
	{
		_sidebarController.toggle();
	}

	/** @return true if App is currently busy with loading data */
	public boolean isBusyLoading() {
		return _busyLoading;
	}

	/** @return current app mode */
	public AppMode getCurrentMode() {
		return _appMode;
	}

	/** @param inMode the current app mode */
	public void setCurrentMode(AppMode inMode) {
		_appMode = inMode;
	}
}
