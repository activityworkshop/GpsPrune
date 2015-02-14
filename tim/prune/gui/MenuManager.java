package tim.prune.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import tim.prune.App;
import tim.prune.DataSubscriber;
import tim.prune.I18nManager;
import tim.prune.data.PhotoList;
import tim.prune.data.Selection;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;

/**
 * Class to manage the menu bar and tool bar,
 * including enabling and disabling the items
 */
public class MenuManager implements DataSubscriber
{
	private JFrame _parent = null;
	private App _app = null;
	private Track _track = null;
	private Selection _selection = null;
	private PhotoList _photos = null;

	// Menu items which need enabling/disabling
	private JMenuItem _saveItem = null;
	private JMenuItem _exportKmlItem = null;
	private JMenuItem _exportPovItem = null;
	private JMenuItem _undoItem = null;
	private JMenuItem _clearUndoItem = null;
	private JMenuItem _editPointItem = null;
	private JMenuItem _editWaypointNameItem = null;
	private JMenuItem _deletePointItem = null;
	private JMenuItem _deleteRangeItem = null;
	private JMenuItem _deleteDuplicatesItem = null;
	private JMenuItem _compressItem = null;
	private JMenuItem _interpolateItem = null;
	private JMenuItem _selectAllItem = null;
	private JMenuItem _selectNoneItem = null;
	private JMenuItem _selectStartItem = null;
	private JMenuItem _selectEndItem = null;
	private JMenuItem _reverseItem = null;
	private JMenu     _rearrangeMenu = null;
	private JMenuItem _rearrangeStartItem = null;
	private JMenuItem _rearrangeEndItem = null;
	private JMenuItem _rearrangeNearestItem = null;
	private JMenuItem _show3dItem = null;
	private JMenuItem _saveExifItem = null;
	private JMenuItem _connectPhotoItem = null;
	private JMenuItem _deletePhotoItem = null;
	// TODO: Does Photo menu require disconnect option?

	// ActionListeners for reuse by menu and toolbar
	private ActionListener _openFileAction = null;
	private ActionListener _addPhotoAction = null;
	private ActionListener _saveAction = null;
	private ActionListener _undoAction = null;
	private ActionListener _editPointAction = null;
	private ActionListener _selectStartAction = null;
	private ActionListener _selectEndAction = null;
	private ActionListener _connectPhotoAction = null;

	// Toolbar buttons which need enabling/disabling
	private JButton _saveButton = null;
	private JButton _undoButton = null;
	private JButton _editPointButton = null;
	private JButton _selectStartButton = null;
	private JButton _selectEndButton = null;
	private JButton _connectPhotoButton = null;


	/**
	 * Constructor
	 * @param inParent parent object for dialogs
	 * @param inApp application to call on menu actions
	 */
	public MenuManager(JFrame inParent, App inApp, TrackInfo inTrackInfo)
	{
		_parent = inParent;
		_app = inApp;
		_track = inTrackInfo.getTrack();
		_selection = inTrackInfo.getSelection();
		_photos = inTrackInfo.getPhotoList();
	}


