package tim.prune.undo;

/**
 * Exception thrown when undo operation fails
 */
public class UndoException extends Exception
{
	/**
	 * Constructor
	 * @param inMessage description of operation which failed
	 */
	public UndoException(String inMessage)
	{
		super(inMessage);
	}
}
