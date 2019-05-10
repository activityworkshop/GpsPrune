package tim.prune.data;

import java.io.ByteArrayInputStream;
import java.io.File;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;

/**
 * Class to represent an audio clip for correlation
 */
public class AudioClip extends MediaObject
{
	/** length of current audio clip in seconds */
	private int _lengthInSeconds = LENGTH_UNKNOWN;

	private static final int LENGTH_UNKNOWN = -1;
	private static final int LENGTH_NOT_AVAILABLE = -2;

	/**
	 * Constructor
	 * @param inFile file object
	 */
	public AudioClip(File inFile)
	{
		// Timestamp is always just taken from the file modification stamp
		super(inFile, new TimestampUtc(inFile.lastModified()));
	}

	/**
	 * Constructor
	 * @param inData byte array of data
	 * @param inName name of source file
	 * @param inUrl url from which it came (or null)
	 */
	public AudioClip(byte[] inData, String inName, String inUrl)
	{
		super(inData, inName, inUrl);
	}

	/**
	 * @return length of this audio clip in seconds
	 */
	public int getLengthInSeconds()
	{
		if (_lengthInSeconds == LENGTH_UNKNOWN)
		{
			try {
				AudioFileFormat format = null;
				if (getFile() != null)
					format = AudioSystem.getAudioFileFormat(getFile());
				else
					format = AudioSystem.getAudioFileFormat(new ByteArrayInputStream(_data));
				_lengthInSeconds = (int) (format.getFrameLength() / format.getFormat().getFrameRate());
			}
			catch (Exception e) {
				_lengthInSeconds = LENGTH_NOT_AVAILABLE;
			}
		}
		return _lengthInSeconds;
	}
}
