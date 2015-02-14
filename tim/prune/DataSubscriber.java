package tim.prune;

/**
 * Interface implemented by clients who want to know
 * about changes made to the data or its selection
 */
public interface DataSubscriber
{
	/**
	 * Inform clients that data has been updated
	 */
	public void dataUpdated();

}
