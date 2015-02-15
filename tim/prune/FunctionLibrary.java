package tim.prune;

import tim.prune.correlate.AudioCorrelator;
import tim.prune.correlate.PhotoCorrelator;
import tim.prune.function.*;
import tim.prune.function.charts.Charter;
import tim.prune.function.compress.CompressTrackFunction;
import tim.prune.function.distance.DistanceFunction;
import tim.prune.function.edit.PointNameEditor;
import tim.prune.function.gpsies.GetGpsiesFunction;
import tim.prune.function.gpsies.UploadGpsiesFunction;
import tim.prune.function.srtm.LookupSrtmFunction;
import tim.prune.load.AudioLoader;
import tim.prune.load.BabelLoadFromFile;
import tim.prune.load.BabelLoadFromGps;
import tim.prune.save.GpsSaver;
import tim.prune.save.GpxExporter;
import tim.prune.save.KmlExporter;
import tim.prune.save.PovExporter;
import tim.prune.save.SvgExporter;

/**
 * Class to provide access to functions
 */
public abstract class FunctionLibrary
{
	public static GenericFunction FUNCTION_GPXEXPORT = null;
	public static GenericFunction FUNCTION_KMLEXPORT = null;
	public static PovExporter FUNCTION_POVEXPORT     = null;
	public static SvgExporter FUNCTION_SVGEXPORT     = null;
	public static GenericFunction FUNCTION_GPSLOAD  = null;
	public static GenericFunction FUNCTION_GPSSAVE  = null;
	public static GenericFunction FUNCTION_IMPORTBABEL = null;
	public static GenericFunction FUNCTION_SAVECONFIG  = null;
	public static GenericFunction FUNCTION_EDIT_WAYPOINT_NAME = null;
	public static RearrangeWaypointsFunction FUNCTION_REARRANGE_WAYPOINTS = null;
	public static GenericFunction FUNCTION_REARRANGE_PHOTOS = null;
	public static GenericFunction FUNCTION_COMPRESS = null;
	public static GenericFunction FUNCTION_LOOKUP_SRTM = null;
	public static GenericFunction FUNCTION_LOOKUP_WIKIPEDIA = null;
	public static GenericFunction FUNCTION_SEARCH_WIKIPEDIA = null;
	public static GenericFunction FUNCTION_DOWNLOAD_OSM = null;
	public static GenericFunction FUNCTION_ADD_TIME_OFFSET  = null;
	public static GenericFunction FUNCTION_ADD_ALTITUDE_OFFSET  = null;
	public static GenericFunction FUNCTION_CONVERT_NAMES_TO_TIMES  = null;
	public static GenericFunction FUNCTION_DELETE_FIELD_VALUES  = null;
	public static GenericFunction FUNCTION_PASTE_COORDINATES = null;
	public static GenericFunction FUNCTION_FIND_WAYPOINT = null;
	public static GenericFunction FUNCTION_DUPLICATE_POINT = null;
	public static GenericFunction FUNCTION_CONNECT_TO_POINT = null;
	public static GenericFunction FUNCTION_DISCONNECT_PHOTO = null;
	public static GenericFunction FUNCTION_REMOVE_PHOTO = null;
	public static GenericFunction FUNCTION_DISCONNECT_AUDIO = null;
	public static GenericFunction FUNCTION_CORRELATE_PHOTOS = null;
	public static GenericFunction FUNCTION_ROTATE_PHOTO_LEFT = null;
	public static GenericFunction FUNCTION_ROTATE_PHOTO_RIGHT = null;
	public static GenericFunction FUNCTION_PHOTO_POPUP = null;
	public static GenericFunction FUNCTION_IGNORE_EXIF_THUMB = null;
	public static GenericFunction FUNCTION_CHARTS = null;
	public static GenericFunction FUNCTION_3D     = null;
	public static GenericFunction FUNCTION_DISTANCES  = null;
	public static GenericFunction FUNCTION_FULL_RANGE_DETAILS = null;
	public static GenericFunction FUNCTION_GET_GPSIES = null;
	public static GenericFunction FUNCTION_UPLOAD_GPSIES = null;
	public static GenericFunction FUNCTION_LOAD_AUDIO = null;
	public static GenericFunction FUNCTION_REMOVE_AUDIO = null;
	public static GenericFunction FUNCTION_CORRELATE_AUDIOS = null;
	public static GenericFunction FUNCTION_PLAY_AUDIO = null;
	public static GenericFunction FUNCTION_STOP_AUDIO = null;
	public static GenericFunction FUNCTION_SET_MAP_BG = null;
	public static GenericFunction FUNCTION_SET_DISK_CACHE = null;
	public static GenericFunction FUNCTION_SET_PATHS  = null;
	public static GenericFunction FUNCTION_SET_KMZ_IMAGE_SIZE = null;
	public static GenericFunction FUNCTION_SET_COLOURS = null;
	public static GenericFunction FUNCTION_SET_LINE_WIDTH = null;
	public static GenericFunction FUNCTION_SET_LANGUAGE = null;
	public static GenericFunction FUNCTION_HELP   = null;
	public static GenericFunction FUNCTION_SHOW_KEYS = null;
	public static GenericFunction FUNCTION_ABOUT  = null;
	public static GenericFunction FUNCTION_CHECK_VERSION  = null;


