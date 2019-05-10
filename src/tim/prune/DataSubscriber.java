package tim.prune;

/**
 * Interface implemented by clients who want to know
 * about changes made to the data or its selection
 */
public interface DataSubscriber
{
	public static final byte DATA_ADDED_OR_REMOVED = 1;
	public static final byte DATA_EDITED           = 2;
	public static final byte SELECTION_CHANGED     = 4;
	public static final byte WAYPOINTS_MODIFIED    = 8;
	public static final byte PHOTOS_MODIFIED       = 16;
	public static final byte UNITS_CHANGED         = 32;
	public static final byte ALL                   = 63;
	public static final byte MAPSERVER_CHANGED     = 64;


	/**
	 * Inform clients that data has been updated
	 * @param inUpdateType type of update
	 */
	public void dataUpdated(byte inUpdateType);

	/**
	 * Inform clients that an action has been completed
	 * @param inMessage message describing action
	 */
	public void actionCompleted(String inMessage);
}
