package tim.prune.load;

import java.io.IOException;
import java.io.InputStream;

/**
 * Class to scoop bytes from an input stream into an array.
 * The size of the array doesn't have to be known in advance.
 * This is used for getting images and sound files out of zip
 * files or from remote URLs.
 */
public class ByteScooper
{
	/** Bucket size in bytes */
	private static final int BUCKET_SIZE = 5000;
	/** Amount by which barrel size is increased on demand */
	private static final int BARREL_SIZE_INCREMENT = 100000;

	/**
	 * Scoop bytes from the given input stream and return the result
	 * @param inIs input stream to scoop bytes from
	 * @return byte array
	 */
	public static byte[] scoop(InputStream inIs) throws IOException
	{
		byte[] _barrel = new byte[BARREL_SIZE_INCREMENT];
		byte[] _bucket = new byte[BUCKET_SIZE];
		int numBytesInBarrel = 0;
		// read from stream into the bucket
		int numBytesRead = inIs.read(_bucket);
		while (numBytesRead >= 0)
		{
			// expand barrel if necessary
			if ((numBytesInBarrel + numBytesRead) > _barrel.length)
			{
				byte[] newBarrel = new byte[_barrel.length + BARREL_SIZE_INCREMENT];
				System.arraycopy(_barrel, 0, newBarrel, 0, numBytesInBarrel);
				_barrel = newBarrel;
			}
			// copy from bucket into barrel
			System.arraycopy(_bucket, 0, _barrel, numBytesInBarrel, numBytesRead);
			numBytesInBarrel += numBytesRead;
			// read next lot from stream into the bucket
			numBytesRead = inIs.read(_bucket);
		}
		// Now we know how many bytes there are, so crop to size
		if (numBytesInBarrel == 0) return null;
		byte[] result = new byte[numBytesInBarrel];
		System.arraycopy(_barrel, 0, result, 0, numBytesInBarrel);
		return result;
	}
}