	/**
	 * Initialise library of functions
	 * @param inApp App object to give to functions
	 */
	public static void initialise(App inApp)
	{
		FUNCTION_GPXEXPORT = new GpxExporter(inApp);
		FUNCTION_KMLEXPORT = new KmlExporter(inApp);
		FUNCTION_POVEXPORT = new PovExporter(inApp);
		FUNCTION_SVGEXPORT = new SvgExporter(inApp);
		FUNCTION_GPSLOAD   = new BabelLoadFromGps(inApp);
		FUNCTION_GPSSAVE   = new GpsSaver(inApp);
		FUNCTION_IMPORTBABEL = new BabelLoadFromFile(inApp);
		FUNCTION_SAVECONFIG = new SaveConfig(inApp);
		FUNCTION_EDIT_WAYPOINT_NAME = new PointNameEditor(inApp);
		FUNCTION_REARRANGE_WAYPOINTS = new RearrangeWaypointsFunction(inApp);
		FUNCTION_REARRANGE_PHOTOS = new RearrangePhotosFunction(inApp);
		FUNCTION_COMPRESS = new CompressTrackFunction(inApp);
		FUNCTION_LOOKUP_SRTM = new LookupSrtmFunction(inApp);
		FUNCTION_LOOKUP_WIKIPEDIA = new GetWikipediaFunction(inApp);
		FUNCTION_SEARCH_WIKIPEDIA = new SearchWikipediaNames(inApp);
		FUNCTION_DOWNLOAD_OSM = new DownloadOsmFunction(inApp);
		FUNCTION_ADD_TIME_OFFSET = new AddTimeOffset(inApp);
		FUNCTION_ADD_ALTITUDE_OFFSET = new AddAltitudeOffset(inApp);
		FUNCTION_CONVERT_NAMES_TO_TIMES = new ConvertNamesToTimes(inApp);
		FUNCTION_DELETE_FIELD_VALUES = new DeleteFieldValues(inApp);
		FUNCTION_PASTE_COORDINATES = new PasteCoordinates(inApp);
		FUNCTION_FIND_WAYPOINT = new FindWaypoint(inApp);
		FUNCTION_DUPLICATE_POINT = new DuplicatePoint(inApp);
		FUNCTION_CONNECT_TO_POINT = new ConnectToPointFunction(inApp);
		FUNCTION_DISCONNECT_PHOTO = new DisconnectPhotoFunction(inApp);
		FUNCTION_REMOVE_PHOTO = new RemovePhotoFunction(inApp);
		FUNCTION_CORRELATE_PHOTOS = new PhotoCorrelator(inApp);
		FUNCTION_ROTATE_PHOTO_LEFT = new RotatePhoto(inApp, false);
		FUNCTION_ROTATE_PHOTO_RIGHT = new RotatePhoto(inApp, true);
		FUNCTION_PHOTO_POPUP = new PhotoPopupFunction(inApp);
		FUNCTION_IGNORE_EXIF_THUMB = new IgnoreExifThumb(inApp);
		FUNCTION_CHARTS = new Charter(inApp);
		FUNCTION_3D     = new ShowThreeDFunction(inApp);
		FUNCTION_DISTANCES = new DistanceFunction(inApp);
		FUNCTION_FULL_RANGE_DETAILS = new FullRangeDetails(inApp);
		FUNCTION_GET_GPSIES = new GetGpsiesFunction(inApp);
		FUNCTION_UPLOAD_GPSIES = new UploadGpsiesFunction(inApp);
		FUNCTION_LOAD_AUDIO = new AudioLoader(inApp);
		FUNCTION_REMOVE_AUDIO = new RemoveAudioFunction(inApp);
		FUNCTION_CORRELATE_AUDIOS = new AudioCorrelator(inApp);
		FUNCTION_PLAY_AUDIO = new PlayAudioFunction(inApp);
		FUNCTION_STOP_AUDIO = new StopAudioFunction(inApp);
		FUNCTION_DISCONNECT_AUDIO = new DisconnectAudioFunction(inApp);
		FUNCTION_SET_MAP_BG = new SetMapBgFunction(inApp);
		FUNCTION_SET_DISK_CACHE = new DiskCacheConfig(inApp);
		FUNCTION_SET_PATHS = new SetPathsFunction(inApp);
		FUNCTION_SET_KMZ_IMAGE_SIZE = new SetKmzImageSize(inApp);
		FUNCTION_SET_COLOURS = new SetColours(inApp);
		FUNCTION_SET_LINE_WIDTH = new SetLineWidth(inApp);
		FUNCTION_SET_LANGUAGE = new SetLanguage(inApp);
		FUNCTION_HELP   = new HelpScreen(inApp);
		FUNCTION_SHOW_KEYS = new ShowKeysScreen(inApp);
		FUNCTION_ABOUT  = new AboutScreen(inApp);
		FUNCTION_CHECK_VERSION= new CheckVersionScreen(inApp);
	}
}
