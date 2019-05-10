package tim.prune.save;

/**
 * Interface used to inform consumers that the base image has been changed
 */
public interface BaseImageConsumer
{
	/** Notify consumer that base image has changed */
	public void baseImageChanged();
}
