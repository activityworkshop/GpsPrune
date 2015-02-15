package tim.prune.load;

/**
 * File filter for jpegs
 */
public class JpegFileFilter extends GenericFileFilter
{
	/** Constructor */
	public JpegFileFilter()
	{
		super("filetype.jpeg", new String[] {"jpg", "jpe", "jpeg"});
	}
}
