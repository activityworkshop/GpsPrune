package tim.prune.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import tim.prune.App;
import tim.prune.DataSubscriber;
import tim.prune.I18nManager;
import tim.prune.data.Selection;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;

/**
 * Class to manage the menu bar,
 * including enabling and disabling the menu items
 */
public class MenuManager implements DataSubscriber
{
	private JFrame _parent = null;
	private App _app = null;
	private Track _track = null;
	private Selection _selection = null;

	// Menu items which need enabling/disabling
	JMenuItem _saveItem = null;
	JMenuItem _exportKmlItem = null;
	JMenuItem _exportPovItem = null;
	JMenuItem _undoItem = null;
	JMenuItem _clearUndoItem = null;
	JMenuItem _editPointItem = null;
	JMenuItem _editWaypointNameItem = null;
	JMenuItem _deletePointItem = null;
	JMenuItem _deleteRangeItem = null;
	JMenuItem _deleteDuplicatesItem = null;
	JMenuItem _compressItem = null;
	JMenuItem _interpolateItem = null;
	JMenuItem _selectAllItem = null;
	JMenuItem _selectNoneItem = null;
	JMenuItem _show3dItem = null;
	JMenuItem _reverseItem = null;
	JMenu     _rearrangeMenu = null;
	JMenuItem _rearrangeStartItem = null;
	JMenuItem _rearrangeEndItem = null;
	JMenuItem _rearrangeNearestItem = null;


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
		openMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.openFile();
			}
		});
		fileMenu.add(openMenuItem);
		// Add photos
		JMenuItem addPhotosMenuItem = new JMenuItem(I18nManager.getText("menu.file.addphotos"));
		addPhotosMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.addPhotos();
			}
		});
		// TODO: Re-add add photos menu item after v2
		// fileMenu.add(addPhotosMenuItem);
		// Save
		_saveItem = new JMenuItem(I18nManager.getText("menu.file.save"), KeyEvent.VK_S);
		_saveItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.saveFile();
			}
		});
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
		_undoItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.beginUndo();
			}
		});
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
		_editPointItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.editCurrentPoint();
			}
		});
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
		menubar.add(selectMenu);

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
		_clearUndoItem.setEnabled(hasUndo);
		// is there a current point?
		boolean hasPoint = (hasData && _selection.getCurrentPointIndex() >= 0);
		_editPointItem.setEnabled(hasPoint);
		_editWaypointNameItem.setEnabled(hasPoint);
		_deletePointItem.setEnabled(hasPoint);
		// is there a current range?
		boolean hasRange = (hasData && _selection.hasRangeSelected());
		_deleteRangeItem.setEnabled(hasRange);
		_interpolateItem.setEnabled(hasRange
			&& (_selection.getEnd() - _selection.getStart()) == 1);
		_reverseItem.setEnabled(hasRange);
	}
}