	/**
	 * Create a JMenuBar containing all menu items
	 * @return JMenuBar
	 */
	public JMenuBar createMenuBar()
	{
		JMenuBar menubar = new JMenuBar();
		JMenu fileMenu = new JMenu(I18nManager.getText("menu.file"));
		// Open file
		JMenuItem openMenuItem = new JMenuItem(I18nManager.getText("menu.file.open"));
		openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		_openFileAction = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.openFile();
			}
		};
		openMenuItem.addActionListener(_openFileAction);
		fileMenu.add(openMenuItem);
		// Add photos
		JMenuItem addPhotosMenuItem = new JMenuItem(I18nManager.getText("menu.file.addphotos"));
		_addPhotoAction = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.addPhotos();
			}
		};
		addPhotosMenuItem.addActionListener(_addPhotoAction);
		fileMenu.add(addPhotosMenuItem);
		// Save
		_saveItem = new JMenuItem(I18nManager.getText("menu.file.save"), KeyEvent.VK_S);
		_saveAction = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.saveFile();
			}
		};
		_saveItem.addActionListener(_saveAction);
		_saveItem.setEnabled(false);
		fileMenu.add(_saveItem);
		// Export
		_exportKmlItem = new JMenuItem(I18nManager.getText("menu.file.exportkml"));
		_exportKmlItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.exportKml();
			}
		});
		_exportKmlItem.setEnabled(false);
		fileMenu.add(_exportKmlItem);
		_exportPovItem = new JMenuItem(I18nManager.getText("menu.file.exportpov"));
		_exportPovItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.exportPov();
			}
		});
		_exportPovItem.setEnabled(false);
		fileMenu.add(_exportPovItem);
		fileMenu.addSeparator();
		JMenuItem exitMenuItem = new JMenuItem(I18nManager.getText("menu.file.exit"));
		exitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.exit();
			}
		});
		fileMenu.add(exitMenuItem);
		menubar.add(fileMenu);
		JMenu editMenu = new JMenu(I18nManager.getText("menu.edit"));
		editMenu.setMnemonic(KeyEvent.VK_E);
		_undoItem = new JMenuItem(I18nManager.getText("menu.edit.undo"));
		_undoAction = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.beginUndo();
			}
		};
		_undoItem.addActionListener(_undoAction);
		_undoItem.setEnabled(false);
		editMenu.add(_undoItem);
		_clearUndoItem = new JMenuItem(I18nManager.getText("menu.edit.clearundo"));
		_clearUndoItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.clearUndo();
			}
		});
		_clearUndoItem.setEnabled(false);
		editMenu.add(_clearUndoItem);
		editMenu.addSeparator();
		_editPointItem = new JMenuItem(I18nManager.getText("menu.edit.editpoint"));
		_editPointAction = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.editCurrentPoint();
			}
		};
		_editPointItem.addActionListener(_editPointAction);
		_editPointItem.setEnabled(false);
		editMenu.add(_editPointItem);
		_editWaypointNameItem = new JMenuItem(I18nManager.getText("menu.edit.editwaypointname"));
		_editWaypointNameItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.editCurrentPointName();
			}
		});
		_editWaypointNameItem.setEnabled(false);
		editMenu.add(_editWaypointNameItem);
		_deletePointItem = new JMenuItem(I18nManager.getText("menu.edit.deletepoint"));
		_deletePointItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.deleteCurrentPoint();
			}
		});
		_deletePointItem.setEnabled(false);
		editMenu.add(_deletePointItem);
		_deleteRangeItem = new JMenuItem(I18nManager.getText("menu.edit.deleterange"));
		_deleteRangeItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.deleteSelectedRange();
			}
		});
		_deleteRangeItem.setEnabled(false);
		editMenu.add(_deleteRangeItem);
		_deleteDuplicatesItem = new JMenuItem(I18nManager.getText("menu.edit.deleteduplicates"));
		_deleteDuplicatesItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.deleteDuplicates();
			}
		});
		_deleteDuplicatesItem.setEnabled(false);
		editMenu.add(_deleteDuplicatesItem);
		_compressItem = new JMenuItem(I18nManager.getText("menu.edit.compress"));
		_compressItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.compressTrack();
			}
		});
		_compressItem.setEnabled(false);
		editMenu.add(_compressItem);
		editMenu.addSeparator();
		_interpolateItem = new JMenuItem(I18nManager.getText("menu.edit.interpolate"));
		_interpolateItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.interpolateSelection();
			}
		});
		_interpolateItem.setEnabled(false);
		editMenu.add(_interpolateItem);
		_reverseItem = new JMenuItem(I18nManager.getText("menu.edit.reverse"));
		_reverseItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.reverseRange();
			}
		});
		_reverseItem.setEnabled(false);
		editMenu.add(_reverseItem);
		// Rearrange waypoints
		_rearrangeMenu = new JMenu(I18nManager.getText("menu.edit.rearrange"));
		_rearrangeMenu.setEnabled(false);
		_rearrangeStartItem = new JMenuItem(I18nManager.getText("menu.edit.rearrange.start"));
		_rearrangeStartItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.rearrangeWaypoints(App.REARRANGE_TO_START);
			}
		});
		_rearrangeStartItem.setEnabled(true);
		_rearrangeMenu.add(_rearrangeStartItem);
		_rearrangeEndItem = new JMenuItem(I18nManager.getText("menu.edit.rearrange.end"));
		_rearrangeEndItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.rearrangeWaypoints(App.REARRANGE_TO_END);
			}
		});
		_rearrangeEndItem.setEnabled(true);
		_rearrangeMenu.add(_rearrangeEndItem);
		_rearrangeNearestItem = new JMenuItem(I18nManager.getText("menu.edit.rearrange.nearest"));
		_rearrangeNearestItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.rearrangeWaypoints(App.REARRANGE_TO_NEAREST);
			}
		});
		_rearrangeNearestItem.setEnabled(true);
		_rearrangeMenu.add(_rearrangeNearestItem);
		editMenu.add(_rearrangeMenu);
		menubar.add(editMenu);

		// Select menu
		JMenu selectMenu = new JMenu(I18nManager.getText("menu.select"));
		_selectAllItem = new JMenuItem(I18nManager.getText("menu.select.all"));
		_selectAllItem.setEnabled(false);
		_selectAllItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.selectAll();
			}
		});
		selectMenu.add(_selectAllItem);
		_selectNoneItem = new JMenuItem(I18nManager.getText("menu.select.none"));
		_selectNoneItem.setEnabled(false);
		_selectNoneItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.selectNone();
			}
		});
		selectMenu.add(_selectNoneItem);
		selectMenu.addSeparator();
		_selectStartItem = new JMenuItem(I18nManager.getText("menu.select.start"));
		_selectStartItem.setEnabled(false);
		_selectStartAction = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_selection.selectRangeStart();
			}
		};
		_selectStartItem.addActionListener(_selectStartAction);
		selectMenu.add(_selectStartItem);
		_selectEndItem = new JMenuItem(I18nManager.getText("menu.select.end"));
		_selectEndItem.setEnabled(false);
		_selectEndAction = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_selection.selectRangeEnd();
			}
		};
		_selectEndItem.addActionListener(_selectEndAction);
		selectMenu.add(_selectEndItem);
		menubar.add(selectMenu);

		// Add photo menu
		JMenu photoMenu = new JMenu(I18nManager.getText("menu.photo"));
		addPhotosMenuItem = new JMenuItem(I18nManager.getText("menu.file.addphotos"));
		addPhotosMenuItem.addActionListener(_addPhotoAction);
		photoMenu.add(addPhotosMenuItem);
		_saveExifItem = new JMenuItem(I18nManager.getText("menu.photo.saveexif"));
		_saveExifItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.saveExif();
			}
		});
		_saveExifItem.setEnabled(false);
		photoMenu.add(_saveExifItem);
		_connectPhotoItem = new JMenuItem(I18nManager.getText("menu.photo.connect"));
		_connectPhotoAction = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.connectPhotoToPoint();
			}
		};
		_connectPhotoItem.addActionListener(_connectPhotoAction);
		_connectPhotoItem.setEnabled(false);
		photoMenu.add(_connectPhotoItem);
		_deletePhotoItem = new JMenuItem(I18nManager.getText("menu.photo.delete"));
		_deletePhotoItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.deleteCurrentPhoto();
			}
		});
		_deletePhotoItem.setEnabled(false);
		photoMenu.add(_deletePhotoItem);
		menubar.add(photoMenu);

		// Add 3d menu (whether java3d available or not)
		JMenu threeDMenu = new JMenu(I18nManager.getText("menu.3d"));
		_show3dItem = new JMenuItem(I18nManager.getText("menu.3d.show3d"));
		_show3dItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.show3dWindow();
			}
		});
		_show3dItem.setEnabled(false);
		threeDMenu.add(_show3dItem);
		menubar.add(threeDMenu);

		// Help menu for About
		JMenu helpMenu = new JMenu(I18nManager.getText("menu.help"));
		JMenuItem aboutItem = new JMenuItem(I18nManager.getText("menu.help.about"));
		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				new AboutScreen(_parent).show();
			}
		});
		helpMenu.add(aboutItem);
		menubar.add(helpMenu);

		return menubar;
	}


	/**
	 * Create a JToolBar containing all toolbar buttons
	 * @return toolbar containing buttons
	 */
	public JToolBar createToolBar()
	{
		JToolBar toolbar = new JToolBar();
		// Add text file
		JButton openFileButton = new JButton(new ImageIcon(getClass().getResource("images/add_textfile_icon.png")));
		openFileButton.setToolTipText(I18nManager.getText("menu.file.open"));
		openFileButton.addActionListener(_openFileAction);
		toolbar.add(openFileButton);
		// Add photo
		JButton addPhotoButton = new JButton(new ImageIcon(getClass().getResource("images/add_photo_icon.png")));
		addPhotoButton.setToolTipText(I18nManager.getText("menu.file.addphotos"));
		addPhotoButton.addActionListener(_addPhotoAction);
		toolbar.add(addPhotoButton);
		// Save
		_saveButton = new JButton(new ImageIcon(getClass().getResource("images/save_icon.gif")));
		_saveButton.setToolTipText(I18nManager.getText("menu.file.save"));
		_saveButton.addActionListener(_saveAction);
		_saveButton.setEnabled(false);
		toolbar.add(_saveButton);
		// Undo
		_undoButton = new JButton(new ImageIcon(getClass().getResource("images/undo_icon.gif")));
		_undoButton.setToolTipText(I18nManager.getText("menu.edit.undo"));
		_undoButton.addActionListener(_undoAction);
		_undoButton.setEnabled(false);
		toolbar.add(_undoButton);
		// Edit point
		_editPointButton = new JButton(new ImageIcon(getClass().getResource("images/edit_point_icon.gif")));
		_editPointButton.setToolTipText(I18nManager.getText("menu.edit.editpoint"));
		_editPointButton.addActionListener(_editPointAction);
		_editPointButton.setEnabled(false);
		toolbar.add(_editPointButton);
		// Select start, end
		_selectStartButton = new JButton(new ImageIcon(getClass().getResource("images/set_start_icon.png")));
		_selectStartButton.setToolTipText(I18nManager.getText("menu.select.start"));
		_selectStartButton.addActionListener(_selectStartAction);
		_selectStartButton.setEnabled(false);
		toolbar.add(_selectStartButton);
		_selectEndButton = new JButton(new ImageIcon(getClass().getResource("images/set_end_icon.png")));
		_selectEndButton.setToolTipText(I18nManager.getText("menu.select.end"));
		_selectEndButton.addActionListener(_selectEndAction);
		_selectEndButton.setEnabled(false);
		toolbar.add(_selectEndButton);
		_connectPhotoButton = new JButton(new ImageIcon(getClass().getResource("images/connect_photo_icon.png")));
		_connectPhotoButton.setToolTipText(I18nManager.getText("menu.photo.connect"));
		_connectPhotoButton.addActionListener(_connectPhotoAction);
		_connectPhotoButton.setEnabled(false);
		toolbar.add(_connectPhotoButton);
		// finish off
		toolbar.setFloatable(false);
		return toolbar;
	}


	/**
	 * Method to update menu when file loaded
	 */
	public void informFileLoaded()
	{
		// save, undo, delete enabled
		_saveItem.setEnabled(true);
		_undoItem.setEnabled(true);
		_deleteDuplicatesItem.setEnabled(true);
		_compressItem.setEnabled(true);
	}


	/**
	 * @see tim.prune.DataSubscriber#dataUpdated(tim.prune.data.Track)
	 */
	public void dataUpdated(byte inUpdateType)
	{
		boolean hasData = (_track != null && _track.getNumPoints() > 0);
		// set functions which require data
		_saveItem.setEnabled(hasData);
		_saveButton.setEnabled(hasData);
		_exportKmlItem.setEnabled(hasData);
		_exportPovItem.setEnabled(hasData);
		_deleteDuplicatesItem.setEnabled(hasData);
		_compressItem.setEnabled(hasData);
		_rearrangeMenu.setEnabled(hasData && _track.hasMixedData());
		_selectAllItem.setEnabled(hasData);
		_selectNoneItem.setEnabled(hasData);
		if (_show3dItem != null)
			_show3dItem.setEnabled(hasData);
		// is undo available?
		boolean hasUndo = !_app.getUndoStack().isEmpty();
		_undoItem.setEnabled(hasUndo);
		_undoButton.setEnabled(hasUndo);
		_clearUndoItem.setEnabled(hasUndo);
		// is there a current point?
		boolean hasPoint = (hasData && _selection.getCurrentPointIndex() >= 0);
		_editPointItem.setEnabled(hasPoint);
		_editPointButton.setEnabled(hasPoint);
		_editWaypointNameItem.setEnabled(hasPoint);
		_deletePointItem.setEnabled(hasPoint);
		_selectStartItem.setEnabled(hasPoint);
		_selectStartButton.setEnabled(hasPoint);
		_selectEndItem.setEnabled(hasPoint);
		_selectEndButton.setEnabled(hasPoint);
		// are there any photos?
		_saveExifItem.setEnabled(_photos != null && _photos.getNumPhotos() > 0);
		// is there a current photo?
		boolean hasPhoto = _photos != null && _photos.getNumPhotos() > 0
			&& _selection.getCurrentPhotoIndex() >= 0;
		// connect is only available when current photo is not connected to current point
		boolean connectAvailable = hasPhoto && hasPoint
			&& _track.getPoint(_selection.getCurrentPointIndex()).getPhoto() == null;
		_connectPhotoItem.setEnabled(connectAvailable);
		_connectPhotoButton.setEnabled(connectAvailable);
		_deletePhotoItem.setEnabled(hasPhoto);
		// is there a current range?
		boolean hasRange = (hasData && _selection.hasRangeSelected());
		_deleteRangeItem.setEnabled(hasRange);
		_interpolateItem.setEnabled(hasRange
			&& (_selection.getEnd() - _selection.getStart()) == 1);
		_reverseItem.setEnabled(hasRange);
	}
}
