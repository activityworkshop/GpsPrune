package tim.prune.cmd;

import tim.prune.DataSubscriber;
import tim.prune.data.AudioClip;
import tim.prune.data.TrackInfo;

/**
 * Command to remove (not delete) an audio file
 */
public class RemoveAudioCmd extends Command
{
	private final int _audioIndex;

	public RemoveAudioCmd(int inAudioIndex) {
		this(null, inAudioIndex);
	}

	protected RemoveAudioCmd(Command inParent, int inAudioIndex)
	{
		super(inParent);
		_audioIndex = inAudioIndex;
	}

	@Override
	public int getUpdateFlags() {
		return DataSubscriber.MEDIA_MODIFIED;
	}

	@Override
	protected boolean executeCommand(TrackInfo inInfo)
	{
		try {
			inInfo.getAudioList().delete(_audioIndex);
			return true;
		}
		catch (IndexOutOfBoundsException obe) {
			return false;
		}
	}

	@Override
	protected Command makeInverse(TrackInfo inInfo)
	{
		AudioClip audio = inInfo.getAudioList().get(_audioIndex);
		return new InsertAudioCmd(this, audio, _audioIndex);
	}
}
