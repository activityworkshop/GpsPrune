package tim.prune;

import java.util.EmptyStackException;
import java.util.Set;
import java.util.Stack;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import tim.prune.data.Altitude;
import tim.prune.data.Coordinate;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.LatLonRectangle;
import tim.prune.data.Latitude;
import tim.prune.data.Longitude;
import tim.prune.data.Photo;
import tim.prune.data.PhotoList;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;
import tim.prune.function.browser.BrowserLauncher;
import tim.prune.function.browser.UrlGenerator;
import tim.prune.function.edit.FieldEditList;
import tim.prune.function.edit.PointEditor;
import tim.prune.function.edit.PointNameEditor;
import tim.prune.gui.MenuManager;
import tim.prune.gui.UndoManager;
import tim.prune.load.FileLoader;
import tim.prune.load.JpegLoader;
import tim.prune.save.ExifSaver;
import tim.prune.save.FileSaver;
import tim.prune.undo.UndoAddTimeOffset;
import tim.prune.undo.UndoCompress;
import tim.prune.undo.UndoConnectPhoto;
import tim.prune.undo.UndoConnectPhotoWithClone;
import tim.prune.undo.UndoCreatePoint;
import tim.prune.undo.UndoCutAndMove;
import tim.prune.undo.UndoDeletePhoto;
import tim.prune.undo.UndoDeletePoint;
import tim.prune.undo.UndoDeleteRange;
import tim.prune.undo.UndoDisconnectPhoto;
import tim.prune.undo.UndoEditPoint;
import tim.prune.undo.UndoException;
import tim.prune.undo.UndoInsert;
import tim.prune.undo.UndoLoad;
import tim.prune.undo.UndoLoadPhotos;
import tim.prune.undo.UndoMergeTrackSegments;
import tim.prune.undo.UndoOperation;
import tim.prune.undo.UndoReverseSection;


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
	private FileLoader _fileLoader = null;
	private JpegLoader _jpegLoader = null;
	private FileSaver _fileSaver = null;
	private Stack<UndoOperation> _undoStack = null;
	private boolean _mangleTimestampsConfirmed = false;

	/**
	 * Constructor
	 * @param inFrame frame object for application
	 */
	public App(JFrame inFrame)
	{
		_frame = inFrame;
		_undoStack = new Stack<UndoOperation>();
		_track = new Track();
		_trackInfo = new TrackInfo(_track);
		FunctionLibrary.initialise(this);
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
			&& (_track.getNumPoints() > 0 || _trackInfo.getPhotoList().getNumPhotos() > 0));
	}

	/**
	 * @return the undo stack
	 */
	public Stack<UndoOperation> getUndoStack()
	{
		return _undoStack;
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
				_fileSaver = new FileSaver(this, _frame, _track);
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
			if (_track.editPoint(currentPoint, inEditList))
			{
				_undoStack.push(undo);
				// Confirm point edit
				UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.point.edit"));
			}
		}
	}


	/**
	 * Edit the name of the currently selected (way)point
	 */
	public void editCurrentPointName()
	{
		if (_track != null)
		{
			DataPoint currentPoint = _trackInfo.getCurrentPoint();
			if (currentPoint != null)
			{
				// Open point dialog to display details
				PointNameEditor editor = new PointNameEditor(this, _frame);
				editor.showDialog(currentPoint);
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
			boolean deletePhoto = false;
			Photo currentPhoto = currentPoint.getPhoto();
			if (currentPhoto != null)
			{
				// Confirm deletion of photo or decoupling
				int response = JOptionPane.showConfirmDialog(_frame,
					I18nManager.getText("dialog.deletepoint.deletephoto") + " " + currentPhoto.getFile().getName(),
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
			DataPoint nextTrackPoint = _trackInfo.getTrack().getNextTrackPoint(pointIndex + 1);
			// Construct Undo object
			UndoOperation undo = new UndoDeletePoint(pointIndex, currentPoint, photoIndex,
				nextTrackPoint != null && nextTrackPoint.getSegmentStart());
			// call track to delete point
			if (_trackInfo.deletePoint())
			{
				// Delete was successful so add undo info to stack
				_undoStack.push(undo);
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
				}
				// Confirm
				UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.deletepoint.single"));
			}
		}
	}


	/**
	 * Delete the currently selected range
	 */
	public void deleteSelectedRange()
	{
		if (_track != null)
		{
			// Find out if photos should be deleted or not
			int selStart = _trackInfo.getSelection().getStart();
			int selEnd = _trackInfo.getSelection().getEnd();
			if (selStart >= 0 && selEnd >= selStart)
			{
				int numToDelete = selEnd - selStart + 1;
				boolean[] deletePhotos = new boolean[numToDelete];
				Photo[] photosToDelete = new Photo[numToDelete];
				boolean deleteAll = false;
				boolean deleteNone = false;
				String[] questionOptions = {I18nManager.getText("button.yes"), I18nManager.getText("button.no"),
					I18nManager.getText("button.yestoall"), I18nManager.getText("button.notoall"),
					I18nManager.getText("button.cancel")};
				DataPoint point = null;
				for (int i=0; i<numToDelete; i++)
				{
					point = _trackInfo.getTrack().getPoint(i + selStart);
					if (point != null && point.getPhoto() != null)
					{
						if (deleteAll)
						{
							deletePhotos[i] = true;
							photosToDelete[i] = point.getPhoto();
						}
						else if (deleteNone) {deletePhotos[i] = false;}
						else
						{
							int response = JOptionPane.showOptionDialog(_frame,
								I18nManager.getText("dialog.deletepoint.deletephoto") + " " + point.getPhoto().getFile().getName(),
								I18nManager.getText("dialog.deletepoint.title"),
								JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
								questionOptions, questionOptions[1]);
							// check for cancel or close
							if (response == 4 || response == -1) {return;}
							// check for yes or yes to all
							if (response == 0 || response == 2)
							{
								deletePhotos[i] = true;
								photosToDelete[i] = point.getPhoto();
								if (response == 2) {deleteAll = true;}
							}
							// check for no to all
							if (response == 3) {deleteNone = true;}
						}
					}
				}
				// add information to undo stack
				UndoDeleteRange undo = new UndoDeleteRange(_trackInfo);
				// delete requested photos
				for (int i=0; i<numToDelete; i++)
				{
					point = _trackInfo.getTrack().getPoint(i + selStart);
					if (point != null && point.getPhoto() != null)
					{
						if (deletePhotos[i])
						{
							// delete photo from list
							_trackInfo.getPhotoList().deletePhoto(_trackInfo.getPhotoList().getPhotoIndex(point.getPhoto()));
						}
						else
						{
							// decouple from point
							point.getPhoto().setDataPoint(null);
						}
					}
				}
				// call track to delete range
				if (_trackInfo.deleteRange())
				{
					_undoStack.push(undo);
					// Confirm
					UpdateMessageBroker.informSubscribers("" + numToDelete + " "
						+ I18nManager.getText("confirm.deletepoint.multi"));
				}
			}
		}
	}


	/**
	 * Finish the compression by deleting the marked points
	 */
	public void finishCompressTrack()
	{
		UndoCompress undo = new UndoCompress(_track);
		// call track to do compress
		int numPointsDeleted = _trackInfo.deleteMarkedPoints();
		// add to undo stack if successful
		if (numPointsDeleted > 0)
		{
			undo.setNumPointsDeleted(numPointsDeleted);
			_undoStack.add(undo);
			UpdateMessageBroker.informSubscribers("" + numPointsDeleted + " "
				 + (numPointsDeleted==1?I18nManager.getText("confirm.deletepoint.single"):I18nManager.getText("confirm.deletepoint.multi")));
		}
		else {
			showErrorMessage("function.compress", "dialog.compress.nonefound");
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
	 * Complete the add time offset function with the specified offset
	 * @param inTimeOffset time offset to add (+ve for add, -ve for subtract)
	 */
	public void finishAddTimeOffset(long inTimeOffset)
	{
		// Construct undo information
		int selStart = _trackInfo.getSelection().getStart();
		int selEnd = _trackInfo.getSelection().getEnd();
		UndoAddTimeOffset undo = new UndoAddTimeOffset(selStart, selEnd, inTimeOffset);
		if (_trackInfo.getTrack().addTimeOffset(selStart, selEnd, inTimeOffset))
		{
			_undoStack.add(undo);
			UpdateMessageBroker.informSubscribers(DataSubscriber.DATA_EDITED);
			UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.addtimeoffset"));
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
			if (_track.mergeTrackSegments(selStart, selEnd)) {
				_undoStack.add(undo);
				UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.mergetracksegments"));
			}
		}
	}


	/**
	 * Interpolate the two selected points
	 */
	public void interpolateSelection()
	{
		// Get number of points to add
		Object numPointsStr = JOptionPane.showInputDialog(_frame,
			I18nManager.getText("dialog.interpolate.parameter.text"),
			I18nManager.getText("dialog.interpolate.title"),
			JOptionPane.QUESTION_MESSAGE, null, null, "");
		int numPoints = parseNumber(numPointsStr);
		if (numPoints <= 0) return;

		UndoInsert undo = new UndoInsert(_trackInfo.getSelection().getStart() + 1,
			numPoints);
		// call track to interpolate
		if (_trackInfo.interpolate(numPoints))
		{
			_undoStack.add(undo);
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
	 * Create a new point at the given lat/long coordinates
	 * @param inLat latitude
	 * @param inLong longitude
	 */
	public void createPoint(double inLat, double inLong)
	{
		// create undo object
		UndoCreatePoint undo = new UndoCreatePoint();
		// create point and add to track
		DataPoint point = new DataPoint(new Latitude(inLat, Coordinate.FORMAT_NONE), new Longitude(inLong, Coordinate.FORMAT_NONE), null);
		point.setSegmentStart(true);
		_track.appendPoints(new DataPoint[] {point});
		_trackInfo.getSelection().selectPoint(_trackInfo.getTrack().getNumPoints()-1);
		// add undo object to stack
		_undoStack.add(undo);
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
				_trackInfo.getSelection().deselectRange();
				UpdateMessageBroker.informSubscribers();
				UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.cutandmove"));
			}
		}
	}


	/**
	 * Select all points
	 */
	public void selectAll()
	{
		_trackInfo.getSelection().select(0, 0, _track.getNumPoints()-1);
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
	 * Receive loaded data and optionally merge with current Track
	 * @param inFieldArray array of fields
	 * @param inDataArray array of data
	 * @param inAltFormat altitude format
	 * @param inFilename filename used
	 */
	public void informDataLoaded(Field[] inFieldArray, Object[][] inDataArray, Altitude.Format inAltFormat,
		String inFilename)
	{
		// Check whether loaded array can be properly parsed into a Track
		Track loadedTrack = new Track();
		loadedTrack.load(inFieldArray, inDataArray, inAltFormat);
		if (loadedTrack.getNumPoints() <= 0)
		{
			showErrorMessage("error.load.dialogtitle", "error.load.nopoints");
			return;
		}
		// Decide whether to load or append
		if (_track.getNumPoints() > 0)
		{
			// ask whether to replace or append
			int answer = JOptionPane.showConfirmDialog(_frame,
				I18nManager.getText("dialog.openappend.text"),
				I18nManager.getText("dialog.openappend.title"),
				JOptionPane.YES_NO_CANCEL_OPTION);
			if (answer == JOptionPane.YES_OPTION)
			{
				// append data to current Track
				_undoStack.add(new UndoLoad(_track.getNumPoints(), loadedTrack.getNumPoints()));
				_track.combine(loadedTrack);
				// set filename if currently empty
				if (_trackInfo.getFileInfo().getNumFiles() == 0)
				{
					_trackInfo.getFileInfo().setFile(inFilename);
				}
				else
				{
					_trackInfo.getFileInfo().addFile();
				}
			}
			else if (answer == JOptionPane.NO_OPTION)
			{
				// Don't append, replace data
				PhotoList photos = null;
				if (_trackInfo.getPhotoList().hasCorrelatedPhotos())
				{
					photos = _trackInfo.getPhotoList().cloneList();
				}
				_undoStack.add(new UndoLoad(_trackInfo, inDataArray.length, photos));
				_lastSavePosition = _undoStack.size();
				_trackInfo.getSelection().clearAll();
				_track.load(loadedTrack);
				_trackInfo.getFileInfo().setFile(inFilename);
				if (photos != null)
				{
					_trackInfo.getPhotoList().removeCorrelatedPhotos();
				}
			}
		}
		else
		{
			// Currently no data held, so transfer received data
			_undoStack.add(new UndoLoad(_trackInfo, inDataArray.length, null));
			_lastSavePosition = _undoStack.size();
			_trackInfo.getSelection().clearAll();
			_track.load(loadedTrack);
			_trackInfo.getFileInfo().setFile(inFilename);
		}
		UpdateMessageBroker.informSubscribers();
		// Update status bar
		UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.loadfile") + " '" + inFilename + "'");
		// update menu
		_menuManager.informFileLoaded();
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
			if (numPhotosAdded == 1)
			{
				UpdateMessageBroker.informSubscribers("" + numPhotosAdded + " " + I18nManager.getText("confirm.jpegload.single"));
			}
			else
			{
				UpdateMessageBroker.informSubscribers("" + numPhotosAdded + " " + I18nManager.getText("confirm.jpegload.multi"));
			}
			// TODO: Improve message when photo(s) fail to load (eg already added)
			UpdateMessageBroker.informSubscribers();
			// update menu
			_menuManager.informFileLoaded();
		}
	}


	/**
	 * Connect the current photo to the current point
	 */
	public void connectPhotoToPoint()
	{
		Photo photo = _trackInfo.getCurrentPhoto();
		DataPoint point = _trackInfo.getCurrentPoint();
		if (photo != null && point != null)
		{
			if (point.getPhoto() != null)
			{
				// point already has a photo, confirm cloning of new point
				if (JOptionPane.showConfirmDialog(_frame,
					I18nManager.getText("dialog.connectphoto.clonepoint"),
					I18nManager.getText("dialog.connect.title"),
					JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)
				{
					// Create undo, clone point and attach
					int pointIndex = _trackInfo.getSelection().getCurrentPointIndex() + 1;
					// insert new point after current one
					point = point.clonePoint();
					UndoConnectPhotoWithClone undo = new UndoConnectPhotoWithClone(
						point, photo.getFile().getName(), pointIndex);
					_track.insertPoint(point, pointIndex);
					photo.setDataPoint(point);
					point.setPhoto(photo);
					_undoStack.add(undo);
					UpdateMessageBroker.informSubscribers(DataSubscriber.SELECTION_CHANGED);
					UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.photo.connect"));
				}
			}
			else
			{
				// point doesn't currently have a photo, so just connect it
				_undoStack.add(new UndoConnectPhoto(point, photo.getFile().getName()));
				photo.setDataPoint(point);
				point.setPhoto(photo);
				UpdateMessageBroker.informSubscribers(DataSubscriber.SELECTION_CHANGED);
				UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.photo.connect"));
			}
		}
	}


	/**
	 * Disconnect the current photo from its point
	 */
	public void disconnectPhotoFromPoint()
	{
		Photo photo = _trackInfo.getCurrentPhoto();
		if (photo != null && photo.getDataPoint() != null)
		{
			DataPoint point = photo.getDataPoint();
			_undoStack.add(new UndoDisconnectPhoto(point, photo.getFile().getName()));
			// disconnect
			photo.setDataPoint(null);
			point.setPhoto(null);
			UpdateMessageBroker.informSubscribers(DataSubscriber.SELECTION_CHANGED);
			UpdateMessageBroker.informSubscribers(I18nManager.getText("confirm.photo.disconnect"));
		}
	}


	/**
	 * Remove the current photo, if any
	 */
	public void deleteCurrentPhoto()
	{
		// Delete the current photo, and optionally its point too, keeping undo information
		Photo currentPhoto = _trackInfo.getCurrentPhoto();
		if (currentPhoto != null)
		{
			// Photo is selected, see if it has a point or not
			boolean photoDeleted = false;
			UndoDeletePhoto undoAction = null;
			if (currentPhoto.getDataPoint() == null)
			{
				// no point attached, so just delete photo
				undoAction = new UndoDeletePhoto(currentPhoto, _trackInfo.getSelection().getCurrentPhotoIndex(),
					null, -1);
				photoDeleted = _trackInfo.deleteCurrentPhoto(false);
			}
			else
			{
				// point is attached, so need to confirm point deletion
				undoAction = new UndoDeletePhoto(currentPhoto, _trackInfo.getSelection().getCurrentPhotoIndex(),
					currentPhoto.getDataPoint(), _trackInfo.getTrack().getPointIndex(currentPhoto.getDataPoint()));
				int response = JOptionPane.showConfirmDialog(_frame,
					I18nManager.getText("dialog.deletephoto.deletepoint"),
					I18nManager.getText("dialog.deletephoto.title"),
					JOptionPane.YES_NO_CANCEL_OPTION);
				boolean deletePointToo = (response == JOptionPane.YES_OPTION);
				// Cancel delete if cancel pressed or dialog closed
				if (response == JOptionPane.YES_OPTION || response == JOptionPane.NO_OPTION)
				{
					photoDeleted = _trackInfo.deleteCurrentPhoto(deletePointToo);
				}
			}
			// Add undo information to stack if necessary
			if (photoDeleted)
			{
				_undoStack.add(undoAction);
			}
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
			new UndoManager(this, _frame);
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
				_undoStack.pop().performUndo(_trackInfo);
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
			UpdateMessageBroker.informSubscribers();
		}
		catch (EmptyStackException empty) {}
	}


	/**
	 * Helper method to parse an Object into an integer
	 * @param inObject object, eg from dialog
	 * @return int value given
	 */
	private static int parseNumber(Object inObject)
	{
		int num = 0;
		if (inObject != null)
		{
			try
			{
				num = Integer.parseInt(inObject.toString());
			}
			catch (NumberFormatException nfe)
			{}
		}
		return num;
	}

	/**
	 * Show a map url in an external browser
	 * @param inSourceIndex index of map source to use
	 */
	public void showExternalMap(int inSourceIndex)
	{
		BrowserLauncher.launchBrowser(UrlGenerator.generateUrl(inSourceIndex, _trackInfo));
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
}
