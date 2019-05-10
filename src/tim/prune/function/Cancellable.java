package tim.prune.function;

/**
 * Interface implemented by functions which can be cancelled
 */
public interface Cancellable
{
	/**
	 * Cancel the function
	 */
	public void cancel();
}
