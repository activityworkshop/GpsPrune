package tim.prune.gui;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import tim.prune.App;
import tim.prune.DataSubscriber;
import tim.prune.FunctionLibrary;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.config.Config;
import tim.prune.data.AudioClip;
import tim.prune.data.DataPoint;
import tim.prune.data.Field;
import tim.prune.data.Photo;
import tim.prune.data.RecentFile;
import tim.prune.data.RecentFileList;
import tim.prune.data.Selection;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;
import tim.prune.function.ChooseSingleParameter;
import tim.prune.function.PasteCoordinateList;
import tim.prune.function.PasteCoordinates;
import tim.prune.function.PlusCodeFunction;
import tim.prune.function.SearchOpenCachingDeFunction;
import tim.prune.function.browser.UrlGenerator;
import tim.prune.function.browser.WebMapFunction;
import tim.prune.function.search.SearchMapillaryFunction;
import tim.prune.function.settings.SaveConfig;

/**
 * Class to manage the menu bar and tool bar,
 * including enabling and disabling the items
 */
public class MenuManager implements DataSubscriber
{
	private App _app = null;
	private Track _track = null;
	private Selection _selection = null;

	// Menu items which need enabling/disabling
	private JMenuItem _sendGpsItem = null;
	private JMenuItem _saveItem = null;
	private JMenuItem _exportKmlItem = null;
	private JMenuItem _exportGpxItem = null;
	private JMenuItem _exportPovItem = null;
	private JMenuItem _exportImageItem = null;
	private JMenu     _recentFileMenu = null;
	private JMenuItem _undoItem = null;
	private JMenuItem _clearUndoItem = null;
	private JMenuItem _editPointItem = null;
	private JMenuItem _editWaypointNameItem = null;
	private JMenuItem _deletePointItem = null;
	private JMenuItem _deleteRangeItem = null;
	private JMenuItem _cropTrackItem = null;
	private JMenuItem _compressItem = null;
	private JMenuItem _markRectangleItem = null;
	private JMenuItem _markUphillLiftsItem = null;
	private JMenuItem _deleteMarkedPointsItem = null;
	private JMenuItem _deleteByDateItem = null;
	private JMenuItem _interpolateItem = null;
	private JMenuItem _averageItem = null;
	private JMenuItem _selectAllItem = null;
	private JMenuItem _selectNoneItem = null;
	private JMenuItem _selectSegmentItem = null;
	private JMenuItem _selectStartItem = null;
	private JMenuItem _selectEndItem = null;
	private JMenuItem _findWaypointItem = null;
	private JMenuItem _duplicatePointItem = null;
	private JMenuItem _projectPointItem = null;
	private JMenuItem _reverseItem = null;
	private JMenuItem _addTimeOffsetItem = null;
	private JMenuItem _addAltitudeOffsetItem = null;
	private JMenuItem _mergeSegmentsItem = null;
	private JMenuItem _rearrangeWaypointsItem = null;
	private JMenuItem _splitSegmentsItem = null;
	private JMenuItem _sewSegmentsItem = null;
	private JMenuItem _createMarkerWaypointsItem = null;
	private JMenuItem _cutAndMoveItem = null;
	private JMenuItem _convertNamesToTimesItem = null;
	private JMenuItem _deleteFieldValuesItem = null;
	private JCheckBoxMenuItem _mapCheckbox = null;
	private JMenuItem _show3dItem = null;
	private JMenu     _browserMapMenu = null;
	private JMenuItem _routingGraphHopperItem = null;
	private JMenuItem _chartItem = null;
	private JMenuItem _lookupSrtmItem = null;
	private JMenuItem _downloadSrtmItem = null;
	private JMenuItem _nearbyWikipediaItem = null;
	private JMenuItem _nearbyOsmPoiItem = null;
	private JMenuItem _showPeakfinderItem = null;
	private JMenuItem _showGeohackItem = null;
	private JMenuItem _searchOpencachingDeItem = null;
	private JMenuItem _searchMapillaryItem = null;
	private JMenuItem _downloadOsmItem = null;
	private JMenuItem _getWeatherItem = null;
	private JMenuItem _distanceItem = null;
	private JMenuItem _viewFullDetailsItem = null;
	private JMenuItem _estimateTimeItem = null;
	private JMenuItem _learnEstimationParams = null;
	private JMenuItem _autoplayTrack = null;
	private JMenuItem _saveExifItem = null;
	private JMenuItem _photoPopupItem = null;
	private JMenuItem _selectNoPhotoItem = null;
	private JMenuItem _connectPhotoItem = null;
	private JMenuItem _removePhotoItem = null;
	private JMenuItem _disconnectPhotoItem = null;
	private JMenuItem _correlatePhotosItem = null;
	private JMenuItem _rearrangePhotosItem = null;
	private JMenuItem _rotatePhotoLeft = null;
	private JMenuItem _rotatePhotoRight = null;
	private JMenuItem _ignoreExifThumb = null;
	private JMenuItem _connectAudioItem = null;
	private JMenuItem _disconnectAudioItem = null;
	private JMenuItem _removeAudioItem = null;
	private JMenuItem _correlateAudiosItem = null;
	private JMenuItem _selectNoAudioItem = null;
	private JCheckBoxMenuItem _onlineCheckbox = null;
	private JCheckBoxMenuItem _autosaveSettingsCheckbox = null;

	// ActionListeners for reuse by menu and toolbar
	private ActionListener _openFileAction = null;
	private ActionListener _addPhotoAction = null;
	private ActionListener _saveAction = null;
	private ActionListener _undoAction = null;
	private ActionListener _editPointAction = null;
	private ActionListener _deletePointAction = null;
	private ActionListener _selectStartAction = null;
	private ActionListener _selectEndAction = null;

	// Toolbar buttons which need enabling/disabling
	private JButton _saveButton = null;
	private JButton _undoButton = null;
	private JButton _editPointButton = null;
	private JButton _deletePointButton = null;
	private JButton _deleteRangeButton = null;
	private JButton _cutAndMoveButton = null;
	private JButton _selectStartButton = null;
	private JButton _selectEndButton = null;
	private JButton _connectButton = null;

