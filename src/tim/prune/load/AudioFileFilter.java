package tim.prune.load;

/**
 * File filter for audio files
 */
public class AudioFileFilter extends GenericFileFilter
{
	/** Constructor */
	public AudioFileFilter()
	{
		super("filetypefilter.audio", new String[] {"mp3", "ogg", "wav"});
	}
}
