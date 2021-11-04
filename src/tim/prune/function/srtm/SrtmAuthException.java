package tim.prune.function.srtm;

/**
 * Exception thrown when the Srtm Auth fails
 * (wrong password, not registered, not authorised)
 */
public class SrtmAuthException extends Exception
{
	/**
	 * Constructor
	 * @param inStatusCode Http status code from server
	 */
	public SrtmAuthException(int inStatusCode)
	{
		super("" + inStatusCode);
	}
}