	/** Array of key events */
	private static final int[] KEY_EVENTS = {
		KeyEvent.VK_A, KeyEvent.VK_B, KeyEvent.VK_C, KeyEvent.VK_D, KeyEvent.VK_E,
		KeyEvent.VK_F, KeyEvent.VK_G, KeyEvent.VK_H, KeyEvent.VK_I, KeyEvent.VK_J,
		KeyEvent.VK_K, KeyEvent.VK_L, KeyEvent.VK_M, KeyEvent.VK_N, KeyEvent.VK_O,
		KeyEvent.VK_P, KeyEvent.VK_Q, KeyEvent.VK_R, KeyEvent.VK_S, KeyEvent.VK_T,
		KeyEvent.VK_U, KeyEvent.VK_V, KeyEvent.VK_W, KeyEvent.VK_X, KeyEvent.VK_Y, KeyEvent.VK_Z};


	/**
	 * Constructor
	 * @param inApp application to call on menu actions
	 * @param inTrackInfo track info object
	 */
	public MenuManager(App inApp, TrackInfo inTrackInfo)
	{
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
		setAltKey(fileMenu, "altkey.menu.file");
		// Open file
		JMenuItem openMenuItem = new JMenuItem(I18nManager.getText("function.open"));
		setShortcut(openMenuItem, "shortcut.menu.file.open");
		_openFileAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.openFile();
			}
		};
		openMenuItem.addActionListener(_openFileAction);
		fileMenu.add(openMenuItem);
		// import through gpsbabel
		JMenuItem importBabelItem = makeMenuItem(FunctionLibrary.FUNCTION_IMPORTBABEL);
		fileMenu.add(importBabelItem);
		// Add photos
		JMenuItem addPhotosMenuItem = new JMenuItem(I18nManager.getText("menu.file.addphotos"));
		_addPhotoAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.addPhotos();
			}
		};
		addPhotosMenuItem.addActionListener(_addPhotoAction);
		fileMenu.add(addPhotosMenuItem);
		// Add audio clips
		JMenuItem addAudioMenuItem = makeMenuItem(FunctionLibrary.FUNCTION_LOAD_AUDIO);
		fileMenu.add(addAudioMenuItem);
		// recent files
		_recentFileMenu = new JMenu(I18nManager.getText("menu.file.recentfiles"));
		_recentFileMenu.setEnabled(false);
		fileMenu.add(_recentFileMenu);
		fileMenu.addSeparator();
		// Load from GPS
		JMenuItem loadFromGpsMenuItem = makeMenuItem(FunctionLibrary.FUNCTION_GPSLOAD);
		setShortcut(loadFromGpsMenuItem, "shortcut.menu.file.load");
		fileMenu.add(loadFromGpsMenuItem);
		// Send to GPS
		_sendGpsItem = makeMenuItem(FunctionLibrary.FUNCTION_GPSSAVE, false);
		fileMenu.add(_sendGpsItem);
		fileMenu.addSeparator();
		// Save
		_saveItem = new JMenuItem(I18nManager.getText("menu.file.save"), KeyEvent.VK_S);
		setShortcut(_saveItem, "shortcut.menu.file.save");
		_saveAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.saveFile();
			}
		};
		_saveItem.addActionListener(_saveAction);
		_saveItem.setEnabled(false);
		fileMenu.add(_saveItem);
		// Export - Kml
		_exportKmlItem = makeMenuItem(FunctionLibrary.FUNCTION_KMLEXPORT, false);
		fileMenu.add(_exportKmlItem);
		// Gpx
		_exportGpxItem = makeMenuItem(FunctionLibrary.FUNCTION_GPXEXPORT, false);
		fileMenu.add(_exportGpxItem);
		// Pov
		_exportPovItem = makeMenuItem(FunctionLibrary.FUNCTION_POVEXPORT, false);
		fileMenu.add(_exportPovItem);
		// Image
		_exportImageItem = makeMenuItem(FunctionLibrary.FUNCTION_IMAGEEXPORT, false);
		fileMenu.add(_exportImageItem);
		fileMenu.addSeparator();
		// Exit
		JMenuItem exitMenuItem = new JMenuItem(I18nManager.getText("menu.file.exit"));
		exitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.exit();
			}
		});
		fileMenu.add(exitMenuItem);
		menubar.add(fileMenu);

		////////////////////////////////////////////////////
		// Online menu
		JMenu onlineMenu = new JMenu(I18nManager.getText("menu.online"));
		setAltKey(onlineMenu, "altkey.menu.online");
		// SRTM
		_lookupSrtmItem = makeMenuItem(FunctionLibrary.FUNCTION_LOOKUP_SRTM, false);
		onlineMenu.add(_lookupSrtmItem);
		_downloadSrtmItem = makeMenuItem(FunctionLibrary.FUNCTION_DOWNLOAD_SRTM, false);
		onlineMenu.add(_downloadSrtmItem);

		onlineMenu.addSeparator();
		// browser submenu
		_browserMapMenu = new JMenu(I18nManager.getText("menu.view.browser"));
		_browserMapMenu.setEnabled(false);
		JMenuItem googleMapsItem = makeMenuItem(new WebMapFunction(_app, UrlGenerator.WebService.MAP_SOURCE_GOOGLE, "menu.view.browser.google"));
		_browserMapMenu.add(googleMapsItem);
		JMenuItem openMapsItem = makeMenuItem(new WebMapFunction(_app, UrlGenerator.WebService.MAP_SOURCE_OSM, "menu.view.browser.openstreetmap"));
		_browserMapMenu.add(openMapsItem);
		JMenuItem mapquestMapsItem = makeMenuItem(new WebMapFunction(_app, UrlGenerator.WebService.MAP_SOURCE_MAPQUEST, "menu.view.browser.mapquest"));
		_browserMapMenu.add(mapquestMapsItem);
		JMenuItem yahooMapsItem = makeMenuItem(new WebMapFunction(_app, UrlGenerator.WebService.MAP_SOURCE_YAHOO, "menu.view.browser.yahoo"));
		_browserMapMenu.add(yahooMapsItem);
		JMenuItem bingMapsItem = makeMenuItem(new WebMapFunction(_app, UrlGenerator.WebService.MAP_SOURCE_BING, "menu.view.browser.bing"));
		_browserMapMenu.add(bingMapsItem);
		JMenuItem inlineMapItem = makeMenuItem(new WebMapFunction(_app, UrlGenerator.WebService.MAP_SOURCE_INLINESKATE, "menu.view.browser.inlinemap"));
		_browserMapMenu.add(inlineMapItem);
		_routingGraphHopperItem = makeMenuItem(new WebMapFunction(_app, UrlGenerator.WebService.MAP_SOURCE_GRAPHHOPPER, "menu.view.browser.graphhopper"));
		_browserMapMenu.add(_routingGraphHopperItem);
		onlineMenu.add(_browserMapMenu);
		// wikipedia
		_nearbyWikipediaItem = makeMenuItem(FunctionLibrary.FUNCTION_NEARBY_WIKIPEDIA, false);
		onlineMenu.add(_nearbyWikipediaItem);
		JMenuItem searchWikipediaNamesItem = makeMenuItem(FunctionLibrary.FUNCTION_SEARCH_WIKIPEDIA);
		onlineMenu.add(searchWikipediaNamesItem);
		_nearbyOsmPoiItem = makeMenuItem(FunctionLibrary.FUNCTION_SEARCH_OSMPOIS);
		onlineMenu.add(_nearbyOsmPoiItem);
		_showPeakfinderItem = makeMenuItem(new WebMapFunction(_app, UrlGenerator.WebService.MAP_SOURCE_PEAKFINDER, "webservice.peakfinder"), false);
		onlineMenu.add(_showPeakfinderItem);
		_showGeohackItem = makeMenuItem(new WebMapFunction(_app, UrlGenerator.WebService.MAP_SOURCE_GEOHACK, "webservice.geohack"), false);
		onlineMenu.add(_showGeohackItem);

		onlineMenu.addSeparator();
		_searchOpencachingDeItem = makeMenuItem(new SearchOpenCachingDeFunction(_app), false);
		onlineMenu.add(_searchOpencachingDeItem);
		_searchMapillaryItem = makeMenuItem(new SearchMapillaryFunction(_app), false);
		onlineMenu.add(_searchMapillaryItem);
		_downloadOsmItem = makeMenuItem(FunctionLibrary.FUNCTION_DOWNLOAD_OSM, false);
		onlineMenu.add(_downloadOsmItem);
		_getWeatherItem = makeMenuItem(FunctionLibrary.FUNCTION_GET_WEATHER_FORECAST, false);
		onlineMenu.add(_getWeatherItem);
		menubar.add(onlineMenu);

		////////////////////////////////////////////////////
		// Track menu
		JMenu trackMenu = new JMenu(I18nManager.getText("menu.track"));
		setAltKey(trackMenu, "altkey.menu.track");
		_undoItem = new JMenuItem(I18nManager.getText("menu.track.undo"));
		setShortcut(_undoItem, "shortcut.menu.track.undo");
		_undoAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.beginUndo();
			}
		};
		_undoItem.addActionListener(_undoAction);
		_undoItem.setEnabled(false);
		trackMenu.add(_undoItem);
		_clearUndoItem = new JMenuItem(I18nManager.getText("menu.track.clearundo"));
		_clearUndoItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.clearUndo();
			}
		});
		_clearUndoItem.setEnabled(false);
		trackMenu.add(_clearUndoItem);
		trackMenu.addSeparator();
		_compressItem = makeMenuItem(FunctionLibrary.FUNCTION_COMPRESS, false);
		setShortcut(_compressItem, "shortcut.menu.track.compress");
		trackMenu.add(_compressItem);
		_markRectangleItem = new JMenuItem(I18nManager.getText("menu.track.markrectangle"));
		_markRectangleItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.setCurrentMode(App.AppMode.DRAWRECT);
				UpdateMessageBroker.informSubscribers();
			}
		});
		_markRectangleItem.setEnabled(false);
		trackMenu.add(_markRectangleItem);
		_markUphillLiftsItem = makeMenuItem(FunctionLibrary.FUNCTION_MARK_LIFTS, false);
		trackMenu.add(_markUphillLiftsItem);
		_deleteMarkedPointsItem = makeMenuItem(FunctionLibrary.FUNCTION_DELETE_MARKED_POINTS, false);
		trackMenu.add(_deleteMarkedPointsItem);
		_deleteByDateItem = makeMenuItem(FunctionLibrary.FUNCTION_DELETE_BY_DATE, false);
		trackMenu.add(_deleteByDateItem);
		trackMenu.addSeparator();
		// Rearrange waypoints
		_rearrangeWaypointsItem = makeMenuItem(FunctionLibrary.FUNCTION_REARRANGE_WAYPOINTS, false);
		trackMenu.add(_rearrangeWaypointsItem);
		// Split track segments
		_splitSegmentsItem = makeMenuItem(FunctionLibrary.FUNCTION_SPLIT_SEGMENTS, false);
		trackMenu.add(_splitSegmentsItem);
		// Sew track segments
		_sewSegmentsItem = makeMenuItem(FunctionLibrary.FUNCTION_SEW_SEGMENTS, false);
		trackMenu.add(_sewSegmentsItem);
		// Create marker waypoints
		_createMarkerWaypointsItem = makeMenuItem(FunctionLibrary.FUNCTION_CREATE_MARKER_WAYPOINTS, false);
		trackMenu.add(_createMarkerWaypointsItem);
		trackMenu.addSeparator();
		_learnEstimationParams = makeMenuItem(FunctionLibrary.FUNCTION_LEARN_ESTIMATION_PARAMS, false);
		trackMenu.add(_learnEstimationParams);
		menubar.add(trackMenu);

		// Range menu
		JMenu rangeMenu = new JMenu(I18nManager.getText("menu.range"));
		setAltKey(rangeMenu, "altkey.menu.range");
		_selectAllItem = new JMenuItem(I18nManager.getText("menu.range.all"));
		setShortcut(_selectAllItem, "shortcut.menu.range.all");
		_selectAllItem.setEnabled(false);
		_selectAllItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_selection.selectRange(0, _track.getNumPoints()-1);
			}
		});
		rangeMenu.add(_selectAllItem);
		_selectNoneItem = new JMenuItem(I18nManager.getText("menu.range.none"));
		_selectNoneItem.setEnabled(false);
		_selectNoneItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.selectNone();
			}
		});
		rangeMenu.add(_selectNoneItem);
		_selectSegmentItem = makeMenuItem(FunctionLibrary.FUNCTION_SELECT_SEGMENT);
		rangeMenu.add(_selectSegmentItem);
		rangeMenu.addSeparator();
		_selectStartItem = new JMenuItem(I18nManager.getText("menu.range.start"));
		_selectStartItem.setEnabled(false);
		_selectStartAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_selection.selectRangeStart();
			}
		};
		_selectStartItem.addActionListener(_selectStartAction);
		rangeMenu.add(_selectStartItem);
		_selectEndItem = new JMenuItem(I18nManager.getText("menu.range.end"));
		_selectEndItem.setEnabled(false);
		_selectEndAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_selection.selectRangeEnd();
			}
		};
		_selectEndItem.addActionListener(_selectEndAction);
		rangeMenu.add(_selectEndItem);
		rangeMenu.addSeparator();
		_deleteRangeItem = makeMenuItem(FunctionLibrary.FUNCTION_DELETE_RANGE, false);
		rangeMenu.add(_deleteRangeItem);
		_cropTrackItem = makeMenuItem(FunctionLibrary.FUNCTION_CROP_TRACK, false);
		rangeMenu.add(_cropTrackItem);
		_reverseItem = new JMenuItem(I18nManager.getText("menu.range.reverse"));
		_reverseItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.reverseRange();
			}
		});
		_reverseItem.setEnabled(false);
		rangeMenu.add(_reverseItem);
		_addTimeOffsetItem = makeMenuItem(FunctionLibrary.FUNCTION_ADD_TIME_OFFSET, false);
		rangeMenu.add(_addTimeOffsetItem);
		_addAltitudeOffsetItem = makeMenuItem(FunctionLibrary.FUNCTION_ADD_ALTITUDE_OFFSET, false);
		rangeMenu.add(_addAltitudeOffsetItem);
		_mergeSegmentsItem = new JMenuItem(I18nManager.getText("menu.range.mergetracksegments"));
		_mergeSegmentsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.mergeTrackSegments();
			}
		});
		_mergeSegmentsItem.setEnabled(false);
		rangeMenu.add(_mergeSegmentsItem);
		_deleteFieldValuesItem = makeMenuItem(FunctionLibrary.FUNCTION_DELETE_FIELD_VALUES, false);
		rangeMenu.add(_deleteFieldValuesItem);
		rangeMenu.addSeparator();
		_interpolateItem = makeMenuItem(new ChooseSingleParameter(_app, FunctionLibrary.FUNCTION_INTERPOLATE), false);
		rangeMenu.add(_interpolateItem);
		_averageItem = new JMenuItem(I18nManager.getText("menu.range.average"));
		_averageItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.averageSelection();
			}
		});
		_averageItem.setEnabled(false);
		rangeMenu.add(_averageItem);
		_cutAndMoveItem = new JMenuItem(I18nManager.getText("menu.range.cutandmove"));
		_cutAndMoveItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.cutAndMoveSelection();
			}
		});
		_cutAndMoveItem.setEnabled(false);
		rangeMenu.add(_cutAndMoveItem);
		_convertNamesToTimesItem = makeMenuItem(FunctionLibrary.FUNCTION_CONVERT_NAMES_TO_TIMES, false);
		rangeMenu.add(_convertNamesToTimesItem);
		menubar.add(rangeMenu);

		// Point menu
		JMenu pointMenu = new JMenu(I18nManager.getText("menu.point"));
		setAltKey(pointMenu, "altkey.menu.point");
		_editPointItem = new JMenuItem(I18nManager.getText("menu.point.editpoint"));
		_editPointAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.editCurrentPoint();
			}
		};
		_editPointItem.addActionListener(_editPointAction);
		_editPointItem.setEnabled(false);
		setShortcut(_editPointItem, "shortcut.menu.point.edit");
		pointMenu.add(_editPointItem);
		_editWaypointNameItem = makeMenuItem(FunctionLibrary.FUNCTION_EDIT_WAYPOINT_NAME, false);
		pointMenu.add(_editWaypointNameItem);
		_deletePointItem = new JMenuItem(I18nManager.getText("menu.point.deletepoint"));
		_deletePointAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.deleteCurrentPoint();
			}
		};
		_deletePointItem.addActionListener(_deletePointAction);
		_deletePointItem.setEnabled(false);
		_deletePointItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		pointMenu.add(_deletePointItem);
		pointMenu.addSeparator();
		// find a waypoint
		_findWaypointItem = makeMenuItem(FunctionLibrary.FUNCTION_FIND_WAYPOINT, false);
		pointMenu.add(_findWaypointItem);
		// duplicate current point
		_duplicatePointItem = makeMenuItem(FunctionLibrary.FUNCTION_DUPLICATE_POINT, false);
		pointMenu.add(_duplicatePointItem);
		// project current point
		_projectPointItem = makeMenuItem(FunctionLibrary.FUNCTION_PROJECT_POINT, false);
		pointMenu.add(_projectPointItem);
		// paste coordinates function
		JMenuItem pasteCoordsItem = makeMenuItem(new PasteCoordinates(_app));
		pointMenu.add(pasteCoordsItem);
		JMenuItem pasteCoordsListItem = makeMenuItem(new PasteCoordinateList(_app));
		pointMenu.add(pasteCoordsListItem);
		// pluscodes function
		JMenuItem plusCodeItem = makeMenuItem(new PlusCodeFunction(_app));
		pointMenu.add(plusCodeItem);
		menubar.add(pointMenu);

		// Add view menu
		JMenu viewMenu = new JMenu(I18nManager.getText("menu.view"));
		setAltKey(viewMenu, "altkey.menu.view");
		// Turn map display on/off
		_mapCheckbox = new JCheckBoxMenuItem(
			I18nManager.getText("menu.map.showmap"), false);
		_mapCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Config.setConfigBoolean(Config.KEY_SHOW_MAP, _mapCheckbox.isSelected());
				UpdateMessageBroker.informSubscribers(MAPSERVER_CHANGED);
			}
		});
		viewMenu.add(_mapCheckbox);
		// Turn off the sidebars
		JCheckBoxMenuItem sidebarsCheckbox = new JCheckBoxMenuItem(I18nManager.getText("menu.view.showsidebars"));
		sidebarsCheckbox.setSelected(true);
		sidebarsCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.toggleSidebars();
			}
		});
		sidebarsCheckbox.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0)); // shortcut F11
		viewMenu.add(sidebarsCheckbox);
		// 3d
		_show3dItem = makeMenuItem(FunctionLibrary.FUNCTION_3D, false);
		viewMenu.add(_show3dItem);
		// Charts
		_chartItem = makeMenuItem(FunctionLibrary.FUNCTION_CHARTS, false);
		viewMenu.add(_chartItem);
		viewMenu.addSeparator();
		// Distances
		_distanceItem = makeMenuItem(FunctionLibrary.FUNCTION_DISTANCES, false);
		viewMenu.add(_distanceItem);
		// full range details
		_viewFullDetailsItem = makeMenuItem(FunctionLibrary.FUNCTION_FULL_DETAILS, false);
		viewMenu.add(_viewFullDetailsItem);
		// estimate time
		_estimateTimeItem = makeMenuItem(FunctionLibrary.FUNCTION_ESTIMATE_TIME, false);
		viewMenu.add(_estimateTimeItem);
		viewMenu.addSeparator();
		// autoplay
		_autoplayTrack = makeMenuItem(FunctionLibrary.FUNCTION_AUTOPLAY_TRACK, false);
		viewMenu.add(_autoplayTrack);
		menubar.add(viewMenu);

		// Add photo menu
		JMenu photoMenu = new JMenu(I18nManager.getText("menu.photo"));
		setAltKey(photoMenu, "altkey.menu.photo");
		addPhotosMenuItem = new JMenuItem(I18nManager.getText("menu.file.addphotos"));
		addPhotosMenuItem.addActionListener(_addPhotoAction);
		photoMenu.add(addPhotosMenuItem);
		_saveExifItem = new JMenuItem(I18nManager.getText("menu.photo.saveexif"));
		_saveExifItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.saveExif();
			}
		});
		_saveExifItem.setEnabled(false);
		photoMenu.add(_saveExifItem);
		// Deselect current photo
		_selectNoPhotoItem = new JMenuItem(I18nManager.getText("menu.range.none"));
		_selectNoPhotoItem.setEnabled(false);
		_selectNoPhotoItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.getTrackInfo().selectPhoto(-1);
			}
		});
		photoMenu.add(_selectNoPhotoItem);
		photoMenu.addSeparator();
		_connectPhotoItem = makeMenuItem(FunctionLibrary.FUNCTION_CONNECT_TO_POINT, false);
		photoMenu.add(_connectPhotoItem);
		// disconnect photo
		_disconnectPhotoItem = makeMenuItem(FunctionLibrary.FUNCTION_DISCONNECT_PHOTO, false);
		photoMenu.add(_disconnectPhotoItem);
		_removePhotoItem = makeMenuItem(FunctionLibrary.FUNCTION_REMOVE_PHOTO, false);
		photoMenu.add(_removePhotoItem);
		// Rotate current photo
		_rotatePhotoLeft = makeMenuItem(FunctionLibrary.FUNCTION_ROTATE_PHOTO_LEFT, false);
		photoMenu.add(_rotatePhotoLeft);
		_rotatePhotoRight = makeMenuItem(FunctionLibrary.FUNCTION_ROTATE_PHOTO_RIGHT, false);
		photoMenu.add(_rotatePhotoRight);
		// Show photo popup
		_photoPopupItem = makeMenuItem(FunctionLibrary.FUNCTION_PHOTO_POPUP, false);
		photoMenu.add(_photoPopupItem);
		_ignoreExifThumb = makeMenuItem(FunctionLibrary.FUNCTION_IGNORE_EXIF_THUMB, false);
		photoMenu.add(_ignoreExifThumb);
		photoMenu.addSeparator();
		// correlate all photos
		_correlatePhotosItem = makeMenuItem(FunctionLibrary.FUNCTION_CORRELATE_PHOTOS, false);
		photoMenu.add(_correlatePhotosItem);
		// rearrange photo points
		_rearrangePhotosItem = makeMenuItem(FunctionLibrary.FUNCTION_REARRANGE_PHOTOS, false);
		photoMenu.add(_rearrangePhotosItem);
		menubar.add(photoMenu);

		// Audio menu
		JMenu audioMenu = new JMenu(I18nManager.getText("menu.audio"));
		setAltKey(audioMenu, "altkey.menu.audio");
		addAudioMenuItem = makeMenuItem(FunctionLibrary.FUNCTION_LOAD_AUDIO);
		audioMenu.add(addAudioMenuItem);
		_selectNoAudioItem = new JMenuItem(I18nManager.getText("menu.range.none"));
		_selectNoAudioItem.setEnabled(false);
		_selectNoAudioItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_app.getTrackInfo().selectAudio(-1);
			}
		});
		audioMenu.add(_selectNoAudioItem);
		audioMenu.addSeparator();
		// connect audio
		_connectAudioItem = makeMenuItem(FunctionLibrary.FUNCTION_CONNECT_TO_POINT, false);
		audioMenu.add(_connectAudioItem);
		// Disconnect current audio clip
		_disconnectAudioItem = makeMenuItem(FunctionLibrary.FUNCTION_DISCONNECT_AUDIO, false);
		audioMenu.add(_disconnectAudioItem);
		// Remove current audio clip
		_removeAudioItem = makeMenuItem(FunctionLibrary.FUNCTION_REMOVE_AUDIO, false);
		audioMenu.add(_removeAudioItem);
		audioMenu.addSeparator();
		// Correlate audio clips
		_correlateAudiosItem = makeMenuItem(FunctionLibrary.FUNCTION_CORRELATE_AUDIOS, false);
		audioMenu.add(_correlateAudiosItem);
		menubar.add(audioMenu);

		// Settings menu
		JMenu settingsMenu = new JMenu(I18nManager.getText("menu.settings"));
		setAltKey(settingsMenu, "altkey.menu.settings");
		// Set the map background
		JMenuItem mapBgItem = makeMenuItem(FunctionLibrary.FUNCTION_SET_MAP_BG);
		settingsMenu.add(mapBgItem);
		_onlineCheckbox = new JCheckBoxMenuItem(I18nManager.getText("menu.settings.onlinemode"));
		_onlineCheckbox.setSelected(Config.getConfigBoolean(Config.KEY_ONLINE_MODE));
		_onlineCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean isOnline = _onlineCheckbox.isSelected();
				Config.setConfigBoolean(Config.KEY_ONLINE_MODE, isOnline);
				if (isOnline) {UpdateMessageBroker.informSubscribers();}
			}
		});
		settingsMenu.add(_onlineCheckbox);
		settingsMenu.add(makeMenuItem(FunctionLibrary.FUNCTION_SET_DISK_CACHE));
		settingsMenu.addSeparator();
		// Set program paths
		settingsMenu.add(makeMenuItem(FunctionLibrary.FUNCTION_SET_PATHS));
		// Set colours
		settingsMenu.add(makeMenuItem(FunctionLibrary.FUNCTION_SET_COLOURS));
		// display settings
		JMenuItem setDisplaySettingsItem = makeMenuItem(FunctionLibrary.FUNCTION_SET_DISPLAY_SETTINGS);
		settingsMenu.add(setDisplaySettingsItem);
		// Set language
		settingsMenu.add(makeMenuItem(FunctionLibrary.FUNCTION_SET_LANGUAGE));
		// Set altitude tolerance
		settingsMenu.add(makeMenuItem(new ChooseSingleParameter(_app, FunctionLibrary.FUNCTION_SET_ALTITUDE_TOLERANCE)));
		// Set timezone
		settingsMenu.add(makeMenuItem(FunctionLibrary.FUNCTION_SET_TIMEZONE));
		settingsMenu.addSeparator();
		// Save configuration
		settingsMenu.add(makeMenuItem(FunctionLibrary.FUNCTION_SAVECONFIG));
		_autosaveSettingsCheckbox = new JCheckBoxMenuItem(
			I18nManager.getText("menu.settings.autosave"), false);
		_autosaveSettingsCheckbox.setSelected(Config.getConfigBoolean(Config.KEY_AUTOSAVE_SETTINGS));
		_autosaveSettingsCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final boolean autosaveOn = _autosaveSettingsCheckbox.isSelected();
				Config.setConfigBoolean(Config.KEY_AUTOSAVE_SETTINGS, autosaveOn);
				// Maybe want to save config?
				new SaveConfig(_app).autosaveSwitched(autosaveOn);
			}
		});
		settingsMenu.add(_autosaveSettingsCheckbox);
		menubar.add(settingsMenu);

		// Help menu
		JMenu helpMenu = new JMenu(I18nManager.getText("menu.help"));
		setAltKey(helpMenu, "altkey.menu.help");
		JMenuItem helpItem = makeMenuItem(FunctionLibrary.FUNCTION_HELP);
		setShortcut(helpItem, "shortcut.menu.help.help");
		helpMenu.add(helpItem);
		helpMenu.add(makeMenuItem(FunctionLibrary.FUNCTION_SHOW_KEYS));
		helpMenu.add(makeMenuItem(FunctionLibrary.FUNCTION_ABOUT));
		helpMenu.add(makeMenuItem(FunctionLibrary.FUNCTION_CHECK_VERSION));
		menubar.add(helpMenu);

		return menubar;
	}

	/**
	 * Convenience method for making a menu item using a function
	 * @param inFunction function
	 * @param inEnabled flag to specify initial enabled state
	 * @return menu item using localized name of function
	 */
	private static JMenuItem makeMenuItem(GenericFunction inFunction, boolean inEnabled)
	{
		JMenuItem item = makeMenuItem(inFunction);
		item.setEnabled(inEnabled);
		return item;
	}

	/**
	 * Convenience method for making a menu item using a function
	 * @param inFunction function
	 * @return menu item using localized name of function
	 */
	private static JMenuItem makeMenuItem(GenericFunction inFunction)
	{
		JMenuItem item = new JMenuItem(I18nManager.getText(inFunction.getNameKey()));
		item.addActionListener(new FunctionLauncher(inFunction));
		return item;
	}

	/**
	 * Set the alt key for the given menu
	 * @param inMenu menu to set
	 * @param inKey key to lookup to get language-sensitive altkey
	 */
	private static void setAltKey(JMenu inMenu, String inKey)
	{
		// Lookup the key in the properties
		String altKey = I18nManager.getText(inKey);
		if (altKey != null && altKey.length() == 1)
		{
			int code = altKey.charAt(0) - 'A';
			if (code >= 0 && code < 26)
			{
				// Found a valid code between A and Z
				inMenu.setMnemonic(KEY_EVENTS[code]);
			}
		}
	}

	/**
	 * Set the shortcut key for the given menu item
	 * @param inMenuItem menu item to set
	 * @param inKey key to lookup to get language-sensitive shortcut
	 */
	private static void setShortcut(JMenuItem inMenuItem, String inKey)
	{
		// Lookup the key in the properties
		String altKey = I18nManager.getText(inKey);
		if (altKey != null && altKey.length() == 1)
		{
			int code = altKey.charAt(0) - 'A';
			if (code >= 0 && code < 26)
			{
				// Found a valid code between A and Z
				inMenuItem.setAccelerator(KeyStroke.getKeyStroke(KEY_EVENTS[code],
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
				// use platform-specific key mask so Ctrl on Linux/Win, Clover on Mac
			}
		}
	}

	/**
	 * Create a JToolBar containing all toolbar buttons
	 * @return toolbar containing buttons
	 */
	public JToolBar createToolBar()
	{
		JToolBar toolbar = new JToolBar();
		// Add text file
		JButton openFileButton = new JButton(IconManager.getImageIcon(IconManager.OPEN_FILE));
		openFileButton.setToolTipText(I18nManager.getText("function.open"));
		openFileButton.addActionListener(_openFileAction);
		toolbar.add(openFileButton);
		// Add photo
		JButton addPhotoButton = new JButton(IconManager.getImageIcon(IconManager.ADD_PHOTO));
		addPhotoButton.setToolTipText(I18nManager.getText("menu.file.addphotos"));
		addPhotoButton.addActionListener(_addPhotoAction);
		toolbar.add(addPhotoButton);
		// Save
		_saveButton = new JButton(IconManager.getImageIcon(IconManager.SAVE_FILE));
		_saveButton.setToolTipText(I18nManager.getText("menu.file.save"));
		_saveButton.addActionListener(_saveAction);
		_saveButton.setEnabled(false);
		toolbar.add(_saveButton);
		// Undo
		_undoButton = new JButton(IconManager.getImageIcon(IconManager.UNDO));
		_undoButton.setToolTipText(I18nManager.getText("menu.track.undo"));
		_undoButton.addActionListener(_undoAction);
		_undoButton.setEnabled(false);
		toolbar.add(_undoButton);
		// Edit point
		_editPointButton = new JButton(IconManager.getImageIcon(IconManager.EDIT_POINT));
		_editPointButton.setToolTipText(I18nManager.getText("menu.point.editpoint"));
		_editPointButton.addActionListener(_editPointAction);
		_editPointButton.setEnabled(false);
		toolbar.add(_editPointButton);
		// Delete point
		_deletePointButton = new JButton(IconManager.getImageIcon(IconManager.DELETE_POINT));
		_deletePointButton.setToolTipText(I18nManager.getText("menu.point.deletepoint"));
		_deletePointButton.addActionListener(_deletePointAction);
		_deletePointButton.setEnabled(false);
		toolbar.add(_deletePointButton);
		// Delete range
		_deleteRangeButton = new JButton(IconManager.getImageIcon(IconManager.DELETE_RANGE));
		_deleteRangeButton.setToolTipText(I18nManager.getText("function.deleterange"));
		_deleteRangeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				FunctionLibrary.FUNCTION_DELETE_RANGE.begin();
			}
		});
		_deleteRangeButton.setEnabled(false);
		toolbar.add(_deleteRangeButton);
		// Cut and move
		_cutAndMoveButton = new JButton(IconManager.getImageIcon(IconManager.CUT_AND_MOVE));
		_cutAndMoveButton.setToolTipText(I18nManager.getText("menu.range.cutandmove"));
		_cutAndMoveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				_app.cutAndMoveSelection();
			}
		});
		_cutAndMoveButton.setEnabled(false);
		toolbar.add(_cutAndMoveButton);
		// Select start, end
		_selectStartButton = new JButton(IconManager.getImageIcon(IconManager.SET_RANGE_START));
		_selectStartButton.setToolTipText(I18nManager.getText("menu.range.start"));
		_selectStartButton.addActionListener(_selectStartAction);
		_selectStartButton.setEnabled(false);
		toolbar.add(_selectStartButton);
		_selectEndButton = new JButton(IconManager.getImageIcon(IconManager.SET_RANGE_END));
		_selectEndButton.setToolTipText(I18nManager.getText("menu.range.end"));
		_selectEndButton.addActionListener(_selectEndAction);
		_selectEndButton.setEnabled(false);
		toolbar.add(_selectEndButton);
		// Connect to point
		_connectButton = new JButton(IconManager.getImageIcon(IconManager.CONNECT_PHOTO));
		_connectButton.setToolTipText(I18nManager.getText(FunctionLibrary.FUNCTION_CONNECT_TO_POINT.getNameKey()));
		_connectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				FunctionLibrary.FUNCTION_CONNECT_TO_POINT.begin();
			}
		});
		_connectButton.setEnabled(false);
		toolbar.add(_connectButton);
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
		_sendGpsItem.setEnabled(true);
		_saveItem.setEnabled(true);
		_undoItem.setEnabled(true);
		_compressItem.setEnabled(true);
		_deleteMarkedPointsItem.setEnabled(false);
	}


	/**
	 * @see tim.prune.DataSubscriber#dataUpdated(tim.prune.data.Track)
	 */
	public void dataUpdated(byte inUpdateType)
	{
		final boolean hasData = _track != null && _track.getNumPoints() > 0;
		final boolean hasMultiplePoints = hasData && _track.getNumPoints() > 1;

		// set functions which require data
		_sendGpsItem.setEnabled(hasData);
		_saveItem.setEnabled(hasData);
		_saveButton.setEnabled(hasData);
		_exportKmlItem.setEnabled(hasData);
		_exportGpxItem.setEnabled(hasData);
		_exportPovItem.setEnabled(hasMultiplePoints);
		_exportImageItem.setEnabled(hasMultiplePoints);
		_compressItem.setEnabled(hasData);
		_markRectangleItem.setEnabled(hasData);
		_markUphillLiftsItem.setEnabled(hasData && _track.hasAltitudeData());
		_deleteMarkedPointsItem.setEnabled(hasData && _track.hasMarkedPoints());
		_rearrangeWaypointsItem.setEnabled(hasData && _track.hasWaypoints() && _track.getNumPoints() > 1);
		final boolean hasSeveralTrackPoints = hasData && _track.hasTrackPoints() && _track.getNumPoints() > 3;
		_splitSegmentsItem.setEnabled(hasSeveralTrackPoints);
		_sewSegmentsItem.setEnabled(hasSeveralTrackPoints);
		_createMarkerWaypointsItem.setEnabled(hasSeveralTrackPoints);
		_selectAllItem.setEnabled(hasData);
		_selectNoneItem.setEnabled(hasData);
		_show3dItem.setEnabled(hasMultiplePoints);
		_chartItem.setEnabled(hasData);
		_browserMapMenu.setEnabled(hasData);
		_distanceItem.setEnabled(hasData);
		_autoplayTrack.setEnabled(hasData && _track.getNumPoints() > 3);
		_lookupSrtmItem.setEnabled(hasData);
		_nearbyWikipediaItem.setEnabled(hasData);
		_nearbyOsmPoiItem.setEnabled(hasData);
		_downloadOsmItem.setEnabled(hasData);
		_getWeatherItem.setEnabled(hasData);
		_findWaypointItem.setEnabled(hasData && _track.hasWaypoints());
		// have we got a cache?
		_downloadSrtmItem.setEnabled(hasData && Config.getConfigString(Config.KEY_DISK_CACHE) != null);
		// have we got any timestamps?
		_deleteByDateItem.setEnabled(hasData && _track.hasData(Field.TIMESTAMP));

		// is undo available?
		boolean hasUndo = !_app.getUndoStack().isEmpty();
		_undoItem.setEnabled(hasUndo);
		_undoButton.setEnabled(hasUndo);
		_clearUndoItem.setEnabled(hasUndo);
		// is there a current point?
		DataPoint currPoint = _app.getTrackInfo().getCurrentPoint();
		boolean hasPoint = (currPoint != null);
		_editPointItem.setEnabled(hasPoint);
		_editPointButton.setEnabled(hasPoint);
		_editWaypointNameItem.setEnabled(hasPoint);
		_deletePointItem.setEnabled(hasPoint);
		_deletePointButton.setEnabled(hasPoint);
		_selectStartItem.setEnabled(hasPoint);
		_selectStartButton.setEnabled(hasPoint);
		_selectEndItem.setEnabled(hasPoint);
		_selectEndButton.setEnabled(hasPoint);
		_duplicatePointItem.setEnabled(hasPoint);
		_projectPointItem.setEnabled(hasPoint);
		_showPeakfinderItem.setEnabled(hasPoint);
		_showGeohackItem.setEnabled(hasPoint);
		_searchOpencachingDeItem.setEnabled(hasPoint);
		_searchMapillaryItem.setEnabled(hasPoint);
		// is it a waypoint?
		_selectSegmentItem.setEnabled(hasPoint && !currPoint.isWaypoint());
		// are there any photos?
		boolean anyPhotos = _app.getTrackInfo().getPhotoList().getNumPhotos() > 0;
		_saveExifItem.setEnabled(anyPhotos && _app.getTrackInfo().getPhotoList().hasMediaWithFile());
		// is there a current photo, audio?
		Photo currentPhoto = _app.getTrackInfo().getCurrentPhoto();
		boolean hasPhoto = currentPhoto != null;
		AudioClip currentAudio = _app.getTrackInfo().getCurrentAudio();
		boolean hasAudio = currentAudio != null;
		// connect is available if (photo/audio) and point selected, and media has no point
		boolean connectAvailable = (hasPhoto && hasPoint && currentPhoto.getDataPoint() == null)
			|| (hasAudio && hasPoint && currentAudio.getDataPoint() == null);
		_connectPhotoItem.setEnabled(hasPhoto && hasPoint && currentPhoto.getDataPoint() == null);
		_connectButton.setEnabled(connectAvailable);
		_disconnectPhotoItem.setEnabled(hasPhoto && currentPhoto.getDataPoint() != null);
		_correlatePhotosItem.setEnabled(anyPhotos && hasData);
		_rearrangePhotosItem.setEnabled(anyPhotos && hasMultiplePoints);
		_removePhotoItem.setEnabled(hasPhoto);
		_rotatePhotoLeft.setEnabled(hasPhoto);
		_rotatePhotoRight.setEnabled(hasPhoto);
		_photoPopupItem.setEnabled(hasPhoto);
		_ignoreExifThumb.setEnabled(hasPhoto && currentPhoto.getExifThumbnail() != null);
		_selectNoPhotoItem.setEnabled(hasPhoto);
		boolean anyAudios = _app.getTrackInfo().getAudioList().getNumAudios() > 0;
		_selectNoAudioItem.setEnabled(hasAudio);
		_removeAudioItem.setEnabled(hasAudio);
		_connectAudioItem.setEnabled(hasAudio && hasPoint && currentAudio.getDataPoint() == null);
		_disconnectAudioItem.setEnabled(hasAudio && currentAudio.getDataPoint() != null);
		_correlateAudiosItem.setEnabled(anyAudios && hasData);
		// is there a current range?
		boolean hasRange = (hasData && _selection.hasRangeSelected());
		_deleteRangeItem.setEnabled(hasRange);
		_deleteRangeButton.setEnabled(hasRange);
		_cropTrackItem.setEnabled(hasRange);
		_interpolateItem.setEnabled(hasRange);
		_averageItem.setEnabled(hasRange);
		_mergeSegmentsItem.setEnabled(hasRange);
		_reverseItem.setEnabled(hasRange);
		_addTimeOffsetItem.setEnabled(hasRange);
		_addAltitudeOffsetItem.setEnabled(hasRange);
		_convertNamesToTimesItem.setEnabled(hasRange && _track.hasWaypoints());
		_deleteFieldValuesItem.setEnabled(hasRange);
		_viewFullDetailsItem.setEnabled(hasRange || hasPoint);
		_estimateTimeItem.setEnabled(hasRange);
		_learnEstimationParams.setEnabled(hasData && _track.hasTrackPoints() && _track.hasData(Field.TIMESTAMP)
			&& _track.hasAltitudeData());
		// Is the currently selected point outside the current range?
		boolean canCutAndMove = hasRange && hasPoint &&
			(_selection.getCurrentPointIndex() < _selection.getStart()
			|| _selection.getCurrentPointIndex() > (_selection.getEnd()+1));
		_cutAndMoveItem.setEnabled(canCutAndMove);
		_cutAndMoveButton.setEnabled(canCutAndMove);
		final boolean isTrackLengthTwo = hasData && _track.getNumPoints() == 2;
		_routingGraphHopperItem.setEnabled(isTrackLengthTwo || (hasData && hasRange));
		// Has the map been switched on/off?
		boolean mapsOn = Config.getConfigBoolean(Config.KEY_SHOW_MAP);
		if (_mapCheckbox.isSelected() != mapsOn) {
			_mapCheckbox.setSelected(mapsOn);
		}
		// Are there any recently-used files?
		RecentFileList rfl = Config.getRecentFileList();
		final int numRecentFiles = rfl.getNumEntries();
		final boolean hasRecentFiles = numRecentFiles > 0;
		_recentFileMenu.setEnabled(hasRecentFiles);
		if (hasRecentFiles)
		{
			int numItems = _recentFileMenu.getMenuComponentCount();
			if (numItems == numRecentFiles)
			{
				// Right number of items, just change texts
				for (int i=0; i<numRecentFiles; i++)
				{
					JMenuItem item = _recentFileMenu.getItem(i);
					RecentFile rf = rfl.getFile(i);
					item.setText(rf==null?"":rf.getFile().getName());
					item.setToolTipText(rf==null?null:rf.getFile().getAbsolutePath());
				}
			}
			else
			{
				// Rebuild menus
				_recentFileMenu.removeAll();
				for (int i=0; i<rfl.getSize(); i++)
				{
					RecentFile rf = rfl.getFile(i);
					if (rf != null && rf.isValid())
					{
						JMenuItem menuItem = new JMenuItem(rf.getFile().getName());
						menuItem.setToolTipText(rf.getFile().getAbsolutePath());
						menuItem.addActionListener(new RecentFileTrigger(_app, i));
						_recentFileMenu.add(menuItem);
					}
				}
			}
		}
	}


	/**
	 * Ignore action completed signals
	 * @see tim.prune.DataSubscriber#actionCompleted(java.lang.String)
	 */
	public void actionCompleted(String inMessage)
	{}
}
