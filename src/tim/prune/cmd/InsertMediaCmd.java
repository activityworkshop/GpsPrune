package tim.prune.cmd;

import java.util.ArrayList;
import java.util.List;

import tim.prune.DataSubscriber;
import tim.prune.data.AudioClip;
import tim.prune.data.Photo;
import tim.prune.data.TrackInfo;

/**
 * Command to insert multiple photos and/or audios back where they were
 */
public class InsertMediaCmd extends Command
{
	private final List<MediaInsertion> _media;

	/**
	 * Constructor
	 * @param inParent parent command
	 * @param inMedia media to insert
	 */
	public InsertMediaCmd(Command inParent, List<MediaInsertion> inMedia)
	{
		super(inParent);
		_media = new ArrayList<>(inMedia);
	}

	@Override
	protected boolean executeCommand(TrackInfo inInfo)
	{
		for (MediaInsertion item : _media)
		{
			if (item.getMedia() instanceof Photo) {
				inInfo.getPhotoList().add((Photo) item.getMedia(), item.getInsertIndex());
			}
			else if (item.getMedia() instanceof AudioClip) {
				inInfo.getAudioList().add((AudioClip) item.getMedia(), item.getInsertIndex());
			}
			else {
				return false;
			}
		}
		return true;
	}

	@Override
	public int getUpdateFlags() {
		return DataSubscriber.MEDIA_MODIFIED;
	}

	@Override
	protected Command makeInverse(TrackInfo inInfo) {
		throw new IllegalArgumentException("InsertMediaCmd can only be an inverse");
	}
}
