package tim.prune.jpeg.drew;

/**
 * Class to indicate a fatal exception processing a jpeg,
 * including IO errors and exif errors
 */
public class JpegException extends Exception
{
	/**
	 * @param message description of error
	 */
	public JpegException(String message)
	{
		super(message);
	}

	/**
	 * @param message description of error
	 * @param cause Throwable which caused the error
	 */
	public JpegException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
