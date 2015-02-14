package tim.prune;

import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.Photo;
import tim.prune.data.PhotoList;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;
import tim.prune.edit.FieldEditList;
import tim.prune.edit.PointEditor;
import tim.prune.edit.PointNameEditor;
import tim.prune.gui.MenuManager;
import tim.prune.gui.UndoManager;
import tim.prune.load.FileLoader;
import tim.prune.load.JpegLoader;
import tim.prune.load.PhotoMeasurer;
import tim.prune.save.ExifSaver;
import tim.prune.save.FileSaver;
import tim.prune.save.KmlExporter;
import tim.prune.save.PovExporter;
import tim.prune.threedee.ThreeDException;
import tim.prune.threedee.ThreeDWindow;
import tim.prune.threedee.WindowFactory;
import tim.prune.undo.UndoCompress;
import tim.prune.undo.UndoConnectPhoto;
import tim.prune.undo.UndoDeleteDuplicates;
import tim.prune.undo.UndoDeletePhoto;
import tim.prune.undo.UndoDeletePoint;
import tim.prune.undo.UndoDeleteRange;
import tim.prune.undo.UndoEditPoint;
import tim.prune.undo.UndoException;
import tim.prune.undo.UndoInsert;
import tim.prune.undo.UndoLoad;
import tim.prune.undo.UndoLoadPhotos;
import tim.prune.undo.UndoOperation;
import tim.prune.undo.UndoRearrangeWaypoints;
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
	private KmlExporter _exporter = null;
	private PovExporter _povExporter = null;
	private Stack _undoStack = null;
	private UpdateMessageBroker _broker = null;
	private boolean _reversePointsConfirmed = false;

	// Constants
	public static final int REARRANGE_TO_START   = 0;
	public static final int REARRANGE_TO_END     = 1;
	public static final int REARRANGE_TO_NEAREST = 2;


	/**
	 * Constructor
	 * @param inFrame frame object for application
	 * @param inBroker message broker
	 */
	public App(JFrame inFrame, UpdateMessageBroker inBroker)
	{
		_frame = inFrame;
		_undoStack = new Stack();
		_broker = inBroker;
		_track = new Track(_broker);
		_trackInfo = new TrackInfo(_track, _broker);
	}


	/**
	 * @return the current TrackInfo
	 */
	public TrackInfo getTrackInfo()
	{
		return _trackInfo;
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
	public Stack getUndoStack()
	{
		return _undoStack;
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
	 * Add a photo or a directory of photos which are already correlated
	 */
	public void addPhotos()
	{
		if (_jpegLoader == null)
			_jpegLoader = new JpegLoader(this, _frame);
		_jpegLoader.openFile();
	}


	/**
	 * Save the file in the selected format
	 */
	public void saveFile()
	{
		if (_track == null)
		{
			JOptionPane.showMessageDialog(_frame, I18nManager.getText("error.save.nodata"),
				I18nManager.getText("error.save.dialogtitle"), JOptionPane.ERROR_MESSAGE);
		}
		else
		{
			FileSaver saver = new FileSaver(this, _frame, _track);
			saver.showDialog(_fileLoader.getLastUsedDelimiter());
		}
	}


	/**
	 * Export track data as Kml
	 */
	public void exportKml()
	{
		if (_track == null)
		{
			JOptionPane.showMessageDialog(_frame, I18nManager.getText("error.save.nodata"),
				I18nManager.getText("error.save.dialogtitle"), JOptionPane.ERROR_MESSAGE);
		}
		else
		{
			// Invoke the export
			if (_exporter == null)
			{
				_exporter = new KmlExporter(_frame, _trackInfo);
			}
			_exporter.showDialog();
		}
	}


	/**
	 * Export track data as Pov without specifying settings
	 */
	public void exportPov()
	{
		exportPov(false, 0.0, 0.0, 0.0, 0);
	}

	/**
	 * Export track data as Pov and also specify settings
	 * @param inX X component of unit vector
	 * @param inY Y component of unit vector
	 * @param inZ Z component of unit vector
	 * @param inAltitudeCap altitude cap
	 */
	public void exportPov(double inX, double inY, double inZ, int inAltitudeCap)
	{
		exportPov(true, inX, inY, inZ, inAltitudeCap);
	}

	/**
	 * Export track data as Pov with optional angle specification
	 * @param inDefineAngles true to define angles, false to ignore
	 * @param inX X component of unit vector
	 * @param inY Y component of unit vector
	 * @param inZ Z component of unit vector
	 */
	private void exportPov(boolean inDefineSettings, double inX, double inY, double inZ, int inAltitudeCap)
	{
		// Check track has data to export
		if (_track == null || _track.getNumPoints() <= 0)
		{
			JOptionPane.showMessageDialog(_frame, I18nManager.getText("error.save.nodata"),
				I18nManager.getText("error.save.dialogtitle"), JOptionPane.ERROR_MESSAGE);
		}
		else
		{
			// Make new exporter if necessary
			if (_povExporter == null)
			{
				_povExporter = new PovExporter(_frame, _track);
			}
			// Specify angles if necessary
			if (inDefineSettings)
			{
				_povExporter.setCameraCoordinates(inX, inY, inZ);
				_povExporter.setAltitudeCap(inAltitudeCap);
			}
			// Initiate export
			_povExporter.showDialog();
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
	 * @param inEditList list of edits
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
		if (_track != null)
		{
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
				// add information to undo stack
				int pointIndex = _trackInfo.getSelection().getCurrentPointIndex();
				int photoIndex = _trackInfo.getPhotoList().getPhotoIndex(currentPhoto);
				// Undo object needs to know index of photo in list (if any) to restore
				UndoOperation undo = new UndoDeletePoint(pointIndex, currentPoint, photoIndex);
				// call track to delete point
				if (_trackInfo.deletePoint())
				{
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
				}
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
				UndoOperation undo = new UndoDeleteRange(_trackInfo);
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
				}
			}
		}
	}


	/**
	 * Delete all the duplicate points in the track
	 */
	public void deleteDuplicates()
	{
		if (_track != null)
		{
			// Save undo information
			UndoOperation undo = new UndoDeleteDuplicates(_track);
			// tell track to do it
			int numDeleted = _trackInfo.deleteDuplicates();
			if (numDeleted > 0)
			{
				_undoStack.add(undo);
				String message = null;
				if (numDeleted == 1)
				{
					message = "1 " + I18nManager.getText("dialog.deleteduplicates.single.text");
				}
				else
				{
					message = "" + numDeleted + " " + I18nManager.getText("dialog.deleteduplicates.multi.text");
				}
				JOptionPane.showMessageDialog(_frame, message,
					I18nManager.getText("dialog.deleteduplicates.title"), JOptionPane.INFORMATION_MESSAGE);
			}
			else
			{
				JOptionPane.showMessageDialog(_frame,
					I18nManager.getText("dialog.deleteduplicates.nonefound"),
					I18nManager.getText("dialog.deleteduplicates.title"), JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}


	/**
	 * Compress the track
	 */
	public void compressTrack()
	{
		UndoCompress undo = new UndoCompress(_track);
		// Get compression parameter
		Object compParam = JOptionPane.showInputDialog(_frame,
			I18nManager.getText("dialog.compresstrack.parameter.text"),
			I18nManager.getText("dialog.compresstrack.title"),
			JOptionPane.QUESTION_MESSAGE, null, null, "100");
		int compNumber = parseNumber(compParam);
		if (compNumber <= 0) return;
		// call track to do compress
		int numPointsDeleted = _trackInfo.compress(compNumber);
		// add to undo stack if successful
		if (numPointsDeleted > 0)
		{
			undo.setNumPointsDeleted(numPointsDeleted);
			_undoStack.add(undo);
			JOptionPane.showMessageDialog(_frame,
				I18nManager.getText("dialog.compresstrack.text") + " - "
				 + numPointsDeleted + " "
				 + (numPointsDeleted==1?I18nManager.getText("dialog.compresstrack.single.text"):I18nManager.getText("dialog.compresstrack.multi.text")),
				I18nManager.getText("dialog.compresstrack.title"), JOptionPane.INFORMATION_MESSAGE);
		}
		else
		{
			JOptionPane.showMessageDialog(_frame, I18nManager.getText("dialog.compresstrack.nonefound"),
				I18nManager.getText("dialog.compresstrack.title"), JOptionPane.WARNING_MESSAGE);
		}
	}


	/**
	 * Reverse a section of the track
	 */
	public void reverseRange()
	{
		// check whether Timestamp field exists, and if so confirm reversal
		int selStart = _trackInfo.getSelection().getStart();
		int selEnd = _trackInfo.getSelection().getEnd();
		if (!_track.hasData(Field.TIMESTAMP, selStart, selEnd)
			|| _reversePointsConfirmed
			|| (JOptionPane.showConfirmDialog(_frame,
				 I18nManager.getText("dialog.confirmreversetrack.text"),
				 I18nManager.getText("dialog.confirmreversetrack.title"),
				 JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION && (_reversePointsConfirmed = true)))
		{
			UndoReverseSection undo = new UndoReverseSection(selStart, selEnd);
			// call track to reverse range
			if (_track.reverseRange(selStart, selEnd))
			{
				_undoStack.add(undo);
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
	 * Rearrange the waypoints into track order
	 */
	public void rearrangeWaypoints(int inFunction)
	{
		UndoRearrangeWaypoints undo = new UndoRearrangeWaypoints(_track);
		boolean success = false;
		if (inFunction == REARRANGE_TO_START || inFunction == REARRANGE_TO_END)
		{
			// Collect the waypoints to the start or end of the track
			success = _track.collectWaypoints(inFunction == REARRANGE_TO_START);
		}
		else
		{
			// Interleave the waypoints into track order
			success = _track.interleaveWaypoints();
		}
		if (success)
		{
			_undoStack.add(undo);
		}
		else
		{
			JOptionPane.showMessageDialog(_frame, I18nManager.getText("error.rearrange.noop"),
				I18nManager.getText("error.function.noop.title"), JOptionPane.WARNING_MESSAGE);
		}
	}


	/**
	 * Open a new window with the 3d view
	 */
	public void show3dWindow()
	{
		ThreeDWindow window = WindowFactory.getWindow(this, _frame);
		if (window == null)
		{
			JOptionPane.showMessageDialog(_frame, I18nManager.getText("error.function.nojava3d"),
				I18nManager.getText("error.function.notavailable.title"), JOptionPane.WARNING_MESSAGE);
		}
		else
		{
			try
			{
				// Pass the track object and show the window
				window.setTrack(_track);
				window.show();
			}
			catch (ThreeDException e)
			{
				JOptionPane.showMessageDialog(_frame, I18nManager.getText("error.3d") + ": " + e.getMessage(),
					I18nManager.getText("error.3d.title"), JOptionPane.ERROR_MESSAGE);
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
	}


	/**
	 * Receive loaded data and optionally merge with current Track
	 * @param inFieldArray array of fields
	 * @param inDataArray array of data
	 */
	public void informDataLoaded(Field[] inFieldArray, Object[][] inDataArray, int inAltFormat, String inFilename)
	{
		// Check whether loaded array can be properly parsed into a Track
		Track loadedTrack = new Track(_broker);
		loadedTrack.load(inFieldArray, inDataArray, inAltFormat);
		if (loadedTrack.getNumPoints() <= 0)
		{
			JOptionPane.showMessageDialog(_frame,
				I18nManager.getText("error.load.nopoints"),
				I18nManager.getText("error.load.dialogtitle"),
				JOptionPane.ERROR_MESSAGE);
			return;
		}
		// Decide whether to load or append
		if (_track != null && _track.getNumPoints() > 0)
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
				// TODO: Should be possible to reuse the Track object already loaded?
				_trackInfo.loadTrack(inFieldArray, inDataArray, inAltFormat);
				_trackInfo.getFileInfo().setFile(inFilename);
				if (photos != null)
				{
					_trackInfo.getPhotoList().removeCorrelatedPhotos();
				}
			}
		}
		else
		{
			// currently no data held, so use received data
			_undoStack.add(new UndoLoad(_trackInfo, inDataArray.length, null));
			_lastSavePosition = _undoStack.size();
			_trackInfo.loadTrack(inFieldArray, inDataArray, inAltFormat);
			_trackInfo.getFileInfo().setFile(inFilename);
		}
		_broker.informSubscribers();
		// update menu
		_menuManager.informFileLoaded();
	}


	/**
	 * Accept a list of loaded photos
	 * @param inPhotoList List of Photo objects
	 */
	public void informPhotosLoaded(List inPhotoList)
	{
		if (inPhotoList != null && !inPhotoList.isEmpty())
		{
			int[] numsAdded = _trackInfo.addPhotos(inPhotoList);
			int numPhotosAdded = numsAdded[0];
			int numPointsAdded = numsAdded[1];
			if (numPhotosAdded > 0)
			{
				// Save numbers so load can be undone
				_undoStack.add(new UndoLoadPhotos(numPhotosAdded, numPointsAdded));
				// Trigger preloading of photo sizes in separate thread
				new PhotoMeasurer(_trackInfo.getPhotoList()).measurePhotos();
			}
			if (numPhotosAdded == 1)
			{
				JOptionPane.showMessageDialog(_frame,
					"" + numPhotosAdded + " " + I18nManager.getText("dialog.jpegload.photoadded"),
					I18nManager.getText("dialog.jpegload.title"), JOptionPane.INFORMATION_MESSAGE);
			}
			else
			{
				JOptionPane.showMessageDialog(_frame,
					"" + numPhotosAdded + " " + I18nManager.getText("dialog.jpegload.photosadded"),
					I18nManager.getText("dialog.jpegload.title"), JOptionPane.INFORMATION_MESSAGE);
			}
			// TODO: Improve message when photo(s) fail to load (eg already added)
			_broker.informSubscribers();
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
		if (photo != null && point != null && point.getPhoto() == null)
		{
			// connect
			_undoStack.add(new UndoConnectPhoto(point, photo.getFile().getName()));
			photo.setDataPoint(point);
			point.setPhoto(photo);
			//TODO: Confirm connect (maybe with status in photo panel?)
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
			_broker.informSubscribers();
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
				((UndoOperation) _undoStack.pop()).performUndo(_trackInfo);
			}
			JOptionPane.showMessageDialog(_frame, "" + inNumUndos + " "
				 + (inNumUndos==1?I18nManager.getText("dialog.confirmundo.single.text"):I18nManager.getText("dialog.confirmundo.multiple.text")),
				I18nManager.getText("dialog.confirmundo.title"),
				JOptionPane.INFORMATION_MESSAGE);
		}
		catch (UndoException ue)
		{
			JOptionPane.showMessageDialog(_frame,
				I18nManager.getText("error.undofailed.text") + " : " + ue.getMessage(),
				I18nManager.getText("error.undofailed.title"),
				JOptionPane.ERROR_MESSAGE);
			_undoStack.clear();
			_broker.informSubscribers();
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
}
