package tim.prune;

/**
 * Interface implemented by clients who want to know
 * about changes made to the data or its selection
 */
public interface DataSubscriber
{
	public static final int DATA_ADDED_OR_REMOVED = 1;
	public static final int DATA_EDITED           = 2;
	public static final int SELECTION_CHANGED     = 4;
	public static final int WAYPOINTS_MODIFIED    = 8;
	public static final int MEDIA_MODIFIED        = 16;
	public static final int UNITS_CHANGED         = 32;
	public static final int FILE_LOADED           = 64;
	public static final int ALL_DATA              = 127;
	public static final int MAPSERVER_CHANGED     = 128;


	/**
	 * Inform clients that data has been updated
	 * @param inUpdateType type of update
	 */
	public void dataUpdated(int inUpdateType);

	/**
	 * Inform clients that an action has been completed
	 * @param inMessage message describing action
	 */
	public void actionCompleted(String inMessage);
}
