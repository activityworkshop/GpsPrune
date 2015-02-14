package tim.prune.save.xml;

/**
 * Interface for receivers of tag strings
 * used for reading tags from xml and reporting them back to a listener
 */
public interface TagReceiver
{
	/**
	 * Method to give a tag string to a listener
	 * @param inTag xml tag
	 */
	public void reportTag(String inTag);
}
