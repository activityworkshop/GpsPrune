package tim.prune.cmd;

import tim.prune.DataSubscriber;
import tim.prune.data.AudioClip;
import tim.prune.data.TrackInfo;

/**
 * Command to insert or append a single audio to the list.
 * This command can only be an inverse of a removal, as
 * audios are usually added by the InsertMediaCmd.
 */
public class InsertAudioCmd extends Command
{
	private final AudioClip _audio;
	private final int _audioIndex;


	/**
	 * Constructor
	 * @param inAudio audio to append
	 */
	public InsertAudioCmd(AudioClip inAudio) {
		this(null, inAudio, -1);
	}

	/**
	 * Constructor to make inverse
	 * @param inParent parent command
	 * @param inAudio audio
	 * @param inAudioIndex point index of insertion
	 */
	InsertAudioCmd(Command inParent, AudioClip inAudio, int inAudioIndex)
	{
		super(inParent);
		_audio = inAudio;
		_audioIndex = inAudioIndex;
	}

	@Override
	public int getUpdateFlags() {
		return DataSubscriber.MEDIA_MODIFIED;
	}

	@Override
	protected boolean executeCommand(TrackInfo inInfo)
	{
		if (_audio == null) {
			return false;
		}
		if (_audioIndex < 0) {
			inInfo.getAudioList().add(_audio);
		}
		else {
			inInfo.getAudioList().add(_audio, _audioIndex);
		}
		return true;
	}

	@Override
	protected Command makeInverse(TrackInfo inInfo) {
		int index = _audioIndex < 0 ? inInfo.getAudioList().getCount() : _audioIndex;
		return new RemoveAudioCmd(this, index);
	}
}
