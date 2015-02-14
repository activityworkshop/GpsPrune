package tim.prune;

import tim.prune.correlate.PhotoCorrelator;
import tim.prune.function.AboutScreen;
import tim.prune.function.AddTimeOffset;
import tim.prune.function.CheckVersionScreen;
import tim.prune.function.HelpScreen;
import tim.prune.function.RearrangeWaypointsFunction;
import tim.prune.function.SetMapBgFunction;
import tim.prune.function.ShowThreeDFunction;
import tim.prune.function.charts.Charter;
import tim.prune.function.compress.CompressTrackFunction;
import tim.prune.function.distance.DistanceFunction;
import tim.prune.load.GpsLoader;
import tim.prune.save.GpsSaver;
import tim.prune.save.GpxExporter;
import tim.prune.save.KmlExporter;
import tim.prune.save.PovExporter;

/**
 * Class to provide access to functions
 */
public abstract class FunctionLibrary
{
	public static GenericFunction FUNCTION_GPXEXPORT = null;
	public static GenericFunction FUNCTION_KMLEXPORT = null;
	public static PovExporter FUNCTION_POVEXPORT     = null;
	public static GenericFunction FUNCTION_GPSLOAD  = null;
	public static GenericFunction FUNCTION_GPSSAVE  = null;
	public static RearrangeWaypointsFunction FUNCTION_REARRANGE_WAYPOINTS = null;
	public static GenericFunction FUNCTION_COMPRESS = null;
	public static GenericFunction FUNCTION_ADD_TIME_OFFSET  = null;
	public static GenericFunction FUNCTION_CORRELATE_PHOTOS = null;
	public static GenericFunction FUNCTION_CHARTS = null;
	public static GenericFunction FUNCTION_3D     = null;
	public static GenericFunction FUNCTION_DISTANCES  = null;
	public static GenericFunction FUNCTION_SET_MAP_BG = null;
	public static GenericFunction FUNCTION_HELP   = null;
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
		FUNCTION_GPSLOAD   = new GpsLoader(inApp);
		FUNCTION_GPSSAVE   = new GpsSaver(inApp);
		FUNCTION_REARRANGE_WAYPOINTS = new RearrangeWaypointsFunction(inApp);
		FUNCTION_COMPRESS = new CompressTrackFunction(inApp);
		FUNCTION_ADD_TIME_OFFSET = new AddTimeOffset(inApp);
		FUNCTION_CORRELATE_PHOTOS = new PhotoCorrelator(inApp);
		FUNCTION_CHARTS = new Charter(inApp);
		FUNCTION_3D     = new ShowThreeDFunction(inApp);
		FUNCTION_DISTANCES = new DistanceFunction(inApp);
		FUNCTION_SET_MAP_BG = new SetMapBgFunction(inApp);
		FUNCTION_HELP   = new HelpScreen(inApp);
		FUNCTION_ABOUT  = new AboutScreen(inApp);
		FUNCTION_CHECK_VERSION= new CheckVersionScreen(inApp);
	}
}
