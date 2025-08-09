package tim.prune.gui;

import javax.swing.ImageIcon;

/**
 * Class to manage the loading of icons
 * for toolbars and map buttons
 */
public class IconManager
{
	/** true for double resolution icons, false for normal (default) */
	private final boolean _doubleResolution;

	/** Icon for window */
	public static final String WINDOW_ICON = "window_icon";

	/** Icons for buttons at the top of the main map display */
	// show scalebar
	public static final String SCALEBAR_BUTTON = "canvas/scalebar-off";
	public static final String SCALEBAR_BUTTON_ON = "canvas/scalebar-on";
	// show map
	public static final String MAP_BUTTON = "canvas/showmap-off";
	public static final String MAP_BUTTON_ON = "canvas/showmap-on";
	// autopan
	public static final String AUTOPAN_BUTTON = "canvas/autopan-off";
	public static final String AUTOPAN_BUTTON_ON = "canvas/autopan-on";
	// four options for points and lines
	public static final String POINTS_CONNECTED_BUTTON = "canvas/points-connected";
	public static final String POINTS_DISCONNECTED_BUTTON = "canvas/points-disconnected";
	public static final String POINTS_HIDDEN_BUTTON = "canvas/points-hidden";
	public static final String POINTS_WITH_ARROWS_BUTTON = "canvas/points-arrows";
	// edit mode
	public static final String EDIT_MODE_BUTTON = "canvas/edit-mode-off";
	public static final String EDIT_MODE_BUTTON_ON = "canvas/edit-mode-on";
	/** Zoom in and out buttons, shown on left of map */
	public static final String ZOOM_IN_BUTTON = "canvas/zoom-in";
	public static final String ZOOM_OUT_BUTTON = "canvas/zoom-out";

	/** Main toolbar */
	public static final String TOOLBAR_IMPORT_FILE = "toolbar/import-file";
	public static final String TOOLBAR_ADD_PHOTO = "toolbar/add-image";
	public static final String TOOLBAR_EXPORT_FILE = "toolbar/export-file";
	public static final String TOOLBAR_UNDO = "toolbar/undo";
	public static final String TOOLBAR_REDO = "toolbar/redo";
	public static final String TOOLBAR_EDIT_POINT = "toolbar/edit-details";
	public static final String TOOLBAR_VIEW_INFO = "toolbar/view-info";
	public static final String TOOLBAR_DELETE_POINT = "toolbar/delete-point";
	public static final String TOOLBAR_DELETE_RANGE = "toolbar/delete-range";
	public static final String TOOLBAR_SET_RANGE_START = "toolbar/set-range-start";
	public static final String TOOLBAR_SET_RANGE_END = "toolbar/set-range-end";
	public static final String TOOLBAR_CUT_AND_MOVE = "toolbar/cut-and-move";
	public static final String TOOLBAR_CONNECT_PHOTO = "toolbar/link";
	public static final String TOOLBAR_VIEW_3D = "toolbar/view-3d";

	/** Photo details and controls for rotation */
	public static final String ROTATE_LEFT = "controls/rotate-left";
	public static final String ROTATE_RIGHT = "controls/rotate-right";
	public static final String SHOW_DETAILS = "controls/show-image";
	/** Controls for audio and autoplay */
	public static final String CONTROL_PLAY = "controls/play";
	public static final String CONTROL_STOP = "controls/stop";
	public static final String CONTROL_PAUSE = "controls/pause";
	public static final String CONTROL_REWIND = "controls/rewind";

	/** Icons for validity status (eg Gpsbabel filters) */
	public static final String ENTRY_VALID = "entry-valid";
	public static final String ENTRY_INVALID = "entry-invalid";
	public static final String ENTRY_NONE = "entry-none";

	public static final String WAYPOINT_ICON_PREFIX = "waypoints/wpicon_";


	/** @param inDoubleResolution true for double resolution */
	public IconManager(boolean inDoubleResolution) {
		_doubleResolution = inDoubleResolution;
	}

	/**
	 * Get the specified image
	 * @param inFilename filename of image (using constants)
	 * @return ImageIcon object containing image
	 */
	public ImageIcon getImageIcon(String inFilename)
	{
		final String sizePrefix = _doubleResolution ? "2x/" : "1x/";
		return getImageIcon(sizePrefix, inFilename);
	}

	/**
	 * Get the specified image
	 * @param inSizePrefix size prefix according to single/double resolution
	 * @param inFilename filename of image (using constants)
	 * @return ImageIcon object containing image
	 */
	private static ImageIcon getImageIcon(String inSizePrefix, String inFilename) {
		return new ImageIcon(IconManager.class.getResource("images/" + inSizePrefix + inFilename + ".png"));
	}

	/**
	 * Get the specified image
	 * @param inFilename filename of image (using constants)
	 * @return ImageIcon object containing image
	 */
	public static ImageIcon getImageIconWithoutSize(String inFilename) {
		return getImageIcon("", inFilename);
	}
}
