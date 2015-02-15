package tim.prune.data;

import java.io.File;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;

/**
 * Class to represent an audio file for correlation
 */
public class AudioFile extends MediaFile
{
	/** length of current audio file in seconds */
	private int _lengthInSeconds = LENGTH_UNKNOWN;

	private static final int LENGTH_UNKNOWN = -1;
	private static final int LENGTH_NOT_AVAILABLE = -2;

	/**
	 * Constructor
	 * @param inFile file object
	 */
	public AudioFile(File inFile)
	{
		// Timestamp is always just taken from the file modification stamp
		super(inFile, new Timestamp(inFile.lastModified()));
	}

	/**
	 * @return length of this audio file in seconds
	 */
	public int getLengthInSeconds()
	{
		if (_lengthInSeconds == LENGTH_UNKNOWN)
		{
			try {
				AudioFileFormat format = AudioSystem.getAudioFileFormat(getFile());
				_lengthInSeconds = (int) (format.getFrameLength() / format.getFormat().getFrameRate());
			}
			catch (Exception e) {
				_lengthInSeconds = LENGTH_NOT_AVAILABLE;
			}
		}
		return _lengthInSeconds;
	}
}
